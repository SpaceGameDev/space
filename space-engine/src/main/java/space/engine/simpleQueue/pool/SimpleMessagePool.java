package space.engine.simpleQueue.pool;

import org.jetbrains.annotations.NotNull;
import space.engine.barrier.Barrier;
import space.engine.barrier.BarrierImpl;
import space.engine.freeableStorage.Freeable;
import space.engine.freeableStorage.FreeableStorage;
import space.engine.simpleQueue.SimpleQueue;

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
	
	private volatile boolean isRunning = true;
	private final AtomicInteger exitCountdown;
	private final BarrierImpl stopBarrier;
	
	public SimpleMessagePool(int threadCnt, @NotNull SimpleQueue<MSG> queue) {
		this(threadCnt, queue, Executors.defaultThreadFactory(), true);
	}
	
	public SimpleMessagePool(int threadCnt, @NotNull SimpleQueue<MSG> queue, ThreadFactory threadFactory) {
		this(threadCnt, queue, threadFactory, true);
	}
	
	protected SimpleMessagePool(int threadCnt, @NotNull SimpleQueue<MSG> queue, ThreadFactory threadFactory, boolean callinit) {
		this.queue = queue;
		this.exitCountdown = new AtomicInteger(threadCnt);
		this.stopBarrier = new BarrierImpl();
		
		Runnable poolMain = () -> {
			while (true) {
				MSG msg = queue.remove();
				if (msg != null) {
					//execute Runnable
					try {
						handle(msg);
					} catch (Throwable e) {
						Thread thread = Thread.currentThread();
						thread.getUncaughtExceptionHandler().uncaughtException(thread, e);
					}
				} else {
					
					//no more work -> call #handleDone()
					try {
						if (!handleDone())
							continue;
					} catch (Throwable e) {
						Thread thread = Thread.currentThread();
						thread.getUncaughtExceptionHandler().uncaughtException(thread, e);
					}
					
					//#handleDone() allows sleeping
					synchronized (this) {
						SOMETHREADSLEEPING.set(this, true);
						
						//check preconditions for sleeping
						if (!isRunning)
							break;
						msg = queue.remove();
						if (msg == null) {
							
							//actually sleep
							try {
								this.wait();
							} catch (InterruptedException ignored) {
							
							}
							continue;
						}
					}
					
					//new work was submitted while thread wanted to go sleeping
					try {
						handle(msg);
					} catch (Throwable e) {
						Thread thread = Thread.currentThread();
						thread.getUncaughtExceptionHandler().uncaughtException(thread, e);
					}
				}
			}
			
			if (exitCountdown.decrementAndGet() == 0)
				stopBarrier.triggerNow();
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
		return new FreeableStorage(null, parents) {
			@Override
			protected @NotNull Barrier handleFree() {
				return stop();
			}
		};
	}
}
