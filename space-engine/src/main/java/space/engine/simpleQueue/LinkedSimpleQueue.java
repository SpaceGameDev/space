package space.engine.simpleQueue;

import org.jetbrains.annotations.Nullable;

/**
 * A linking based FILO queue.
 */
public class LinkedSimpleQueue<E> implements SimpleQueue<E> {
	
	private @Nullable Node<E> head;
	private @Nullable Node<E> tail;
	
	@Override
	public boolean add(E e) {
		Node<E> node = new Node<>(e);
		if (tail != null)
			tail.next = node;
		tail = node;
		
		if (head == null)
			head = node;
		return true;
	}
	
	@Nullable
	@Override
	public E remove() {
		Node<E> removed = head;
		if (removed == null)
			return null;
		head = removed.next;
		return removed.item;
	}
	
	/**
	 * current size of the SimpleQueue, for testing purposes
	 */
	public int size() {
		Node<E> n = head;
		for (int i = 0; true; i++) {
			if (n == null)
				return i;
			n = n.next;
		}
	}
	
	public static class Node<E> {
		
		private final E item;
		private @Nullable Node<E> next;
		
		public Node(E item) {
			this.item = item;
		}
	}
}
