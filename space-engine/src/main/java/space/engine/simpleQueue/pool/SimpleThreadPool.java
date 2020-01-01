package space.engine.simpleQueue.pool;

import org.jetbrains.annotations.NotNull;
import space.engine.simpleQueue.SimpleQueue;

import java.util.Collection;
import java.util.concurrent.ThreadFactory;

public class SimpleThreadPool extends SimpleMessagePool<Runnable> implements Executor {
	
	public SimpleThreadPool(int threadCnt) {
		super(threadCnt);
	}
	
	public SimpleThreadPool(int threadCnt, ThreadFactory threadFactory) {
		super(threadCnt, threadFactory);
	}
	
	public SimpleThreadPool(int threadCnt, ThreadFactory threadFactory, @NotNull SimpleQueue<Runnable> queue, int pauseCountdown) {
		super(threadCnt, threadFactory, queue, pauseCountdown);
	}
	
	@SuppressWarnings("unused")
	protected SimpleThreadPool(int threadCnt, ThreadFactory threadFactory, @NotNull SimpleQueue<Runnable> queue, int pauseCountdown, boolean callinit) {
		super(threadCnt, threadFactory, queue, pauseCountdown, callinit);
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
