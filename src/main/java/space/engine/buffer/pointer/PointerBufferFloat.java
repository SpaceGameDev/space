package space.engine.buffer.pointer;

import org.jetbrains.annotations.NotNull;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.NioBufferWrapper;
import space.engine.primitive.JavaPrimitives;
import space.engine.primitive.Primitive;

import java.nio.FloatBuffer;

import static space.engine.Empties.EMPTY_OBJECT_ARRAY;

//single
public class PointerBufferFloat extends AbstractPointerBuffer<PointerBufferFloat> {
	
	public static final Primitive<?> TYPE = JavaPrimitives.FLOAT;
	
	//alloc
	
	/**
	 * Allocates a new {@link PointerBufferFloat} and fills it with the supplied value. If the {@link PointerBufferFloat} is freed, it will free the memory.
	 */
	public static PointerBufferFloat alloc(AllocatorFrame allocator, float value) {
		return alloc(allocator, value, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Allocates a new {@link PointerBufferFloat} and fills it with the supplied value. If the {@link PointerBufferFloat} is freed, it will free the memory.
	 */
	public static PointerBufferFloat alloc(Allocator allocator, float value, @NotNull Object[] parents) {
		PointerBufferFloat buffer = new PointerBufferFloat(allocator, allocator.malloc(TYPE.bytes), parents);
		buffer.putFloat(value);
		return buffer;
	}
	
	/**
	 * Allocates a new {@link PointerBufferFloat}. The Contents are undefined. If the {@link PointerBufferFloat} is freed, it will free the memory.
	 */
	public static PointerBufferFloat malloc(AllocatorFrame allocator) {
		return malloc(allocator, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Allocates a new {@link PointerBufferFloat}. The Contents are undefined. If the {@link PointerBufferFloat} is freed, it will free the memory.
	 */
	public static PointerBufferFloat malloc(Allocator allocator, @NotNull Object[] parents) {
		return new PointerBufferFloat(allocator, allocator.malloc(TYPE.bytes), parents);
	}
	
	/**
	 * Allocates a new {@link PointerBufferFloat}. The Contents are initialized to 0. If the {@link PointerBufferFloat} is freed, it will free the memory.
	 */
	public static PointerBufferFloat calloc(AllocatorFrame allocator) {
		return calloc(allocator, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Allocates a new {@link PointerBufferFloat}. The Contents are initialized to 0. If the {@link PointerBufferFloat} is freed, it will free the memory.
	 */
	public static PointerBufferFloat calloc(Allocator allocator, @NotNull Object[] parents) {
		return new PointerBufferFloat(allocator, allocator.calloc(TYPE.bytes), parents);
	}
	
	//create
	
	/**
	 * Creates a new {@link PointerBufferFloat} from the given address. If the {@link PointerBufferFloat} is freed, it <b>WILL</b> free the memory.
	 */
	public static PointerBufferFloat create(Allocator allocator, long address, @NotNull Object[] parents) {
		return new PointerBufferFloat(allocator, address, parents);
	}
	
	/**
	 * Creates a new {@link PointerBufferFloat} from the given address. It will <b>NEVER</b> free the memory but will still throw {@link space.engine.baseobject.exceptions.FreedException} if it is freed.
	 */
	public static PointerBufferFloat wrap(long address) {
		return wrap(address, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Creates a new {@link PointerBufferFloat} from the given address. It will <b>NEVER</b> free the memory but will still throw {@link space.engine.baseobject.exceptions.FreedException} if it is freed.
	 */
	public static PointerBufferFloat wrap(long address, @NotNull Object[] parents) {
		return create(Allocator.noop(), address, parents);
	}
	
	//object
	protected PointerBufferFloat(Allocator allocator, long address, @NotNull Object[] parents) {
		super(allocator, address, parents);
	}
	
	@Override
	public Primitive<?> type() {
		return TYPE;
	}
	
	@Override
	public FloatBuffer nioBuffer() {
		return NioBufferWrapper.wrapFloat(this, 1);
	}
	
	//single
	public float getFloat() {
		return UNSAFE.getFloat(address());
	}
	
	public void putFloat(float b) {
		UNSAFE.putFloat(address(), b);
	}
	
	//copy
	@Override
	public void copyInto(PointerBufferFloat dest) {
		dest.putFloat(this.getFloat());
	}
	
	@Override
	public void copyFrom(PointerBufferFloat src) {
		this.putFloat(src.getFloat());
	}
}
