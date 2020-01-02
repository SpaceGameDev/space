package space.engine.barrier;

import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A basic Implementation of {@link Barrier}. The {@link Barrier} is triggered by calling {@link #triggerNow()}.
 */
public class BarrierImpl implements Barrier {
	
	private static final VarHandle TRIGGERED;
	
	static {
		try {
			Lookup lookup = MethodHandles.lookup();
			TRIGGERED = lookup.findVarHandle(BarrierImpl.class, "finished", boolean.class);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new ExceptionInInitializerError(e);
		}
	}
	
	private volatile boolean finished;
	private @NotNull ArrayList<Runnable> hookList;
	
	public BarrierImpl() {
		this.hookList = new ArrayList<>();
	}
	
	public BarrierImpl(boolean initialTriggerState) {
		this();
		this.finished = initialTriggerState;
	}
	
	//trigger
	public void triggerNow() {
		synchronized (this) {
			if (!TRIGGERED.compareAndSet(this, false, true))
				throw exceptionBarrierAlreadyTriggered();
		}
		
		//run all hooks
		ArrayList<Runnable> hookList = this.hookList;
		//noinspection ConstantConditions
		this.hookList = null;
		hookList.forEach(Runnable::run);
	}
	
	protected static IllegalStateException exceptionBarrierAlreadyTriggered() {
		return new IllegalStateException("Barrier already triggered!");
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
					hookList.add(run);
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
