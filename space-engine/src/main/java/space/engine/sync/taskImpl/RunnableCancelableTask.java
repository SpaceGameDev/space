package space.engine.sync.taskImpl;

import org.jetbrains.annotations.NotNull;
import space.engine.baseobject.CanceledCheck;
import space.engine.sync.barrier.Barrier;
import space.engine.sync.barrier.CancelableBarrier;
import space.engine.sync.lock.SyncLock;

/**
 * Extends {@link RunnableTask} and adds cancel functionality.
 * This class <b>WILL NOT CANCEL AUTOMATICALLY</b> and only provides a framework to check whether it has been canceled with {@link #isCanceled()}.
 * What exactly happens when it is canceled has to be implemented by the user of this class.
 */
public abstract class RunnableCancelableTask extends RunnableTask implements CancelableBarrier, CanceledCheck {
	
	public RunnableCancelableTask(@NotNull SyncLock[] locks, @NotNull Barrier[] barriers) {
		super(locks, barriers);
	}
	
	/**
	 * REQUIRES calling {@link #init(Barrier[])} later to start execution
	 */
	public RunnableCancelableTask(SyncLock[] locks) {
		super(locks);
	}
	
	//canceling
	private volatile boolean isCanceled;
	
	@Override
	public void cancel() {
		isCanceled = true;
	}
	
	@Override
	public boolean isCanceled() {
		return isCanceled;
	}
}
