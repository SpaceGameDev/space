package space.engine.buffer;

import org.jetbrains.annotations.NotNull;
import space.engine.baseobject.Dumpable;
import space.engine.freeable.Freeable;
import space.engine.math.MathUtils;
import space.engine.string.String2D;
import space.engine.string.StringBuilder2D;
import space.engine.unsafe.UnsafeInstance;
import sun.misc.Unsafe;

public abstract class Buffer implements Freeable, Dumpable {
	
	protected static final Unsafe UNSAFE = UnsafeInstance.getUnsafe();
	
	//index checks (copied from Object.checkIndex())
	public static long checkIndex(long index, long bufferLength) {
		if (index < 0 || index >= bufferLength)
			throw new IndexOutOfBoundsException(String.format("Index %d out-of-bounds for length %d", index, bufferLength));
		return index;
	}
	
	public static long checkFromToIndex(long fromIndex, long toIndex, long bufferLength) {
		if (fromIndex < 0 || fromIndex > toIndex || toIndex > bufferLength)
			throw new IndexOutOfBoundsException(String.format("Range [%d, %d) out-of-bounds for length %d", fromIndex, toIndex, bufferLength));
		return fromIndex;
	}
	
	public static long checkFromIndexSize(long fromIndex, long size, long bufferLength) {
		if ((bufferLength | fromIndex | size) < 0 || size > bufferLength - fromIndex)
			throw new IndexOutOfBoundsException(String.format("Range [%d, %<d + %d) out-of-bounds for length %d", fromIndex, size, bufferLength));
		return fromIndex;
	}
	
	//buffer properties
	public abstract long address();
	
	public abstract long sizeOf();
	
	//dump
	@Override
	public @NotNull String2D dump() {
		return dumpBuffer(address(), sizeOf());
	}
	
	public static String2D dumpBuffer(long address, long length) {
		if (length > (1 << 30))
			return new String2D("Buffer too big!");
		int lengthInt = (int) length;
		
		StringBuilder2D b = new StringBuilder2D(2, lengthInt * 3);
		for (int i = 0; i < lengthInt; i++) {
			int pos = i * 3;
			byte d = UNSAFE.getByte(address + i);
			
			if (i % 8 == 0)
				b.setY(0).setX(pos).append(Integer.toHexString(i));
			b.setY(1).setX(pos).append(MathUtils.DIGITS[(d >>> 4) & 0xF]).append(MathUtils.DIGITS[d & 0xF]);
		}
		return b.toString2D();
	}
	
	//copy
	public static void copyMemory(Buffer src, long srcOffset, Buffer dest, long destOffset, long length) {
		Buffer.checkFromIndexSize(srcOffset, length, src.sizeOf());
		Buffer.checkFromIndexSize(destOffset, length, dest.sizeOf());
		UNSAFE.copyMemory(src.address() + srcOffset, dest.address() + destOffset, length);
	}
	
	//container
	private Object container;
	
	public static void setContainer(Buffer buffer, Object container) {
		if (buffer.container != null)
			throw new RuntimeException("container already set!");
		buffer.container = container;
	}
	
	public static Object getContainer(Buffer buffer) {
		return buffer.container;
	}
}
