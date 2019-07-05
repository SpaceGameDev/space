package space.engine.sync;

import org.jetbrains.annotations.NotNull;
import space.engine.sync.barrier.Barrier;
import space.engine.sync.lock.SyncLock;

import static space.engine.sync.barrier.Barrier.EMPTY_BARRIER_ARRAY;
import static space.engine.sync.lock.SyncLock.EMPTY_SYNCLOCK_ARRAY;

/**
 * A {@link TaskCreator} is something which is created to be executed by some thread in a protected environment,
 * signaling back it's execution, completion and error states. It also allows for Hooks to be added and to be awaited on.
 */
public interface TaskCreator<TASK extends Barrier> {
	
	/**
	 * Creates a new Task based upn this template.
	 * The Task may start execution after declared {@link Barrier Barriers} are triggered and while holding declared {@link SyncLock SyncLocks}.
	 *
	 * @return the created Task of generic type TASK
	 */
	default @NotNull TASK submit() {
		return submit(EMPTY_SYNCLOCK_ARRAY, EMPTY_BARRIER_ARRAY);
	}
	
	/**
	 * Creates a new Task based upn this template.
	 * The Task may start execution after declared {@link Barrier Barriers} are triggered and while holding declared {@link SyncLock SyncLocks}.
	 *
	 * @return the created Task of generic type TASK
	 */
	default @NotNull TASK submit(@NotNull Barrier... barriers) {
		return submit(EMPTY_SYNCLOCK_ARRAY, barriers);
	}
	
	/**
	 * Creates a new Task based upn this template.
	 * The Task may start execution after declared {@link Barrier Barriers} are triggered and while holding declared {@link SyncLock SyncLocks}.
	 *
	 * @return the created Task of generic type TASK
	 */
	@NotNull TASK submit(@NotNull SyncLock[] locks, @NotNull Barrier... barriers);
}
