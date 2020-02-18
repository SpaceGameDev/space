package space.engine.barrier.functions;

import space.engine.barrier.DelayTask;

@FunctionalInterface
public interface ConsumerWithDelay<T> {
	
	void accept(T t) throws DelayTask;
}
