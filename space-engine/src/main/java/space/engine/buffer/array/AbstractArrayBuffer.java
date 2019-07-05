package space.engine.buffer.array;

import org.jetbrains.annotations.NotNull;
import space.engine.buffer.AbstractBuffer;
import space.engine.buffer.Allocator;
import space.engine.buffer.Buffer;
import space.engine.primitive.Primitive;

public abstract class AbstractArrayBuffer<SELF extends AbstractArrayBuffer<SELF>> extends AbstractBuffer {
	
	protected final long length;
	
	public AbstractArrayBuffer(Allocator allocator, long address, long length, @NotNull Object[] parents) {
		super(allocator, address, parents);
		this.length = length;
	}
	
	public abstract Primitive<?> type();
	
	public abstract java.nio.Buffer nioBuffer();
	
	@Override
	public long sizeOf() {
		return type().multiply(length);
	}
	
	public long length() {
		return length;
	}
	
	//access
	
	/**
	 * src == this
	 */
	public void copyInto(long srcIndex, SELF dest, long destIndex, long length) {
		Primitive<?> type = type();
		Buffer.checkFromIndexSize(srcIndex, length, this.length());
		Buffer.checkFromIndexSize(destIndex, length, dest.length());
		UNSAFE.copyMemory(this.address() + type.multiply(srcIndex), dest.address() + type.multiply(destIndex), type.multiply(length));
	}
	
	/**
	 * dest == this
	 */
	public void copyFrom(SELF src, long srcIndex, long destIndex, long length) {
		Primitive<?> type = type();
		Buffer.checkFromIndexSize(srcIndex, length, src.length());
		Buffer.checkFromIndexSize(destIndex, length, this.length());
		UNSAFE.copyMemory(src.address() + type.multiply(srcIndex), this.address() + type.multiply(destIndex), type.multiply(length));
	}
	
	public void clear() {
		UNSAFE.setMemory(address(), sizeOf(), (byte) 0);
	}
}
