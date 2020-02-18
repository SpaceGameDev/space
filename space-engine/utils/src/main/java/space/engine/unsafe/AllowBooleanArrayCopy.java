package space.engine.unsafe;

import sun.misc.Unsafe;

import static sun.misc.Unsafe.ARRAY_BOOLEAN_BASE_OFFSET;

/**
 * @see AllowBooleanArrayCopy#ALLOW_BOOLEAN_ARRAY_COPY
 */
public class AllowBooleanArrayCopy {
	
	private static final Unsafe UNSAFE = UnsafeInstance.getUnsafe();
	
	/**
	 * this is true if booleans are
	 * <code>false == 0 && true != 0</code>.<br>
	 * This allows us to just copy any boolean arrays into off-heap buffers instead of converting them individually.
	 */
	public static final boolean ALLOW_BOOLEAN_ARRAY_COPY;
	
	static {
		ALLOW_BOOLEAN_ARRAY_COPY = UNSAFE.getByte(new boolean[] {true}, (long) ARRAY_BOOLEAN_BASE_OFFSET) != (byte) 0
				&& UNSAFE.getByte(new boolean[] {false}, (long) ARRAY_BOOLEAN_BASE_OFFSET) == (byte) 0;
	}
}
