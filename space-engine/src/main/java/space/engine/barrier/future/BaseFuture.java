package space.engine.barrier.future;

import space.engine.barrier.Barrier;

import java.util.concurrent.TimeUnit;

public interface BaseFuture<R> extends Barrier {
	
	R awaitGetAnyException() throws Throwable;
	
	R awaitGetAnyException(long time, TimeUnit unit) throws Throwable;
	
	R assertGetAnyException() throws Throwable;
}
