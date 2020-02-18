package space.engine.stack;

import static java.lang.Math.*;

/**
 * Allows for {@link PointerList#push(long)} and {@link PointerList#pop()} of longs. Can be used to eg. store {@link Stack} or {@link space.engine.stack.multistack.IMultiStack} pointers.
 */
public class PointerList {
	
	public static final int defaultSize = 10;
	public static final float defaultExpander = 1.5f;
	
	public long[] array;
	public int id = 0;
	public float expander;
	
	public PointerList() {
		this(defaultSize, defaultExpander);
	}
	
	public PointerList(int pushPopSize) {
		this(pushPopSize, defaultExpander);
	}
	
	public PointerList(int pushPopSize, float pushPopExpander) {
		this.array = new long[pushPopSize];
		this.expander = pushPopExpander;
	}
	
	public void push(long pointer) {
		ensureCapacity(id + 1);
		array[id] = pointer;
		id++;
	}
	
	public long pop() {
		id--;
		return array[id];
	}
	
	public void ensureCapacity(int capacity) {
		if (array.length < capacity) {
			expandTo(capacity);
		}
	}
	
	public void expandTo(int capacity) {
		long[] old = array;
		int newcapa = max((int) floor(old.length * expander) + 1, capacity);
		array = new long[newcapa];
		System.arraycopy(old, 0, array, 0, old.length);
	}
}
