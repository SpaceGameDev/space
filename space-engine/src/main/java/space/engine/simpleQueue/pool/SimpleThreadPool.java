package space.engine.simpleQueue.pool;

import org.jetbrains.annotations.NotNull;
import space.engine.simpleQueue.SimpleQueue;

import java.util.Collection;
import java.util.concurrent.ThreadFactory;

public class SimpleThreadPool extends SimpleMessagePool<Runnable> implements Executor {
	
	public SimpleThreadPool(int threadCnt, @NotNull SimpleQueue<Runnable> queue) {
		super(threadCnt, queue);
	}
	
	public SimpleThreadPool(int threadCnt, @NotNull SimpleQueue<Runnable> queue, ThreadFactory threadFactory) {
		super(threadCnt, queue, threadFactory);
	}
	
	//handle
	@Override
	protected void handle(Runnable runnable) {
		runnable.run();
	}
	
	//executor
	@Override
	public void execute(@NotNull Runnable command) {
		add(command);
	}
	
	public void executeAll(@NotNull Collection<@NotNull Runnable> commands) {
		addAll(commands);
	}
	
	public void executeAll(@NotNull Runnable[] commands) {
		addAll(commands);
	}
}
