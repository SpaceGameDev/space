package space.engine.delegate.set;

import space.engine.delegate.collection.UnmodifiableCollection;

import java.util.Set;

/**
 * The {@link UnmodifiableSet} makes the {@link Set} unmodifiable.
 */
public class UnmodifiableSet<E> extends UnmodifiableCollection<E> implements Set<E> {
	
	public UnmodifiableSet(Set<E> coll) {
		super(coll);
	}
}
