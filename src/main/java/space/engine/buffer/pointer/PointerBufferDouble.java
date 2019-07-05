package space.engine.buffer.pointer;

import org.jetbrains.annotations.NotNull;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.NioBufferWrapper;
import space.engine.primitive.JavaPrimitives;
import space.engine.primitive.Primitive;

import java.nio.DoubleBuffer;

import static space.engine.Empties.EMPTY_OBJECT_ARRAY;

//single
public class PointerBufferDouble extends AbstractPointerBuffer<PointerBufferDouble> {
	
	public static final Primitive<?> TYPE = JavaPrimitives.DOUBLE;
	
	//alloc
	
	/**
	 * Allocates a new {@link PointerBufferDouble} and fills it with the supplied value. If the {@link PointerBufferDouble} is freed, it will free the memory.
	 */
	public static PointerBufferDouble alloc(AllocatorFrame allocator, double value) {
		return alloc(allocator, value, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Allocates a new {@link PointerBufferDouble} and fills it with the supplied value. If the {@link PointerBufferDouble} is freed, it will free the memory.
	 */
	public static PointerBufferDouble alloc(Allocator allocator, double value, @NotNull Object[] parents) {
		PointerBufferDouble buffer = new PointerBufferDouble(allocator, allocator.malloc(TYPE.bytes), parents);
		buffer.putDouble(value);
		return buffer;
	}
	
	/**
	 * Allocates a new {@link PointerBufferDouble}. The Contents are undefined. If the {@link PointerBufferDouble} is freed, it will free the memory.
	 */
	public static PointerBufferDouble malloc(AllocatorFrame allocator) {
		return malloc(allocator, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Allocates a new {@link PointerBufferDouble}. The Contents are undefined. If the {@link PointerBufferDouble} is freed, it will free the memory.
	 */
	public static PointerBufferDouble malloc(Allocator allocator, @NotNull Object[] parents) {
		return new PointerBufferDouble(allocator, allocator.malloc(TYPE.bytes), parents);
	}
	
	/**
	 * Allocates a new {@link PointerBufferDouble}. The Contents are initialized to 0. If the {@link PointerBufferDouble} is freed, it will free the memory.
	 */
	public static PointerBufferDouble calloc(AllocatorFrame allocator) {
		return calloc(allocator, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Allocates a new {@link PointerBufferDouble}. The Contents are initialized to 0. If the {@link PointerBufferDouble} is freed, it will free the memory.
	 */
	public static PointerBufferDouble calloc(Allocator allocator, @NotNull Object[] parents) {
		return new PointerBufferDouble(allocator, allocator.calloc(TYPE.bytes), parents);
	}
	
	//create
	
	/**
	 * Creates a new {@link PointerBufferDouble} from the given address. If the {@link PointerBufferDouble} is freed, it <b>WILL</b> free the memory.
	 */
	public static PointerBufferDouble create(Allocator allocator, long address, @NotNull Object[] parents) {
		return new PointerBufferDouble(allocator, address, parents);
	}
	
	/**
	 * Creates a new {@link PointerBufferDouble} from the given address. It will <b>NEVER</b> free the memory but will still throw {@link space.engine.baseobject.exceptions.FreedException} if it is freed.
	 */
	public static PointerBufferDouble wrap(long address) {
		return wrap(address, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Creates a new {@link PointerBufferDouble} from the given address. It will <b>NEVER</b> free the memory but will still throw {@link space.engine.baseobject.exceptions.FreedException} if it is freed.
	 */
	public static PointerBufferDouble wrap(long address, @NotNull Object[] parents) {
		return create(Allocator.noop(), address, parents);
	}
	
	//object
	protected PointerBufferDouble(Allocator allocator, long address, @NotNull Object[] parents) {
		super(allocator, address, parents);
	}
	
	@Override
	public Primitive<?> type() {
		return TYPE;
	}
	
	@Override
	public DoubleBuffer nioBuffer() {
		return NioBufferWrapper.wrapDouble(this, 1);
	}
	
	//single
	public double getDouble() {
		return UNSAFE.getDouble(address());
	}
	
	public void putDouble(double b) {
		UNSAFE.putDouble(address(), b);
	}
	
	//copy
	@Override
	public void copyInto(PointerBufferDouble dest) {
		dest.putDouble(this.getDouble());
	}
	
	@Override
	public void copyFrom(PointerBufferDouble src) {
		this.putDouble(src.getDouble());
	}
}
