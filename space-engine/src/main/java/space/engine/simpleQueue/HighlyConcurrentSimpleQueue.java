package space.engine.simpleQueue;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;

/**
 * A concurrent threadsafe linking based FILO queue.
 */
public class HighlyConcurrentSimpleQueue<E> implements SimpleQueue<E> {
	
	private static final VarHandle HEAD;
	private static final VarHandle TAIL;
	
	static {
		try {
			Lookup lookup = MethodHandles.lookup();
			HEAD = lookup.findVarHandle(HighlyConcurrentSimpleQueue.class, "head", Node.class);
			TAIL = lookup.findVarHandle(HighlyConcurrentSimpleQueue.class, "tail", Node.class);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new ExceptionInInitializerError(e);
		}
	}
	
	private final int blockSize;
	@SuppressWarnings("FieldCanBeLocal")
	private volatile @NotNull Node<E> head;
	@SuppressWarnings("FieldCanBeLocal")
	private volatile @NotNull Node<E> tail;
	private ThreadLocal<Cache<E>> cache = ThreadLocal.withInitial(Cache::new);
	
	public HighlyConcurrentSimpleQueue(int blockSize) {
		if (blockSize <= 0)
			throw new IllegalArgumentException("blocksize");
		
		this.blockSize = blockSize;
		Node<E> starterNode = new Node<>(0);
		head = starterNode;
		tail = starterNode;
	}
	
	@Override
	public boolean add(E e) {
		Cache<E> cache = this.cache.get();
		Node<E> node = cache.add;
		
		//no node
		boolean newNode = node == null;
		boolean unlock = false;
		//lock node -> new node if fails
		if (!newNode)
			newNode = !((node == cache.remove) || (unlock = Node.STATE.compareAndSet(node, Node.STATE_OPEN, Node.STATE_ADDING)));
		//node is full
		if (!newNode)
			newNode = node.itemsIndex == node.items.length;
		
		//newNode -> create new node
		if (newNode) {
			if (unlock)
				node.state = Node.STATE_OPEN;
			node = new Node<>(blockSize);
			cache.add = node;
		}
		
		//add item
		node.items[node.itemsIndex++] = e;
		
		//unlock
		if (unlock)
			node.state = Node.STATE_OPEN;
		
		//newNode -> append to TAIL
		if (newNode) {
			//noinspection unchecked
			Node<E> oldTail = (Node<E>) TAIL.getAndSet(this, node);
			oldTail.next = node;
		}
		
		return true;
	}
	
	@Nullable
	@Override
	public E remove() {
		Cache<E> cache = this.cache.get();
		Node<E> node = cache.remove;
		
		//no cached node
		boolean newNode = node == null;
		//node is empty
		if (!newNode)
			newNode = node.itemsIndex <= 0;
		
		if (newNode) {
			label:
			while (true) {
//				if (cache.add != null && cache.add.state == Node.STATE_OPEN) {
//					//use cache.add node
//					node = cache.add;
//				} else {
				//get next node from LinkedList
				Node<E> head;
				do {
					head = this.head;
					node = head.next;
					if (node == null)
						return null;
				} while (!HEAD.compareAndSet(this, head, node));
//				}
				
				while (true) {
					//lock node
					if (Node.STATE.compareAndSet(node, Node.STATE_OPEN, Node.STATE_REMOVING))
						break label;
					//node already getting removed -> retry
					if (node.state == Node.STATE_REMOVING)
						continue label;
					//node.state == Node.STATE_ADDING
					//currently adding -> wait a little
					Thread.yield();
				}
			}
			
			cache.remove = node;
		}
		
		return node.items[--node.itemsIndex];
	}
	
	@Override
	public int size() {
		int i = 0;
		for (Node<E> node = head; node != null; node = node.next)
			i++;
		return i;
	}
	
	private static class Node<E> {
		
		private static final VarHandle STATE;
		private static final int STATE_OPEN = 0;
		private static final int STATE_ADDING = 1;
		private static final int STATE_REMOVING = 2;
		
		static {
			try {
				STATE = MethodHandles.lookup().findVarHandle(Node.class, "state", int.class);
			} catch (NoSuchFieldException | IllegalAccessException e) {
				throw new ExceptionInInitializerError(e);
			}
		}
		
		//object
		private volatile int state = STATE_OPEN;
		private volatile @Nullable Node<E> next;
		
		private int itemsIndex;
		private final E[] items;
		
		public Node(int blockSize) {
			//noinspection unchecked
			this.items = (E[]) new Object[blockSize];
		}
	}
	
	private static class Cache<E> {
		
		private @Nullable Node<E> add;
		private @Nullable Node<E> remove;
	}
}
