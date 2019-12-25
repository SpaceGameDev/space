package space.engine.barrier;

import org.jetbrains.annotations.NotNull;
import space.engine.barrier.functions.CancelableRunnableWithDelay;
import space.engine.barrier.functions.CancelableStarter;
import space.engine.barrier.functions.RunnableWithDelay;
import space.engine.barrier.functions.Starter;
import space.engine.barrier.functions.SupplierWithDelay;
import space.engine.barrier.functions.SupplierWithDelayAnd2Exception;
import space.engine.barrier.functions.SupplierWithDelayAnd3Exception;
import space.engine.barrier.functions.SupplierWithDelayAnd4Exception;
import space.engine.barrier.functions.SupplierWithDelayAnd5Exception;
import space.engine.barrier.functions.SupplierWithDelayAndException;
import space.engine.barrier.future.BaseFuture;
import space.engine.barrier.future.CompletableFuture;
import space.engine.barrier.future.CompletableFutureWithException;
import space.engine.barrier.future.CompletableFutureWithException.CompletableFutureWith2Exception;
import space.engine.barrier.future.CompletableFutureWithException.CompletableFutureWith3Exception;
import space.engine.barrier.future.CompletableFutureWithException.CompletableFutureWith4Exception;
import space.engine.barrier.future.CompletableFutureWithException.CompletableFutureWith5Exception;
import space.engine.barrier.future.Future;
import space.engine.barrier.future.FutureNotFinishedException;
import space.engine.barrier.future.FutureWithException;
import space.engine.barrier.future.FutureWithException.FutureWith2Exception;
import space.engine.barrier.future.FutureWithException.FutureWith3Exception;
import space.engine.barrier.future.FutureWithException.FutureWith4Exception;
import space.engine.barrier.future.FutureWithException.FutureWith5Exception;
import space.engine.barrier.lock.SyncLock;
import space.engine.simpleQueue.pool.Executor;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static space.engine.Side.pool;

/**
 * An Object which can be {@link #await() awaited} upon. You can also {@link #addHook(Runnable) add a Hook} to be called when the Barrier {@link #isDone() is finished}. <br>
 * <b>It cannot be triggered more than once or reset</b>.
 */
public interface Barrier {
	
	boolean BARRIER_DEBUG = false;
	
	class DoneBarrier implements Barrier {
		
		protected DoneBarrier() {
		}
		
		@Override
		public boolean isDone() {
			return true;
		}
		
		@Override
		public void addHook(@NotNull Runnable run) {
			run.run();
		}
		
		@Override
		public void await() {
		
		}
		
		@Override
		public void await(long time, TimeUnit unit) {
		
		}
	}
	
	Barrier DONE_BARRIER = new DoneBarrier();
	
	//getter
	
	/**
	 * Gets the state if this {@link Barrier}, whether it is finished or not.<br>
	 * NOTE: The triggered state should be considered immediately stale, as it can change any Moment without notice.
	 *
	 * @return whether the {@link Barrier} is triggered or not
	 */
	boolean isDone();
	
	//hooks
	
	/**
	 * Adds a hook to be called when the Event triggers
	 *
	 * @param run the hook as a {@link Runnable}
	 */
	void addHook(@NotNull Runnable run);
	
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
	
