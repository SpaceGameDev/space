package space.engine.buffer.array;

import org.jetbrains.annotations.NotNull;
import space.engine.baseobject.exceptions.FreedException;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.Buffer;
import space.engine.buffer.NioBufferWrapper;
import space.engine.primitive.Primitive;

import java.util.stream.LongStream;

import static space.engine.Device.IS_64_BIT;
import static space.engine.Empties.EMPTY_OBJECT_ARRAY;
import static space.engine.primitive.Primitives.POINTER;
import static sun.misc.Unsafe.*;

public class ArrayBufferPointer extends AbstractArrayBuffer<ArrayBufferPointer> {
	
	public static final Primitive<?> TYPE = POINTER;
	
	//alloc
	
	/**
	 * Allocates a new {@link ArrayBufferPointer} and fills it with the contents of the array. If the {@link ArrayBufferPointer} is freed, it will free the memory.
	 */
	public static ArrayBufferPointer alloc(AllocatorFrame allocator, Buffer[] array) {
		return alloc(allocator, array, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Allocates a new {@link ArrayBufferPointer} and fills it with the contents of the array. If the {@link ArrayBufferPointer} is freed, it will free the memory.
	 */
	public static ArrayBufferPointer alloc(Allocator allocator, Buffer[] array, @NotNull Object[] parents) {
		ArrayBufferPointer buffer = new ArrayBufferPointer(allocator, allocator.malloc(array.length * TYPE.bytes), array.length, parents);
		buffer.copyFrom(array);
		return buffer;
	}
	
	/**
	 * Allocates a new {@link ArrayBufferPointer} and fills it with the contents of the array. If the {@link ArrayBufferPointer} is freed, it will free the memory.
	 */
	public static ArrayBufferPointer alloc(AllocatorFrame allocator, java.nio.Buffer[] array) {
		return alloc(allocator, array, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Allocates a new {@link ArrayBufferPointer} and fills it with the contents of the array. If the {@link ArrayBufferPointer} is freed, it will free the memory.
	 */
	public static ArrayBufferPointer alloc(Allocator allocator, java.nio.Buffer[] array, @NotNull Object[] parents) {
		ArrayBufferPointer buffer = new ArrayBufferPointer(allocator, allocator.malloc(array.length * TYPE.bytes), array.length, parents);
		buffer.copyFrom(array);
		return buffer;
	}
	
	/**
	 * Allocates a new {@link ArrayBufferPointer} and fills it with the contents of the array. If the {@link ArrayBufferPointer} is freed, it will free the memory.
	 */
	public static ArrayBufferPointer alloc(AllocatorFrame allocator, long[] array) {
		return alloc(allocator, array, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Allocates a new {@link ArrayBufferPointer} and fills it with the contents of the array. If the {@link ArrayBufferPointer} is freed, it will free the memory.
	 */
	public static ArrayBufferPointer alloc(Allocator allocator, long[] array, @NotNull Object[] parents) {
		ArrayBufferPointer buffer = new ArrayBufferPointer(allocator, allocator.malloc(array.length * TYPE.bytes), array.length, parents);
		buffer.copyFrom(array);
		return buffer;
	}
	
	/**
	 * Allocates a new {@link ArrayBufferPointer} of length. The Contents are undefined. If the {@link ArrayBufferPointer} is freed, it will free the memory.
	 */
	public static ArrayBufferPointer malloc(AllocatorFrame allocator, long length) {
		return malloc(allocator, length, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Allocates a new {@link ArrayBufferPointer} of length. The Contents are undefined. If the {@link ArrayBufferPointer} is freed, it will free the memory.
	 */
	public static ArrayBufferPointer malloc(Allocator allocator, long length, @NotNull Object[] parents) {
		return new ArrayBufferPointer(allocator, allocator.malloc(length * TYPE.bytes), length, parents);
	}
	
	/**
	 * Allocates a new {@link ArrayBufferPointer} of length. The Contents are initialized to 0. If the {@link ArrayBufferPointer} is freed, it will free the memory.
	 */
	public static ArrayBufferPointer calloc(AllocatorFrame allocator, long length) {
		return calloc(allocator, length, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Allocates a new {@link ArrayBufferPointer} of length. The Contents are initialized to 0. If the {@link ArrayBufferPointer} is freed, it will free the memory.
	 */
	public static ArrayBufferPointer calloc(Allocator allocator, long length, @NotNull Object[] parents) {
		return new ArrayBufferPointer(allocator, allocator.calloc(length * TYPE.bytes), length, parents);
	}
	
	//create
	
	/**
	 * Creates a new {@link ArrayBufferPointer} from the given address and length. If the {@link ArrayBufferPointer} is freed, it <b>WILL</b> free the memory.
	 */
	public static ArrayBufferPointer create(Allocator allocator, long address, long length, @NotNull Object[] parents) {
		return new ArrayBufferPointer(allocator, address, length, parents);
	}
	
	/**
	 * Creates a new {@link ArrayBufferPointer} from the given address and length. It will <b>NEVER</b> free the memory but will still throw {@link FreedException} if it is freed.
	 */
	public static ArrayBufferPointer wrap(long address, long length) {
		return wrap(address, length, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Creates a new {@link ArrayBufferPointer} from the given address and length. It will <b>NEVER</b> free the memory but will still throw {@link FreedException} if it is freed.
	 */
	public static ArrayBufferPointer wrap(long address, long length, @NotNull Object[] parents) {
		return create(Allocator.noop(), address, length, parents);
	}
	
	//object
	protected ArrayBufferPointer(Allocator allocator, long address, long length, @NotNull Object[] parents) {
		super(allocator, address, length, parents);
	}
	
	@Override
	public Primitive<?> type() {
		return TYPE;
	}
	
	@Override
	public java.nio.Buffer nioBuffer() {
		return NioBufferWrapper.wrapByte(this, sizeOf());
	}
	
	//single
	public long getPointer(long index) {
		Buffer.checkIndex(index, this.length);
		return UNSAFE.getAddress(address() + type().multiply(index));
	}
	
	public void putPointer(long index, Buffer b) {
		putPointer(index, b.address());
	}
	
	public void putPointer(long index, java.nio.Buffer b) {
		putPointer(index, NioBufferWrapper.getAddress(b));
	}
	
	public void putPointer(long index, long b) {
		Buffer.checkIndex(index, this.length);
		UNSAFE.putAddress(address() + type().multiply(index), b);
	}
	
	//array
	public void copyInto(long[] dest) {
		copyInto(0, dest, 0, dest.length);
	}
	
	public void copyInto(long srcIndex, long[] dest, int destIndex, int length) {
		Buffer.checkFromIndexSize(srcIndex, length, this.length);
		Buffer.checkFromIndexSize(destIndex, length, dest.length);
		
		if (IS_64_BIT) {
			UNSAFE.copyMemory(null, address() + type().multiply(srcIndex), dest, ARRAY_LONG_BASE_OFFSET + destIndex * ARRAY_LONG_INDEX_SCALE, type().multiply(length));
		} else {
			long srcAddress = address();
			for (int i = 0; i < length; i++)
				dest[destIndex + i] = (long) UNSAFE.getInt(srcAddress + type().multiply(srcIndex)) & 0xFFFF_FFFFL;
		}
	}
	
	public void copyFrom(Buffer[] src) {
		copyFrom(src, 0, 0, src.length);
	}
	
	public void copyFrom(Buffer[] src, int srcIndex, long destIndex, int length) {
		Buffer.checkFromIndexSize(srcIndex, length, src.length);
		Buffer.checkFromIndexSize(destIndex, length, this.length);
		
		long destAddress = address();
		for (int i = 0; i < length; i++)
			UNSAFE.putAddress(destAddress + type().multiply(destIndex + i), src[srcIndex + i].address());
	}
	
	public void copyFrom(java.nio.Buffer[] src) {
		copyFrom(src, 0, 0, src.length);
	}
	
	public void copyFrom(java.nio.Buffer[] src, int srcIndex, long destIndex, int length) {
		Buffer.checkFromIndexSize(srcIndex, length, src.length);
		Buffer.checkFromIndexSize(destIndex, length, this.length);
		
		long destAddress = address();
		for (int i = 0; i < length; i++)
			UNSAFE.putAddress(destAddress + type().multiply(destIndex + i), NioBufferWrapper.getAddress(src[srcIndex + i]));
	}
	
	public void copyFrom(long[] src) {
		copyFrom(src, 0, 0, src.length);
	}
	
	public void copyFrom(long[] src, int srcIndex, long destIndex, int length) {
		Buffer.checkFromIndexSize(srcIndex, length, src.length);
		Buffer.checkFromIndexSize(destIndex, length, this.length);
		
		if (IS_64_BIT) {
			UNSAFE.copyMemory(src, ARRAY_LONG_BASE_OFFSET + srcIndex * ARRAY_LONG_INDEX_SCALE, null, address() + type().multiply(destIndex), type().multiply(length));
		} else {
			long destAddress = address();
			for (int i = 0; i < length; i++)
				UNSAFE.putInt(destAddress + type().multiply(destIndex + i), (int) src[srcIndex + i]);
		}
	}
	
	public LongStream stream() {
		return LongStream.range(0, length()).map(this::getPointer);
	}
}