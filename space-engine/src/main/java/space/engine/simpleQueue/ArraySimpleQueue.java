package space.engine.simpleQueue;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * An array based FIFO queue.
 */
public class ArraySimpleQueue<E> implements SimpleQueue<E> {
	
	private int index;
	private E[] array;
	
	public ArraySimpleQueue(int size) {
		//noinspection unchecked
		this.array = (E[]) new Object[size];
	}
	
	@Override
	public boolean add(E e) {
		if (index + 1 >= array.length)
			return false;
		array[index++] = e;
		return true;
	}
	
	@Override
	public int addArray(E[] collection, int offset, int count) {
		int length = Integer.min(count, array.length - index);
		System.arraycopy(collection, 0, array, index, length);
		index += length;
		return length;
	}
	
	@Override
	public int addCollection(Collection<E> collection) {
		//noinspection unchecked
		return addArray((E[]) collection.toArray());
	}
	
	@Nullable
	@Override
	public E remove() {
		if (index - 1 < 0)
			return null;
		return array[--index];
	}
	
	@Override
	public int removeArray(E[] array, int offset, int count) {
		int removeCnt = Integer.min(index, count);
		index -= removeCnt;
		System.arraycopy(this.array, index, array, offset, removeCnt);
		return removeCnt;
	}
	
	@Override
	public @Nullable Collection<E> removeCollection(int count) {
		int removeCnt = Integer.min(index, count);
		index -= removeCnt;
		//noinspection unchecked
		@Nullable E[] ret = (E[]) new Object[removeCnt];
		System.arraycopy(this.array, index, array, 0, removeCnt);
		return List.of(ret);
	}
	
	/**
	 * current size of the SimpleQueue, for testing purposes
	 */
	@Override
	public int size() {
		return index;
	}
}
