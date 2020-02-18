package space.engine.barrier.lock;

import org.junit.Test;
import space.engine.barrier.BarrierImpl;

import static org.junit.Assert.*;
import static space.engine.barrier.Barrier.nowLock;

public class SyncLockTest {
	
	private final SyncLockImpl lock1 = new SyncLockImpl();
	private final SyncLockImpl lock2 = new SyncLockImpl();
	
	@Test
	public void testDirectTryLockNow() {
		assertFalse(lock1.isLocked());
		assertTrue(lock1.tryLockNow());
		assertTrue(lock1.isLocked());
		assertFalse(lock1.tryLockNow());
		assertTrue(lock1.isLocked());
		
		//independence
		assertTrue(lock2.tryLockNow());
		assertFalse(lock2.tryLockNow());
		//no unlocking
	}
	
	@Test
	public void testDirectLockAndUnlock() {
		assertFalse(lock1.isLocked());
		assertTrue(lock1.tryLockNow());
		assertTrue(lock1.isLocked());
		lock1.unlock().run();
		assertFalse(lock1.isLocked());
	}
	
	@Test
	public void testDirectTryLockLaterTrue() {
		assertTrue(lock1.tryLockNow());
		assertTrue(lock1.isLocked());
		
		lock1.tryLockLater(() -> true);
		lock1.unlock().run();
		assertTrue(lock1.isLocked());
		lock1.unlock().run();
		assertFalse(lock1.isLocked());
	}
	
	@Test
	public void testDirectTryLockLaterFalse() {
		assertTrue(lock1.tryLockNow());
		assertTrue(lock1.isLocked());
		
		lock1.tryLockLater(() -> false);
		lock1.unlock().run();
		assertFalse(lock1.isLocked());
	}
	
	@Test
	public void testAquireAndUnlock() {
		SyncLock[] locks = {lock1, lock2};
		assertFalse(lock1.isLocked());
		assertFalse(lock2.isLocked());
		
		BarrierImpl locked1 = new BarrierImpl();
		SyncLock.acquireLocks(locks, locked1::triggerNow);
		locked1.awaitUninterrupted();
		
		assertTrue(lock1.isLocked());
		assertTrue(lock2.isLocked());
		
		BarrierImpl locked2 = new BarrierImpl();
		SyncLock.acquireLocks(locks, locked2::triggerNow);
		assertFalse(locked2.isDone());
		
		SyncLock.unlockLocks(locks);
		assertTrue(locked2.isDone());
		assertTrue(lock1.isLocked());
		assertTrue(lock2.isLocked());
		
		SyncLock.unlockLocks(locks);
		assertFalse(lock1.isLocked());
		assertFalse(lock2.isLocked());
	}
	
	@Test
	public void testThenLock() {
		BarrierImpl locked = new BarrierImpl();
		BarrierImpl unlock = new BarrierImpl();
		BarrierImpl unlocked = new BarrierImpl();
		
		assertFalse(lock1.isLocked());
		assertFalse(lock2.isLocked());
		
		nowLock(new SyncLock[] {lock1, lock2}, () -> {
			locked.triggerNow();
			return unlock;
		}).addHook(unlocked::triggerNow);
		locked.awaitUninterrupted();
		assertTrue(lock1.isLocked());
		assertTrue(lock2.isLocked());
		
		unlock.triggerNow();
		unlocked.awaitUninterrupted();
		assertFalse(lock1.isLocked());
		assertFalse(lock2.isLocked());
	}
}
