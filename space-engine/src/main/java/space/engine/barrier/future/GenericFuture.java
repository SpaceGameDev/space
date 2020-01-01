package space.engine.barrier.future;

import space.engine.barrier.Barrier;

import java.util.concurrent.TimeUnit;

public interface GenericFuture<R> extends Barrier {
	
	R awaitGetAnyException() throws Throwable;
	
	R awaitGetAnyException(long time, TimeUnit unit) throws Throwable;
	
	R assertGetAnyException() throws Throwable;
	
	static RuntimeException newUnexpectedException(Throwable e) {
		return new RuntimeException("Exception caught by Future that was not expected to be thrown", e);
	}
}
