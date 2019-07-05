package space.engine.primitive;

public class PrimitiveUnsignedInteger<T> extends Primitive<T> {
	
	/**
	 * the max value this unsigned Integer can have
	 */
	public final T maxValue;
	
	public PrimitiveUnsignedInteger(int bytes, T maxValue) {
		super(bytes);
		this.maxValue = maxValue;
	}
}
