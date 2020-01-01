package space.engine.buffer;

import org.jetbrains.annotations.NotNull;
import space.engine.freeable.Freeable;
import space.engine.primitive.JavaPrimitives;
import space.engine.unsafe.UnsafeInstance;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

import static space.engine.unsafe.UnsafeInstance.objectFieldOffsetWithSuper;

public class NioBufferWrapper {
	
	private static final Unsafe UNSAFE = UnsafeInstance.getUnsafe();
	private static final boolean bigEndian = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;
	
	private static final Class<? extends ByteBuffer> CLASS_BYTE;
	private static final Class<? extends ShortBuffer> CLASS_SHORT;
	private static final Class<? extends IntBuffer> CLASS_INT;
	private static final Class<? extends LongBuffer> CLASS_LONG;
	private static final Class<? extends FloatBuffer> CLASS_FLOAT;
	private static final Class<? extends DoubleBuffer> CLASS_DOUBLE;
	
	private static final long ADDRESS;
	private static final long MARK;
	private static final long POSITION;
	private static final long LIMIT;
	private static final long CAPACITY;
	
	private static final long BIG_ENDIAN_BYTE;
	private static final long NATIVE_BYTE_ORDER_BYTE;
	
	private static final long ATTACHMENT_BYTE;
	private static final long ATTACHMENT_SHORT;
	private static final long ATTACHMENT_INT;
	private static final long ATTACHMENT_LONG;
	private static final long ATTACHMENT_FLOAT;
	private static final long ATTACHMENT_DOUBLE;
	
