package space.engine.primitive;

import space.engine.Device;

@SuppressWarnings("unused")
public class Primitives {
	
	public static final PrimitiveUnsignedInteger<Byte> BYTE = new PrimitiveUnsignedInteger<>(1, (byte) 0xFF);
	public static final Primitive<Boolean> BOOLEAN = new Primitive<>(1);
	public static final PrimitiveUnsignedInteger<Long> POINTER = new PrimitiveUnsignedInteger<>(Device.ARCH_BITS / 8, (1L << Device.ARCH_BITS) - 1);
	
	public static final PrimitiveUnsignedInteger<Byte> UINT8 = BYTE;
	public static final PrimitiveUnsignedInteger<Short> UINT16 = new PrimitiveUnsignedInteger<>(2, (short) 0xFFFF);
	public static final PrimitiveUnsignedInteger<Integer> UINT32 = new PrimitiveUnsignedInteger<>(4, 0xFFFFFFFF);
	public static final PrimitiveUnsignedInteger<Long> UINT64 = new PrimitiveUnsignedInteger<>(8, 0xFFFFFFFFFFFFFFFFL);
	
	public static final PrimitiveInteger<Byte> INT8 = new PrimitiveInteger<>(1, (byte) 0x80, (byte) 0x7F, (byte) 0xFF, (byte) 0x7F);
	public static final PrimitiveInteger<Short> INT16 = new PrimitiveInteger<>(2, (short) 0x8000, (short) 0x7FFF, (short) 0xFFFF, (short) 0x7FFF);
	public static final PrimitiveInteger<Integer> INT32 = new PrimitiveInteger<>(4, 0x80000000, 0x7FFFFFFF, 0x7FFFFFFF, 0xFF);
	public static final PrimitiveInteger<Long> INT64 = new PrimitiveInteger<>(8, 0x8000000000000000L, 0x7FFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFFL, 0x7FFFFFFFFFFFFFFFL);
	
	public static final PrimitiveFloatingPoint<Float> FP32 = new PrimitiveFloatingPoint<>(4, 0, Float.intBitsToFloat(0x80000000), Float.intBitsToFloat(0x7FFFFFFF), 0, 23, Float.intBitsToFloat(0x007FFFFF), 23, 8, Float.intBitsToFloat(0x7F800000));
	public static final PrimitiveFloatingPoint<Double> FP64 = new PrimitiveFloatingPoint<>(8, 0, Double.longBitsToDouble(0x8000000000000000L), Double.longBitsToDouble(0x7FFFFFFFFFFFFFFFL), 0, 52, Double.longBitsToDouble(0x000FFFFFFFFFFFFFL), 52, 11, Double.longBitsToDouble(0x7FF0000000000000L));
}
