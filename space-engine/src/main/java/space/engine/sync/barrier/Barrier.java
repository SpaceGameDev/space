package space.engine.sync.barrier;

import org.jetbrains.annotations.NotNull;
import space.engine.sync.future.Future;
import space.engine.sync.future.FutureNotFinishedException;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * An Object which can be {@link #await() awaited} upon. You can also {@link #addHook(Runnable) add a Hook} to be called when the Barrier {@link #isFinished() is finished}. <br>
 * <b>It cannot be triggered more than once or reset</b>.
 */
public interface Barrier {
	
	Barrier ALWAYS_TRIGGERED_BARRIER = new Barrier() {
		@Override
		public boolean isFinished() {
			return true;
		}
		
		@Override
		public void addHook(@NotNull Runnable run) {
			run.run();
		}
		
		@Override
		public void removeHook(@NotNull Runnable run) {
		
		}
		
		@Override
		public void await() {
		
		}
		
		@Override
		public void await(long time, TimeUnit unit) {
		
		}
		
		@Override
		public Barrier dereference() {
			return ALWAYS_TRIGGERED_BARRIER;
		}
	};
	Barrier[] EMPTY_BARRIER_ARRAY = new Barrier[0];
	
	//getter
	
	/**
	 * Gets the state if this {@link Barrier}, whether it is finished or not.<br>
	 * NOTE: The triggered state should be considered immediately stale, as it can change any Moment without notice.
	 *
	 * @return whether the {@link Barrier} is triggered or not
	 */
	boolean isFinished();
	
	//hooks
	
	/**
	 * Adds a hook to be called when the Event triggers
	 *
	 * @param run the hook as a {@link Runnable}
	 */
	void addHook(@NotNull Runnable run);
	
	/**
	 * Removes a previously added hook. This Method is not expected to be called very often.
	 *
	 * @param run a previously added hook to remove
	 * @implNote The removal algorithm can be slow, as it is not expected to be called very often.
	 */
	void removeHook(@NotNull Runnable run);
	
	//await
	
	/**
	 * Waits until event is triggered
	 *
	 * @throws InterruptedException if an interrupt occurs
	 */
	void await() throws InterruptedException;
	
	/**
	 * Waits until event is triggered with a timeout
	 *
	 * @throws InterruptedException if an interrupt occurs
	 * @throws TimeoutException     thrown if waiting takes longer than the specified timeout
	 */
	void await(long time, TimeUnit unit) throws InterruptedException, TimeoutException;
	
	/**
	 * Waits until event is triggered and doesn't return when interrupted.
	 * The interrupt status of this {@link Thread} will be restored.
	 */
	default void awaitUninterrupted() {
		boolean interrupted = false;
		try {
			while (true) {
				try {
					await();
					return;
				} catch (InterruptedException e) {
					interrupted = true;
				}
			}
		} finally {
			if (interrupted)
				Thread.currentThread().interrupt();
		}
	}
	
	/**
	 * Waits until event is triggered with a timeout and doesn't return when interrupted.
	 * The interrupt status of this {@link Thread} will be restored.
	 *
	 * @throws TimeoutException thrown if waiting takes longer than the specified timeout
	 */
	default void awaitUninterrupted(long time, TimeUnit unit) throws TimeoutException {
		boolean interrupted = false;
		try {
			while (true) {
				try {
					await(time, unit);
					return;
				} catch (InterruptedException e) {
					interrupted = true;
				}
			}
		} finally {
			if (interrupted)
				Thread.currentThread().interrupt();
		}
	}
	
	//default methods
	default <R> Future<R> toFuture(Future<R> supplier) {
		return toFuture(supplier::assertGet);
	}
	
	default <R> Future<R> toFuture(Supplier<R> supplier) {
		return new Future<>() {
			@Override
			public R awaitGet() throws InterruptedException {
				Barrier.this.await();
				return supplier.get();
			}
			
			@Override
			public R awaitGet(long time, TimeUnit unit) throws InterruptedException, TimeoutException {
				Barrier.this.await(time, unit);
				return supplier.get();
			}
			
			@Override
			public R assertGet() throws FutureNotFinishedException {
				if (!Barrier.this.isFinished())
					throw new FutureNotFinishedException(this);
				return supplier.get();
			}
			
			@Override
			public boolean isFinished() {
				return Barrier.this.isFinished();
			}
			
			@Override
			public void addHook(@NotNull Runnable run) {
				Barrier.this.addHook(run);
			}
			
			@Override
			public void removeHook(@NotNull Runnable run) {
				Barrier.this.removeHook(run);
			}
			
			@Override
			public void await() throws InterruptedException {
				Barrier.this.await();
			}
			
			@Override
			public void await(long time, TimeUnit unit) throws InterruptedException, TimeoutException {
				Barrier.this.await(time, unit);
			}
		};
	}
	
	/**
	 * Creates a new Barrier which can be used just like this but does not hold a reference to this.
	 * Should be used when holding a Barrier for a very long time (eg. lifetime of an object) to prevent eg. a RunnableTask to stay referenced
	 */
	default Barrier dereference() {
		BarrierImpl barrier = new BarrierImpl();
		this.addHook(barrier::triggerNow);
		return barrier;
	}
	
	//static
	
	/**
	 * Awaits for all {@link Barrier Barriers} to be triggered, then triggers the returned {@link Barrier}. This Operation is non-blocking.
	 * If no Barriers are given, the Barrier is returned triggered.
	 *
	 * @param barriers the {@link Barrier Barriers} to await upon
	 * @return A {@link Barrier} which is triggered when all supplied {@link Barrier Barriers} have.
	 */
	static Barrier awaitAll(@NotNull Collection<? extends Barrier> barriers) {
		return awaitAll(barriers.toArray(new Barrier[0]));
	}
	
	/**
	 * Awaits for all {@link Barrier Barriers} to be triggered, then triggers the returned {@link Barrier}. This Operation is non-blocking.
	 * If no Barriers are given, the Barrier is returned triggered.
	 *
	 * @param barriers the {@link Barrier Barriers} to await upon
	 * @return A {@link Barrier} which is triggered when all supplied {@link Barrier Barriers} have.
	 */
	static Barrier awaitAll(@NotNull Stream<? extends Barrier> barriers) {
		return awaitAll(barriers.toArray(Barrier[]::new));
	}
	
	/**
	 * Awaits for all {@link Barrier Barriers} to be triggered, then triggeres the returned {@link Barrier}. This Operation is non-blocking.
	 * If no Barriers are given, the Barrier is returned triggered.
	 *
	 * @param barriers the {@link Barrier Barriers} to await upon
	 * @return A {@link Barrier} which is triggered when all supplied {@link Barrier Barriers} have.
	 */
	static Barrier awaitAll(@NotNull Barrier... barriers) {
		if (barriers.length == 0)
			return ALWAYS_TRIGGERED_BARRIER;
		
		BarrierImpl ret = new BarrierImpl();
		awaitAll(ret::triggerNow, barriers);
		return ret;
	}
	
	/**
	 * Awaits for all {@link Barrier Barriers} to be triggered, then executes toRun. This Operation is non-blocking.
	 * If no Barriers are given, the Runnable is executed immediately.
	 *
	 * @param toRun    something to be executed when all {@link Barrier Barriers} are triggered
	 * @param barriers the {@link Barrier Barriers} to await upon
	 */
	static void awaitAll(@NotNull Runnable toRun, @NotNull Collection<@NotNull ? extends Barrier> barriers) {
		awaitAll(toRun, barriers.toArray(new Barrier[0]));
	}
	
	/**
	 * Awaits for all {@link Barrier Barriers} to be triggered, then executes toRun. This Operation is non-blocking.
	 * If no Barriers are given, the Runnable is executed immediately.
	 *
	 * @param toRun    something to be executed when all {@link Barrier Barriers} are triggered
	 * @param barriers the {@link Barrier Barriers} to await upon
	 */
	static void awaitAll(@NotNull Runnable toRun, @NotNull Stream<@NotNull ? extends Barrier> barriers) {
		awaitAll(toRun, barriers.toArray(Barrier[]::new));
	}
	
	/**
	 * Awaits for all {@link Barrier Barriers} to be triggered, then executes toRun. This Operation is non-blocking.
	 * If no Barriers are given, the Runnable is executed immediately.
	 *
	 * @param toRun    something to be executed when all {@link Barrier Barriers} are triggered
	 * @param barriers the {@link Barrier Barriers} to await upon
	 */
	static void awaitAll(@NotNull Runnable toRun, @NotNull Barrier... barriers) {
		if (barriers.length == 0) {
			toRun.run();
			return;
		}
		
		final AtomicInteger cnt = new AtomicInteger(barriers.length);
		for (Barrier barrier : barriers) {
			barrier.addHook(() -> {
				if (cnt.decrementAndGet() == 0)
					toRun.run();
			});
		}
	}
	
	/**
	 * Returns a new {@link Barrier} which triggers when the 'inner' {@link Barrier} of the supplied {@link Future} is triggered.
	 *
	 * @param future the Future containing the Barrier to await for
	 * @return see description
	 */
	static Barrier innerBarrier(@NotNull Future<? extends @NotNull Barrier> future) {
		BarrierImpl ret = new BarrierImpl();
		future.addHook(() -> future.assertGet().addHook(ret::triggerNow));
		return ret;
	}
	
	/**
	 * Returns a new {@link Barrier} which triggers when the 'inner' {@link Barrier} of the supplied {@link Future} is triggered.
	 *
	 * @param future the Future containing the Barrier to await for
	 * @return see description
	 */
	static Barrier innerInnerBarrier(@NotNull Future<? extends @NotNull Future<? extends @NotNull Barrier>> future) {
		BarrierImpl ret = new BarrierImpl();
		future.addHook(() -> {
			Future<? extends Barrier> inner1 = future.assertGet();
			inner1.addHook(() -> inner1.assertGet().addHook(ret::triggerNow));
		});
		return ret;
	}
}