	//toFuture
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
				if (!Barrier.this.isDone())
					throw new FutureNotFinishedException(this);
				return supplier.get();
			}
			
			@Override
			public boolean isDone() {
				return Barrier.this.isDone();
			}
			
			@Override
			public void addHook(@NotNull Runnable run) {
				Barrier.this.addHook(run);
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
		if (isDone())
			return done();
		
		BarrierImpl barrier = new BarrierImpl();
		this.addHook(barrier::triggerNow);
		return barrier;
	}
	
	//run
	default Barrier thenRun(RunnableWithDelay runnable) {
		return thenRun(pool(), runnable);
	}
	
	default Barrier thenRun(Executor executor, RunnableWithDelay runnable) {
		BarrierImpl ret = new BarrierImpl();
		addHook(() -> executor.execute(() -> {
			try {
				runnable.run();
				ret.triggerNow();
			} catch (DelayTask delayTask) {
				delayTask.barrier.addHook(ret::triggerNow);
			}
		}));
		return ret;
	}
	
	static Barrier nowRun(RunnableWithDelay runnable) {
		return when().thenRun(runnable);
	}
	
	static Barrier nowRun(Executor executor, RunnableWithDelay runnable) {
		return when().thenRun(executor, runnable);
	}
	
	//start
	default Barrier thenStart(Starter runnable) {
		BarrierImpl ret = new BarrierImpl();
		addHook(() -> runnable.startNoException().addHook(ret::triggerNow));
		return ret;
	}
	
	default Barrier thenStart(Executor executor, Starter runnable) {
		BarrierImpl ret = new BarrierImpl();
		addHook(() -> executor.execute(() -> runnable.startNoException().addHook(ret::triggerNow)));
		return ret;
	}
	
	/**
	 * This method just delegates to {@link Starter#startNoException()}
	 */
	static Barrier nowStart(Starter runnable) {
		return runnable.startNoException();
	}
	
	static Barrier nowStart(Executor executor, Starter runnable) {
		return when().thenStart(executor, runnable);
	}
	
	//runCancelable
	default CancelableBarrier thenRunCancelable(CancelableRunnableWithDelay runnable) {
		return thenRunCancelable(pool(), runnable);
	}
	
	default CancelableBarrier thenRunCancelable(Executor executor, CancelableRunnableWithDelay runnable) {
		CancelableBarrierImpl ret = new CancelableBarrierImpl();
		addHook(() -> executor.execute(() -> {
			try {
				runnable.run(ret);
				ret.triggerNow();
			} catch (DelayTask delayTask) {
				delayTask.barrier.addHook(ret::triggerNow);
			}
		}));
		return ret;
	}
	
	static CancelableBarrier nowRunCancelable(CancelableRunnableWithDelay runnable) {
		return when().thenRunCancelable(runnable);
	}
	
	static CancelableBarrier nowRunCancelable(Executor executor, CancelableRunnableWithDelay runnable) {
		return when().thenRunCancelable(executor, runnable);
	}
	
	//startCancelable
	default CancelableBarrier thenStartCancelable(CancelableStarter runnable) {
		CancelableBarrierImpl ret = new CancelableBarrierImpl();
		addHook(() -> runnable.startNoException(ret).addHook(ret::triggerNow));
		return ret;
	}
	
	default CancelableBarrier thenStartCancelable(Executor executor, CancelableStarter runnable) {
		CancelableBarrierImpl ret = new CancelableBarrierImpl();
		addHook(() -> executor.execute(() -> runnable.startNoException(ret).addHook(ret::triggerNow)));
		return ret;
	}
	
	static CancelableBarrier nowStartCancelable(CancelableStarter runnable) {
		return when().thenStartCancelable(runnable);
	}
	
	static CancelableBarrier nowStartCancelable(Executor executor, CancelableStarter runnable) {
		return when().thenStartCancelable(executor, runnable);
	}
	
	//Future
	default <T> Future<T> thenStartFuture(SupplierWithDelay<T> runnable) {
		return thenFuture(Runnable::run, runnable);
	}
	
	default <T> Future<T> thenFuture(SupplierWithDelay<T> runnable) {
		return thenFuture(pool(), runnable);
	}
	
	default <T> Future<T> thenFuture(Executor executor, SupplierWithDelay<T> runnable) {
		CompletableFuture<T> ret = new CompletableFuture<>();
		addHook(() -> executor.execute(() -> {
			try {
				ret.complete(runnable.get());
			} catch (DelayTask delayTask) {
				if (BARRIER_DEBUG && !(delayTask.barrier instanceof Future))
					throw new IllegalArgumentException("DelayTask.barrier is not a Future<?>!", delayTask);
				
				//noinspection unchecked
				Future<T> future = (Future<T>) delayTask.barrier;
				future.addHook(() -> ret.complete(future.assertGet()));
			}
		}));
		return ret;
	}
	
	static <T> Future<T> nowFuture(SupplierWithDelay<T> runnable) {
		return when().thenFuture(runnable);
	}
	
	static <T> Future<T> nowFuture(Executor executor, SupplierWithDelay<T> runnable) {
		return when().thenFuture(executor, runnable);
	}
	
	//FutureWithException
	default <T, EX extends Throwable> FutureWithException<T, EX> thenStartFutureWithException(Class<EX> exceptionClass, SupplierWithDelayAndException<T, EX> runnable) {
		return thenFutureWithException(exceptionClass, Runnable::run, runnable);
	}
	
	default <T> Future<T> thenFutureWithException(SupplierWithDelay<T> runnable) {
		return thenFuture(pool(), runnable);
	}
	
	default <T, EX extends Throwable> FutureWithException<T, EX> thenFutureWithException(Class<EX> exceptionClass, SupplierWithDelayAndException<T, EX> runnable) {
		return thenFutureWithException(exceptionClass, pool(), runnable);
	}
	
	default <T, EX extends Throwable> FutureWithException<T, EX> thenFutureWithException(Class<EX> exceptionClass, Executor executor, SupplierWithDelayAndException<T, EX> runnable) {
		CompletableFutureWithException<T, EX> ret = new CompletableFutureWithException<>(exceptionClass);
		addHook(() -> executor.execute(() -> {
			try {
				ret.completeCallable(runnable::get);
			} catch (DelayTask delayTask) {
				if (BARRIER_DEBUG && !(delayTask.barrier instanceof BaseFuture))
					throw new IllegalArgumentException("DelayTask.barrier is not a Future<?>!", delayTask);
				
				//noinspection unchecked
				BaseFuture<T> future = (BaseFuture<T>) delayTask.barrier;
				future.addHook(() -> {
					try {
						ret.completeCallable(future::assertGetAnyException);
					} catch (DelayTask e) {
						throw new RuntimeException("impossible", e);
					}
				});
			}
		}));
		return ret;
	}
	
	static <T, EX extends Throwable> FutureWithException<T, EX> nowFutureWithException(Class<EX> exceptionClass, SupplierWithDelayAndException<T, EX> runnable) {
		return when().thenFutureWithException(exceptionClass, runnable);
	}
	
	static <T, EX extends Throwable> FutureWithException<T, EX> nowFutureWithException(Class<EX> exceptionClass, Executor executor, SupplierWithDelayAndException<T, EX> runnable) {
		return when().thenFutureWithException(exceptionClass, executor, runnable);
	}
	
	//FutureWith2Exception
	default <T, EX1 extends Throwable, EX2 extends Throwable> FutureWith2Exception<T, EX1, EX2> thenStartFutureWith2Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, SupplierWithDelayAnd2Exception<T, EX1, EX2> runnable) {
		return thenFutureWith2Exception(exceptionClass1, exceptionClass2, Runnable::run, runnable);
	}
	
	default <T> Future<T> thenFutureWith2Exception(SupplierWithDelay<T> runnable) {
		return thenFuture(pool(), runnable);
	}
	
	default <T, EX1 extends Throwable, EX2 extends Throwable> FutureWith2Exception<T, EX1, EX2> thenFutureWith2Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, SupplierWithDelayAnd2Exception<T, EX1, EX2> runnable) {
		return thenFutureWith2Exception(exceptionClass1, exceptionClass2, pool(), runnable);
	}
	
	default <T, EX1 extends Throwable, EX2 extends Throwable> FutureWith2Exception<T, EX1, EX2> thenFutureWith2Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Executor executor, SupplierWithDelayAnd2Exception<T, EX1, EX2> runnable) {
		CompletableFutureWith2Exception<T, EX1, EX2> ret = new CompletableFutureWith2Exception<>(exceptionClass1, exceptionClass2);
		addHook(() -> executor.execute(() -> {
			try {
				ret.completeCallable(runnable::get);
			} catch (DelayTask delayTask) {
				if (BARRIER_DEBUG && !(delayTask.barrier instanceof BaseFuture))
					throw new IllegalArgumentException("DelayTask.barrier is not a Future<?>!", delayTask);
				
				//noinspection unchecked
				BaseFuture<T> future = (BaseFuture<T>) delayTask.barrier;
				future.addHook(() -> {
					try {
						ret.completeCallable(future::assertGetAnyException);
					} catch (DelayTask e) {
						throw new RuntimeException("impossible", e);
					}
				});
			}
		}));
		return ret;
	}
	
	static <T, EX1 extends Throwable, EX2 extends Throwable> FutureWith2Exception<T, EX1, EX2> nowFutureWith2Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, SupplierWithDelayAnd2Exception<T, EX1, EX2> runnable) {
		return when().thenFutureWith2Exception(exceptionClass1, exceptionClass2, runnable);
	}
	
	static <T, EX1 extends Throwable, EX2 extends Throwable> FutureWith2Exception<T, EX1, EX2> nowFutureWith2Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Executor executor, SupplierWithDelayAnd2Exception<T, EX1, EX2> runnable) {
		return when().thenFutureWith2Exception(exceptionClass1, exceptionClass2, executor, runnable);
	}
	
	//FutureWith3Exception
	default <T, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable> FutureWith3Exception<T, EX1, EX2, EX3> thenStartFutureWith3Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Class<EX3> exceptionClass3, SupplierWithDelayAnd3Exception<T, EX1, EX2, EX3> runnable) {
		return thenFutureWith3Exception(exceptionClass1, exceptionClass2, exceptionClass3, Runnable::run, runnable);
	}
	
	default <T> Future<T> thenFutureWith3Exception(SupplierWithDelay<T> runnable) {
		return thenFuture(pool(), runnable);
	}
	
	default <T, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable> FutureWith3Exception<T, EX1, EX2, EX3> thenFutureWith3Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Class<EX3> exceptionClass3, SupplierWithDelayAnd3Exception<T, EX1, EX2, EX3> runnable) {
		return thenFutureWith3Exception(exceptionClass1, exceptionClass2, exceptionClass3, pool(), runnable);
	}
	
	default <T, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable> FutureWith3Exception<T, EX1, EX2, EX3> thenFutureWith3Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Class<EX3> exceptionClass3, Executor executor, SupplierWithDelayAnd3Exception<T, EX1, EX2, EX3> runnable) {
		CompletableFutureWith3Exception<T, EX1, EX2, EX3> ret = new CompletableFutureWith3Exception<>(exceptionClass1, exceptionClass2, exceptionClass3);
		addHook(() -> executor.execute(() -> {
			try {
				ret.completeCallable(runnable::get);
			} catch (DelayTask delayTask) {
				if (BARRIER_DEBUG && !(delayTask.barrier instanceof BaseFuture))
					throw new IllegalArgumentException("DelayTask.barrier is not a Future<?>!", delayTask);
				
				//noinspection unchecked
				BaseFuture<T> future = (BaseFuture<T>) delayTask.barrier;
				future.addHook(() -> {
					try {
						ret.completeCallable(future::assertGetAnyException);
					} catch (DelayTask e) {
						throw new RuntimeException("impossible", e);
					}
				});
			}
		}));
		return ret;
	}
	
	static <T, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable> FutureWith3Exception<T, EX1, EX2, EX3> nowFutureWith3Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Class<EX3> exceptionClass3, SupplierWithDelayAnd3Exception<T, EX1, EX2, EX3> runnable) {
		return when().thenFutureWith3Exception(exceptionClass1, exceptionClass2, exceptionClass3, runnable);
	}
	
	static <T, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable> FutureWith3Exception<T, EX1, EX2, EX3> nowFutureWith3Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Class<EX3> exceptionClass3, Executor executor, SupplierWithDelayAnd3Exception<T, EX1, EX2, EX3> runnable) {
		return when().thenFutureWith3Exception(exceptionClass1, exceptionClass2, exceptionClass3, executor, runnable);
	}
	
	//FutureWith4Exception
	default <T, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable, EX4 extends Throwable> FutureWith4Exception<T, EX1, EX2, EX3, EX4> thenStartFutureWith4Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Class<EX3> exceptionClass3, Class<EX4> exceptionClass4, SupplierWithDelayAnd4Exception<T, EX1, EX2, EX3, EX4> runnable) {
		return thenFutureWith4Exception(exceptionClass1, exceptionClass2, exceptionClass3, exceptionClass4, Runnable::run, runnable);
	}
	
	default <T> Future<T> thenFutureWith4Exception(SupplierWithDelay<T> runnable) {
		return thenFuture(pool(), runnable);
	}
	
	default <T, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable, EX4 extends Throwable> FutureWith4Exception<T, EX1, EX2, EX3, EX4> thenFutureWith4Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Class<EX3> exceptionClass3, Class<EX4> exceptionClass4, SupplierWithDelayAnd4Exception<T, EX1, EX2, EX3, EX4> runnable) {
		return thenFutureWith4Exception(exceptionClass1, exceptionClass2, exceptionClass3, exceptionClass4, pool(), runnable);
	}
	
	default <T, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable, EX4 extends Throwable> FutureWith4Exception<T, EX1, EX2, EX3, EX4> thenFutureWith4Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Class<EX3> exceptionClass3, Class<EX4> exceptionClass4, Executor executor, SupplierWithDelayAnd4Exception<T, EX1, EX2, EX3, EX4> runnable) {
		CompletableFutureWith4Exception<T, EX1, EX2, EX3, EX4> ret = new CompletableFutureWith4Exception<>(exceptionClass1, exceptionClass2, exceptionClass3, exceptionClass4);
		addHook(() -> executor.execute(() -> {
			try {
				ret.completeCallable(runnable::get);
			} catch (DelayTask delayTask) {
				if (BARRIER_DEBUG && !(delayTask.barrier instanceof BaseFuture))
					throw new IllegalArgumentException("DelayTask.barrier is not a Future<?>!", delayTask);
				
				//noinspection unchecked
				BaseFuture<T> future = (BaseFuture<T>) delayTask.barrier;
				future.addHook(() -> {
					try {
						ret.completeCallable(future::assertGetAnyException);
					} catch (DelayTask e) {
						throw new RuntimeException("impossible", e);
					}
				});
			}
		}));
		return ret;
	}
	
	static <T, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable, EX4 extends Throwable> FutureWith4Exception<T, EX1, EX2, EX3, EX4> nowFutureWith4Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Class<EX3> exceptionClass3, Class<EX4> exceptionClass4, SupplierWithDelayAnd4Exception<T, EX1, EX2, EX3, EX4> runnable) {
		return when().thenFutureWith4Exception(exceptionClass1, exceptionClass2, exceptionClass3, exceptionClass4, runnable);
	}
	
	static <T, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable, EX4 extends Throwable> FutureWith4Exception<T, EX1, EX2, EX3, EX4> nowFutureWith4Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Class<EX3> exceptionClass3, Class<EX4> exceptionClass4, Executor executor, SupplierWithDelayAnd4Exception<T, EX1, EX2, EX3, EX4> runnable) {
		return when().thenFutureWith4Exception(exceptionClass1, exceptionClass2, exceptionClass3, exceptionClass4, executor, runnable);
	}
	
	//FutureWith5Exception
	default <T, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable, EX4 extends Throwable, EX5 extends Throwable> FutureWith5Exception<T, EX1, EX2, EX3, EX4, EX5> thenStartFutureWith5Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Class<EX3> exceptionClass3, Class<EX4> exceptionClass4, Class<EX5> exceptionClass5, SupplierWithDelayAnd5Exception<T, EX1, EX2, EX3, EX4, EX5> runnable) {
		return thenFutureWith5Exception(exceptionClass1, exceptionClass2, exceptionClass3, exceptionClass4, exceptionClass5, Runnable::run, runnable);
	}
	
	default <T> Future<T> thenFutureWith5Exception(SupplierWithDelay<T> runnable) {
		return thenFuture(pool(), runnable);
	}
	
	default <T, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable, EX4 extends Throwable, EX5 extends Throwable> FutureWith5Exception<T, EX1, EX2, EX3, EX4, EX5> thenFutureWith5Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Class<EX3> exceptionClass3, Class<EX4> exceptionClass4, Class<EX5> exceptionClass5, SupplierWithDelayAnd5Exception<T, EX1, EX2, EX3, EX4, EX5> runnable) {
		return thenFutureWith5Exception(exceptionClass1, exceptionClass2, exceptionClass3, exceptionClass4, exceptionClass5, pool(), runnable);
	}
	
	default <T, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable, EX4 extends Throwable, EX5 extends Throwable> FutureWith5Exception<T, EX1, EX2, EX3, EX4, EX5> thenFutureWith5Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Class<EX3> exceptionClass3, Class<EX4> exceptionClass4, Class<EX5> exceptionClass5, Executor executor, SupplierWithDelayAnd5Exception<T, EX1, EX2, EX3, EX4, EX5> runnable) {
		CompletableFutureWith5Exception<T, EX1, EX2, EX3, EX4, EX5> ret = new CompletableFutureWith5Exception<>(exceptionClass1, exceptionClass2, exceptionClass3, exceptionClass4, exceptionClass5);
		addHook(() -> executor.execute(() -> {
			try {
				ret.completeCallable(runnable::get);
			} catch (DelayTask delayTask) {
				if (BARRIER_DEBUG && !(delayTask.barrier instanceof BaseFuture))
					throw new IllegalArgumentException("DelayTask.barrier is not a Future<?>!", delayTask);
				
				//noinspection unchecked
				BaseFuture<T> future = (BaseFuture<T>) delayTask.barrier;
				future.addHook(() -> {
					try {
						ret.completeCallable(future::assertGetAnyException);
					} catch (DelayTask e) {
						throw new RuntimeException("impossible", e);
					}
				});
			}
		}));
		return ret;
	}
	
	static <T, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable, EX4 extends Throwable, EX5 extends Throwable> FutureWith5Exception<T, EX1, EX2, EX3, EX4, EX5> nowFutureWith5Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Class<EX3> exceptionClass3, Class<EX4> exceptionClass4, Class<EX5> exceptionClass5, SupplierWithDelayAnd5Exception<T, EX1, EX2, EX3, EX4, EX5> runnable) {
		return when().thenFutureWith5Exception(exceptionClass1, exceptionClass2, exceptionClass3, exceptionClass4, exceptionClass5, runnable);
	}
	
	static <T, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable, EX4 extends Throwable, EX5 extends Throwable> FutureWith5Exception<T, EX1, EX2, EX3, EX4, EX5> nowFutureWith5Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Class<EX3> exceptionClass3, Class<EX4> exceptionClass4, Class<EX5> exceptionClass5, Executor executor, SupplierWithDelayAnd5Exception<T, EX1, EX2, EX3, EX4, EX5> runnable) {
		return when().thenFutureWith5Exception(exceptionClass1, exceptionClass2, exceptionClass3, exceptionClass4, exceptionClass5, executor, runnable);
	}
	
	//locks
	default Barrier thenLock(SyncLock[] locks, Starter runnable) {
		BarrierImpl ret = new BarrierImpl();
		addHook(() -> SyncLock.acquireLocks(locks, () -> {
			Barrier barrier = runnable.startNoException();
			
			//prevents StackOverflow from too many Barrier.addHook() calling the Runnable immediately in combination with SyncLock.unlockLocks()
			//if it is run immediately -> ThenLockUnlockRunnable.immediatelyRun will be false
			ThenLockUnlockRunnable run = new ThenLockUnlockRunnable(locks, ret);
			barrier.addHook(run);
			//if it wasn't run immediately allow it to do so
			run.immediateCAS();
		}));
		return ret;
	}
	
	class ThenLockUnlockRunnable implements Runnable {
		
		private static final VarHandle IMMEDIATE;
		
		static {
			try {
				IMMEDIATE = MethodHandles.lookup().findVarHandle(ThenLockUnlockRunnable.class, "immediatelyRun", boolean.class);
			} catch (NoSuchFieldException | IllegalAccessException e) {
				throw new ExceptionInInitializerError(e);
			}
		}
		
		private final SyncLock[] locks;
		private final BarrierImpl ret;
		
		@SuppressWarnings("unused")
		private volatile boolean immediatelyRun = true;
		
		private ThenLockUnlockRunnable(SyncLock[] locks, BarrierImpl ret) {
			this.locks = locks;
			this.ret = ret;
		}
		
		private boolean immediateCAS() {
			return IMMEDIATE.compareAndSet(this, true, false);
		}
		
		@Override
		public void run() {
			if (immediateCAS()) {
				pool().execute(this);
				return;
			}
			SyncLock.unlockLocks(locks);
			ret.triggerNow();
		}
	}
	
	static Barrier nowLock(SyncLock[] locks, Starter runnable) {
		return when().thenLock(locks, runnable);
	}
	
	//static
	
	/**
	 * returns a Barrier which is always done
	 */
	static Barrier done() {
		return DONE_BARRIER;
	}
	
	/**
	 * Awaits for all {@link Barrier Barriers} to be triggered, then triggers the returned {@link Barrier}. This Operation is non-blocking.
	 * If no Barriers are given, a triggered Barrier is returned.
	 *
	 * @return A {@link Barrier} which is triggered when all supplied {@link Barrier Barriers} have.
	 * @implNote this specific method just returns {@link #done()}
	 */
	static Barrier when() {
		return DONE_BARRIER;
	}
	
	/**
	 * Awaits for all {@link Barrier Barriers} to be triggered, then triggers the returned {@link Barrier}. This Operation is non-blocking.
	 * If no Barriers are given, a triggered Barrier is returned.
	 *
	 * @param barrier the {@link Barrier Barriers} to await upon
	 * @return A {@link Barrier} which is triggered when all supplied {@link Barrier Barriers} have.
	 * @implNote this specific method just returns the supplied barrier
	 */
	static Barrier when(Barrier barrier) {
		return barrier;
	}
	
	/**
	 * Awaits for all {@link Barrier Barriers} to be triggered, then triggers the returned {@link Barrier}. This Operation is non-blocking.
	 * If no Barriers are given, a triggered Barrier is returned.
	 *
	 * @param barriers the {@link Barrier Barriers} to await upon
	 * @return A {@link Barrier} which is triggered when all supplied {@link Barrier Barriers} have.
	 */
	static Barrier when(@NotNull Collection<? extends Barrier> barriers) {
		return when(barriers.toArray(new Barrier[0]));
	}
	
	/**
	 * Awaits for all {@link Barrier Barriers} to be triggered, then triggers the returned {@link Barrier}. This Operation is non-blocking.
	 * If no Barriers are given, a triggered Barrier is returned.
	 *
	 * @param barriers the {@link Barrier Barriers} to await upon
	 * @return A {@link Barrier} which is triggered when all supplied {@link Barrier Barriers} have.
	 */
	static Barrier when(@NotNull Stream<? extends Barrier> barriers) {
		return when(barriers.toArray(Barrier[]::new));
	}
	
	/**
	 * Awaits for all {@link Barrier Barriers} to be triggered, then triggers the returned {@link Barrier}. This Operation is non-blocking.
	 * If no Barriers are given, a triggered Barrier is returned.
	 *
	 * @param barriers the {@link Barrier Barriers} to await upon
	 * @return A {@link Barrier} which is triggered when all supplied {@link Barrier Barriers} have.
	 */
	static Barrier when(@NotNull Barrier... barriers) {
		if (barriers.length == 0)
			return when();
		if (barriers.length == 1)
			return when(barriers[0]);
		
		BarrierImpl ret = new BarrierImpl();
		AtomicInteger cnt = new AtomicInteger(barriers.length);
		Runnable hook = () -> {
			if (cnt.decrementAndGet() == 0)
				ret.triggerNow();
		};
		for (Barrier barrier : barriers)
			barrier.addHook(hook);
		return ret;
	}
	
	/**
	 * Returns a new {@link Barrier} which triggers when the 'inner' {@link Barrier} of the supplied {@link Future} is triggered.
	 *
	 * @param future the Future containing the Barrier to await for
	 * @return see description
	 */
	static Barrier inner(@NotNull Future<? extends @NotNull Barrier> future) {
		if (future.isDone())
			return future.assertGet();
		
		BarrierImpl ret = new BarrierImpl();
		future.addHook(() -> future.assertGet().addHook(ret::triggerNow));
		return ret;
	}
}
