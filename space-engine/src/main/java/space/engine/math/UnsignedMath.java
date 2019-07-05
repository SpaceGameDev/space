package space.engine.math;

import space.engine.primitive.Primitives;

@SuppressWarnings("SameParameterValue")
public class UnsignedMath {
	
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
		return (short) (a & 0xFF);
	}
	
	public static short toShort(int a) {
		return (short) a;
	}
	
	public static short toShort(long a) {
		return (short) a;
	}
	
	public static int toInt(byte a) {
		return a & 0xFF;
	}
	
	public static int toInt(short a) {
		return a & 0xFFFF;
	}
	
	public static int toInt(long a) {
		return (int) a;
	}
	
	public static long toLong(byte a) {
		return a & 0xFFL;
	}
	
	public static long toLong(short a) {
		return a & 0xFFFFL;
	}
	
	public static long toLong(int a) {
		return a & 0xFFFFFFFFL;
	}
	
	//divide
	public static byte divide(byte dividend, byte divisor) {
		return (byte) (toShort(dividend) / toShort(divisor));
	}
	
	public static short divide(short dividend, short divisor) {
		return (short) (toInt(dividend) / toInt(divisor));
	}
	
	public static int divide(int dividend, int divisor) {
		return (int) (toLong(dividend) / toLong(divisor));
	}
	
	public static long divide(long dividend, long divisor) {
		return Long.divideUnsigned(dividend, divisor);
	}
	
	//remainder
	public static byte remainder(byte dividend, byte divisor) {
		return (byte) (toShort(dividend) % toShort(divisor));
	}
	
	public static short remainder(short dividend, short divisor) {
		return (short) (toInt(dividend) % toInt(divisor));
	}
	
	public static int remainder(int dividend, int divisor) {
		return (int) (toLong(dividend) % toLong(divisor));
	}
	
	public static long remainder(long dividend, long divisor) {
		return Long.remainderUnsigned(dividend, divisor);
	}
	
	//compare
	public static int compare(byte x, byte y) {
		return SignedMath.compare(x + Primitives.INT8.signMask, y + Primitives.INT8.signMask);
	}
	
	public static int compare(short x, short y) {
		return SignedMath.compare(x + Primitives.INT16.signMask, y + Primitives.INT16.signMask);
	}
	
	public static int compare(int x, int y) {
		return SignedMath.compare(x + Primitives.INT32.signMask, y + Primitives.INT32.signMask);
	}
	
	public static int compare(long x, long y) {
		return SignedMath.compare(x + Primitives.INT64.signMask, y + Primitives.INT64.signMask);
	}
	
	//isBiggerEquals x >= y
	public static boolean isBiggerEquals(byte x, byte y) {
		return x + Primitives.INT8.signMask >= y + Primitives.INT8.signMask;
	}
	
	public static boolean isBiggerEquals(short x, short y) {
		return x + Primitives.INT16.signMask >= y + Primitives.INT16.signMask;
	}
	
	public static boolean isBiggerEquals(int x, int y) {
		return x + Primitives.INT32.signMask >= y + Primitives.INT32.signMask;
	}
	
	public static boolean isBiggerEquals(long x, long y) {
		return x + Primitives.INT64.signMask >= y + Primitives.INT64.signMask;
	}
	
	//isBigger x > y
	public static boolean isBigger(byte x, byte y) {
		return x + Primitives.INT8.signMask > y + Primitives.INT8.signMask;
	}
	
	public static boolean isBigger(short x, short y) {
		return x + Primitives.INT16.signMask > y + Primitives.INT16.signMask;
	}
	
	public static boolean isBigger(int x, int y) {
		return x + Primitives.INT32.signMask > y + Primitives.INT32.signMask;
	}
	
	public static boolean isBigger(long x, long y) {
		return x + Primitives.INT64.signMask > y + Primitives.INT64.signMask;
	}
	
	//equals x == y
	public static boolean equals(byte x, byte y) {
		return x + Primitives.INT8.signMask == y + Primitives.INT8.signMask;
	}
	
	public static boolean equals(short x, short y) {
		return x + Primitives.INT16.signMask == y + Primitives.INT16.signMask;
	}
	
	public static boolean equals(int x, int y) {
		return x + Primitives.INT32.signMask == y + Primitives.INT32.signMask;
	}
	
	public static boolean equals(long x, long y) {
		return x + Primitives.INT64.signMask == y + Primitives.INT64.signMask;
	}
	
	//notEquals x != y
	public static boolean notEquals(byte x, byte y) {
		return x + Primitives.INT8.signMask != y + Primitives.INT8.signMask;
	}
	
	public static boolean notEquals(short x, short y) {
		return x + Primitives.INT16.signMask != y + Primitives.INT16.signMask;
	}
	
	public static boolean notEquals(int x, int y) {
		return x + Primitives.INT32.signMask != y + Primitives.INT32.signMask;
	}
	
	public static boolean notEquals(long x, long y) {
		return x + Primitives.INT64.signMask != y + Primitives.INT64.signMask;
	}
	
	//isLess x < y
	public static boolean isLess(byte x, byte y) {
		return x + Primitives.INT8.signMask < y + Primitives.INT8.signMask;
	}
	
	public static boolean isLess(short x, short y) {
		return x + Primitives.INT16.signMask < y + Primitives.INT16.signMask;
	}
	
	public static boolean isLess(int x, int y) {
		return x + Primitives.INT32.signMask < y + Primitives.INT32.signMask;
	}
	
	public static boolean isLess(long x, long y) {
		return x + Primitives.INT64.signMask < y + Primitives.INT64.signMask;
	}
	
	//isLessEquals x <= y
	public static boolean isLessEquals(byte x, byte y) {
		return x + Primitives.INT8.signMask <= y + Primitives.INT8.signMask;
	}
	
	public static boolean isLessEquals(short x, short y) {
		return x + Primitives.INT16.signMask <= y + Primitives.INT16.signMask;
	}
	
	public static boolean isLessEquals(int x, int y) {
		return x + Primitives.INT32.signMask <= y + Primitives.INT32.signMask;
	}
	
	public static boolean isLessEquals(long x, long y) {
		return x + Primitives.INT64.signMask <= y + Primitives.INT64.signMask;
	}
	
	//powerOfTwo is
	public static boolean isPowerOfTwo(byte x) {
		return x != 0 && (x & (x - 1)) == 0;
	}
	
	public static boolean isPowerOfTwo(short x) {
		return x != 0 && (x & (x - 1)) == 0;
	}
	
	public static boolean isPowerOfTwo(int x) {
		return x != 0 && (x & (x - 1)) == 0;
	}
	
	public static boolean isPowerOfTwo(long x) {
		return x != 0 && (x & (x - 1)) == 0;
	}
	
	//powerOfTwo is fast
	public static boolean isPowerOfTwoFast(byte x) {
		return x == 1 || x == 2 || x == 4 || x == 8 || x == 16 || isPowerOfTwo(x);
	}
	
	public static boolean isPowerOfTwoFast(short x) {
		return x == 1 || x == 2 || x == 4 || x == 8 || x == 16 || isPowerOfTwo(x);
	}
	
	public static boolean isPowerOfTwoFast(int x) {
		return x == 1 || x == 2 || x == 4 || x == 8 || x == 16 || isPowerOfTwo(x);
	}
	
	public static boolean isPowerOfTwoFast(long x) {
		return x == 1 || x == 2 || x == 4 || x == 8 || x == 16 || isPowerOfTwo(x);
	}
	
	//powerOfTwo get floor
	public static int getPowerOfTwoFloor(byte x) {
		return 7 - numberOfLeadingZeros(x);
	}
	
	public static int getPowerOfTwoFloor(short x) {
		return 15 - numberOfLeadingZeros(x);
	}
	
	public static int getPowerOfTwoFloor(int x) {
		return 31 - numberOfLeadingZeros(x);
	}
	
	public static int getPowerOfTwoFloor(long x) {
		return 63 - numberOfLeadingZeros(x);
	}
	
	//powerOfTwo get ceil
	public static int getPowerOfTwoCeil(byte x) {
		return 8 - numberOfLeadingZeros(x - 1);
	}
	
	public static int getPowerOfTwoCeil(short x) {
		return 16 - numberOfLeadingZeros(x - 1);
	}
	
	public static int getPowerOfTwoCeil(int x) {
		return 32 - numberOfLeadingZeros(x - 1);
	}
	
	public static int getPowerOfTwoCeil(long x) {
		return 64 - numberOfLeadingZeros(x - 1);
	}
	
	//powerOfTwo get floor fast
	public static int getPowerOfTwoFloorFast(byte x) {
		return x == 1 ? 0 : x == 2 ? 1 : x == 4 ? 2 : x == 8 ? 3 : x == 16 ? 4 : getPowerOfTwoFloor(x);
	}
	
	public static int getPowerOfTwoFloorFast(short x) {
		return x == 1 ? 0 : x == 2 ? 1 : x == 4 ? 2 : x == 8 ? 3 : x == 16 ? 4 : getPowerOfTwoFloor(x);
	}
	
	public static int getPowerOfTwoFloorFast(int x) {
		return x == 1 ? 0 : x == 2 ? 1 : x == 4 ? 2 : x == 8 ? 3 : x == 16 ? 4 : getPowerOfTwoFloor(x);
	}
	
	public static int getPowerOfTwoFloorFast(long x) {
		return x == 1 ? 0 : x == 2 ? 1 : x == 4 ? 2 : x == 8 ? 3 : x == 16 ? 4 : getPowerOfTwoFloor(x);
	}
	
	//powerOfTwo get ceil fast
	public static int getPowerOfTwoCeilFast(byte x) {
		return x == 1 ? 0 : x == 2 ? 1 : x == 4 ? 2 : x == 8 ? 3 : x == 16 ? 4 : getPowerOfTwoCeil(x);
	}
	
	public static int getPowerOfTwoCeilFast(short x) {
		return x == 1 ? 0 : x == 2 ? 1 : x == 4 ? 2 : x == 8 ? 3 : x == 16 ? 4 : getPowerOfTwoCeil(x);
	}
	
	public static int getPowerOfTwoCeilFast(int x) {
		return x == 1 ? 0 : x == 2 ? 1 : x == 4 ? 2 : x == 8 ? 3 : x == 16 ? 4 : getPowerOfTwoCeil(x);
	}
	
	public static int getPowerOfTwoCeilFast(long x) {
		return x == 1 ? 0 : x == 2 ? 1 : x == 4 ? 2 : x == 8 ? 3 : x == 16 ? 4 : getPowerOfTwoCeil(x);
	}
	
	//numberOfLeadingZeros
	public static int numberOfLeadingZeros(byte x) {
		if (x == 0)
			return 8;
		int n = 1;
		
		if (x >>> 4 == 0) {
			n += 4;
			x <<= 4;
		}
		if (x >>> 6 == 0) {
			n += 2;
			x <<= 2;
		}
		
		n -= x >>> 7;
		return n;
	}
	
	public static int numberOfLeadingZeros(short x) {
		if (x == 0)
			return 16;
		int n = 1;
		
		if (x >>> 8 == 0) {
			n += 8;
			x <<= 8;
		}
		if (x >>> 12 == 0) {
			n += 4;
			x <<= 4;
		}
		if (x >>> 14 == 0) {
			n += 2;
			x <<= 2;
		}
		
		n -= x >>> 15;
		return n;
	}
	
	public static int numberOfLeadingZeros(int x) {
		return Integer.numberOfLeadingZeros(x);
	}
	
	public static int numberOfLeadingZeros(long x) {
		return Long.numberOfLeadingZeros(x);
	}
	
	//casts to fp
	public static float toFloatUnsigned(byte i) {
		float ret = (float) (i & Primitives.INT8.numberMask);
		if ((i & Primitives.INT8.signMask) != 0)
			ret += 0x1P7f;
		return ret;
	}
	
	public static float toFloatUnsigned(short i) {
		float ret = (float) (i & Primitives.INT16.numberMask);
		if ((i & Primitives.INT16.signMask) != 0)
			ret += 0x1P15f;
		return ret;
	}
	
	public static float toFloatUnsigned(int i) {
		float ret = (float) (i & Primitives.INT32.numberMask);
		if ((i & Primitives.INT32.signMask) != 0)
			ret += 0x1P31f;
		return ret;
	}
	
	public static float toFloatUnsigned(long i) {
		float ret = (float) (i & Primitives.INT64.numberMask);
		if ((i & Primitives.INT64.signMask) != 0)
			ret += 0x1P63f;
		return ret;
	}
	
	public static double toDoubleUnsigned(byte i) {
		double ret = (double) (i & Primitives.INT8.numberMask);
		if ((i & Primitives.INT8.signMask) != 0)
			ret += 0x1P7d;
		return ret;
	}
	
	public static double toDoubleUnsigned(short i) {
		double ret = (double) (i & Primitives.INT16.numberMask);
		if ((i & Primitives.INT16.signMask) != 0)
			ret += 0x1P15d;
		return ret;
	}
	
	public static double toDoubleUnsigned(int i) {
		double ret = (double) (i & Primitives.INT32.numberMask);
		if ((i & Primitives.INT32.signMask) != 0)
			ret += 0x1P31d;
		return ret;
	}
	
	public static double toDoubleUnsigned(long i) {
		double ret = (double) (i & Primitives.INT64.numberMask);
		if ((i & Primitives.INT64.signMask) != 0)
			ret += 0x1P63d;
		return ret;
	}
}
