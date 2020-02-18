package space.engine.barrier.future;

import space.engine.barrier.BarrierImpl;
import space.engine.barrier.DelayTask;
import space.engine.barrier.functions.Callable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CompletableFutureWith5Exception<R, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable, EX4 extends Throwable, EX5 extends Throwable> extends BarrierImpl implements FutureWith5Exception<R, EX1, EX2, EX3, EX4, EX5>, GenericCompletable<R> {
	
	public final Class<EX1> exceptionClass1;
	public final Class<EX2> exceptionClass2;
	public final Class<EX3> exceptionClass3;
	public final Class<EX4> exceptionClass4;
	public final Class<EX5> exceptionClass5;
	
	protected volatile R result;
	protected volatile EX1 exception1;
	protected volatile EX2 exception2;
	protected volatile EX3 exception3;
	protected volatile EX4 exception4;
	protected volatile EX5 exception5;
	
	public CompletableFutureWith5Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Class<EX3> exceptionClass3, Class<EX4> exceptionClass4, Class<EX5> exceptionClass5) {
		this.exceptionClass1 = exceptionClass1;
		this.exceptionClass2 = exceptionClass2;
		this.exceptionClass3 = exceptionClass3;
		this.exceptionClass4 = exceptionClass4;
		this.exceptionClass5 = exceptionClass5;
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
			else if (exceptionClass3.isInstance(e))
				//noinspection unchecked
				completeExceptional3((EX3) e);
			else if (exceptionClass4.isInstance(e))
				//noinspection unchecked
				completeExceptional4((EX4) e);
			else if (exceptionClass5.isInstance(e))
				//noinspection unchecked
				completeExceptional5((EX5) e);
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
	
	public void completeExceptional3(EX3 exception) {
		this.exception3 = exception;
		super.triggerNow();
	}
	
	public void completeExceptional4(EX4 exception) {
		this.exception4 = exception;
		super.triggerNow();
	}
	
	public void completeExceptional5(EX5 exception) {
		this.exception5 = exception;
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
	public R awaitGet() throws InterruptedException, EX1, EX2, EX3, EX4, EX5 {
		await();
		return getInternal();
	}
	
	@Override
	public R awaitGet(long time, TimeUnit unit) throws InterruptedException, TimeoutException, EX1, EX2, EX3, EX4, EX5 {
		await(time, unit);
		return getInternal();
	}
	
	@Override
	public R assertGet() throws FutureNotFinishedException, EX1, EX2, EX3, EX4, EX5 {
		if (!isDone())
			throw new FutureNotFinishedException(this);
		return getInternal();
	}
	
	protected synchronized R getInternal() throws EX1, EX2, EX3, EX4, EX5 {
		if (exception1 != null)
			throw exception1;
		if (exception2 != null)
			throw exception2;
		if (exception3 != null)
			throw exception3;
		if (exception4 != null)
			throw exception4;
		if (exception5 != null)
			throw exception5;
		return result;
	}
}
