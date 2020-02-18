package space.engine.barrier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

/**
 * A basic Implementation of {@link Barrier}. The {@link Barrier} is triggered by calling {@link #triggerNow()}.
 */
public class BarrierImpl implements Barrier {
	
	private static final VarHandle FINISHED;
	
	static {
		try {
			Lookup lookup = MethodHandles.lookup();
			FINISHED = lookup.findVarHandle(BarrierImpl.class, "finished", boolean.class);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new ExceptionInInitializerError(e);
		}
	}
	
	private volatile boolean finished;
	private @Nullable Runnable hookFirst;
	private @Nullable Stream.Builder<Runnable> hookList;
	
	public BarrierImpl() {
	}
	
	public BarrierImpl(boolean initialTriggerState) {
		this.finished = initialTriggerState;
	}
	
	//trigger
	
	/**
	 * triggers the {@link BarrierImpl}.
	 *
	 * @return true if it triggered just now. false if it was already triggered.
	 */
	public boolean triggerNow() {
		if (!FINISHED.compareAndSet(this, false, true))
			return false;
		
		//run all hooks
		synchronized (this) {
			if (hookFirst != null) {
				hookFirst.run();
				hookFirst = null;
			}
			if (hookList != null) {
				this.hookList.build().forEach(Runnable::run);
				this.hookList = null;
			}
		}
		return true;
	}
	
	//impl
	@Override
	public boolean isDone() {
		return finished;
	}
	
	@Override
	public void addHook(@NotNull Runnable run) {
		if (!finished) {
			synchronized (this) {
				if (!finished) {
					if (hookFirst == null) {
						hookFirst = run;
					} else {
						if (hookList == null)
							hookList = Stream.builder();
						hookList.add(run);
					}
					return;
				}
			}
		}
		
		run.run();
	}
	
	protected Runnable createAwaitNotifyRunnable() {
		Runnable runnable = new Runnable() {
			@Override
			public synchronized void run() {
				this.notify();
			}
		};
		addHook(runnable);
		return runnable;
	}
	
	@Override
	public void await() throws InterruptedException {
		Runnable runnable = createAwaitNotifyRunnable();
		//noinspection SynchronizationOnLocalVariableOrMethodParameter
		synchronized (runnable) {
			while (!finished)
				runnable.wait();
		}
	}
	
	@Override
	public void await(long time, TimeUnit unit) throws InterruptedException, TimeoutException {
		Runnable runnable = createAwaitNotifyRunnable();
		long sleepTime = unit.toNanos(time);
		long deadline = System.nanoTime() + sleepTime;
		
		//noinspection SynchronizationOnLocalVariableOrMethodParameter
		synchronized (runnable) {
			while (!finished) {
				runnable.wait(sleepTime / 1000000, (int) (sleepTime % 1000000));
				sleepTime = deadline - System.nanoTime();
				if (sleepTime <= 0)
					throw new TimeoutException();
			}
		}
	}
	
	@Override
	public String toString() {
		return finished ? "finished" : "waiting";
	}
}
