package space.engine.buffer.pointer;

import org.jetbrains.annotations.NotNull;
import space.engine.buffer.AbstractBuffer;
import space.engine.buffer.Allocator;
import space.engine.primitive.Primitive;

public abstract class AbstractPointerBuffer<SELF extends AbstractPointerBuffer<SELF>> extends AbstractBuffer {
	
	public AbstractPointerBuffer(Allocator allocator, long address, @NotNull Object[] parents) {
		super(allocator, address, parents);
	}
	
	public abstract Primitive<?> type();
	
	public abstract java.nio.Buffer nioBuffer();
	
	@Override
	public long sizeOf() {
		return type().bytes;
	}
	
	//access
	
	/**
	 * src == this
	 */
	public abstract void copyInto(SELF dest);
	
	/**
	 * dest == this
	 */
	public abstract void copyFrom(SELF src);
	
	public void clear() {
		UNSAFE.setMemory(address(), (long) type().bytes, (byte) 0);
	}
}
