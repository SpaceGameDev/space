package space.engine.buffer.array;

import org.jetbrains.annotations.NotNull;
import space.engine.baseobject.exceptions.FreedException;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.Buffer;
import space.engine.buffer.NioBufferWrapper;
import space.engine.primitive.JavaPrimitives;
import space.engine.primitive.Primitive;

import java.nio.ShortBuffer;

import static space.engine.Empties.EMPTY_OBJECT_ARRAY;
import static sun.misc.Unsafe.*;

public class ArrayBufferShort extends AbstractArrayBuffer<ArrayBufferShort> {
	
	public static final Primitive<?> TYPE = JavaPrimitives.SHORT;
	
	//alloc
	
	/**
	 * Allocates a new {@link ArrayBufferShort} and fills it with the contents of the array. If the {@link ArrayBufferShort} is freed, it will free the memory.
	 */
	public static ArrayBufferShort alloc(AllocatorFrame allocator, short[] array) {
		return alloc(allocator, array, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Allocates a new {@link ArrayBufferShort} and fills it with the contents of the array. If the {@link ArrayBufferShort} is freed, it will free the memory.
	 */
	public static ArrayBufferShort alloc(Allocator allocator, short[] array, @NotNull Object[] parents) {
		ArrayBufferShort buffer = new ArrayBufferShort(allocator, allocator.malloc(array.length * TYPE.bytes), array.length, parents);
		buffer.copyFrom(array);
		return buffer;
	}
	
	/**
	 * Allocates a new {@link ArrayBufferShort} of length. The Contents are undefined. If the {@link ArrayBufferShort} is freed, it will free the memory.
	 */
	public static ArrayBufferShort malloc(AllocatorFrame allocator, long length) {
		return malloc(allocator, length, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Allocates a new {@link ArrayBufferShort} of length. The Contents are undefined. If the {@link ArrayBufferShort} is freed, it will free the memory.
	 */
	public static ArrayBufferShort malloc(Allocator allocator, long length, @NotNull Object[] parents) {
		return new ArrayBufferShort(allocator, allocator.malloc(length * TYPE.bytes), length, parents);
	}
	
	/**
	 * Allocates a new {@link ArrayBufferShort} of length. The Contents are initialized to 0. If the {@link ArrayBufferShort} is freed, it will free the memory.
	 */
	public static ArrayBufferShort calloc(AllocatorFrame allocator, long length) {
		return calloc(allocator, length, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Allocates a new {@link ArrayBufferShort} of length. The Contents are initialized to 0. If the {@link ArrayBufferShort} is freed, it will free the memory.
	 */
	public static ArrayBufferShort calloc(Allocator allocator, long length, @NotNull Object[] parents) {
		return new ArrayBufferShort(allocator, allocator.calloc(length * TYPE.bytes), length, parents);
	}
	
	//create
	
	/**
	 * Creates a new {@link ArrayBufferShort} from the given address and length. If the {@link ArrayBufferShort} is freed, it <b>WILL</b> free the memory.
	 */
	public static ArrayBufferShort create(Allocator allocator, long address, long length, @NotNull Object[] parents) {
		return new ArrayBufferShort(allocator, address, length, parents);
	}
	
	/**
	 * Creates a new {@link ArrayBufferShort} from the given address and length. It will <b>NEVER</b> free the memory but will still throw {@link FreedException} if it is freed.
	 */
	public static ArrayBufferShort wrap(long address, long length) {
		return wrap(address, length, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Creates a new {@link ArrayBufferShort} from the given address and length. It will <b>NEVER</b> free the memory but will still throw {@link FreedException} if it is freed.
	 */
	public static ArrayBufferShort wrap(long address, long length, @NotNull Object[] parents) {
		return create(Allocator.noop(), address, length, parents);
	}
	
	//object
	protected ArrayBufferShort(Allocator allocator, long address, long length, @NotNull Object[] parents) {
		super(allocator, address, length, parents);
	}
	
	@Override
	public Primitive<?> type() {
		return TYPE;
	}
	
	@Override
	public ShortBuffer nioBuffer() {
		return NioBufferWrapper.wrapShort(this, length);
	}
	
	//single
	public short getShort(long index) {
		Buffer.checkIndex(index, this.length);
		return UNSAFE.getShort(address() + type().multiply(index));
	}
	
	public void putShort(long index, short b) {
		Buffer.checkIndex(index, this.length);
		UNSAFE.putShort(address() + type().multiply(index), b);
	}
	
	//array
	public void copyInto(short[] dest) {
		copyInto(0, dest, 0, dest.length);
	}
	
	public void copyInto(long srcIndex, short[] dest, int destIndex, int length) {
		Buffer.checkFromIndexSize(srcIndex, length, this.length);
		Buffer.checkFromIndexSize(destIndex, length, dest.length);
		UNSAFE.copyMemory(null, address() + type().multiply(srcIndex), dest, ARRAY_SHORT_BASE_OFFSET + destIndex * ARRAY_SHORT_INDEX_SCALE, type().multiply(length));
	}
	
	public void copyFrom(short[] src) {
		copyFrom(src, 0, 0, src.length);
	}
	
	public void copyFrom(short[] src, int srcIndex, long destIndex, int length) {
		Buffer.checkFromIndexSize(srcIndex, length, src.length);
		Buffer.checkFromIndexSize(destIndex, length, this.length);
		UNSAFE.copyMemory(src, ARRAY_SHORT_BASE_OFFSET + srcIndex * ARRAY_SHORT_INDEX_SCALE, null, address() + type().multiply(destIndex), type().multiply(length));
	}
}