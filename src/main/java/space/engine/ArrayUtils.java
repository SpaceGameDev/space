package space.engine;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.ToIntBiFunction;

public class ArrayUtils {
	
	//hashCode31
	public static int hashCode31CacheSize = 16;
	public static int[] hashCode31Cache;
	
	static {
		hashCode31Cache = new int[hashCode31CacheSize];
		int result = 1;
		for (int i = 0; i < hashCode31CacheSize; i++)
			hashCode31Cache[i] = (result *= 31);
	}
	
	//getSafeOU
	public static byte getSafeOU(byte[] array, int index, byte overflow, byte underflow) {
		return index >= array.length ? overflow : index < 0 ? underflow : array[index];
	}
	
	public static short getSafeOU(short[] array, int index, short overflow, short underflow) {
		return index >= array.length ? overflow : index < 0 ? underflow : array[index];
	}
	
	public static int getSafeOU(int[] array, int index, int overflow, int underflow) {
		return index >= array.length ? overflow : index < 0 ? underflow : array[index];
	}
	
	public static long getSafeOU(long[] array, int index, long overflow, long underflow) {
		return index >= array.length ? overflow : index < 0 ? underflow : array[index];
	}
	
	public static float getSafeOU(float[] array, int index, float overflow, float underflow) {
		return index >= array.length ? overflow : index < 0 ? underflow : array[index];
	}
	
	public static double getSafeOU(double[] array, int index, double overflow, double underflow) {
		return index >= array.length ? overflow : index < 0 ? underflow : array[index];
	}
	
	public static char getSafeOU(char[] array, int index, char overflow, char underflow) {
		return index >= array.length ? overflow : index < 0 ? underflow : array[index];
	}
	
	public static boolean getSafeOU(boolean[] array, int index, boolean overflow, boolean underflow) {
		return index >= array.length ? overflow : index < 0 ? underflow : array[index];
	}
	
	public static <T> T getSafeOU(T[] array, int index, T overflow, T underflow) {
		return index >= array.length ? overflow : index < 0 ? underflow : array[index];
	}
	
	//getSafeO
	public static byte getSafeO(byte[] array, int index, byte overflow) {
		return index >= array.length ? overflow : array[index];
	}
	
	public static short getSafeO(short[] array, int index, short overflow) {
		return index >= array.length ? overflow : array[index];
	}
	
	public static int getSafeO(int[] array, int index, int overflow) {
		return index >= array.length ? overflow : array[index];
	}
	
	public static long getSafeO(long[] array, int index, long overflow) {
		return index >= array.length ? overflow : array[index];
	}
	
	public static float getSafeO(float[] array, int index, float overflow) {
		return index >= array.length ? overflow : array[index];
	}
	
	public static double getSafeO(double[] array, int index, double overflow) {
		return index >= array.length ? overflow : array[index];
	}
	
	public static char getSafeO(char[] array, int index, char overflow) {
		return index >= array.length ? overflow : array[index];
	}
	
	public static boolean getSafeO(boolean[] array, int index, boolean overflow) {
		return index >= array.length ? overflow : array[index];
	}
	
	public static <T> T getSafeO(T[] array, int index, T overflow) {
		return index >= array.length ? overflow : array[index];
	}
	
	//getSafeOU
	public static byte getSafeU(byte[] array, int index, byte underflow) {
		return index < 0 ? underflow : array[index];
	}
	
	public static short getSafeU(short[] array, int index, short underflow) {
		return index < 0 ? underflow : array[index];
	}
	
	public static int getSafeU(int[] array, int index, int underflow) {
		return index < 0 ? underflow : array[index];
	}
	
	public static long getSafeU(long[] array, int index, long underflow) {
		return index < 0 ? underflow : array[index];
	}
	
	public static float getSafeU(float[] array, int index, float underflow) {
		return index < 0 ? underflow : array[index];
	}
	
	public static double getSafeU(double[] array, int index, double underflow) {
		return index < 0 ? underflow : array[index];
	}
	
	public static char getSafeU(char[] array, int index, char underflow) {
		return index < 0 ? underflow : array[index];
	}
	
	public static boolean getSafeU(boolean[] array, int index, boolean underflow) {
		return index < 0 ? underflow : array[index];
	}
	
	public static <T> T getSafeU(T[] array, int index, T underflow) {
		return index < 0 ? underflow : array[index];
	}
	
	//hashCode
	public static int hashCode31Get(int index) {
		if (index < hashCode31Cache.length)
			return hashCode31Cache[index];
		
		int result = 1;
		for (int i = 0; i < index; i++)
			result += 31;
		return result;
	}
	
	public static <T> int hashCodeIgnoreSequence(T[] o) {
		int result = 0;
		for (T e : o)
			result ^= (e == null) ? 0 : e.hashCode();
		return result * hashCode31Get(o.length);
	}
	
	//ignore Sequence
	public static <T> boolean equalsIgnoreSequenceEquals(T[] o1, T[] o2) {
		return equalsIgnoreSequence(o1, o2, (o, e) -> {
			if (e == null)
				return -1;
			
			for (int i = 0; i < o.length; i++) {
				T e0 = o[i];
				if (e0 != null && !e0.equals(e))
					return i;
			}
			return -1;
		}, -1);
	}
	
