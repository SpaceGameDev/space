package space.engine.barrier.functions;

import space.engine.barrier.Barrier;
import space.engine.barrier.DelayTask;

@FunctionalInterface
public interface StarterWith3Parameter<B extends Barrier, P1, P2, P3> {
	
	B start(P1 p1, P2 p2, P3 p3) throws DelayTask;
	
	default B startInlineException(P1 p1, P2 p2, P3 p3) {
		try {
			return start(p1, p2, p3);
		} catch (DelayTask delayTask) {
			//noinspection unchecked
			return (B) delayTask.barrier;
		}
	}
}
