package space.engine.barrier.functions;

import space.engine.barrier.DelayTask;

@FunctionalInterface
public interface FunctionWithDelay<T, R> {
	
	R apply(T t) throws DelayTask;
}
