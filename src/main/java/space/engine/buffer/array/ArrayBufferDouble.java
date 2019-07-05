package space.engine.buffer.array;

import org.jetbrains.annotations.NotNull;
import space.engine.baseobject.exceptions.FreedException;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.Buffer;
import space.engine.buffer.NioBufferWrapper;
import space.engine.primitive.JavaPrimitives;
import space.engine.primitive.Primitive;

import java.nio.DoubleBuffer;
import java.util.stream.DoubleStream;
import java.util.stream.LongStream;

import static space.engine.Empties.EMPTY_OBJECT_ARRAY;
import static sun.misc.Unsafe.*;

public class ArrayBufferDouble extends AbstractArrayBuffer<ArrayBufferDouble> {
	
	public static final Primitive<?> TYPE = JavaPrimitives.DOUBLE;
	
	//alloc
	
	/**
	 * Allocates a new {@link ArrayBufferDouble} and fills it with the contents of the array. If the {@link ArrayBufferDouble} is freed, it will free the memory.
	 */
	public static ArrayBufferDouble alloc(AllocatorFrame allocator, double[] array) {
		return alloc(allocator, array, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Allocates a new {@link ArrayBufferDouble} and fills it with the contents of the array. If the {@link ArrayBufferDouble} is freed, it will free the memory.
	 */
	public static ArrayBufferDouble alloc(Allocator allocator, double[] array, @NotNull Object[] parents) {
		ArrayBufferDouble buffer = new ArrayBufferDouble(allocator, allocator.malloc(array.length * TYPE.bytes), array.length, parents);
		buffer.copyFrom(array);
		return buffer;
	}
	
	/**
	 * Allocates a new {@link ArrayBufferDouble} of length. The Contents are undefined. If the {@link ArrayBufferDouble} is freed, it will free the memory.
	 */
	public static ArrayBufferDouble malloc(AllocatorFrame allocator, long length) {
		return malloc(allocator, length, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Allocates a new {@link ArrayBufferDouble} of length. The Contents are undefined. If the {@link ArrayBufferDouble} is freed, it will free the memory.
	 */
	public static ArrayBufferDouble malloc(Allocator allocator, long length, @NotNull Object[] parents) {
		return new ArrayBufferDouble(allocator, allocator.malloc(length * TYPE.bytes), length, parents);
	}
	
	/**
	 * Allocates a new {@link ArrayBufferDouble} of length. The Contents are initialized to 0. If the {@link ArrayBufferDouble} is freed, it will free the memory.
	 */
	public static ArrayBufferDouble calloc(AllocatorFrame allocator, long length) {
		return calloc(allocator, length, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Allocates a new {@link ArrayBufferDouble} of length. The Contents are initialized to 0. If the {@link ArrayBufferDouble} is freed, it will free the memory.
	 */
	public static ArrayBufferDouble calloc(Allocator allocator, long length, @NotNull Object[] parents) {
		return new ArrayBufferDouble(allocator, allocator.calloc(length * TYPE.bytes), length, parents);
	}
	
	//create
	
	/**
	 * Creates a new {@link ArrayBufferDouble} from the given address and length. If the {@link ArrayBufferDouble} is freed, it <b>WILL</b> free the memory.
	 */
	public static ArrayBufferDouble create(Allocator allocator, long address, long length, @NotNull Object[] parents) {
		return new ArrayBufferDouble(allocator, address, length, parents);
	}
	
	/**
	 * Creates a new {@link ArrayBufferDouble} from the given address and length. It will <b>NEVER</b> free the memory but will still throw {@link FreedException} if it is freed.
	 */
	public static ArrayBufferDouble wrap(long address, long length) {
		return wrap(address, length, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Creates a new {@link ArrayBufferDouble} from the given address and length. It will <b>NEVER</b> free the memory but will still throw {@link FreedException} if it is freed.
	 */
	public static ArrayBufferDouble wrap(long address, long length, @NotNull Object[] parents) {
		return create(Allocator.noop(), address, length, parents);
	}
	
	//object
	protected ArrayBufferDouble(Allocator allocator, long address, long length, @NotNull Object[] parents) {
		super(allocator, address, length, parents);
	}
	
	@Override
	public Primitive<?> type() {
		return TYPE;
	}
	
	@Override
	public DoubleBuffer nioBuffer() {
		return NioBufferWrapper.wrapDouble(this, length);
	}
	
	//single
	public double getDouble(long index) {
		Buffer.checkIndex(index, this.length);
		return UNSAFE.getDouble(address() + type().multiply(index));
	}
	
	public void putDouble(long index, double b) {
		Buffer.checkIndex(index, this.length);
		UNSAFE.putDouble(address() + type().multiply(index), b);
	}
	
	//array
	public void copyInto(double[] dest) {
		copyInto(0, dest, 0, dest.length);
	}
	
	public void copyInto(long srcIndex, double[] dest, int destIndex, int length) {
		Buffer.checkFromIndexSize(srcIndex, length, this.length);
		Buffer.checkFromIndexSize(destIndex, length, dest.length);
		UNSAFE.copyMemory(null, address() + type().multiply(srcIndex), dest, ARRAY_DOUBLE_BASE_OFFSET + destIndex * ARRAY_DOUBLE_INDEX_SCALE, type().multiply(length));
	}
	
	public void copyFrom(double[] src) {
		copyFrom(src, 0, 0, src.length);
	}
	
	public void copyFrom(double[] src, int srcIndex, long destIndex, int length) {
		Buffer.checkFromIndexSize(srcIndex, length, src.length);
		Buffer.checkFromIndexSize(destIndex, length, this.length);
		UNSAFE.copyMemory(src, ARRAY_DOUBLE_BASE_OFFSET + srcIndex * ARRAY_DOUBLE_INDEX_SCALE, null, address() + type().multiply(destIndex), type().multiply(length));
	}
	
	public DoubleStream stream() {
		return LongStream.range(0, length()).mapToDouble(this::getDouble);
	}
}