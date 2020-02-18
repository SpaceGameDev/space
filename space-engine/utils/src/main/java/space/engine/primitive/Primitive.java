package space.engine.primitive;

import space.engine.math.UnsignedMath;

public class Primitive<T> {
	
	/**
	 * size in bytes (= {@link Primitive#bits} / 8)
	 */
	public final int bytes;
	
	/**
	 * size in bits (= {@link Primitive#bytes} * 8)
	 */
	public final int bits;
	
	/**
	 * true if type is aligned (when {@link Primitive#bytes} is power of two).
	 */
	public final boolean isAligned;
	
	/**
	 * if {@link Primitive#isAligned}: shift distance to skip one element<br>
	 * otherwise: -1 (to cause an {@link Exception} if you shift)
	 */
	public final int shift;
	
	public Primitive(int bytes) {
		this.bytes = bytes;
		this.bits = bytes * 8;
		this.isAligned = UnsignedMath.isPowerOfTwo(bytes);
		this.shift = isAligned ? UnsignedMath.getPowerOfTwoFloor(bytes) : -1;
	}
	
	public final int multiply(int i) {
		return isAligned ? i << shift : i * bytes;
	}
	
	public final long multiply(long i) {
		return isAligned ? i << shift : i * bytes;
	}
	
	public final int divide(int i) {
		return isAligned ? i >> shift : i / bytes;
	}
	
	public final long divide(long i) {
		return isAligned ? i >> shift : i / bytes;
	}
	
}
