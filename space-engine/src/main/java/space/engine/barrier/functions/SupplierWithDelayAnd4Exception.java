package space.engine.barrier.functions;

import space.engine.barrier.DelayTask;

//thenFutureWith4Exception
@FunctionalInterface
public
interface SupplierWithDelayAnd4Exception<T, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable, EX4 extends Throwable> {
	
	T get() throws DelayTask, EX1, EX2, EX3, EX4;
}
