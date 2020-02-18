package space.engine;

import java.lang.reflect.Array;
import java.util.Arrays;

import static java.lang.System.arraycopy;

public class FlagUtil {
	
	//int
	public static int initFlag(int flag) {
		return flag;
	}
	
	public static int addFlag(int flag, int add) {
		return flag | add;
	}
	
	public static int removeFlag(int flag, int remove) {
		return flag & ~remove;
	}
	
	public static boolean hasFlag(int flag, int type) {
		return (flag & type) != 0;
	}
	
	public static int initFlag(int... flag) {
		return addFlag(0, flag);
	}
	
	public static int addFlag(int flag, int... add) {
		for (int i : add)
			flag |= i;
		return flag;
	}
	
	public static int removeFlag(int flag, int... remove) {
		return flag & ~initFlag(remove);
	}
	
	public static boolean hasFlagAll(int flag, int... type) {
		int sieve = initFlag(type);
		return (flag & sieve) == sieve;
	}
	
	public static boolean hasFlagAny(int flag, int... type) {
		return (flag & initFlag(type)) != 0;
	}
	
	public static int sieve(int flag, int sieve) {
		return flag & sieve;
	}
	
	//long
	public static long initFlag(long flag) {
		return flag;
	}
	
	public static long addFlag(long flag, long add) {
		return flag | add;
	}
	
	public static long removeFlag(long flag, long remove) {
		return flag & ~remove;
	}
	
	public static boolean hasFlag(long flag, long type) {
		return (flag & type) != 0;
	}
	
	public static long initFlag(long... flag) {
		return addFlag(0, flag);
	}
	
	public static long addFlag(long flag, long... add) {
		for (long i : add)
			flag |= i;
		return flag;
	}
	
	public static long removeFlag(long flag, long... remove) {
		return flag & ~initFlag(remove);
	}
	
	public static boolean hasFlagAll(long flag, long... type) {
		long sieve = initFlag(type);
		return (flag & sieve) == sieve;
	}
	
	public static boolean hasFlagAny(long flag, long... type) {
		return (flag & initFlag(type)) != 0;
	}
	
	public static long sieve(long flag, long sieve) {
		return flag & sieve;
	}
	
	//int[]
	public static int[] initFlagPointer(int flag) {
		return new int[] {flag};
	}
	
	public static int[] addFlagPointer(int[] flag, int add) {
		flag[0] |= add;
		return flag;
	}
	
	public static int[] removeFlagPointer(int[] flag, int remove) {
		flag[0] &= ~remove;
		return flag;
	}
	
	public static boolean hasFlagPointer(int[] flag, int type) {
		return (flag[0] & type) != 0;
	}
	
	public static int[] initFlagPointer(int... flag) {
		return flag;
	}
	
	public static int[] addFlagPointer(int[] flag, int... add) {
		for (int i : add)
			flag[0] |= i;
		return flag;
	}
	
	public static int[] removeFlagPointer(int[] flag, int... remove) {
		flag[0] &= ~initFlag(remove);
		return flag;
	}
	
	public static boolean hasFlagAllPointer(int[] flag, int... type) {
		int sieve = initFlag(type);
		return (flag[0] & sieve) == sieve;
	}
	
	public static boolean hasFlagAnyPointer(int[] flag, int... type) {
		return (flag[0] & initFlag(type)) != 0;
	}
	
	public static int sieve(int[] flag, int sieve) {
		return flag[0] & sieve;
	}
	
	//long[]
	public static long[] initFlagPointer(long flag) {
		return new long[] {flag};
	}
	
	public static long[] addFlagPointer(long[] flag, long add) {
		flag[0] |= add;
		return flag;
	}
	
	public static long[] removeFlagPointer(long[] flag, long remove) {
		flag[0] &= ~remove;
		return flag;
	}
	
	public static boolean hasFlagPointer(long[] flag, long type) {
		return (flag[0] & type) != 0;
	}
	
	public static long[] initFlagPointer(long... flag) {
		return flag;
	}
	
	public static long[] addFlagPointer(long[] flag, long... add) {
		for (long i : add)
			flag[0] |= i;
		return flag;
	}
	
	public static long[] removeFlagPointer(long[] flag, long... remove) {
		flag[0] &= ~initFlag(remove);
		return flag;
	}
	
	public static boolean hasFlagAllPointer(long[] flag, long... type) {
		long sieve = initFlag(type);
		return (flag[0] & sieve) == sieve;
	}
	
	public static boolean hasFlagAnyPointer(long[] flag, long... type) {
		return (flag[0] & initFlag(type)) != 0;
	}
	
	public static long sieve(long[] flag, long sieve) {
		return flag[0] & sieve;
	}
	
	//T[]
	public static <T> boolean hasFlag(T[] flag, T type) {
		return Arrays.binarySearch(flag, type) != -1;
	}
	
	@SafeVarargs
	public static <T> T[] initFlag(T... flag) {
		return flag;
	}
	
	@SafeVarargs
	@SuppressWarnings("unchecked")
	public static <T> T[] addFlag(T[] flag, T... add) {
		T[] ret = (T[]) Array.newInstance(flag.getClass(), flag.length + add.length);
		arraycopy(flag, 0, ret, 0, flag.length);
		arraycopy(add, 0, ret, flag.length, add.length);
		return ret;
	}
	
	@SafeVarargs
	@SuppressWarnings("unchecked")
	public static <T> T[] removeFlag(T[] flag, T... remove) {
		boolean[] p = new boolean[flag.length];
		int existingCnt = 0;
		for (int i = 0; i < flag.length; i++)
			if (p[i] = Arrays.binarySearch(remove, flag[i]) != -1)
				existingCnt++;
		
		T[] ret = (T[]) Array.newInstance(flag.getClass(), existingCnt);
		int index = 0;
		for (int i = 0; i < flag.length; i++)
			if (p[i])
				ret[index++] = flag[i];
		
		return ret;
	}
	
	@SafeVarargs
	public static <T> boolean hasFlagAll(T[] flag, T... type) {
		for (T t : type)
			if (!hasFlag(flag, t))
				return false;
		return true;
	}
	
	@SafeVarargs
	public static <T> boolean hasFlagAny(T[] flag, T... type) {
		for (T t : type)
			if (hasFlag(flag, t))
				return true;
		return false;
	}
	
	@SafeVarargs
	public static <T> T[] sieve(T[] flag, T... sieve) {
		return removeFlag(flag, sieve);
	}
}
