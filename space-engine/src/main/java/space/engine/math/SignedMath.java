package space.engine.math;

@SuppressWarnings({"SameParameterValue", "UseCompareMethod"})
public class SignedMath {
	
	//casts
	public static byte toByte(short a) {
		return (byte) a;
	}
	
	public static byte toByte(int a) {
		return (byte) a;
	}
	
	public static byte toByte(long a) {
		return (byte) a;
	}
	
	public static short toShort(byte a) {
		return (short) a;
	}
	
	public static short toShort(int a) {
		return (short) a;
	}
	
	public static short toShort(long a) {
		return (short) a;
	}
	
	public static int toInt(byte a) {
		return a;
	}
	
	public static int toInt(short a) {
		return a;
	}
	
	public static int toInt(long a) {
		return (int) a;
	}
	
	public static long toLong(byte a) {
		return a;
	}
	
	public static long toLong(short a) {
		return a;
	}
	
	public static long toLong(int a) {
		return a;
	}
	
	//divide
	public static byte divide(byte dividend, byte divisor) {
		return (byte) (dividend / divisor);
	}
	
	public static short divide(short dividend, short divisor) {
		return (short) (dividend / divisor);
	}
	
	public static int divide(int dividend, int divisor) {
		return dividend / divisor;
	}
	
	public static long divide(long dividend, long divisor) {
		return dividend / divisor;
	}
	
	//remainder
	public static byte remainder(byte dividend, byte divisor) {
		return (byte) (dividend % divisor);
	}
	
	public static short remainder(short dividend, short divisor) {
		return (short) (dividend % divisor);
	}
	
	public static int remainder(int dividend, int divisor) {
		return dividend % divisor;
	}
	
	public static long remainder(long dividend, long divisor) {
		return dividend % divisor;
	}
	
	//compare
	public static int compare(byte b1, byte b2) {
		return b1 > b2 ? 1 : b1 < b2 ? -1 : 0;
	}
	
	public static int compare(short b1, short b2) {
		return b1 > b2 ? 1 : b1 < b2 ? -1 : 0;
	}
	
	public static int compare(int b1, int b2) {
		return b1 > b2 ? 1 : b1 < b2 ? -1 : 0;
	}
	
	public static int compare(long b1, long b2) {
		return b1 > b2 ? 1 : b1 < b2 ? -1 : 0;
	}
	
	//isBiggerEquals x >= y
	public static boolean isBiggerEquals(byte x, byte y) {
		return x >= y;
	}
	
	public static boolean isBiggerEquals(short x, short y) {
		return x >= y;
	}
	
	public static boolean isBiggerEquals(int x, int y) {
		return x >= y;
	}
	
	public static boolean isBiggerEquals(long x, long y) {
		return x >= y;
	}
	
	//isBigger x > y
	public static boolean isBigger(byte x, byte y) {
		return x > y;
	}
	
	public static boolean isBigger(short x, short y) {
		return x > y;
	}
	
	public static boolean isBigger(int x, int y) {
		return x > y;
	}
	
	public static boolean isBigger(long x, long y) {
		return x > y;
	}
	
	//equals x == y
	public static boolean equals(byte x, byte y) {
		return x == y;
	}
	
	public static boolean equals(short x, short y) {
		return x == y;
	}
	
	public static boolean equals(int x, int y) {
		return x == y;
	}
	
	public static boolean equals(long x, long y) {
		return x == y;
	}
	
	//notEquals x != y
	public static boolean notEquals(byte x, byte y) {
		return x != y;
	}
	
	public static boolean notEquals(short x, short y) {
		return x != y;
	}
	
	public static boolean notEquals(int x, int y) {
		return x != y;
	}
	
	public static boolean notEquals(long x, long y) {
		return x != y;
	}
	
	//isLess x < y
	public static boolean isLess(byte x, byte y) {
		return x < y;
	}
	
	public static boolean isLess(short x, short y) {
		return x < y;
	}
	
	public static boolean isLess(int x, int y) {
		return x < y;
	}
	
	public static boolean isLess(long x, long y) {
		return x < y;
	}
	
	//isLessEquals x <= y
	public static boolean isLessEquals(byte x, byte y) {
		return x <= y;
	}
	
	public static boolean isLessEquals(short x, short y) {
		return x <= y;
	}
	
	public static boolean isLessEquals(int x, int y) {
		return x <= y;
	}
	
	public static boolean isLessEquals(long x, long y) {
		return x <= y;
	}
	
