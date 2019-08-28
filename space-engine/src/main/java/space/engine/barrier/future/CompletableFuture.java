package space.engine.barrier.future;

import space.engine.barrier.BarrierImpl;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CompletableFuture<R> extends BarrierImpl implements Future<R> {
	
	private volatile R result;
	
	//no synchronized -> deadlocks in triggerNow() callback handling
	public void complete(R result) {
		this.result = result;
		super.triggerNow();
	}
	
	@Override
	@Deprecated
	public void triggerNow() {
		throw new UnsupportedOperationException("Use compete(R)!");
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
