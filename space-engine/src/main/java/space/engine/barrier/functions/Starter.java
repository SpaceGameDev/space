package space.engine.barrier.functions;

import space.engine.barrier.Barrier;
import space.engine.barrier.DelayTask;

@FunctionalInterface
public interface Starter {
	
	Barrier start() throws DelayTask;
	
	default Barrier startNoException() {
		try {
			return start();
		} catch (DelayTask delayTask) {
			return delayTask.barrier;
		}
	}
	
	static Starter noop() {
		return Barrier::done;
	}
}
