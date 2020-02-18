package space.engine.lock.simplelock;

import space.engine.lock.keylock.BlockingKeyLockImpl;

/**
 * a simple wrapper class from {@link BlockingLock} to {@link BlockingKeyLockImpl}, which gets the Key by the Constructor argument
 */
public class BlockingLockImpl<KEY> extends AbstractBlockingLock<KEY> {
	
	public BlockingLockImpl(KEY key) {
		super(() -> key);
	}
	
	public BlockingLockImpl(KEY key, BlockingKeyLockImpl<? super KEY> lock) {
		super(() -> key, lock);
	}
}
