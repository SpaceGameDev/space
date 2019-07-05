package space.engine.sync.taskImpl;

import org.jetbrains.annotations.NotNull;
import space.engine.sync.barrier.Barrier;
import space.engine.sync.barrier.BarrierImpl;
import space.engine.sync.lock.SyncLock;

public abstract class MultiTask extends AbstractTask {
	
	protected BarrierImpl submitBarrier;
	protected Barrier endBarrier;
	
	public MultiTask(@NotNull SyncLock[] locks, @NotNull Barrier[] barriers) {
		super(locks, barriers);
	}
	
	/**
	 * REQUIRES calling {@link #init(Barrier[])} later to start execution
	 */
	protected MultiTask(SyncLock[] locks) {
		super(locks);
	}
	
	@Override
	protected void init(@NotNull Barrier[] barriers) {
		endBarrier = setup(submitBarrier = new BarrierImpl());
		endBarrier.addHook(this::executionFinished);
		super.init(barriers);
	}
	
	protected abstract Barrier setup(Barrier start);
	
	@Override
	protected void submit() {
		submitBarrier.triggerNow();
	}
}
