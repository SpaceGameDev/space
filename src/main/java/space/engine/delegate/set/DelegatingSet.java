package space.engine.delegate.set;

import space.engine.delegate.collection.DelegatingCollection;

import java.util.Set;

/**
 * A {@link Set} delegating all calls to it's Field {@link DelegatingSet#coll}, supplied with the Constructor.
 */
public class DelegatingSet<E> extends DelegatingCollection<E> implements Set<E> {
	
	public DelegatingSet(Set<E> coll) {
		super(coll);
	}
}
