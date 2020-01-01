package space.engine.barrier.functions;

import space.engine.barrier.Barrier;
import space.engine.barrier.DelayTask;
import space.engine.baseobject.CanceledCheck;

@FunctionalInterface
public interface CancelableStarter {
	
	Barrier start(CanceledCheck check) throws DelayTask;
	
	default Barrier startNoException(CanceledCheck check) {
		try {
			return start(check);
		} catch (DelayTask delayTask) {
			return delayTask.barrier;
		}
	}
	
	static CancelableStarter noop() {
		return check -> Barrier.done();
	}
}
