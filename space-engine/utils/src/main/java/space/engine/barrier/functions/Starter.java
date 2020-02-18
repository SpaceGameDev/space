package space.engine.barrier.functions;

import space.engine.barrier.Barrier;
import space.engine.barrier.DelayTask;

@FunctionalInterface
public interface Starter<B extends Barrier> {
	
	B start() throws DelayTask;
	
	default B startInlineException() {
		try {
			return start();
		} catch (DelayTask delayTask) {
			//noinspection unchecked
			return (B) delayTask.barrier;
		}
	}
	
	static Starter<Barrier> noop() {
		return Barrier::done;
	}
}
