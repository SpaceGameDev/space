package space.engine.buffer.pointer;

import org.jetbrains.annotations.NotNull;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.Buffer;
import space.engine.buffer.NioBufferWrapper;
import space.engine.primitive.Primitive;
import space.engine.primitive.Primitives;

import static space.engine.Empties.EMPTY_OBJECT_ARRAY;

//single
public class PointerBufferPointer extends AbstractPointerBuffer<PointerBufferPointer> {
	
	public static final Primitive<?> TYPE = Primitives.POINTER;
	
	//alloc
	
	/**
	 * Allocates a new {@link PointerBufferPointer} and fills it with the supplied value. If the {@link PointerBufferPointer} is freed, it will free the memory.
	 */
	public static PointerBufferPointer alloc(AllocatorFrame allocator, Buffer value) {
		return alloc(allocator, value, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Allocates a new {@link PointerBufferPointer} and fills it with the supplied value. If the {@link PointerBufferPointer} is freed, it will free the memory.
	 */
	public static PointerBufferPointer alloc(Allocator allocator, Buffer value, @NotNull Object[] parents) {
		PointerBufferPointer buffer = new PointerBufferPointer(allocator, allocator.malloc(TYPE.bytes), parents);
		buffer.putPointer(value);
		return buffer;
	}
	
	/**
	 * Allocates a new {@link PointerBufferPointer} and fills it with the supplied value. If the {@link PointerBufferPointer} is freed, it will free the memory.
	 */
	public static PointerBufferPointer alloc(AllocatorFrame allocator, java.nio.Buffer value) {
		return alloc(allocator, value, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Allocates a new {@link PointerBufferPointer} and fills it with the supplied value. If the {@link PointerBufferPointer} is freed, it will free the memory.
	 */
	public static PointerBufferPointer alloc(Allocator allocator, java.nio.Buffer value, @NotNull Object[] parents) {
		PointerBufferPointer buffer = new PointerBufferPointer(allocator, allocator.malloc(TYPE.bytes), parents);
		buffer.putPointer(value);
		return buffer;
	}
	
	/**
	 * Allocates a new {@link PointerBufferPointer} and fills it with the supplied value. If the {@link PointerBufferPointer} is freed, it will free the memory.
	 */
	public static PointerBufferPointer alloc(AllocatorFrame allocator, long value) {
		return alloc(allocator, value, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Allocates a new {@link PointerBufferPointer} and fills it with the supplied value. If the {@link PointerBufferPointer} is freed, it will free the memory.
	 */
	public static PointerBufferPointer alloc(Allocator allocator, long value, @NotNull Object[] parents) {
		PointerBufferPointer buffer = new PointerBufferPointer(allocator, allocator.malloc(TYPE.bytes), parents);
		buffer.putPointer(value);
		return buffer;
	}
	
	/**
	 * Allocates a new {@link PointerBufferPointer}. The Contents are undefined. If the {@link PointerBufferPointer} is freed, it will free the memory.
	 */
	public static PointerBufferPointer malloc(AllocatorFrame allocator) {
		return malloc(allocator, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Allocates a new {@link PointerBufferPointer}. The Contents are undefined. If the {@link PointerBufferPointer} is freed, it will free the memory.
	 */
	public static PointerBufferPointer malloc(Allocator allocator, @NotNull Object[] parents) {
		return new PointerBufferPointer(allocator, allocator.malloc(TYPE.bytes), parents);
	}
	
	/**
	 * Allocates a new {@link PointerBufferPointer}. The Contents are initialized to 0. If the {@link PointerBufferPointer} is freed, it will free the memory.
	 */
	public static PointerBufferPointer calloc(AllocatorFrame allocator) {
		return calloc(allocator, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Allocates a new {@link PointerBufferPointer}. The Contents are initialized to 0. If the {@link PointerBufferPointer} is freed, it will free the memory.
	 */
	public static PointerBufferPointer calloc(Allocator allocator, @NotNull Object[] parents) {
		return new PointerBufferPointer(allocator, allocator.calloc(TYPE.bytes), parents);
	}
	
	//create
	
	/**
	 * Creates a new {@link PointerBufferPointer} from the given address. If the {@link PointerBufferPointer} is freed, it <b>WILL</b> free the memory.
	 */
	public static PointerBufferPointer create(Allocator allocator, long address, @NotNull Object[] parents) {
		return new PointerBufferPointer(allocator, address, parents);
	}
	
	/**
	 * Creates a new {@link PointerBufferPointer} from the given address. It will <b>NEVER</b> free the memory but will still throw {@link space.engine.baseobject.exceptions.FreedException} if it is freed.
	 */
	public static PointerBufferPointer wrap(long address) {
		return wrap(address, EMPTY_OBJECT_ARRAY);
	}
	
	/**
	 * Creates a new {@link PointerBufferPointer} from the given address. It will <b>NEVER</b> free the memory but will still throw {@link space.engine.baseobject.exceptions.FreedException} if it is freed.
	 */
	public static PointerBufferPointer wrap(long address, @NotNull Object[] parents) {
		return create(Allocator.noop(), address, parents);
	}
	
	//object
	protected PointerBufferPointer(Allocator allocator, long address, @NotNull Object[] parents) {
		super(allocator, address, parents);
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
	public long getPointer() {
		return UNSAFE.getAddress(address());
	}
	
	public void putPointer(Buffer b) {
		putPointer(b.address());
	}
	
	public void putPointer(java.nio.Buffer b) {
		putPointer(NioBufferWrapper.getAddress(b));
	}
	
	public void putPointer(long b) {
		UNSAFE.putAddress(address(), b);
	}
	
	//copy
	@Override
	public void copyInto(PointerBufferPointer dest) {
		dest.putPointer(this.getPointer());
	}
	
	@Override
	public void copyFrom(PointerBufferPointer src) {
		this.putPointer(src.getPointer());
	}
}
