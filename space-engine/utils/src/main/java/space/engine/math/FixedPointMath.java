package space.engine.math;

public class FixedPointMath {
	
	public static final int maskBit31 = 0x80000000;
	public static final long maskBit32 = 0x100000000L;
	public static final long maskBit63 = 0x8000000000000000L;
	
	public static int add31(int a, int b, int[] overflow) {
		int r = a + b;
		if (((a ^ r) & (b ^ r)) < 0)
			overflow[0] += (r & maskBit31) == 0 ? -1 : 1;
		return r;
	}
	
	public static int sub31(int a, int b, int[] overflow) {
		int r = a - b;
		if (((a ^ b) & (a ^ r)) < 0)
			overflow[0] += (r & maskBit31) == 0 ? -1 : 1;
		return r;
	}
	
	public static int add32(int a, int b, int[] overflow) {
		long r = (long) a + b;
		if (((a ^ r) & (b ^ r)) < 0)
			overflow[0] += (r & maskBit32) == 0 ? -1 : 1;
		return (int) r;
	}
	
	public static int sub32(int a, int b, int[] overflow) {
		long r = (long) a - b;
		if (((a ^ b) & (a ^ r)) < 0)
			overflow[0] += (r & maskBit32) == 0 ? -1 : 1;
		return (int) r;
	}
	
	public static long add63(long a, long b, int[] overflow) {
		long r = a + b;
		if (((a ^ r) & (b ^ r)) < 0)
			overflow[0] += (r & maskBit63) == 0 ? -1 : 1;
		return r;
	}
	
	public static long sub63(long a, long b, int[] overflow) {
		long r = a - b;
		if (((a ^ b) & (a ^ r)) < 0)
			overflow[0] += (r & maskBit63) == 0 ? -1 : 1;
		return r;
	}
	
	public static long add64(long a, long b, int[] overflow) {
		int al = BigMath.getLower(a);
		int au = BigMath.getUpper(a);
		int bl = BigMath.getLower(b);
		int bu = BigMath.getUpper(b);
		
		long ret;
		
		long temp1 = maskBit32 + al + bl;
		ret = BigMath.getLower(temp1);
		
		long temp2 = maskBit32 + au + bu + (BigMath.getUpper(temp1) - 1);
		ret += temp2 << 32;
		
		overflow[0] += BigMath.getUpper(temp2) - 1;
		
		return ret;
	}
	
	public static long sub64(long a, long b, int[] overflow) {
		int al = BigMath.getLower(a);
		int au = BigMath.getUpper(a);
		int bl = BigMath.getLower(b);
		int bu = BigMath.getUpper(b);
		
		long ret;
		
		long temp1 = maskBit32 + al - bl;
		ret = BigMath.getLower(temp1);
		
		long temp2 = maskBit32 + au - bu + (BigMath.getUpper(temp1) - 1);
		ret += BigMath.toUpper(temp2);
		
		overflow[0] += BigMath.getUpper(temp2) - 1;
		
		return ret;
	}
}
