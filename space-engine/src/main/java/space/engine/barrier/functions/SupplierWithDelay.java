package space.engine.barrier.functions;

import space.engine.barrier.DelayTask;

//thenFuture
@FunctionalInterface
public
interface SupplierWithDelay<T> {
	
	T get() throws DelayTask;
}
