package space.engine.barrier.future;

import space.engine.barrier.BarrierImpl;
import space.engine.barrier.DelayTask;
import space.engine.barrier.functions.Callable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CompletableFuture<R> extends BarrierImpl implements Future<R>, GenericCompletable<R> {
	
	private volatile R result;
	
	//complete
	@Override
	public void completeCallable(Callable<R> callable) throws DelayTask {
		try {
			complete(callable.call());
		} catch (DelayTask delayTask) {
			throw delayTask;
		} catch (Throwable e) {
			throw GenericFuture.newUnexpectedException(e);
		}
	}
	
	//no synchronized -> deadlocks in triggerNow() callback handling
	@Override
	public void complete(R result) {
		this.result = result;
		super.triggerNow();
	}
	
	/**
	 * Use {@link #complete(Object)}
	 */
	@Override
	@Deprecated
	public void triggerNow() {
		throw new UnsupportedOperationException("Use #compete(Object)");
	}
	
	//implement
	@Override
	public R awaitGet() throws InterruptedException {
		await();
		return result;
	}
	
	@Override
	public R awaitGet(long time, TimeUnit unit) throws InterruptedException, TimeoutException {
		await(time, unit);
		return result;
	}
	
	@Override
	public R assertGet() throws FutureNotFinishedException {
		if (!isDone())
			throw new FutureNotFinishedException(this);
		return result;
	}
}
