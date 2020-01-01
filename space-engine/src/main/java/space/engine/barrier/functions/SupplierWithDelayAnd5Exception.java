package space.engine.barrier.functions;

import space.engine.barrier.DelayTask;

@FunctionalInterface
public interface SupplierWithDelayAnd5Exception<T, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable, EX4 extends Throwable, EX5 extends Throwable> {
	
	T get() throws DelayTask, EX1, EX2, EX3, EX4, EX5;
}
