package space.engine.barrier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A basic Implementation of {@link Barrier}. The {@link Barrier} is triggered by calling {@link #triggerNow()}.
 */
public class BarrierImpl implements Barrier {
	
	private static final VarHandle HOOKLIST;
	
	static {
		try {
			Lookup lookup = MethodHandles.lookup();
			HOOKLIST = lookup.findVarHandle(BarrierImpl.class, "hookList", List.class);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new ExceptionInInitializerError(e);
		}
	}
	
	private volatile @Nullable List<Runnable> hookList;
	
	public BarrierImpl() {
		this(false);
	}
	
	public BarrierImpl(boolean initialTriggerState) {
		hookList = initialTriggerState ? null : new ArrayList<>(0);
	}
	
	//trigger
	public void triggerNow() {
		List<Runnable> hookList;
		synchronized (this) {
			//noinspection unchecked
			hookList = (List<Runnable>) HOOKLIST.getAndSet(this, null);
			if (hookList == null)
				throw exceptionBarrierAlreadyTriggered();
			
			//trigger this Barrier
			this.notifyAll();
		}
		hookList.forEach(Runnable::run);
	}
	
	protected static IllegalStateException exceptionBarrierAlreadyTriggered() {
		return new IllegalStateException("Barrier already triggered!");
	}
	
	//impl
	@Override
	public boolean isDone() {
		return hookList == null;
	}
	
	@Override
	public void addHook(@NotNull Runnable run) {
		List<Runnable> hookList = this.hookList;
		if (hookList != null) {
			synchronized (this) {
				hookList = this.hookList;
				if (hookList != null) {
					hookList.add(run);
					return;
				}
			}
		}
		
		run.run();
	}
	
	@Override
	public synchronized void await() throws InterruptedException {
		while (hookList != null)
			this.wait();
	}
	
	@Override
	public synchronized void await(long time, TimeUnit unit) throws InterruptedException, TimeoutException {
		long sleepTime = unit.toNanos(time);
		long deadline = System.nanoTime() + sleepTime;
		
		while (hookList != null) {
			this.wait(sleepTime / 1000000, (int) (sleepTime % 1000000));
			sleepTime = deadline - System.nanoTime();
			if (sleepTime <= 0)
				throw new TimeoutException();
		}
	}
	
	@Override
	public String toString() {
		return isDone() ? "finished" : "waiting";
	}
}
