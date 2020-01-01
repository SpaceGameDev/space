package space.engine.barrier.lock;

import org.jetbrains.annotations.NotNull;
import space.engine.simpleQueue.ConcurrentLinkedSimpleQueue;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.util.function.BooleanSupplier;

import static space.engine.Side.pool;

public class SyncLockImpl implements SyncLock {
	
	public static int SYNCLOCK_CALLBACK_TRIES = 2;
	
	private static final VarHandle LOCKED;
	private static final VarHandle MODID;
	
	static {
		try {
			Lookup lookup = MethodHandles.lookup();
			LOCKED = lookup.findVarHandle(SyncLockImpl.class, "locked", boolean.class);
			MODID = lookup.findVarHandle(SyncLockImpl.class, "modId", int.class);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new ExceptionInInitializerError(e);
		}
	}
	
	@SuppressWarnings("unused")
	private volatile boolean locked;
	@SuppressWarnings("unused")
	private volatile int modId;
	private @NotNull ConcurrentLinkedSimpleQueue<BooleanSupplier> notifyUnlock = new ConcurrentLinkedSimpleQueue<>();
	
	@SuppressWarnings("ResultOfMethodCallIgnored")
	public SyncLockImpl() {
		//calls hashCode() to generate identity hashcode and disable 'Biased locking' on Hotspot.
		//https://srvaroa.github.io/jvm/java/openjdk/biased-locking/2017/01/30/hashCode.html
		hashCode();
	}
	
	@Override
	public boolean tryLockNow() {
		if (!LOCKED.compareAndSet(this, false, true))
			return false;
		
		//success, only one thread beyond this point
		MODID.getAndAdd(this, 1);
		return true;
	}
	
	@Override
	public Runnable unlock() {
		final int modId = (int) MODID.get(this);
		LOCKED.set(this, false);
		
		return () -> unlockFindNext(modId);
	}
	
	private void unlockFindNext(final int modId) {
		//locking
		if (!LOCKED.compareAndSet(this, false, true))
			return;
		//ensures no duplicate findNext attempts
		if (modId != ((int) MODID.get(this))) {
			LOCKED.set(this, false);
			return;
		}
		
		for (int i = 0; i < SYNCLOCK_CALLBACK_TRIES; i++) {
			BooleanSupplier callback = notifyUnlock.remove();
			if (callback == null) {
				//queue ran dry
				synchronized (this) {
					callback = notifyUnlock.remove();
					if (callback == null) {
						//queue actually dry -> no-one whats this lock
						LOCKED.set(this, false);
						return;
					}
				}
			}
			if (callback.getAsBoolean()) {
				//accepted lock
				return;
			}
		}
		
		//out of tries -> enqueue and try again later
		LOCKED.set(this, false);
		pool().execute(() -> unlockFindNext(modId));
	}
	
	@Override
	public void tryLockLater(BooleanSupplier callback) {
		if (!LOCKED.compareAndSet(this, false, true)) {
			synchronized (this) {
				if (!LOCKED.compareAndSet(this, false, true)) {
					//lock is actually locked -> enqueue
					notifyUnlock.add(callback);
					return;
				}
			}
		}
		
		//lock was unlocked and has been locked
		if (!callback.getAsBoolean()) {
			//not accepted lock
			unlock().run();
		}
	}
	
	/**
	 * <b>For testing only!</b>
	 * And only in highly controlled environments, as the returned value is immediately stale.
	 */
	public boolean isLocked() {
		return (boolean) LOCKED.get(this);
	}
}
