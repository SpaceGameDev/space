package space.engine.buffer.alloc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.engine.buffer.AbstractBuffer.Storage;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack;
import space.engine.buffer.alloc.AllocatorStackImpl.AllocatorFrame;
import space.engine.freeable.Freeable;
import space.engine.freeable.Freeable.CleanerWrapper;
import space.engine.freeable.stack.AbstractFreeableStack;
import space.engine.unsafe.UnsafeInstance;
import sun.misc.Unsafe;

public class AllocatorStackImpl extends AbstractFreeableStack<AllocatorFrame> implements AllocatorStack, CleanerWrapper {
	
	private static Unsafe UNSAFE = UnsafeInstance.getUnsafe();
	
	protected final @NotNull Storage storage;
	protected final long capacity;
	
	public AllocatorStackImpl(@NotNull Allocator allocator, long capacity, @NotNull Object[] parents) {
		this.capacity = capacity;
		storage = new Storage(this, allocator, allocator.malloc(capacity), parents);
	}
	
	@Override
	public @NotNull Freeable getStorage() {
		return storage;
	}
	
	@Override
	protected @NotNull AllocatorStackImpl.AllocatorFrame createFrame(@Nullable AllocatorStackImpl.AllocatorFrame prev) {
		return new AllocatorFrame(prev);
	}
	
	public class AllocatorFrame extends AbstractFreeableStack<AllocatorFrame>.Frame implements AllocatorStack.AllocatorFrame {
		
		protected long pointerStack;
		
		public AllocatorFrame(@Nullable AllocatorStackImpl.AllocatorFrame prev) {
			super(prev);
			this.pointerStack = prev != null ? prev.pointerStack : 0;
		}
		
		//allocator
		@Override
		public long malloc(long sizeOf) {
			assertTopFrame();
			long oldPointerStack = pointerStack;
			//align to 8
			pointerStack += (sizeOf + 0x7) & ~0x7;
			if (pointerStack > capacity)
				throw new OutOfMemoryError("Stack overflow!");
			return storage.getAddress() + oldPointerStack;
		}
		
		@Override
		public long calloc(long sizeOf) {
			long address = malloc(sizeOf);
			UNSAFE.setMemory(address, sizeOf, (byte) 0);
			return address;
		}
		
		@Override
		public void free(long address) {
		
		}
	}
}
