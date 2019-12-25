package space.engine.barrier.lock;

import java.util.function.BooleanSupplier;

/**
 * A Lock made for fast non-blocking operation.
 * See the static Methods {@link #acquireLocks(SyncLock[], Runnable)} and {@link #unlockLocks(SyncLock[])} for usage. Do <b>NOT</b> use the interface methods directly!
 */
public interface SyncLock {
	
	SyncLock[] EMPTY_SYNCLOCK_ARRAY = new SyncLock[0];
	
	/**
	 * Tries to lock this {@link SyncLock} now.
	 *
	 * @return true if successful
	 */
	boolean tryLockNow();
	
	/**
	 * Gives this {@link SyncLock} a callback to call as soon as this {@link SyncLock} is ready for locking again.
	 * When the Callback is called it has already acquired the Lock on this {@link SyncLock}. Trying to acquire it again may cause a deadlock.
	 * If the {@link SyncLock} is not locked while this Method is called, it should call the callback immediately while still maintaining said condition.
	 *
	 * @param callback the callback to call
	 */
	void tryLockLater(BooleanSupplier callback);
	
	/**
	 * Unlocks this {@link SyncLock} immediately. Returns a Runnable which will be called after all hold {@link SyncLock} are unlocked.
	 * The Runnable should be used to find the next candidate to lock this {@link SyncLock}.
	 * The returned Runnable may do nothing if it sees fit (Example: someone has already locked and maybe even unlocked this {@link SyncLock} again during that timeframe).
	 *
	 * @return the Runnable described above
	 */
	Runnable unlock();
	
	//static
	
	/**
	 * This Method will try to acquire all {@link SyncLock SyncLocks} given to it and when it has run the callback.
	 * Unlocking them afterwards with {@link #unlockLocks(SyncLock[])} has to be done manually.
	 *
	 * @param locks    the Locks to aquire before calling the callback
	 * @param callback the callback to be called when all locks are aquired
	 */
	static void acquireLocks(SyncLock[] locks, Runnable callback) {
		acquireLocks(locks, -1, callback);
	}
	
	private static boolean acquireLocks(SyncLock[] locks, int exceptLock, Runnable callback) {
		if (locks.length == 0) {
			callback.run();
			return true;
		}
		
		int i;
		boolean success = true;
		for (i = 0; i < locks.length; i++) {
			if (i != exceptLock && !locks[i].tryLockNow()) {
				success = false;
				break;
			}
		}
		
		if (success) {
			callback.run();
			return true;
		}
		
		//failure
		final int fi = i;
		unlockLocks(locks, fi, exceptLock);
		locks[fi].tryLockLater(() -> acquireLocks(locks, fi, callback));
		return false;
	}
	
	/**
	 * This Method will unlock all given {@link SyncLock SyncLocks}.
	 * <b>Failing to own any of the given {@link SyncLock} will cause catastrophic failures.</b>
	 *
	 * <b>DON'T synchronize on anything when calling this method!</b>
	 */
	static void unlockLocks(SyncLock[] locks) {
		unlockLocks(locks, locks.length, -1);
	}
	
	/**
	 * <b>DON'T synchronize on anything when calling this method!</b>
	 */
	private static void unlockLocks(SyncLock[] locks, int maxExclusive, int exceptLock) {
		if (maxExclusive == 0)
			return;
		
		Runnable[] notifyCallback = new Runnable[maxExclusive];
		for (int i = maxExclusive - 1; i >= 0; i--)
			if (i != exceptLock)
				notifyCallback[i] = locks[i].unlock();
		for (int i = maxExclusive - 1; i >= 0; i--)
			if (notifyCallback[i] != null)
				notifyCallback[i].run();
	}
}
