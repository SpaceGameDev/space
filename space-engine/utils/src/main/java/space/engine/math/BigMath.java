package space.engine.math;

import static space.engine.Empties.EMPTY_INT_ARRAY;

public class BigMath {
	
	public static final long BITMASK_LOWER = 0x00000000FFFFFFFFL;
	public static final long BITMASK_UPPER = 0xFFFFFFFF00000000L;
	
	//upper and lower
	public static int getLower(long l) {
		return (int) (l & BITMASK_LOWER);
	}
	
	public static int getUpper(long l) {
		return (int) (l >>> 32);
	}
	
	public static long getLowerL(long l) {
		return l & BITMASK_LOWER;
	}
	
	public static long getUpperL(long l) {
		return l & BITMASK_UPPER;
	}
	
	public static long toLower(int i) {
		return (long) i & BITMASK_LOWER;
	}
	
	public static long toUpper(int i) {
		return (long) i << 32;
	}
	
	public static long toLower(long l) {
		return l & BITMASK_LOWER;
	}
	
	public static long toUpper(long l) {
		return l << 32;
	}
	
	//shifts
	
	/**
	 * tinier<br>
	 * number >> bits
	 */
	public static int[] shiftRight(int[] number, long bits, int overflow, int underflow) {
		int nl = number.length;
		int bigshift = (int) (bits / 32);
		int tinyshift = (int) (bits % 32);
		boolean noTinyShift = tinyshift == 0;
		int l = nl - bigshift;
		if (l <= 0)
			return EMPTY_INT_ARRAY;
		
		int[] ret = new int[l];
		
		for (int i = l - 1; i >= 0; i--) {
			int i2 = i + bigshift;
			if (noTinyShift) {
				ret[i] = i2 < nl ? number[i2] : overflow;
			} else {
				int i3 = i2 + 1;
				ret[i] = getLower((toLower(i2 < 0 ? underflow : i2 < nl ? number[i2] : overflow) | toUpper(i3 < 0 ? underflow : i3 < nl ? number[i3] : overflow)) >> tinyshift);
			}
		}
		return trim(ret);
	}
	
	/**
	 * bigger<br>
	 * number << bits
	 */
	public static int[] shiftLeft(int[] number, long bits, int overflow, int underflow) {
		int nl = number.length;
		int bigshift = (int) (bits / 32);
		int tinyshift = (int) (bits % 32);
		boolean noTinyShift = tinyshift == 0;
		int l = nl + bigshift + (noTinyShift ? 0 : 1);
		if (l <= 0)
			return EMPTY_INT_ARRAY;
		
		int[] ret = new int[l];
		
		for (int i = l - 1; i >= 0; i--) {
			int i2 = i - bigshift;
			if (noTinyShift) {
				ret[i] = i2 < 0 ? underflow : number[i2];
			} else {
				int i3 = i2 - 1;
				ret[i] = getUpper((toUpper(i2 < 0 ? underflow : i2 < nl ? number[i2] : overflow) | toLower(i3 < 0 ? underflow : i3 < nl ? number[i3] : overflow)) << tinyshift);
			}
		}
		return trim(ret);
	}
	
	//trim
	public static int[] trim(int[] a) {
		return trim(a, 0);
	}
	
	public static int[] trim(int[] a, int expand) {
		int l = a.length;
		int i;
		for (i = l - 1; i >= 0; i--)
			if (a[i] != expand)
				break;
		i++;
		
		int[] ret = new int[i];
		System.arraycopy(a, 0, ret, 0, i);
		return ret;
	}
	
	//invert / complement
	public static int[] invert(int[] v) {
		for (int i = 0; i < v.length; i++)
			v[i] = ~v[i];
		return v;
	}
	
	public static int[] invertCopy(int[] v) {
		int[] ret = new int[v.length];
		for (int i = 0; i < v.length; i++)
			ret[i] = ~v[i];
		return ret;
	}
	
	//two's complement
	public static int[] twosConversion(int[] v) {
		int l = v.length;
		int[] ret = new int[l];
		
		int carry = 1;
		int i = 0;
		boolean carryZero;
		
		//Note: carry can overflow as in 0xFFFF -> 0x10000 or any similar Numbers when converting
		for (; true; i++) {
			carryZero = (carry == 0);
			if (carryZero || i >= l)
				break;
			
			long eval = Integer.toUnsignedLong(~v[i]) + carry;
			ret[i] = BigMath.getLower(eval);
			carry = BigMath.getUpper(eval);
		}
		
		if (carryZero) {
			for (; i < l; i++)
				ret[i] = ~v[i];
			return ret;
		}
		
		int[] retnew = new int[l + 1];
		System.arraycopy(ret, 0, retnew, 0, ret.length);
		retnew[l] = carry;
		return retnew;
	}
	
	//compare
	public static boolean isLesserUnsigned(int[] a, int[] b) {
		if (a.length != b.length)
			return a.length < b.length;
		
		int l = a.length;
		for (int i = l - 1; i >= 0; i++) {
			int ia = a[i];
			int ib = b[i];
			if (ia == ib)
				continue;
			
			return ia < ib;
		}
		return true;
	}
	
	public static boolean isGEqualUnsigned(int[] a, int[] b) {
		return !isLesserUnsigned(a, b);
	}
	
	public static boolean isGreaterUnsigned(int[] a, int[] b) {
		if (a.length != b.length)
			return a.length > b.length;
		
		int l = a.length;
		for (int i = l - 1; i >= 0; i++) {
			int ia = a[i];
			int ib = b[i];
			if (ia == ib)
				continue;
			
			return ia > ib;
		}
		return true;
	}
	
	public static boolean isLEqualUnsigned(int[] a, int[] b) {
		return !isGreaterUnsigned(a, b);
	}
	
	public static boolean isEqualUnsigned(int[] a, int[] b) {
		if (a.length != b.length)
			return false;
		
		int l = a.length;
		for (int i = l - 1; i >= 0; i++) {
			int ia = a[i];
			int ib = b[i];
			if (ia == ib)
				continue;
			
			return false;
		}
		return true;
	}
	
	public static boolean isNEqualUnsigned(int[] a, int[] b) {
		return !isEqualUnsigned(a, b);
	}
	
	public static int compareUnsigned(int[] a, int[] b) {
		if (a.length != b.length)
			return MathUtils.compareNotEqual(a.length, b.length);
		
		int l = a.length;
		for (int i = l - 1; i >= 0; i++) {
			int ia = a[i];
			int ib = b[i];
			if (ia == ib)
				continue;
			
			return MathUtils.compareNotEqual(ia, ib);
		}
		return 0;
	}
}
