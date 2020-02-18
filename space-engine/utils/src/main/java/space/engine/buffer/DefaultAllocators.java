package space.engine.buffer;

import space.engine.buffer.alloc.AllocatorStackImpl;
import space.engine.unsafe.UnsafeInstance;
import sun.misc.Unsafe;

import static space.engine.Empties.EMPTY_OBJECT_ARRAY;

/**
 * use {@link Allocator#heap()} etc. to access these fields
 */
class DefaultAllocators {
	
	private static Unsafe UNSAFE = UnsafeInstance.getUnsafe();
	
	static final Allocator ALLOCATOR_HEAP = new Allocator() {
		@Override
		public long malloc(long sizeOf) {
			return UNSAFE.allocateMemory(sizeOf);
		}
		
		@Override
		public long calloc(long sizeOf) {
			//calloc sadly not supported by Unsafe
			long address = UNSAFE.allocateMemory(sizeOf);
			UNSAFE.setMemory(address, sizeOf, (byte) 0);
			return address;
		}
		
		@Override
		public void free(long address) {
			UNSAFE.freeMemory(address);
		}
	};
	
	static final ThreadLocal<AllocatorStack> ALLOCATOR_STACK = ThreadLocal.withInitial(() -> new AllocatorStackImpl(ALLOCATOR_HEAP, 64L * 1024, EMPTY_OBJECT_ARRAY));
	
	static final Allocator ALLOCATOR_NOOP = new Allocator() {
		@Override
		public long malloc(long sizeOf) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public long calloc(long sizeOf) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public void free(long address) {
		
		}
	};
}
