package space.engine.barrier.entity;

import space.engine.barrier.Barrier;
import space.engine.barrier.Delegate;
import space.engine.barrier.functions.StarterWithParameter;
import space.engine.barrier.lock.SyncLock;

import java.util.function.BooleanSupplier;

public interface EntityRef<E extends Entity> extends SyncLock {
	
	EntityAccessKey<E> getAccessKey();
	
	//static
	static <E extends Entity> EntityRef<E> createRef(E entity) {
		return new BasicEntityRef<>(entity);
	}
	
	class BasicEntityRef<T extends Entity> implements EntityRef<T> {
		
		public final T entity;
		
		public BasicEntityRef(T entity) {
			this.entity = entity;
		}
		
		//synclock
		@Override
		public boolean tryLockNow() {
			return entity.syncLock().tryLockNow();
		}
		
		@Override
		public void tryLockLater(BooleanSupplier callback) {
			entity.syncLock().tryLockLater(callback);
		}
		
		@Override
		public Runnable unlock() {
			return entity.syncLock().unlock();
		}
		
		//accesskey
		@Override
		public EntityAccessKey<T> getAccessKey() {
			return new EntityAccessKey<>() {
				
				@Override
				public <B extends Barrier, C extends B> B startOn0(StarterWithParameter<? extends B, T> runnable, Delegate<B, C> delegate) {
					return runnable.startInlineException(entity);
				}
			};
		}
	}
}
