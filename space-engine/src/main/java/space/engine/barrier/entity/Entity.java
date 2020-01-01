package space.engine.barrier.entity;

import space.engine.barrier.lock.SyncLock;

public interface Entity {
	
	SyncLock syncLock();
}
