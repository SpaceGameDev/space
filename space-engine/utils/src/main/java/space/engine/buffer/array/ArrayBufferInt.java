package space.engine.buffer.array;

import org.jetbrains.annotations.NotNull;
import space.engine.baseobject.exceptions.FreedException;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.Buffer;
import space.engine.buffer.NioBufferWrapper;
import space.engine.primitive.JavaPrimitives;
import space.engine.primitive.Primitive;

import java.nio.IntBuffer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static space.engine.Empties.EMPTY_OBJECT_ARRAY;
import static sun.misc.Unsafe.*;

public class ArrayBufferInt extends AbstractArrayBuffer<ArrayBufferInt> {
	
	public static final Primitive<?> TYPE = JavaPrimitives.INT;
	
	//alloc
	
	/**
	 * Allocates a new {@link ArrayBufferInt} and fills it with the contents of the array. If the {@link ArrayBufferInt} is freed, it will free the memory.
	 */
	public static ArrayBufferInt alloc(AllocatorFrame allocator, int[] array) {
		return alloc(allocator, array, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Allocates a new {@link ArrayBufferInt} and fills it with the contents of the array. If the {@link ArrayBufferInt} is freed, it will free the memory.
	 */
	public static ArrayBufferInt alloc(Allocator allocator, int[] array, @NotNull Object[] parents) {
		ArrayBufferInt buffer = new ArrayBufferInt(allocator, allocator.malloc(array.length * TYPE.bytes), array.length, parents);
		buffer.copyFrom(array);
		return buffer;
	}
	
	/**
	 * Allocates a new {@link ArrayBufferInt} of length. The Contents are undefined. If the {@link ArrayBufferInt} is freed, it will free the memory.
	 */
	public static ArrayBufferInt malloc(AllocatorFrame allocator, long length) {
		return malloc(allocator, length, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Allocates a new {@link ArrayBufferInt} of length. The Contents are undefined. If the {@link ArrayBufferInt} is freed, it will free the memory.
	 */
	public static ArrayBufferInt malloc(Allocator allocator, long length, @NotNull Object[] parents) {
		return new ArrayBufferInt(allocator, allocator.malloc(length * TYPE.bytes), length, parents);
	}
	
	/**
	 * Allocates a new {@link ArrayBufferInt} of length. The Contents are initialized to 0. If the {@link ArrayBufferInt} is freed, it will free the memory.
	 */
	public static ArrayBufferInt calloc(AllocatorFrame allocator, long length) {
		return calloc(allocator, length, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Allocates a new {@link ArrayBufferInt} of length. The Contents are initialized to 0. If the {@link ArrayBufferInt} is freed, it will free the memory.
	 */
	public static ArrayBufferInt calloc(Allocator allocator, long length, @NotNull Object[] parents) {
		return new ArrayBufferInt(allocator, allocator.calloc(length * TYPE.bytes), length, parents);
	}
	
	//create
	
	/**
	 * Creates a new {@link ArrayBufferInt} from the given address and length. If the {@link ArrayBufferInt} is freed, it <b>WILL</b> free the memory.
	 */
	public static ArrayBufferInt create(Allocator allocator, long address, long length, @NotNull Object[] parents) {
		return new ArrayBufferInt(allocator, address, length, parents);
	}
	
	/**
	 * Creates a new {@link ArrayBufferInt} from the given address and length. It will <b>NEVER</b> free the memory but will still throw {@link FreedException} if it is freed.
	 */
	public static ArrayBufferInt wrap(long address, long length) {
		return wrap(address, length, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Creates a new {@link ArrayBufferInt} from the given address and length. It will <b>NEVER</b> free the memory but will still throw {@link FreedException} if it is freed.
	 */
	public static ArrayBufferInt wrap(long address, long length, @NotNull Object[] parents) {
		return create(Allocator.noop(), address, length, parents);
	}
	
	//object
	protected ArrayBufferInt(Allocator allocator, long address, long length, @NotNull Object[] parents) {
		super(allocator, address, length, parents);
	}
	
	@Override
	public Primitive<?> type() {
		return TYPE;
	}
	
	@Override
	public IntBuffer nioBuffer() {
		return NioBufferWrapper.wrapInt(this, length);
	}
	
	//single
	public int getInt(long index) {
		Buffer.checkIndex(index, this.length);
		return UNSAFE.getInt(address() + type().multiply(index));
	}
	
	public void putInt(long index, int b) {
		Buffer.checkIndex(index, this.length);
		UNSAFE.putInt(address() + type().multiply(index), b);
	}
	
	//array
	public void copyInto(int[] dest) {
		copyInto(0, dest, 0, dest.length);
	}
	
	public void copyInto(long srcIndex, int[] dest, int destIndex, int length) {
		Buffer.checkFromIndexSize(srcIndex, length, this.length);
		Buffer.checkFromIndexSize(destIndex, length, dest.length);
		UNSAFE.copyMemory(null, address() + type().multiply(srcIndex), dest, ARRAY_INT_BASE_OFFSET + destIndex * ARRAY_INT_INDEX_SCALE, type().multiply(length));
	}
	
	public void copyFrom(int[] src) {
		copyFrom(src, 0, 0, src.length);
	}
	
	public void copyFrom(int[] src, int srcIndex, long destIndex, int length) {
		Buffer.checkFromIndexSize(srcIndex, length, src.length);
		Buffer.checkFromIndexSize(destIndex, length, this.length);
		UNSAFE.copyMemory(src, ARRAY_INT_BASE_OFFSET + srcIndex * ARRAY_INT_INDEX_SCALE, null, address() + type().multiply(destIndex), type().multiply(length));
	}
	
	public IntStream stream() {
		return LongStream.range(0, length()).mapToInt(this::getInt);
	}
}