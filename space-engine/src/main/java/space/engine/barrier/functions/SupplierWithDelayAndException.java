package space.engine.barrier.functions;

import space.engine.barrier.DelayTask;

@FunctionalInterface
public interface SupplierWithDelayAndException<T, EX extends Throwable> {
	
	T get() throws DelayTask, EX;
}
