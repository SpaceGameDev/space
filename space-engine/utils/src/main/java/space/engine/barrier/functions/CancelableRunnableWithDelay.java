package space.engine.barrier.functions;

import space.engine.barrier.DelayTask;
import space.engine.baseobject.CanceledCheck;

@FunctionalInterface
public interface CancelableRunnableWithDelay {
	
	void run(CanceledCheck check) throws DelayTask;
}
