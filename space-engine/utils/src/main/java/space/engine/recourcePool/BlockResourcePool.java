package space.engine.recourcePool;

import space.engine.simpleQueue.ArraySimpleQueue;
import space.engine.simpleQueue.LinkedSimpleQueue;
import space.engine.simpleQueue.SimpleQueue;

import java.util.function.IntFunction;
import java.util.stream.Stream;

/**
 * A {@link BlockResourcePool} allows you to {@link #allocate()} and {@link #release(Object)} Resources of type E.
 */
public abstract class BlockResourcePool<E> implements ResourcePool<E> {
	
	public static <E> ResourcePool<E> withLambda(int blockSize, IntFunction<E[]> generator) {
		return new BlockResourcePool<>(blockSize) {
			@Override
			protected E[] allocateNewBlock(int count) {
				return generator.apply(count);
			}
		};
	}
	
	public final int blockSize;
	final LinkedSimpleQueue<E[]> global;
	final ThreadLocal<ArraySimpleQueue<E>> local;
	
	public BlockResourcePool(int blockSize) {
		this.blockSize = blockSize;
		this.global = new LinkedSimpleQueue<>();
		this.local = ThreadLocal.withInitial(() -> new ArraySimpleQueue<>(2 * blockSize));
	}
	
	/**
	 * Allocates a new block of objects. Calls to the method must be synchronized on this.
	 *
	 * @param count the count of objects to allocate. Usually a multiple of {@link #blockSize}
	 * @return an E[] with length == #count
	 */
	protected abstract E[] allocateNewBlock(int count);
	
	@Override
	public E allocate() {
		SimpleQueue<E> local = this.local.get();
		
		while (true) {
			//from local
			E e = local.remove();
			if (e != null)
				return e;
			
			//refill from global
			E[] refill;
			synchronized (this) {
				refill = global.remove();
				if (refill == null)
					refill = allocateNewBlock(blockSize);
			}
			local.addArray(refill);
		}
	}
	
	@Override
	public E[] allocate(E[] es) {
		SimpleQueue<E> local = this.local.get();
		
		int index = 0;
		if (es.length > blockSize) {
			//allocate most from global directly
			int fullBlocks = es.length / blockSize;
			index = fullBlocks * blockSize;
			
			E[] refill, allocated = null;
			int missing;
			synchronized (this) {
				//noinspection SuspiciousToArrayCall,unchecked
				refill = (E[]) global.removeCollection(fullBlocks).stream().flatMap(Stream::of).toArray(Object[]::new);
				missing = refill.length - index;
				if (missing > 0)
					allocated = allocateNewBlock(missing);
			}
			System.arraycopy(refill, 0, es, 0, refill.length);
			if (missing > 0)
				System.arraycopy(allocated, 0, es, refill.length, missing);
			
			if (index == es.length)
				return es;
			//allocate remaining
		}
		
		while (true) {
			//from local
			index += local.removeArray(es, index, es.length - index);
			if (index == es.length)
				return es;
			
			//refill from global
			E[] refill;
			synchronized (this) {
				refill = global.remove();
				if (refill == null)
					refill = allocateNewBlock(blockSize);
			}
			local.addArray(refill);
		}
	}
	
	@Override
	public void release(E e) {
		SimpleQueue<E> local = this.local.get();
		
		while (true) {
			//just add
			if (local.add(e))
				return;
			
			//overflow
			//noinspection unchecked
			E[] array = (E[]) new Object[blockSize];
			local.removeArray(array, 0, array.length);
			synchronized (this) {
				global.add(array);
			}
		}
	}
	
	@Override
	public void release(E[] es) {
		SimpleQueue<E> local = this.local.get();
		int index = 0;
		while (true) {
			//just add
			index += local.addArray(es, index, es.length - index);
			if (index == es.length)
				return;
			
			//overflow
			//noinspection unchecked
			E[] array = (E[]) new Object[blockSize];
			local.removeArray(array, 0, array.length);
			synchronized (this) {
				global.add(array);
			}
		}
	}
}
