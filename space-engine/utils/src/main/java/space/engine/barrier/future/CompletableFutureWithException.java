package space.engine.barrier.future;

import space.engine.barrier.BarrierImpl;
import space.engine.barrier.DelayTask;
import space.engine.barrier.functions.Callable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CompletableFutureWithException<R, EX extends Throwable> extends BarrierImpl implements FutureWithException<R, EX>, GenericCompletable<R> {
	
	public final Class<EX> exceptionClass;
	
	protected volatile R result;
	protected volatile EX exception;
	
	public CompletableFutureWithException(Class<EX> exceptionClass) {
		this.exceptionClass = exceptionClass;
	}
	
	//complete
	@Override
	public void completeCallable(Callable<R> callable) throws DelayTask {
		try {
			complete(callable.call());
		} catch (DelayTask delayTask) {
			throw delayTask;
		} catch (Throwable e) {
			if (exceptionClass.isInstance(e))
				//noinspection unchecked
				completeExceptional((EX) e);
			else
				throw GenericFuture.newUnexpectedException(e);
		}
	}
	
	//no synchronized -> deadlocks in triggerNow() callback handling
	@Override
	public void complete(R result) {
		this.result = result;
		super.triggerNow();
	}
	
	public void completeExceptional(EX exception) {
		this.exception = exception;
		super.triggerNow();
	}
	
	/**
	 * Use {@link #complete(Object)}
	 */
	@Override
	@Deprecated
	public boolean triggerNow() {
		throw new UnsupportedOperationException("Use #compete(Object)");
	}
	
	//get
	@Override
	public R awaitGet() throws InterruptedException, EX {
		await();
		return getInternal();
	}
	
	@Override
	public R awaitGet(long time, TimeUnit unit) throws InterruptedException, TimeoutException, EX {
		await(time, unit);
		return getInternal();
	}
	
	@Override
	public R assertGet() throws FutureNotFinishedException, EX {
		if (!isDone())
			throw new FutureNotFinishedException(this);
		return getInternal();
	}
	
	protected R getInternal() throws EX {
		if (exception != null)
			throw exception;
		return result;
	}
}