	public static <T> boolean equalsIgnoreSequenceBinary(T[] o1, T[] o2) {
		return equalsIgnoreSequence(o1, o2, Arrays::binarySearch, -1);
	}
	
	public static <T> boolean equalsIgnoreSequence(T[] o1, T[] o2, ToIntBiFunction<T[], T> contains, int containsFalse) {
		if (o1 == o2)
			return true;
		if (o1 == null || o2 == null)
			return false;
		
		int l = o1.length;
		if (l != o2.length)
			return false;
		boolean[] b2 = new boolean[l];
		
		for (T e1 : o1) {
			int pos = contains.applyAsInt(o2, e1);
			if (pos == containsFalse) {
				return false;
			} else {
				b2[pos] = true;
			}
		}
		
		for (int i = 0; i < l; i++) {
			if (b2[i])
				continue;
			if (contains.applyAsInt(o1, o2[i]) == containsFalse)
				return false;
		}
		
		return true;
	}
	
	//optimal size
	public static int getOptimalArraySizeExpansion(int currSize, int requestedSize, int expandShift) {
		int newSizeOld = currSize << expandShift;
		return requestedSize >= newSizeOld ? requestedSize << expandShift : newSizeOld;
	}
	
	public static long getOptimalArraySizeExpansion(long currSize, long requestedSize, long expandShift) {
		long newSizeOld = currSize << expandShift;
		return requestedSize >= newSizeOld ? requestedSize << expandShift : newSizeOld;
	}
	
	public static int getOptimalArraySizeStart(int defaultSize, int requestedSize) {
		return requestedSize == 0 ? 0 : (requestedSize > defaultSize ? requestedSize : defaultSize);
	}
	
	public static long getOptimalArraySizeStart(long defaultSize, long requestedSize) {
		return requestedSize == 0 ? 0 : (requestedSize > defaultSize ? requestedSize : defaultSize);
	}
	
	//indexOf / contains Equals
	public static <T> boolean containsEquals(T[] array, T obj) {
		return indexOfEquals(array, obj) != -1;
	}
	
	public static <T> int indexOfEquals(T[] array, T obj) {
		for (int i = 0; i < array.length; i++) {
			T o = array[i];
			if (Objects.equals(obj, o))
				return i;
		}
		return -1;
	}
	
	//merge
	@SuppressWarnings("unchecked")
	public static <T> T[] merge(T[] array1, T[] array2) {
		return merge(length -> (T[]) new Object[length], array1, array2);
	}
	
	public static <T> T[] merge(IntFunction<T[]> arrayCreator, T[] array1, T[] array2) {
		T[] ret = arrayCreator.apply(array1.length + array2.length);
		System.arraycopy(array1, 0, ret, 0, array1.length);
		System.arraycopy(array2, 0, ret, array1.length, array2.length);
		return ret;
	}
	
	@SafeVarargs
	@SuppressWarnings("unchecked")
	public static <T> T[] merge(T[]... arrays) {
		return merge(length -> (T[]) new Object[length], arrays);
	}
	
	@SafeVarargs
	public static <T> T[] merge(IntFunction<T[]> arrayCreator, T[]... arrays) {
		int length = 0;
		for (T[] a : arrays)
			length += a.length;
		
		T[] ret = arrayCreator.apply(length);
		int index = 0;
		for (T[] a : arrays) {
			System.arraycopy(a, 0, ret, index, a.length);
			index += a.length;
		}
		
		return ret;
	}
	
	//mergeIfNeeded
	public static <T> T[] mergeIfNeeded(T[] array1, T[] array2) {
		if (array2.length == 0)
			return array1;
		if (array1.length == 0)
			return array2;
		//noinspection unchecked
		return merge(length -> (T[]) new Object[length], array1, array2);
	}
	
	public static <T> T[] mergeIfNeeded(IntFunction<T[]> arrayCreator, T[] array1, T[] array2) {
		if (array2.length == 0)
			return array1;
		if (array1.length == 0)
			return array2;
		return merge(arrayCreator, array1, array2);
	}
	
	@SafeVarargs
	public static <T> T[] mergeIfNeeded(T[]... arrays) {
		switch (arrays.length) {
			case 0:
				//noinspection unchecked
				return (T[]) new Object[0];
			case 1:
				return arrays[0];
			case 2:
				if (arrays[0].length == 0)
					return arrays[1];
				if (arrays[0].length == 1)
					return arrays[0];
				break;
		}
		//noinspection unchecked
		return mergeIfNeeded(length -> (T[]) new Object[length], arrays);
	}
	
	@SafeVarargs
	public static <T> T[] mergeIfNeeded(IntFunction<T[]> arrayCreator, T[]... arrays) {
		switch (arrays.length) {
			case 0:
				return arrayCreator.apply(0);
			case 1:
				return arrays[0];
			case 2:
				if (arrays[0].length == 0)
					return arrays[1];
				if (arrays[0].length == 1)
					return arrays[0];
				break;
		}
		return merge(arrayCreator, arrays);
	}
}
