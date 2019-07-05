package space.engine.sync.taskImpl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.engine.sync.DelayTask;
import space.engine.sync.barrier.Barrier;
import space.engine.sync.future.Future;
import space.engine.sync.future.FutureNotFinishedException;
import space.engine.sync.lock.SyncLock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class FutureTask<R> extends RunnableTask implements Future<R> {
	
	protected R ret;
	
	public FutureTask(@NotNull SyncLock[] locks, @NotNull Barrier[] barriers) {
		super(locks, barriers);
	}
	
	/**
	 * REQUIRES calling {@link #init(Barrier[])} later to start execution
	 */
	protected FutureTask(SyncLock[] locks) {
		super(locks);
	}
	
	//execute
	@Override
	protected void execute() throws DelayTask {
		ret = execute0();
	}
	
	protected abstract R execute0() throws DelayTask;
	
	@Override
	protected void executionFinished(Barrier awaitTask) {
		//noinspection unchecked
		ret = ((Future<R>) awaitTask).assertGet();
		super.executionFinished(awaitTask);
	}
	
	//get
	@Override
	public synchronized R awaitGet() throws InterruptedException {
		await();
		return ret;
	}
	
	@Override
	public R awaitGet(long time, TimeUnit unit) throws InterruptedException, TimeoutException {
		await(time, unit);
		return ret;
	}
	
	@Nullable
	@Override
	public synchronized R assertGet() throws FutureNotFinishedException {
		if (state != TaskState.FINISHED)
			throw new FutureNotFinishedException(this);
		return ret;
	}
}
