package space.engine.sync.taskImpl;

import org.jetbrains.annotations.NotNull;
import space.engine.sync.DelayTask;
import space.engine.sync.barrier.Barrier;
import space.engine.sync.lock.SyncLock;

/**
 * The most basic task with 2 abstract Methods:
 * <ul>
 * <li>submit1(Runnable): for submitting this Task to an executor</li>
 * <li>execute(): to do your work</li>
 * </ul>
 */
public abstract class RunnableTask extends AbstractTask implements Runnable {
	
	public RunnableTask(@NotNull SyncLock[] locks, @NotNull Barrier[] barriers) {
		super(locks, barriers);
	}
	
	/**
	 * REQUIRES calling {@link #init(Barrier[])} later to start execution
	 */
	protected RunnableTask(SyncLock[] locks) {
		super(locks);
	}
	
	//submit
	protected synchronized void submit() {
		submit1(this);
	}
	
	protected abstract void submit1(Runnable toRun);
	
	//execution
	public void run() {
		try {
			execute();
			executionFinished();
		} catch (DelayTask e) {
			e.barrier.addHook(() -> executionFinished(e.barrier));
		} catch (Throwable e) {
			try {
				executionFinished();
			} catch (Throwable e2) {
				RuntimeException run = new RuntimeException(e);
				run.addSuppressed(e2);
				throw run;
			}
			throw e;
		}
	}
	
	protected void executionFinished(Barrier awaitTask) {
		executionFinished();
	}
	
	protected abstract void execute() throws DelayTask;
}
