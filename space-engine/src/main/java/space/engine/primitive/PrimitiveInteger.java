package space.engine.primitive;

public class PrimitiveInteger<T> extends PrimitiveSigned<T> {
	
	/**
	 * the min value this Integer can have
	 */
	public final T minValue;
	
	/**
	 * the max value this Integer can have
	 */
	public final T maxValue;
	
	public PrimitiveInteger(int bytes, T signMask, T numberMask, T minValue, T maxValue) {
		super(bytes, bytes * 8 - 1, signMask, numberMask);
		this.minValue = minValue;
		this.maxValue = maxValue;
	}
}
