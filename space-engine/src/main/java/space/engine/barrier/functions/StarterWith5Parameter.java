package space.engine.barrier.functions;

import space.engine.barrier.Barrier;
import space.engine.barrier.DelayTask;

@FunctionalInterface
public interface StarterWith5Parameter<B extends Barrier, P1, P2, P3, P4, P5> {
	
	B start(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5) throws DelayTask;
	
	default B startInlineException(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5) {
		try {
			return start(p1, p2, p3, p4, p5);
		} catch (DelayTask delayTask) {
			//noinspection unchecked
			return (B) delayTask.barrier;
		}
	}
}
