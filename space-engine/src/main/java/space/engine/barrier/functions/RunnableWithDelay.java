package space.engine.barrier.functions;

import space.engine.barrier.DelayTask;

@FunctionalInterface
public interface RunnableWithDelay {
	
	void run() throws DelayTask;
}
