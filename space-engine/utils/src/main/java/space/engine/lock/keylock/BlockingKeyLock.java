package space.engine.lock.keylock;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

/**
 * A simple Lock which can be locked with a KEY instead of the current Thread
 */
public interface BlockingKeyLock<KEY> {
	
	//lock
	void lock(@NotNull KEY key);
	
	void lock(@NotNull KEY key, long time, TimeUnit unit);
	
	void lockInterruptibly(@NotNull KEY key) throws InterruptedException;
	
	void lockInterruptibly(@NotNull KEY key, long time, TimeUnit unit) throws InterruptedException;
	
	/**
	 * call is non-blocking
	 */
	boolean tryLock(@NotNull KEY key);
	
	//unlock
	void unlock(@NotNull KEY key);
	
	/**
	 * call is non-blocking
	 */
	boolean tryUnlock(@NotNull KEY key);
	
	//getter
	
	/**
	 * may be an {@link UnsupportedOperationException}
	 */
	boolean isLocked();
	
	/**
	 * may be an {@link UnsupportedOperationException}
	 */
	@Nullable KEY getHolder();
	
	default void execute(@NotNull KEY key, Runnable command) {
		lock(key);
		try {
			command.run();
		} finally {
			unlock(key);
		}
	}
	
	default void executeInterruptibly(@NotNull KEY key, Runnable command) {
		try {
			lockInterruptibly(key);
			try {
				command.run();
			} finally {
				unlock(key);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
