package space.engine.barrier.functions;

import space.engine.barrier.Barrier;
import space.engine.barrier.DelayTask;

@FunctionalInterface
public interface StarterWith2Parameter<B extends Barrier, P1, P2> {
	
	B start(P1 p1, P2 p2) throws DelayTask;
	
	default B startInlineException(P1 p1, P2 p2) {
		try {
			return start(p1, p2);
		} catch (DelayTask delayTask) {
			//noinspection unchecked
			return (B) delayTask.barrier;
		}
	}
}
