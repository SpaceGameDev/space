package space.engine.sync.future;

import space.engine.sync.barrier.Barrier;

import java.util.concurrent.TimeUnit;

public interface BaseFuture<R> extends Barrier {
	
	R awaitGetAnyException() throws Throwable;
	
	R awaitGetAnyException(long time, TimeUnit unit) throws Throwable;
	
	R assertGetAnyException() throws Throwable;
}