	static {
		try {
			ByteBuffer bb = ByteBuffer.allocateDirect(0);
			bb.order(ByteOrder.nativeOrder());
			
			CLASS_BYTE = bb.getClass();
			CLASS_SHORT = bb.asShortBuffer().getClass();
			CLASS_INT = bb.asIntBuffer().getClass();
			CLASS_LONG = bb.asLongBuffer().getClass();
			CLASS_FLOAT = bb.asFloatBuffer().getClass();
			CLASS_DOUBLE = bb.asDoubleBuffer().getClass();
			
			ADDRESS = objectFieldOffsetWithSuper(CLASS_BYTE, "address");
			MARK = objectFieldOffsetWithSuper(CLASS_BYTE, "mark");
			POSITION = objectFieldOffsetWithSuper(CLASS_BYTE, "position");
			LIMIT = objectFieldOffsetWithSuper(CLASS_BYTE, "limit");
			CAPACITY = objectFieldOffsetWithSuper(CLASS_BYTE, "capacity");
			
			BIG_ENDIAN_BYTE = objectFieldOffsetWithSuper(CLASS_BYTE, "bigEndian");
			NATIVE_BYTE_ORDER_BYTE = objectFieldOffsetWithSuper(CLASS_BYTE, "nativeByteOrder");
			
			ATTACHMENT_BYTE = findAttachment(bb.slice(), bb);
			ATTACHMENT_SHORT = findAttachment(bb.asShortBuffer(), bb);
			ATTACHMENT_INT = findAttachment(bb.asIntBuffer(), bb);
			ATTACHMENT_LONG = findAttachment(bb.asLongBuffer(), bb);
			ATTACHMENT_FLOAT = findAttachment(bb.asFloatBuffer(), bb);
			ATTACHMENT_DOUBLE = findAttachment(bb.asDoubleBuffer(), bb);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static long findAttachment(java.nio.Buffer child, ByteBuffer parent) {
		try {
			for (Class<?> clazz = child.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
				for (Field field : clazz.getDeclaredFields()) {
					field.setAccessible(true);
					if (field.get(child) == parent) {
						return UNSAFE.objectFieldOffset(field);
					}
				}
			}
			throw new RuntimeException("attachment not found!");
		} catch (IllegalAccessException e) {
			throw new RuntimeException("attachment not found!", e);
		}
	}
	
	//getAddress
	public static long getAddress(@NotNull java.nio.Buffer buffer) {
		return UNSAFE.getLong(buffer, ADDRESS);
	}
	
	public static long getLength(@NotNull java.nio.Buffer buffer) {
		return UNSAFE.getLong(buffer, CAPACITY);
	}
	
	//wrap
	public static @NotNull ByteBuffer wrapByte(@NotNull Buffer buffer) {
		return wrapByte(buffer, 0, buffer.sizeOf());
	}
	
	public static @NotNull ByteBuffer wrapByte(@NotNull Buffer buffer, long length) {
		return wrapByte(buffer, 0, length);
	}
	
	public static @NotNull ByteBuffer wrapByte(@NotNull Buffer buffer, long offset, long length) {
		Buffer.checkFromIndexSize(offset, length * JavaPrimitives.BYTE.bytes, buffer.sizeOf());
		ByteBuffer b = wrap(CLASS_BYTE, buffer.address() + offset, length);
		UNSAFE.putBoolean(b, BIG_ENDIAN_BYTE, bigEndian);
		UNSAFE.putBoolean(b, NATIVE_BYTE_ORDER_BYTE, true);
		UNSAFE.putObject(b, ATTACHMENT_BYTE, Freeable.createDummy(new Object[] {buffer}));
		return b;
	}
	
	public static @NotNull ShortBuffer wrapShort(@NotNull Buffer buffer, long length) {
		return wrapShort(buffer, 0, length);
	}
	
	public static @NotNull ShortBuffer wrapShort(@NotNull Buffer buffer, long offset, long length) {
		Buffer.checkFromIndexSize(offset, length * JavaPrimitives.SHORT.bytes, buffer.sizeOf());
		ShortBuffer b = wrap(CLASS_SHORT, buffer.address() + offset, length);
		UNSAFE.putObject(b, ATTACHMENT_SHORT, Freeable.createDummy(new Object[] {buffer}));
		return b;
	}
	
	public static @NotNull IntBuffer wrapInt(@NotNull Buffer buffer, long length) {
		return wrapInt(buffer, 0, length);
	}
	
	public static @NotNull IntBuffer wrapInt(@NotNull Buffer buffer, long offset, long length) {
		Buffer.checkFromIndexSize(offset, length * JavaPrimitives.INT.bytes, buffer.sizeOf());
		IntBuffer b = wrap(CLASS_INT, buffer.address() + offset, length);
		UNSAFE.putObject(b, ATTACHMENT_INT, Freeable.createDummy(new Object[] {buffer}));
		return b;
	}
	
	public static @NotNull LongBuffer wrapLong(@NotNull Buffer buffer, long length) {
		return wrapLong(buffer, 0, length);
	}
	
	public static @NotNull LongBuffer wrapLong(@NotNull Buffer buffer, long offset, long length) {
		Buffer.checkFromIndexSize(offset, length * JavaPrimitives.LONG.bytes, buffer.sizeOf());
		LongBuffer b = wrap(CLASS_LONG, buffer.address() + offset, length);
		UNSAFE.putObject(b, ATTACHMENT_LONG, Freeable.createDummy(new Object[] {buffer}));
		return b;
	}
	
	public static @NotNull FloatBuffer wrapFloat(@NotNull Buffer buffer, long length) {
		return wrapFloat(buffer, 0, length);
	}
	
	public static @NotNull FloatBuffer wrapFloat(@NotNull Buffer buffer, long offset, long length) {
		Buffer.checkFromIndexSize(offset, length * JavaPrimitives.FLOAT.bytes, buffer.sizeOf());
		FloatBuffer b = wrap(CLASS_FLOAT, buffer.address() + offset, length);
		UNSAFE.putObject(b, ATTACHMENT_FLOAT, Freeable.createDummy(new Object[] {buffer}));
		return b;
	}
	
	public static @NotNull DoubleBuffer wrapDouble(@NotNull Buffer buffer, long length) {
		return wrapDouble(buffer, 0, length);
	}
	
	public static @NotNull DoubleBuffer wrapDouble(@NotNull Buffer buffer, long offset, long length) {
		Buffer.checkFromIndexSize(offset, length * JavaPrimitives.DOUBLE.bytes, buffer.sizeOf());
		DoubleBuffer b = wrap(CLASS_DOUBLE, buffer.address() + offset, length);
		UNSAFE.putObject(b, ATTACHMENT_DOUBLE, Freeable.createDummy(new Object[] {buffer}));
		return b;
	}
	
	private static <T extends java.nio.Buffer> T wrap(@NotNull Class<T> bufferClazz, long address, long sizeOf) {
		int sizeOfInt = (int) sizeOf;
		if (sizeOfInt != sizeOf)
			throw new RuntimeException("Buffer sizeOf " + sizeOf + " exceeds 32bit sizeOf limit of DirectBuffer!");
		
		T b;
		try {
			//noinspection unchecked
			b = (T) UNSAFE.allocateInstance(bufferClazz);
		} catch (InstantiationException e) {
			throw new RuntimeException("Couldn't allocate instance of " + bufferClazz, e);
		}
		
		UNSAFE.putLong(b, ADDRESS, address);
		UNSAFE.putInt(b, MARK, -1);
		UNSAFE.putInt(b, POSITION, 0);
		UNSAFE.putInt(b, LIMIT, sizeOfInt);
		UNSAFE.putInt(b, CAPACITY, sizeOfInt);
		return b;
	}
}
