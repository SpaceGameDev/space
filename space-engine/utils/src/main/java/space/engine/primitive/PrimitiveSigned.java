package space.engine.primitive;

public class PrimitiveSigned<T> extends Primitive<T> {
	
	/**
	 * bit representing the sign
	 */
	public final int signBit;
	
	/**
	 * the mask of the sign
	 */
	public final T signMask;
	
	/**
	 * the mask of the number without the sign
	 */
	public final T numberMask;
	
	public PrimitiveSigned(int bytes, int signBit, T signMask, T numberMask) {
		super(bytes);
		this.signBit = signBit;
		this.signMask = signMask;
		this.numberMask = numberMask;
	}
}
