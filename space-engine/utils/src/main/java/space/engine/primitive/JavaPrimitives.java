package space.engine.primitive;

@SuppressWarnings("unused")
public class JavaPrimitives {
	
	public static final Primitive<Boolean> BOOLEAN = Primitives.BOOLEAN;
	public static final PrimitiveUnsignedInteger<Character> CHAR = new PrimitiveUnsignedInteger<>(2, (char) 0xFFFF);
	public static final PrimitiveInteger<Byte> BYTE = Primitives.INT8;
	public static final PrimitiveInteger<Short> SHORT = Primitives.INT16;
	public static final PrimitiveInteger<Integer> INT = Primitives.INT32;
	public static final PrimitiveInteger<Long> LONG = Primitives.INT64;
	public static final PrimitiveFloatingPoint<Float> FLOAT = Primitives.FP32;
	public static final PrimitiveFloatingPoint<Double> DOUBLE = Primitives.FP64;
}
