package space.engine.primitive;

public class PrimitiveFloatingPoint<T> extends PrimitiveSigned<T> {
	
	/**
	 * bit where the fraction starts
	 */
	public final int fractionStart;
	
	/**
	 * bit where the fraction ends
	 */
	public final int fractionEnd;
	
	/**
	 * size of the fraction
	 */
	public final int fractionSize;
	
	/**
	 * mask for only fraction
	 */
	public final T fractionMask;
	
	/**
	 * size of the fraction including the leading 1 not in the data type
	 */
	public final int fractionFullSize;
	
	/**
	 * bit where the exponent starts
	 */
	public final int exponentStart;
	
	/**
	 * bit where the exponent ends
	 */
	public final int exponentEnd;
	
	/**
	 * size of the exponent
	 */
	public final int exponentSize;
	
	/**
	 * mask for only exponent
	 */
	public final T exponentMask;
	
	public PrimitiveFloatingPoint(int bytes, int signBit, T signMask, T numberMask, int fractionStart, int fractionSize, T fractionMask, int exponentStart, int exponentSize, T exponentMask) {
		super(bytes, signBit, signMask, numberMask);
		
		this.fractionStart = fractionStart;
		this.fractionEnd = fractionStart + fractionSize;
		this.fractionSize = fractionSize;
		this.fractionFullSize = fractionSize + 1;
		this.fractionMask = fractionMask;
		this.exponentStart = exponentStart;
		this.exponentEnd = exponentStart + exponentSize;
		this.exponentSize = exponentSize;
		this.exponentMask = exponentMask;
	}
}
