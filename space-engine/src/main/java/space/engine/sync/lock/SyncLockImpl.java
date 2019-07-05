package space.engine.sync.lock;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import static space.engine.Side.pool;

public class SyncLockImpl implements SyncLock {
	
	public static int SYNCLOCK_CALLBACK_TRIES = 2;
	
	private boolean locked;
	private int modId;
	private @NotNull List<BooleanSupplier> notifyUnlock = new ArrayList<>();
	
	@SuppressWarnings("ResultOfMethodCallIgnored")
	public SyncLockImpl() {
		//calls hashCode() to generate identity hashcode and disable 'Biased locking' on Hotspot.
		//https://srvaroa.github.io/jvm/java/openjdk/biased-locking/2017/01/30/hashCode.html
		hashCode();
	}
	
	@Override
	public synchronized boolean tryLockNow() {
		if (locked)
			return false;
		
		//success
		locked = true;
		modId++;
		return true;
	}
	
	@Override
	public Runnable unlock() {
		final int modId;
		synchronized (this) {
			locked = false;
			modId = this.modId;
		}
		
		return () -> unlockFindNext(modId);
	}
	
	private void unlockFindNext(final int modId) {
		BooleanSupplier callback;
		
		synchronized (this) {
			if (locked || modId != this.modId) {
				//someone else locked this Lock -> he will search for the next task
				return;
			}
			
			//lock and get first callback
			if (notifyUnlock.isEmpty()) {
				//no callback found to accept lock
				return;
			}
			callback = notifyUnlock.remove(0);
			locked = true;
		}
		
		for (int i = 0; true; i++) {
			if (callback.getAsBoolean()) {
				//accepted lock
				return;
			}
			
			synchronized (this) {
				if (i >= SYNCLOCK_CALLBACK_TRIES) {
					//out of tries -> enqueue and try again later
					locked = false;
					pool().execute(() -> unlockFindNext(modId));
					return;
				}
				
				//get next callback
				if (notifyUnlock.isEmpty()) {
					//no callback found to accept lock
					locked = false;
					return;
				}
				callback = notifyUnlock.remove(0);
			}
			
			//performance advantage is negligible; only at extreme transaction counts (> 100,000) between the same two objects it is still very unnoticeable
//			Thread.yield();
		}
	}
	
	@Override
	public void tryLockLater(BooleanSupplier callback) {
		synchronized (this) {
			if (locked) {
				//locked -> add callback
				notifyUnlock.add(callback);
				return;
			}
			
			//not locked -> lock and call callback
			locked = true;
		}
		
		if (!callback.getAsBoolean()) {
			//not accepted lock
			unlock().run();
		}
	}
}
