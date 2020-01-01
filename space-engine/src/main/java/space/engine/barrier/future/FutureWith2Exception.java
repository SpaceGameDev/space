package space.engine.barrier.future;

import org.jetbrains.annotations.NotNull;
import space.engine.barrier.Barrier;
import space.engine.barrier.Delegate;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public interface FutureWith2Exception<R, EX1 extends Throwable, EX2 extends Throwable> extends GenericFuture<R>, Barrier {
	
	static <T, EX1 extends Throwable, EX2 extends Throwable> Delegate<FutureWith2Exception<T, EX1, EX2>, CompletableFutureWith2Exception<T, EX1, EX2>> delegate(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2) {
		return new Delegate<>() {
			@Override
			public CompletableFutureWith2Exception<T, EX1, EX2> createCompletable() {
				return new CompletableFutureWith2Exception<>(exceptionClass1, exceptionClass2);
			}
			
			@Override
			public void complete(CompletableFutureWith2Exception<T, EX1, EX2> ret, FutureWith2Exception<T, EX1, EX2> delegate) {
				ret.completeCallableNoDelay(delegate::assertGetAnyException);
			}
		};
	}
	
	//abstract
	R awaitGet() throws InterruptedException, EX1, EX2;
	
	R awaitGet(long time, TimeUnit unit) throws InterruptedException, TimeoutException, EX1, EX2;
	
	R assertGet() throws FutureNotFinishedException, EX1, EX2;
	
	//awaitGetUninterrupted
	
	/**
	 * Waits until event is triggered and doesn't return when interrupted.
	 * The interrupt status of this {@link Thread} will be restored.
	 */
	default R awaitGetUninterrupted() throws EX1, EX2 {
		boolean interrupted = false;
		try {
			while (true) {
				try {
					return awaitGet();
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
	default R awaitGetUninterrupted(long time, TimeUnit unit) throws TimeoutException, EX1, EX2 {
		boolean interrupted = false;
		try {
			while (true) {
				try {
					return awaitGet(time, unit);
				} catch (InterruptedException e) {
					interrupted = true;
				}
			}
		} finally {
			if (interrupted)
				Thread.currentThread().interrupt();
		}
	}
	
	//anyException
	@Override
	default R awaitGetAnyException() throws Throwable {
		return awaitGet();
	}
	
	@Override
	default R awaitGetAnyException(long time, TimeUnit unit) throws Throwable {
		return awaitGet(time, unit);
	}
	
	@Override
	default R assertGetAnyException() throws Throwable {
		return assertGet();
	}
	
	//static
	static <R, EX1 extends Throwable, EX2 extends Throwable> FutureWith2Exception<R, EX1, EX2> finished(R get) {
		return new FutureWith2Exception<>() {
			@Override
			public R awaitGet() {
				return get;
			}
			
			@Override
			public R awaitGet(long time, TimeUnit unit) {
				return get;
			}
			
			@Override
			public R assertGet() throws FutureNotFinishedException {
				return get;
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
		};
	}
}
