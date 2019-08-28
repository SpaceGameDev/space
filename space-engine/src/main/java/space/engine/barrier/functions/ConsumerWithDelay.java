package space.engine.barrier.functions;

import space.engine.barrier.DelayTask;

//other functional interfaces with Delay
@FunctionalInterface
public interface ConsumerWithDelay<T> {
	
	void accept(T t) throws DelayTask;
}
