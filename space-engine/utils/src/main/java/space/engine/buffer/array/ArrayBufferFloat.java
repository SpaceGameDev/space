package space.engine.buffer.array;

import org.jetbrains.annotations.NotNull;
import space.engine.baseobject.exceptions.FreedException;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.Buffer;
import space.engine.buffer.NioBufferWrapper;
import space.engine.primitive.JavaPrimitives;
import space.engine.primitive.Primitive;

import java.nio.FloatBuffer;

import static space.engine.Empties.EMPTY_OBJECT_ARRAY;
import static sun.misc.Unsafe.*;

public class ArrayBufferFloat extends AbstractArrayBuffer<ArrayBufferFloat> {
	
	public static final Primitive<?> TYPE = JavaPrimitives.FLOAT;
	
	//alloc
	
	/**
	 * Allocates a new {@link ArrayBufferFloat} and fills it with the contents of the array. If the {@link ArrayBufferFloat} is freed, it will free the memory.
	 */
	public static ArrayBufferFloat alloc(AllocatorFrame allocator, float[] array) {
		return alloc(allocator, array, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Allocates a new {@link ArrayBufferFloat} and fills it with the contents of the array. If the {@link ArrayBufferFloat} is freed, it will free the memory.
	 */
	public static ArrayBufferFloat alloc(Allocator allocator, float[] array, @NotNull Object[] parents) {
		ArrayBufferFloat buffer = new ArrayBufferFloat(allocator, allocator.malloc(array.length * TYPE.bytes), array.length, parents);
		buffer.copyFrom(array);
		return buffer;
	}
	
	/**
	 * Allocates a new {@link ArrayBufferFloat} of length. The Contents are undefined. If the {@link ArrayBufferFloat} is freed, it will free the memory.
	 */
	public static ArrayBufferFloat malloc(AllocatorFrame allocator, long length) {
		return malloc(allocator, length, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Allocates a new {@link ArrayBufferFloat} of length. The Contents are undefined. If the {@link ArrayBufferFloat} is freed, it will free the memory.
	 */
	public static ArrayBufferFloat malloc(Allocator allocator, long length, @NotNull Object[] parents) {
		return new ArrayBufferFloat(allocator, allocator.malloc(length * TYPE.bytes), length, parents);
	}
	
	/**
	 * Allocates a new {@link ArrayBufferFloat} of length. The Contents are initialized to 0. If the {@link ArrayBufferFloat} is freed, it will free the memory.
	 */
	public static ArrayBufferFloat calloc(AllocatorFrame allocator, long length) {
		return calloc(allocator, length, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Allocates a new {@link ArrayBufferFloat} of length. The Contents are initialized to 0. If the {@link ArrayBufferFloat} is freed, it will free the memory.
	 */
	public static ArrayBufferFloat calloc(Allocator allocator, long length, @NotNull Object[] parents) {
		return new ArrayBufferFloat(allocator, allocator.calloc(length * TYPE.bytes), length, parents);
	}
	
	//create
	
	/**
	 * Creates a new {@link ArrayBufferFloat} from the given address and length. If the {@link ArrayBufferFloat} is freed, it <b>WILL</b> free the memory.
	 */
	public static ArrayBufferFloat create(Allocator allocator, long address, long length, @NotNull Object[] parents) {
		return new ArrayBufferFloat(allocator, address, length, parents);
	}
	
	/**
	 * Creates a new {@link ArrayBufferFloat} from the given address and length. It will <b>NEVER</b> free the memory but will still throw {@link FreedException} if it is freed.
	 */
	public static ArrayBufferFloat wrap(long address, long length) {
		return wrap(address, length, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Creates a new {@link ArrayBufferFloat} from the given address and length. It will <b>NEVER</b> free the memory but will still throw {@link FreedException} if it is freed.
	 */
	public static ArrayBufferFloat wrap(long address, long length, @NotNull Object[] parents) {
		return create(Allocator.noop(), address, length, parents);
	}
	
	//object
	protected ArrayBufferFloat(Allocator allocator, long address, long length, @NotNull Object[] parents) {
		super(allocator, address, length, parents);
	}
	
	@Override
	public Primitive<?> type() {
		return TYPE;
	}
	
	@Override
	public FloatBuffer nioBuffer() {
		return NioBufferWrapper.wrapFloat(this, length);
	}
	
	//single
	public float getFloat(long index) {
		Buffer.checkIndex(index, this.length);
		return UNSAFE.getFloat(address() + type().multiply(index));
	}
	
	public void putFloat(long index, float b) {
		Buffer.checkIndex(index, this.length);
		UNSAFE.putFloat(address() + type().multiply(index), b);
	}
	
	//array
	public void copyInto(float[] dest) {
		copyInto(0, dest, 0, dest.length);
	}
	
	public void copyInto(long srcIndex, float[] dest, int destIndex, int length) {
		Buffer.checkFromIndexSize(srcIndex, length, this.length);
		Buffer.checkFromIndexSize(destIndex, length, dest.length);
		UNSAFE.copyMemory(null, address() + type().multiply(srcIndex), dest, ARRAY_FLOAT_BASE_OFFSET + destIndex * ARRAY_FLOAT_INDEX_SCALE, type().multiply(length));
	}
	
	public void copyFrom(float[] src) {
		copyFrom(src, 0, 0, src.length);
	}
	
	public void copyFrom(float[] src, int srcIndex, long destIndex, int length) {
		Buffer.checkFromIndexSize(srcIndex, length, src.length);
		Buffer.checkFromIndexSize(destIndex, length, this.length);
		UNSAFE.copyMemory(src, ARRAY_FLOAT_BASE_OFFSET + srcIndex * ARRAY_FLOAT_INDEX_SCALE, null, address() + type().multiply(destIndex), type().multiply(length));
	}
}