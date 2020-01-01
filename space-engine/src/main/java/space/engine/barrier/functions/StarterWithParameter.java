package space.engine.barrier.functions;

import space.engine.barrier.Barrier;
import space.engine.barrier.DelayTask;

@FunctionalInterface
public interface StarterWithParameter<B extends Barrier, P> {
	
	B start(P p) throws DelayTask;
	
	default B startInlineException(P p) {
		try {
			return start(p);
		} catch (DelayTask delayTask) {
			//noinspection unchecked
			return (B) delayTask.barrier;
		}
	}
}
