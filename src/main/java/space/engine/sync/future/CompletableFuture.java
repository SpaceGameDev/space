package space.engine.sync.future;

import space.engine.sync.barrier.BarrierImpl;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CompletableFuture<R> extends BarrierImpl implements Future<R> {
	
	private R result;
	
	public synchronized void complete(R result) {
		this.result = result;
		triggerNow();
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
		if (!isFinished())
			throw new FutureNotFinishedException(this);
		return result;
	}
}
