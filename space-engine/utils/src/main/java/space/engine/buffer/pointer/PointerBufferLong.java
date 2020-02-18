package space.engine.buffer.pointer;

import org.jetbrains.annotations.NotNull;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.NioBufferWrapper;
import space.engine.primitive.JavaPrimitives;
import space.engine.primitive.Primitive;

import java.nio.LongBuffer;

import static space.engine.Empties.EMPTY_OBJECT_ARRAY;

//single
public class PointerBufferLong extends AbstractPointerBuffer<PointerBufferLong> {
	
	public static final Primitive<?> TYPE = JavaPrimitives.LONG;
	
	//alloc
	
	/**
	 * Allocates a new {@link PointerBufferLong} and fills it with the supplied value. If the {@link PointerBufferLong} is freed, it will free the memory.
	 */
	public static PointerBufferLong alloc(AllocatorFrame allocator, long value) {
		return alloc(allocator, value, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Allocates a new {@link PointerBufferLong} and fills it with the supplied value. If the {@link PointerBufferLong} is freed, it will free the memory.
	 */
	public static PointerBufferLong alloc(Allocator allocator, long value, @NotNull Object[] parents) {
		PointerBufferLong buffer = new PointerBufferLong(allocator, allocator.malloc(TYPE.bytes), parents);
		buffer.putLong(value);
		return buffer;
	}
	
	/**
	 * Allocates a new {@link PointerBufferLong}. The Contents are undefined. If the {@link PointerBufferLong} is freed, it will free the memory.
	 */
	public static PointerBufferLong malloc(AllocatorFrame allocator) {
		return malloc(allocator, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Allocates a new {@link PointerBufferLong}. The Contents are undefined. If the {@link PointerBufferLong} is freed, it will free the memory.
	 */
	public static PointerBufferLong malloc(Allocator allocator, @NotNull Object[] parents) {
		return new PointerBufferLong(allocator, allocator.malloc(TYPE.bytes), parents);
	}
	
	/**
	 * Allocates a new {@link PointerBufferLong}. The Contents are initialized to 0. If the {@link PointerBufferLong} is freed, it will free the memory.
	 */
	public static PointerBufferLong calloc(AllocatorFrame allocator) {
		return calloc(allocator, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Allocates a new {@link PointerBufferLong}. The Contents are initialized to 0. If the {@link PointerBufferLong} is freed, it will free the memory.
	 */
	public static PointerBufferLong calloc(Allocator allocator, @NotNull Object[] parents) {
		return new PointerBufferLong(allocator, allocator.calloc(TYPE.bytes), parents);
	}
	
	//create
	
	/**
	 * Creates a new {@link PointerBufferLong} from the given address. If the {@link PointerBufferLong} is freed, it <b>WILL</b> free the memory.
	 */
	public static PointerBufferLong create(Allocator allocator, long address, @NotNull Object[] parents) {
		return new PointerBufferLong(allocator, address, parents);
	}
	
	/**
	 * Creates a new {@link PointerBufferLong} from the given address. It will <b>NEVER</b> free the memory but will still throw {@link space.engine.baseobject.exceptions.FreedException} if it is freed.
	 */
	public static PointerBufferLong wrap(long address) {
		return wrap(address, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Creates a new {@link PointerBufferLong} from the given address. It will <b>NEVER</b> free the memory but will still throw {@link space.engine.baseobject.exceptions.FreedException} if it is freed.
	 */
	public static PointerBufferLong wrap(long address, @NotNull Object[] parents) {
		return create(Allocator.noop(), address, parents);
	}
	
	//object
	protected PointerBufferLong(Allocator allocator, long address, @NotNull Object[] parents) {
		super(allocator, address, parents);
	}
	
	@Override
	public Primitive<?> type() {
		return TYPE;
	}
	
	@Override
	public LongBuffer nioBuffer() {
		return NioBufferWrapper.wrapLong(this, 1);
	}
	
	//single
	public long getLong() {
		return UNSAFE.getLong(address());
	}
	
	public void putLong(long b) {
		UNSAFE.putLong(address(), b);
	}
	
	//copy
	@Override
	public void copyInto(PointerBufferLong dest) {
		dest.putLong(this.getLong());
	}
	
	@Override
	public void copyFrom(PointerBufferLong src) {
		this.putLong(src.getLong());
	}
}
