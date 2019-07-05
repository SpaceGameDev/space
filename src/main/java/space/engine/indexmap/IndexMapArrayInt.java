package space.engine.indexmap;

import space.engine.ArrayUtils;

import java.util.Arrays;

public class IndexMapArrayInt {
	
	public static int defaultCapacity = 4;
	public static int expandShift = 1;
	
	public int[] array;
	
	public IndexMapArrayInt() {
		this(defaultCapacity);
	}
	
	public IndexMapArrayInt(int initCapacity) {
		array = new int[initCapacity];
	}
	
	public boolean ensureCapacityAvailable(int index) {
		return ensureCapacity(index + 1);
	}
	
	public boolean ensureCapacity(int capa) {
		int oldl = array.length;
		if (oldl < capa) {
			
			int[] oldArray = array;
			array = new int[ArrayUtils.getOptimalArraySizeExpansion(oldl, capa, expandShift)];
			System.arraycopy(oldArray, 0, array, 0, oldl);
			return true;
		}
		return false;
	}
	
	public int size() {
		return array.length;
	}
	
	public boolean contains(int pos) {
		return get(pos) != 0;
	}
	
	public int get(int index) {
		if (index < 0)
			throw new IndexOutOfBoundsException("no negative index!");
		if (index > array.length)
			return 0;
		
		return array[index];
	}
	
	public int remove(int index) {
		if (index < 0)
			throw new IndexOutOfBoundsException("no negative index!");
		if (index > array.length)
			return 0;
		
		int ret = array[index];
		array[index] = 0;
		return ret;
	}
	
	public int put(int index, int v) {
		if (index < 0)
			throw new IndexOutOfBoundsException("no negative index!");
		if (index > array.length)
			return 0;
		
		int ret = array[index];
		array[index] = v;
		return ret;
	}
	
	public void clear() {
		Arrays.fill(array, 0);
	}
	
	@Override
	public String toString() {
		return Arrays.toString(array);
	}
}
