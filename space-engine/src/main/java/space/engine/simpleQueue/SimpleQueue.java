package space.engine.simpleQueue;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public interface SimpleQueue<E> {
	
	static <E> SimpleQueue<E> recommendSinglethread() {
		return new LinkedSimpleQueue<>();
	}
	
	static <E> SimpleQueue<E> recommendConcurrent(int threadCount) {
		return new HighlyConcurrentSimpleQueue<>(Math.max(threadCount * 1, 16));
	}
	
	/**
	 * add a new element e to the {@link SimpleQueue}.
	 *
	 * @param e the new element to add
	 * @return true if adding was successful
	 */
	boolean add(E e);
	
	/**
	 * adds multiple elements to the {@link SimpleQueue}.
	 *
	 * @param collection the elements to add
	 * @return the amount of elements added
	 */
	default int addCollection(Collection<E> collection) {
		Iterator<E> iter = collection.iterator();
		for (int i = 0; iter.hasNext(); i++)
			if (!add(iter.next()))
				return i;
		return collection.size();
	}
	
	default int addArray(E[] collection) {
		return addArray(collection, 0, collection.length);
	}
	
	/**
	 * adds multiple elements to the {@link SimpleQueue}.
	 *
	 * @param collection the elements to add
	 * @return the amount of elements added
	 */
	default int addArray(E[] collection, int offset, int count) {
		for (int i = 0; i < count; i++)
			if (!add(collection[offset + i]))
				return i;
		return count;
	}
	
	/**
	 * removes an element from the {@link SimpleQueue} and returns it
	 *
	 * @return the removed element
	 */
	@Nullable E remove();
	
	/**
	 * removes count elements from the {@link SimpleQueue} and puts them in the supplied array
	 *
	 * @param array  the array to fill
	 * @param offset the offset to start writing in the array
	 * @param count  the count of elements to remove
	 * @return the count of removed elements
	 */
	default int removeArray(E[] array, int offset, int count) {
		for (int i = 0; i < count; i++) {
			E e = remove();
			if (e == null)
				return i - 1;
			array[offset + i] = e;
		}
		return count;
	}
	
	/**
	 * removes count elements from the {@link SimpleQueue} and returns them in a {@link Collection} of maximum size #count.
	 * NOTE: the collection will be tinier than #count if there are no more elements to remove.
	 *
	 * @param count the count of elements to remove
	 * @return the removed elements as a {@link Collection}
	 */
	default Collection<@NotNull E> removeCollection(int count) {
		ArrayList<E> ret = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			E e = remove();
			if (e == null)
				break;
			ret.add(e);
		}
		return ret;
	}
	
	/**
	 * Gets the size of the Queue. For debug, monitor and testing only.
	 * May throw an {@link UnsupportedOperationException} if not implemented.
	 *
	 * @return the size of the Queue
	 * @throws UnsupportedOperationException if not supported by the queue
	 */
	default int size() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
}
