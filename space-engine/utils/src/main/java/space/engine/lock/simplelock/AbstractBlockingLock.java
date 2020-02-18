package space.engine.lock.simplelock;

import org.jetbrains.annotations.NotNull;
import space.engine.lock.keylock.BlockingKeyLockImpl;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * a simple wrapper class from {@link BlockingLock} to {@link BlockingKeyLockImpl}, which calls the supplier for the key
 */
public class AbstractBlockingLock<KEY> implements BlockingLock {
	
	public Supplier<KEY> key;
	public BlockingKeyLockImpl<? super KEY> lock;
	
	public AbstractBlockingLock(Supplier<KEY> key) {
		this(key, new BlockingKeyLockImpl<>());
	}
	
	public AbstractBlockingLock(Supplier<KEY> key, BlockingKeyLockImpl<? super KEY> lock) {
		this.key = key;
		this.lock = lock;
	}
	
	@Override
	public void lock() {
		lock.lock(key.get());
	}
	
	@Override
	public void lock(long time, @NotNull TimeUnit unit) {
		lock.lock(key.get(), time, unit);
	}
	
	@Override
	public void lockInterruptibly() throws InterruptedException {
		lock.lockInterruptibly(key.get());
	}
	
	@Override
	public void lockInterruptibly(long time, @NotNull TimeUnit unit) throws InterruptedException {
		lock.lockInterruptibly(key.get(), time, unit);
	}
	
	@Override
	public boolean tryLock() {
		return lock.tryLock(key.get());
	}
	
	@Override
	public void unlock() {
		lock.unlock(key.get());
	}
	
	@Override
	public boolean tryUnlock() {
		return lock.tryUnlock(key.get());
	}
	
	@Override
	public boolean isLocked() {
		return lock.isLocked();
	}
}
