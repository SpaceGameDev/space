package space.engine.lock.keylock;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

public class BlockingKeyLockImpl<KEY> implements BlockingKeyLock<KEY> {
	
	@Nullable
	public KEY currentKey;
	public int stackLevel;
	
	//lock
	@Override
	public synchronized void lock(@NotNull KEY key) {
		while (!tryLock(key))
			try {
				wait();
			} catch (InterruptedException ignore) {
				
			}
	}
	
	@Override
	public synchronized void lock(@NotNull KEY key, long time, @NotNull TimeUnit unit) {
		while (!tryLock(key))
			try {
				wait(unit.toMillis(time));
			} catch (InterruptedException ignore) {
				
			}
	}
	
	@Override
	public synchronized void lockInterruptibly(@NotNull KEY key) throws InterruptedException {
		while (!tryLock(key))
			wait();
	}
	
	@Override
	public synchronized void lockInterruptibly(@NotNull KEY key, long time, TimeUnit unit) throws InterruptedException {
		while (!tryLock(key))
			wait(unit.toMillis(time));
	}
	
	@Override
	public synchronized boolean tryLock(@NotNull KEY key) {
		if (currentKey == null) {
			currentKey = key;
			stackLevel = 1;
			return true;
		}
		if (currentKey == key) {
			stackLevel++;
			return true;
		}
		
		return false;
	}
	
	//unlock
	@Override
	public synchronized boolean tryUnlock(@NotNull KEY key) {
		if (currentKey == null || currentKey != key)
			return false;
		unlock();
		return true;
	}
	
	@Override
	public synchronized void unlock(@NotNull KEY key) {
		if (currentKey == null)
			throw new IllegalStateException("Not locked!");
		if (currentKey != key)
			throw new IllegalStateException("Locked by other key!");
		unlock();
	}
	
	/**
	 * will force unlock ignoring the key
	 */
	protected synchronized void unlock() {
		stackLevel--;
		if (stackLevel == 0) {
			currentKey = null;
			notify();
		}
	}
	
	//getter
	@Override
	public synchronized boolean isLocked() {
		return currentKey != null;
	}
	
	@Nullable
	@Override
	public synchronized KEY getHolder() {
		return currentKey;
	}
}
