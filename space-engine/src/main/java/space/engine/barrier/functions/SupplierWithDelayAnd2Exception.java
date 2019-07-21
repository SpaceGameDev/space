package space.engine.barrier.functions;

import space.engine.barrier.DelayTask;

//thenFutureWith2Exception
@FunctionalInterface
public
interface SupplierWithDelayAnd2Exception<T, EX1 extends Throwable, EX2 extends Throwable> {
	
	T get() throws DelayTask, EX1, EX2;
}
