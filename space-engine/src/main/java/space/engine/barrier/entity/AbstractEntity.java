package space.engine.barrier.entity;

import space.engine.barrier.lock.SyncLock;
import space.engine.barrier.lock.SyncLockImpl;

public abstract class AbstractEntity implements Entity {
	
	protected SyncLockImpl syncLock = new SyncLockImpl();
	
	@Override
	public SyncLock syncLock() {
		return syncLock;
	}
}
