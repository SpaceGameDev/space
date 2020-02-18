package space.engine.barrier.future;

import space.engine.barrier.BarrierImpl;
import space.engine.barrier.DelayTask;
import space.engine.barrier.functions.Callable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CompletableFutureWith2Exception<R, EX1 extends Throwable, EX2 extends Throwable> extends BarrierImpl implements FutureWith2Exception<R, EX1, EX2>, GenericCompletable<R> {
	
	public final Class<EX1> exceptionClass1;
	public final Class<EX2> exceptionClass2;
	
	protected volatile R result;
	protected volatile EX1 exception1;
	protected volatile EX2 exception2;
	
	public CompletableFutureWith2Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2) {
		this.exceptionClass1 = exceptionClass1;
		this.exceptionClass2 = exceptionClass2;
	}
	
	//complete
	@Override
	public void completeCallable(Callable<R> callable) throws DelayTask {
		try {
			complete(callable.call());
		} catch (DelayTask delayTask) {
			throw delayTask;
		} catch (Throwable e) {
			if (exceptionClass1.isInstance(e))
				//noinspection unchecked
				completeExceptional1((EX1) e);
			else if (exceptionClass2.isInstance(e))
				//noinspection unchecked
				completeExceptional2((EX2) e);
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
	
	public void completeExceptional1(EX1 exception) {
		this.exception1 = exception;
		super.triggerNow();
	}
	
	public void completeExceptional2(EX2 exception) {
		this.exception2 = exception;
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
	public R awaitGet() throws InterruptedException, EX1, EX2 {
		await();
		return getInternal();
	}
	
	@Override
	public R awaitGet(long time, TimeUnit unit) throws InterruptedException, TimeoutException, EX1, EX2 {
		await(time, unit);
		return getInternal();
	}
	
	@Override
	public R assertGet() throws FutureNotFinishedException, EX1, EX2 {
		if (!isDone())
			throw new FutureNotFinishedException(this);
		return getInternal();
	}
	
	protected R getInternal() throws EX1, EX2 {
		if (exception1 != null)
			throw exception1;
		if (exception2 != null)
			throw exception2;
		return result;
	}
}
