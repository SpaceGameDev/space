package space.engine.barrier.functions;

import space.engine.barrier.Barrier;
import space.engine.barrier.DelayTask;
import space.engine.baseobject.CanceledCheck;

//thenRunCancelable
@FunctionalInterface
public
interface CancelableRunnableWithDelay {
	
	Barrier run(CanceledCheck check) throws DelayTask;
	
	static CancelableRunnableWithDelay noop() {
		return check -> Barrier.done();
	}
}