	//powerOfTwo is
	public static boolean isPowerOfTwo(byte x) {
		return x > 0 && (x & (x - 1)) == 0;
	}
	
	public static boolean isPowerOfTwo(short x) {
		return x > 0 && (x & (x - 1)) == 0;
	}
	
	public static boolean isPowerOfTwo(int x) {
		return x > 0 && (x & (x - 1)) == 0;
	}
	
	public static boolean isPowerOfTwo(long x) {
		return x > 0 && (x & (x - 1)) == 0;
	}
	
	//powerOfTwo get floor
	public static int getPowerOfTwoFloor(byte x) {
		return UnsignedMath.getPowerOfTwoFloor(MathUtils.abs(x));
	}
	
	public static int getPowerOfTwoFloor(short x) {
		return UnsignedMath.getPowerOfTwoFloor(MathUtils.abs(x));
	}
	
	public static int getPowerOfTwoFloor(int x) {
		return UnsignedMath.getPowerOfTwoFloor(MathUtils.abs(x));
	}
	
	public static int getPowerOfTwoFloor(long x) {
		return UnsignedMath.getPowerOfTwoFloor(MathUtils.abs(x));
	}
	
	//powerOfTwo get ceil
	public static int getPowerOfTwoCeil(byte x) {
		return UnsignedMath.getPowerOfTwoCeil(MathUtils.abs(x));
	}
	
	public static int getPowerOfTwoCeil(short x) {
		return UnsignedMath.getPowerOfTwoCeil(MathUtils.abs(x));
	}
	
	public static int getPowerOfTwoCeil(int x) {
		return UnsignedMath.getPowerOfTwoCeil(MathUtils.abs(x));
	}
	
	public static int getPowerOfTwoCeil(long x) {
		return UnsignedMath.getPowerOfTwoCeil(MathUtils.abs(x));
	}
	
	//powerOfTwo get floor fast
	public static int getPowerOfTwoFloorFast(byte x) {
		return UnsignedMath.getPowerOfTwoFloorFast(x);
	}
	
	public static int getPowerOfTwoFloorFast(short x) {
		return UnsignedMath.getPowerOfTwoFloorFast(x);
	}
	
	public static int getPowerOfTwoFloorFast(int x) {
		return UnsignedMath.getPowerOfTwoFloorFast(x);
	}
	
	public static int getPowerOfTwoFloorFast(long x) {
		return UnsignedMath.getPowerOfTwoFloorFast(x);
	}
	
	//powerOfTwo get ceil fast
	public static int getPowerOfTwoCeilFast(byte x) {
		return UnsignedMath.getPowerOfTwoCeilFast(x);
	}
	
	public static int getPowerOfTwoCeilFast(short x) {
		return UnsignedMath.getPowerOfTwoCeilFast(x);
	}
	
	public static int getPowerOfTwoCeilFast(int x) {
		return UnsignedMath.getPowerOfTwoCeilFast(x);
	}
	
	public static int getPowerOfTwoCeilFast(long x) {
		return UnsignedMath.getPowerOfTwoCeilFast(x);
	}
	
	//numberOfLeadingZeros
	public static int numberOfLeadingZeros(byte x) {
		return UnsignedMath.numberOfLeadingZeros(MathUtils.abs(x));
	}
	
	public static int numberOfLeadingZeros(short x) {
		return UnsignedMath.numberOfLeadingZeros(MathUtils.abs(x));
	}
	
	public static int numberOfLeadingZeros(int x) {
		return UnsignedMath.numberOfLeadingZeros(MathUtils.abs(x));
	}
	
	public static int numberOfLeadingZeros(long x) {
		return UnsignedMath.numberOfLeadingZeros(MathUtils.abs(x));
	}
	
	//casts to fp
	public static float toFloatUnsigned(byte i) {
		return (float) i;
	}
	
	public static float toFloatUnsigned(short i) {
		return (float) i;
	}
	
	public static float toFloatUnsigned(int i) {
		return (float) i;
	}
	
	public static float toFloatUnsigned(long i) {
		return (float) i;
	}
	
	public static double toDoubleUnsigned(byte i) {
		return (double) i;
	}
	
	public static double toDoubleUnsigned(short i) {
		return (double) i;
	}
	
	public static double toDoubleUnsigned(int i) {
		return (double) i;
	}
	
	public static double toDoubleUnsigned(long i) {
		return (double) i;
	}
}
