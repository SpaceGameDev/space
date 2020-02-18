package space.engine.delegate.iterator;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * {@link Iteratorable} allows you to put {@link Iterator Iterators} (like the {@link java.util.ListIterator}) inside a iterating for-each loop (with ':').
 * The {@link Iteratorable#iterator()} just returns itself.
 */
public interface Iteratorable<T> extends Iterator<T>, Iterable<T> {
	
	//static
	Iteratorable<Object> EMPTY = new Iteratorable<>() {
		
		@Override
		public boolean hasNext() {
			return false;
		}
		
		@Override
		public Object next() {
			throw new NoSuchElementException();
		}
	};
	
	//this is the Magic!     - and it is so simple
	@NotNull
	@Override
	default Iterator<T> iterator() {
		return this;
	}
	
	@SuppressWarnings("unchecked")
	static <E> Iteratorable<E> empty() {
		return (Iteratorable<E>) EMPTY;
	}
	
	static <E> Iteratorable<E> single(E elem) {
		return new Iteratorable<>() {
			boolean gotten = false;
			
			@Override
			public boolean hasNext() {
				return gotten;
			}
			
			@Override
			public E next() {
				if (gotten)
					throw new NoSuchElementException();
				gotten = true;
				return elem;
			}
		};
	}
	
	static <E> Iteratorable<E> toIteratorable(Iterator<E> iter) {
		return iter instanceof Iteratorable ? (Iteratorable<E>) iter : new Iteratorable<>() {
			
			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}
			
			@Override
			public E next() {
				return iter.next();
			}
		};
	}
}
