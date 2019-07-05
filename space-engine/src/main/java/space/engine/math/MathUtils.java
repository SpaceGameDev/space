package space.engine.math;

import java.util.Arrays;

@SuppressWarnings({"SameParameterValue", "unused"})
public class MathUtils {
	
	/**
	 * from Integer.DIGITS, but that is package-private...
	 */
	public final static char[] DIGITS = {
			//@formatter:off
			'0', '1', '2', '3', '4', '5', '6', '7', '8',
			'9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
			'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q',
			'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
			//@formatter:on
	};
	
	public static final double PI2 = 2 * Math.PI;
	public static final double sqrt2 = Math.sqrt(2);
	public static final double sqrt2inv = 1 / sqrt2;
	public static final double sqrt3 = Math.sqrt(3);
	public static final double sqrt3inv = 1 / sqrt3;
	public static final double sqrt2Div2 = sqrt2 / 2;
	public static final double sqrt2Div2inv = 1 / sqrt2Div2;

//	public static FPFormatter fpFormatter0 = new FPFormatterDefRounding(0);
//	public static FPFormatter fpFormatter2 = new FPFormatterDefRounding(2);
//	public static FPFormatter fpFormatter4 = new FPFormatterDefRounding(4);
//	public static FPFormatter fpFormatterInf = new FPFormatterDefRounding(Integer.MAX_VALUE);
//	public static FPFormatter fpFormatterDef = MathUtils.fpFormatter2;
//	public static FPFormatter fpFormatterAcc = MathUtils.fpFormatter4;
//
//	public static IntFormatter intFormatter0 = new IntFormatterJava();
//	public static IntFormatter intFormatter2Spaces = new IntFormatterSpaces(intFormatter0, 2, ' ');
//	public static IntFormatter intFormatter4Spaces = new IntFormatterSpaces(intFormatter0, 4, ' ');
//	public static IntFormatter intFormatter2Zero = new IntFormatterSpaces(intFormatter0, 2, '0');
//	public static IntFormatter intFormatter4Zero = new IntFormatterSpaces(intFormatter0, 4, '0');
//	public static IntFormatter intFormatterHex = new IntFormatterHex();
//	public static IntFormatter intFormatterDef = new IntFormatterDef();
	
	public static int compare(byte a, int b) {
		return a == b ? 0 : compareNotEqual(a, b);
	}
	
	public static int compare(short a, short b) {
		return a == b ? 0 : compareNotEqual(a, b);
	}
	
	public static int compare(int a, int b) {
		return a == b ? 0 : compareNotEqual(a, b);
	}
	
	public static int compare(long a, long b) {
		return a == b ? 0 : compareNotEqual(a, b);
	}
	
	public static int compare(float a, float b) {
		return a == b ? 0 : compareNotEqual(a, b);
	}
	
	public static int compare(double a, double b) {
		return a == b ? 0 : compareNotEqual(a, b);
	}
	
	public static int compareNotEqual(byte a, byte b) {
		return a > b ? 1 : -1;
	}
	
	public static int compareNotEqual(short a, short b) {
		return a > b ? 1 : -1;
	}
	
	public static int compareNotEqual(int a, int b) {
		return a > b ? 1 : -1;
	}
	
	public static int compareNotEqual(long a, long b) {
		return a > b ? 1 : -1;
	}
	
	public static int compareNotEqual(float a, float b) {
		return a > b ? 1 : -1;
	}
	
	public static int compareNotEqual(double a, double b) {
		return a > b ? 1 : -1;
	}
	
	public static byte clamp(byte v, byte min, byte max) {
		return v < min ? min : v > max ? max : v;
	}
	
	public static short clamp(short v, short min, short max) {
		return v < min ? min : v > max ? max : v;
	}
	
	public static int clamp(int v, int min, int max) {
		return v < min ? min : v > max ? max : v;
	}
	
	public static long clamp(long v, long min, long max) {
		return v < min ? min : v > max ? max : v;
	}
	
	public static float clamp(float v, float min, float max) {
		return v < min ? min : v > max ? max : v;
	}
	
	public static double clamp(double v, double min, double max) {
		return v < min ? min : v > max ? max : v;
	}
	
	public static byte abs(byte v) {
		return v < 0 ? (byte) -v : v;
	}
	
	public static short abs(short v) {
		return v < 0 ? (short) -v : v;
	}
	
	public static int abs(int v) {
		return v < 0 ? -v : v;
	}
	
	public static long abs(long v) {
		return v < 0 ? -v : v;
	}
	
	public static float abs(float v) {
		return v < 0 ? -v : v;
	}
	
	public static double abs(double v) {
		return v < 0 ? -v : v;
	}
	
	public static byte max(byte x, byte y) {
		return x > y ? x : y;
	}
	
	public static short max(short x, short y) {
		return x > y ? x : y;
	}
	
	public static int max(int x, int y) {
		return x > y ? x : y;
	}
	
	public static long max(long x, long y) {
		return x > y ? x : y;
	}
	
	public static float max(float x, float y) {
		return x > y ? x : y;
	}
	
	public static double max(double x, double y) {
		return x > y ? x : y;
	}
	
	public static byte min(byte x, byte y) {
		return x < y ? x : y;
	}
	
	public static short min(short x, short y) {
		return x < y ? x : y;
	}
	
	public static int min(int x, int y) {
		return x < y ? x : y;
	}
	
	public static long min(long x, long y) {
		return x < y ? x : y;
	}
	
	public static float min(float x, float y) {
		return x < y ? x : y;
	}
	
	public static double min(double x, double y) {
		return x < y ? x : y;
	}
	
	public static byte square(byte v) {
		return (byte) (v * v);
	}
	
	public static short square(short v) {
		return (short) (v * v);
	}
	
	public static int square(int v) {
		return v * v;
	}
	
	public static long square(long v) {
		return v * v;
	}
	
	public static float square(float v) {
		return v * v;
	}
	
	public static double square(double v) {
		return v * v;
	}
	
	public static float floor(float v) {
		return (float) Math.floor(v);
	}
	
	public static double floor(double v) {
		return Math.floor(v);
	}
	
	public static float ceil(float v) {
		return (float) Math.ceil(v);
	}
	
	public static double ceil(double v) {
		return Math.ceil(v);
	}
	
	public static byte pow(byte base, byte exp) {
		byte result = 1;
		while (exp != 0) {
			if ((exp & 1) != 0)
				result *= base;
			exp >>= 1;
			base *= base;
		}
		return result;
	}
	
	public static short pow(short base, short exp) {
		short result = 1;
		while (exp != 0) {
			if ((exp & 1) != 0)
				result *= base;
			exp >>= 1;
			base *= base;
		}
		return result;
	}
	
	public static int pow(int base, int exp) {
		int result = 1;
		while (exp != 0) {
			if ((exp & 1) != 0)
				result *= base;
			exp >>= 1;
			base *= base;
		}
		return result;
	}
	
	public static long pow(long base, long exp) {
		long result = 1;
		while (exp != 0) {
			if ((exp & 1) != 0)
				result *= base;
			exp >>= 1;
			base *= base;
		}
		return result;
	}
	
	public static int getMultiplyOf(int... numbers) {
		int ret = 1;
		Arrays.sort(numbers);
		for (int number : numbers)
			if (ret % number != 0)
				ret *= number;
		return ret;
	}
	
	public static long getMultiplyOf(long... numbers) {
		long ret = 1;
		Arrays.sort(numbers);
		for (long number : numbers)
			if (ret % number != 0)
				ret *= number;
		return ret;
	}
}
