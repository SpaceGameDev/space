package space.engine.barrier.future;

import space.engine.barrier.DelayTask;
import space.engine.barrier.functions.Callable;

public interface GenericCompletable<R> extends GenericFuture<R> {
	
	default void completeCallableNoDelay(Callable<R> callable) {
		try {
			completeCallable(callable);
		} catch (DelayTask e) {
			throw GenericFuture.newUnexpectedException(e);
		}
	}
	
	void completeCallable(Callable<R> callable) throws DelayTask;
	
	void complete(R result);
}
