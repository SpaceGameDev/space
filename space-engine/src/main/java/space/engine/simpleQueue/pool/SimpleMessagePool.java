package space.engine.simpleQueue.pool;

import org.jetbrains.annotations.NotNull;
import space.engine.barrier.Barrier;
import space.engine.barrier.BarrierImpl;
import space.engine.freeable.Cleaner;
import space.engine.freeable.Freeable;
import space.engine.simpleQueue.ConcurrentLinkedSimpleQueue;
import space.engine.simpleQueue.SimpleQueue;
import space.engine.simpleQueue.pool.ThreadBound.Entry;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public abstract class SimpleMessagePool<MSG> {
	
	public static final int DEFAULT_PAUSE_COUNTDOWN = 1000;
	private static final VarHandle SOMETHREADSLEEPING;
	
	static {
		try {
			SOMETHREADSLEEPING = MethodHandles.lookup().findVarHandle(SimpleMessagePool.class, "someThreadSleeping", boolean.class);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new ExceptionInInitializerError(e);
		}
	}
	
	protected final @NotNull SimpleQueue<MSG> queue;
	protected final Thread[] threads;
	private volatile boolean someThreadSleeping = false;
	
	//shutdown
	private volatile boolean isRunning = true;
	private final AtomicInteger exitCountdown;
	private final BarrierImpl stopBarrier;
	
	/**
	 * @see SimpleMessagePool#SimpleMessagePool(int, ThreadFactory, SimpleQueue, int, boolean)
	 */
	public SimpleMessagePool(int threadCnt) {
		this(threadCnt, Executors.defaultThreadFactory(), new ConcurrentLinkedSimpleQueue<>(), DEFAULT_PAUSE_COUNTDOWN);
	}
	
	/**
	 * @see SimpleMessagePool#SimpleMessagePool(int, ThreadFactory, SimpleQueue, int, boolean)
	 */
	public SimpleMessagePool(int threadCnt, ThreadFactory threadFactory) {
		this(threadCnt, threadFactory, new ConcurrentLinkedSimpleQueue<>(), DEFAULT_PAUSE_COUNTDOWN);
	}
	
	public SimpleMessagePool(int threadCnt, ThreadFactory threadFactory, @NotNull SimpleQueue<MSG> queue) {
		this(threadCnt, threadFactory, queue, DEFAULT_PAUSE_COUNTDOWN, true);
	}
	
	/**
	 * Creates a new {@link SimpleThreadPool}
	 *
	 * @param threadCnt      the amount of threads in the pool
	 * @param queue          the {@link SimpleQueue} to use. Recommended: {@link ConcurrentLinkedSimpleQueue}
	 * @param threadFactory  the {@link ThreadFactory} to construct {@link Thread Threads} from
	 * @param pauseCountdown the amount of tasks to execute before 'pausing' and handling {@link ThreadBound} tasks
	 */
	public SimpleMessagePool(int threadCnt, ThreadFactory threadFactory, @NotNull SimpleQueue<MSG> queue, int pauseCountdown) {
		this(threadCnt, threadFactory, queue, pauseCountdown, true);
	}
	
	protected SimpleMessagePool(int threadCnt, ThreadFactory threadFactory, @NotNull SimpleQueue<MSG> queue, int pauseCountdown, boolean callinit) {
		if (pauseCountdown <= 0)
			throw new IllegalArgumentException("pauseCountdown " + pauseCountdown + " <= 0");
		
		this.queue = queue;
		this.exitCountdown = new AtomicInteger(threadCnt);
		this.stopBarrier = new BarrierImpl();
		
		Runnable poolMain = new Runnable() {
			@Override
			public void run() {
				Thread thread = Thread.currentThread();
				ConcurrentLinkedSimpleQueue<Runnable> threadBoundQueue = new ConcurrentLinkedSimpleQueue<>();
				Entry threadBoundEntry = ThreadBound.addQueue(thread, command -> {
					threadBoundQueue.add(command);
					thread.interrupt();
				});
				prepare(thread);
				
				while (true) {
					//poll and execute util dry or pauseCountdown
					boolean queueDry = false;
					for (int i = 0; i < pauseCountdown; i++) {
						MSG msg = queue.remove();
						if (msg == null) {
							queueDry = true;
							break;
						}
						handle(msg);
					}
					
					//poll from ThreadAffine
					Runnable run;
					while ((run = threadBoundQueue.remove()) != null)
						run.run();
					
					//no more work -> call #handleDone()
					if (!(handleDone() && queueDry))
						continue;
					
					//sleeping is allowed
					synchronized (SimpleMessagePool.this) {
						SOMETHREADSLEEPING.set(SimpleMessagePool.this, true);
						
						//preconditions for sleeping
						if (!isRunning)
							break;
						MSG msg = queue.remove();
						if (msg != null) {
							handle(msg);
							continue;
						}
						
						//actually sleep
						try {
							SimpleMessagePool.this.wait();
						} catch (InterruptedException ignored) {
						
						}
					}
				}
				
				//shutdown ThreadBound
				Barrier threadBoundShutdownBarrier = threadBoundEntry.free();
				threadBoundShutdownBarrier.addHook(thread::interrupt);
				while (true) {
					Runnable run;
					while ((run = threadBoundQueue.remove()) != null)
						run.run();
					
					if (threadBoundShutdownBarrier.isDone())
						break;
					
					try {
						Thread.sleep(500);
					} catch (InterruptedException ignored) {
					
					}
				}
				
				if (exitCountdown.decrementAndGet() == 0)
					stopBarrier.triggerNow();
			}
			
			public void handle(MSG msg) {
				try {
					SimpleMessagePool.this.handle(msg);
				} catch (Throwable e) {
					Thread thread = Thread.currentThread();
					thread.getUncaughtExceptionHandler().uncaughtException(thread, e);
				}
			}
			
			public boolean handleDone() {
				try {
					return SimpleMessagePool.this.handleDone();
				} catch (Throwable e) {
					Thread thread = Thread.currentThread();
					thread.getUncaughtExceptionHandler().uncaughtException(thread, e);
					return true;
				}
			}
		};
		
		this.threads = IntStream.range(0, threadCnt)
								.mapToObj(i -> threadFactory.newThread(poolMain))
								.toArray(Thread[]::new);
		
		if (callinit)
			init();
	}
	
	public void init() {
		Arrays.stream(threads).forEach(Thread::start);
	}
	
	//handle
	
	/**
	 * Called by each thread once on startup. Default implementation does nothing.
	 *
	 * @param thread the thread to initialize (== {@link Thread#currentThread()})
	 */
	protected void prepare(Thread thread) {
	
	}
	
	/**
	 * handle the messsage
	 */
	protected abstract void handle(MSG msg);
	
	/**
	 * no more work in the queue
	 *
	 * @return whether the Thread is allowed to sleep
	 */
	protected boolean handleDone() {
		return true;
	}
	
	//park
	protected void unparkThreads() {
		if (SOMETHREADSLEEPING.compareAndSet(this, true, false)) {
			synchronized (this) {
				this.notifyAll();
			}
		}
	}
	
	//add
	public void add(MSG msg) {
		assertRunning();
		queue.add(msg);
		unparkThreads();
	}
	
	public void addAll(Collection<MSG> collection) {
		assertRunning();
		queue.addCollection(collection);
		unparkThreads();
	}
	
	public void addAll(MSG[] collection) {
		assertRunning();
		queue.addArray(collection);
		unparkThreads();
	}
	
	//execute
	public void assertRunning() throws RejectedExecutionException {
		if (!isRunning)
			throw new RejectedExecutionException("SimpleThreadPool no longer running");
	}
	
	//getter
	public @NotNull SimpleQueue<MSG> getQueue() {
		return queue;
	}
	
	//stop
	public Barrier stop() {
		isRunning = false;
		unparkThreads();
		return stopBarrier;
	}
	
	public Barrier stopBarrier() {
		return stopBarrier;
	}
	
	public Freeable createStopFreeable(Object[] parents) {
		//will be strongly referenced by out thread anyway
		return new Cleaner(null, parents) {
			@Override
			protected @NotNull Barrier handleFree() {
				return stop();
			}
		};
	}
}
