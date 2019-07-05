package space.engine.buffer.pointer;

import org.jetbrains.annotations.NotNull;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.NioBufferWrapper;
import space.engine.primitive.JavaPrimitives;
import space.engine.primitive.Primitive;

import java.nio.ShortBuffer;

import static space.engine.Empties.EMPTY_OBJECT_ARRAY;

//single
public class PointerBufferShort extends AbstractPointerBuffer<PointerBufferShort> {
	
	public static final Primitive<?> TYPE = JavaPrimitives.SHORT;
	
	//alloc
	
	/**
	 * Allocates a new {@link PointerBufferShort} and fills it with the supplied value. If the {@link PointerBufferShort} is freed, it will free the memory.
	 */
	public static PointerBufferShort alloc(AllocatorFrame allocator, short value) {
		return alloc(allocator, value, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Allocates a new {@link PointerBufferShort} and fills it with the supplied value. If the {@link PointerBufferShort} is freed, it will free the memory.
	 */
	public static PointerBufferShort alloc(Allocator allocator, short value, @NotNull Object[] parents) {
		PointerBufferShort buffer = new PointerBufferShort(allocator, allocator.malloc(TYPE.bytes), parents);
		buffer.putShort(value);
		return buffer;
	}
	
	/**
	 * Allocates a new {@link PointerBufferShort}. The Contents are undefined. If the {@link PointerBufferShort} is freed, it will free the memory.
	 */
	public static PointerBufferShort malloc(AllocatorFrame allocator) {
		return malloc(allocator, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Allocates a new {@link PointerBufferShort}. The Contents are undefined. If the {@link PointerBufferShort} is freed, it will free the memory.
	 */
	public static PointerBufferShort malloc(Allocator allocator, @NotNull Object[] parents) {
		return new PointerBufferShort(allocator, allocator.malloc(TYPE.bytes), parents);
	}
	
	/**
	 * Allocates a new {@link PointerBufferShort}. The Contents are initialized to 0. If the {@link PointerBufferShort} is freed, it will free the memory.
	 */
	public static PointerBufferShort calloc(AllocatorFrame allocator) {
		return calloc(allocator, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Allocates a new {@link PointerBufferShort}. The Contents are initialized to 0. If the {@link PointerBufferShort} is freed, it will free the memory.
	 */
	public static PointerBufferShort calloc(Allocator allocator, @NotNull Object[] parents) {
		return new PointerBufferShort(allocator, allocator.calloc(TYPE.bytes), parents);
	}
	
	//create
	
	/**
	 * Creates a new {@link PointerBufferShort} from the given address. If the {@link PointerBufferShort} is freed, it <b>WILL</b> free the memory.
	 */
	public static PointerBufferShort create(Allocator allocator, long address, @NotNull Object[] parents) {
		return new PointerBufferShort(allocator, address, parents);
	}
	
	/**
	 * Creates a new {@link PointerBufferShort} from the given address. It will <b>NEVER</b> free the memory but will still throw {@link space.engine.baseobject.exceptions.FreedException} if it is freed.
	 */
	public static PointerBufferShort wrap(long address) {
		return wrap(address, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Creates a new {@link PointerBufferShort} from the given address. It will <b>NEVER</b> free the memory but will still throw {@link space.engine.baseobject.exceptions.FreedException} if it is freed.
	 */
	public static PointerBufferShort wrap(long address, @NotNull Object[] parents) {
		return create(Allocator.noop(), address, parents);
	}
	
	//object
	protected PointerBufferShort(Allocator allocator, long address, @NotNull Object[] parents) {
		super(allocator, address, parents);
	}
	
	@Override
	public Primitive<?> type() {
		return TYPE;
	}
	
	@Override
	public ShortBuffer nioBuffer() {
		return NioBufferWrapper.wrapShort(this, 1);
	}
	
	//single
	public short getShort() {
		return UNSAFE.getShort(address());
	}
	
	public void putShort(short b) {
		UNSAFE.putShort(address(), b);
	}
	
	//copy
	@Override
	public void copyInto(PointerBufferShort dest) {
		dest.putShort(this.getShort());
	}
	
	@Override
	public void copyFrom(PointerBufferShort src) {
		this.putShort(src.getShort());
	}
}
