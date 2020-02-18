package space.engine.buffer.array;

import org.jetbrains.annotations.NotNull;
import space.engine.baseobject.exceptions.FreedException;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.Buffer;
import space.engine.buffer.NioBufferWrapper;
import space.engine.primitive.JavaPrimitives;
import space.engine.primitive.Primitive;

import java.nio.LongBuffer;
import java.util.stream.LongStream;

import static space.engine.Empties.EMPTY_OBJECT_ARRAY;
import static sun.misc.Unsafe.*;

public class ArrayBufferLong extends AbstractArrayBuffer<ArrayBufferLong> {
	
	public static final Primitive<?> TYPE = JavaPrimitives.LONG;
	
	//alloc
	
	/**
	 * Allocates a new {@link ArrayBufferLong} and fills it with the contents of the array. If the {@link ArrayBufferLong} is freed, it will free the memory.
	 */
	public static ArrayBufferLong alloc(AllocatorFrame allocator, long[] array) {
		return alloc(allocator, array, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Allocates a new {@link ArrayBufferLong} and fills it with the contents of the array. If the {@link ArrayBufferLong} is freed, it will free the memory.
	 */
	public static ArrayBufferLong alloc(Allocator allocator, long[] array, @NotNull Object[] parents) {
		ArrayBufferLong buffer = new ArrayBufferLong(allocator, allocator.malloc(array.length * TYPE.bytes), array.length, parents);
		buffer.copyFrom(array);
		return buffer;
	}
	
	/**
	 * Allocates a new {@link ArrayBufferLong} of length. The Contents are undefined. If the {@link ArrayBufferLong} is freed, it will free the memory.
	 */
	public static ArrayBufferLong malloc(AllocatorFrame allocator, long length) {
		return malloc(allocator, length, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Allocates a new {@link ArrayBufferLong} of length. The Contents are undefined. If the {@link ArrayBufferLong} is freed, it will free the memory.
	 */
	public static ArrayBufferLong malloc(Allocator allocator, long length, @NotNull Object[] parents) {
		return new ArrayBufferLong(allocator, allocator.malloc(length * TYPE.bytes), length, parents);
	}
	
	/**
	 * Allocates a new {@link ArrayBufferLong} of length. The Contents are initialized to 0. If the {@link ArrayBufferLong} is freed, it will free the memory.
	 */
	public static ArrayBufferLong calloc(AllocatorFrame allocator, long length) {
		return calloc(allocator, length, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Allocates a new {@link ArrayBufferLong} of length. The Contents are initialized to 0. If the {@link ArrayBufferLong} is freed, it will free the memory.
	 */
	public static ArrayBufferLong calloc(Allocator allocator, long length, @NotNull Object[] parents) {
		return new ArrayBufferLong(allocator, allocator.calloc(length * TYPE.bytes), length, parents);
	}
	
	//create
	
	/**
	 * Creates a new {@link ArrayBufferLong} from the given address and length. If the {@link ArrayBufferLong} is freed, it <b>WILL</b> free the memory.
	 */
	public static ArrayBufferLong create(Allocator allocator, long address, long length, @NotNull Object[] parents) {
		return new ArrayBufferLong(allocator, address, length, parents);
	}
	
	/**
	 * Creates a new {@link ArrayBufferLong} from the given address and length. It will <b>NEVER</b> free the memory but will still throw {@link FreedException} if it is freed.
	 */
	public static ArrayBufferLong wrap(long address, long length) {
		return wrap(address, length, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Creates a new {@link ArrayBufferLong} from the given address and length. It will <b>NEVER</b> free the memory but will still throw {@link FreedException} if it is freed.
	 */
	public static ArrayBufferLong wrap(long address, long length, @NotNull Object[] parents) {
		return create(Allocator.noop(), address, length, parents);
	}
	
	//object
	protected ArrayBufferLong(Allocator allocator, long address, long length, @NotNull Object[] parents) {
		super(allocator, address, length, parents);
	}
	
	@Override
	public Primitive<?> type() {
		return TYPE;
	}
	
	@Override
	public LongBuffer nioBuffer() {
		return NioBufferWrapper.wrapLong(this, length);
	}
	
	//single
	public long getLong(long index) {
		Buffer.checkIndex(index, this.length);
		return UNSAFE.getLong(address() + type().multiply(index));
	}
	
	public void putLong(long index, long b) {
		Buffer.checkIndex(index, this.length);
		UNSAFE.putLong(address() + type().multiply(index), b);
	}
	
	//array
	public void copyInto(long[] dest) {
		copyInto(0, dest, 0, dest.length);
	}
	
	public void copyInto(long srcIndex, long[] dest, int destIndex, int length) {
		Buffer.checkFromIndexSize(srcIndex, length, this.length);
		Buffer.checkFromIndexSize(destIndex, length, dest.length);
		UNSAFE.copyMemory(null, address() + type().multiply(srcIndex), dest, ARRAY_LONG_BASE_OFFSET + destIndex * ARRAY_LONG_INDEX_SCALE, type().multiply(length));
	}
	
	public void copyFrom(long[] src) {
		copyFrom(src, 0, 0, src.length);
	}
	
	public void copyFrom(long[] src, int srcIndex, long destIndex, int length) {
		Buffer.checkFromIndexSize(srcIndex, length, src.length);
		Buffer.checkFromIndexSize(destIndex, length, this.length);
		UNSAFE.copyMemory(src, ARRAY_LONG_BASE_OFFSET + srcIndex * ARRAY_LONG_INDEX_SCALE, null, address() + type().multiply(destIndex), type().multiply(length));
	}
	
	public LongStream stream() {
		return LongStream.range(0, length()).map(this::getLong);
	}
}