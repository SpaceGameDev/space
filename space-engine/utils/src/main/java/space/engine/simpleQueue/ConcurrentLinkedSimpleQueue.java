package space.engine.simpleQueue;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;

/**
 * A concurrent threadsafe linking based FILO queue.
 */
public class ConcurrentLinkedSimpleQueue<E> implements SimpleQueue<E> {
	
	private static final VarHandle HEAD;
	private static final VarHandle TAIL;
	
	static {
		try {
			Lookup lookup = MethodHandles.lookup();
			HEAD = lookup.findVarHandle(ConcurrentLinkedSimpleQueue.class, "head", Node.class);
			TAIL = lookup.findVarHandle(ConcurrentLinkedSimpleQueue.class, "tail", Node.class);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new ExceptionInInitializerError(e);
		}
	}
	
	@SuppressWarnings("FieldCanBeLocal")
	private volatile @NotNull Node<E> head;
	@SuppressWarnings("FieldCanBeLocal")
	private volatile @NotNull Node<E> tail;
	
	public ConcurrentLinkedSimpleQueue() {
		//noinspection ConstantConditions
		Node<E> starterNode = new Node<>(null);
		head = starterNode;
		tail = starterNode;
	}
	
	@Override
	public boolean add(E e) {
		Node<E> node = new Node<>(e);
		//noinspection unchecked
		Node<E> oldTail = (Node<E>) TAIL.getAndSet(this, node);
		oldTail.next = node;
		return true;
	}
	
	@Nullable
	@Override
	public E remove() {
		Node<E> head, next;
		do {
			head = this.head;
			next = head.next;
			if (next == null)
				return null;
		}
		while (!HEAD.compareAndSet(this, head, next));
		return next.item;
	}
	
	@Override
	public int size() {
		int i = 0;
		for (Node<E> node = head; node != null; node = node.next)
			i++;
		return i;
	}
	
	private static class Node<E> {
		
		private final E item;
		private volatile @Nullable Node<E> next;
		
		public Node(E item) {
			this.item = item;
		}
	}
}
