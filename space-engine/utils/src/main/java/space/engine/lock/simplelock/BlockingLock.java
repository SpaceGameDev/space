package space.engine.lock.simplelock;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

/**
 * A Lock which is locked by your Thread
 */
public interface BlockingLock {
	
	//lock
	void lock();
	
	void lock(long time, @NotNull TimeUnit unit);
	
	void lockInterruptibly() throws InterruptedException;
	
	void lockInterruptibly(long time, @NotNull TimeUnit unit) throws InterruptedException;
	
	boolean tryLock();
	
	//unlock
	void unlock();
	
	/**
	 * may be an {@link UnsupportedOperationException}
	 */
	boolean tryUnlock();
	
	//getter
	
	/**
	 * may be an {@link UnsupportedOperationException}
	 */
	boolean isLocked();
	
	default void execute(@NotNull Runnable command) {
		lock();
		try {
			command.run();
		} finally {
			unlock();
		}
	}
	
	default void executeInterruptibly(@NotNull Runnable command) {
		try {
			lockInterruptibly();
			try {
				command.run();
			} finally {
				unlock();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
