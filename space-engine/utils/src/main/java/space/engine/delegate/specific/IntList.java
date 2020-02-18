package space.engine.delegate.specific;

import org.jetbrains.annotations.NotNull;
import space.engine.ArrayUtils;
import space.engine.baseobject.ToString;
import space.engine.string.toStringHelper.ToStringHelper;

/**
 * A List of Ints
 */
public class IntList implements ToString {
	
	public static final IntList EMPTY = new IntList(0);
	public static int defaultCapacity = 4;
	public static int expandShift = 1;
	
	public int[] array;
	public int size;
	
	public IntList() {
		this(defaultCapacity);
	}
	
	public IntList(int initCapacity) {
		this(new int[initCapacity], 0);
	}
	
	public IntList(int[] array) {
		this(array, array.length);
	}
	
	public IntList(int[] array, int size) {
		this.array = array;
		this.size = size;
	}
	
	public IntList(IntList list) {
		this.array = list.array.clone();
		this.size = list.size;
	}
	
	public boolean ensureCapacityIndex(int index) {
		return ensureCapacity(index + 1);
	}
	
	public boolean ensureCapacity(int capa) {
		if (capa >= array.length) {
			int[] old = array;
			array = new int[ArrayUtils.getOptimalArraySizeExpansion(old.length, capa, expandShift)];
			System.arraycopy(old, 0, array, 0, old.length);
			return true;
		}
		return false;
	}
	
	public void checkIndex(int index) {
		if (index >= size)
			throw new IndexOutOfBoundsException();
	}
	
	public int size() {
		return size;
	}
	
	public boolean isEmpty() {
		return size == 0;
	}
	
	public void add(int i) {
		ensureCapacityIndex(size);
		array[size++] = i;
	}
	
	public int set(int index, int i) {
		checkIndex(index);
		int old = array[index];
		array[index] = i;
		return old;
	}
	
	public int get(int index) {
		checkIndex(index);
		return array[index];
	}
	
	public int poll() {
		if (size == 0)
			throw new IndexOutOfBoundsException();
		int ret = array[--size];
		array[size] = 0;
		return ret;
	}
	
	public void remove(int index) {
		System.arraycopy(array, index + 1, array, index, (--size) - index);
	}
	
	public int[] toArray() {
		int[] ret = new int[size];
		System.arraycopy(array, 0, ret, 0, size);
		return ret;
	}
	
	public int[] toArrayBackwards() {
		int[] ret = new int[size];
		int g = size - 1;
		for (int i = 0; i < size; ) {
			ret[i] = array[g];
			i++;
			g--;
		}
		return ret;
	}
	
	@NotNull
	@Override
	public <T> T toTSH(@NotNull ToStringHelper<T> api) {
		return api.toString(array, 0, size);
	}
	
	@Override
	public String toString() {
		return toString0();
	}
}
