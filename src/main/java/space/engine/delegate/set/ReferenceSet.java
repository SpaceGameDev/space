package space.engine.delegate.set;

import space.engine.delegate.collection.ReferenceCollection;

import java.lang.ref.Reference;
import java.util.Set;
import java.util.function.Function;

/**
 * Remaps all Entries to a {@link Reference} of type E. These References are created by refCreator in the Constructor.<br>
 * <b>Added References have to be removed manually.</b> Otherwise References may accumulate.
 */
public class ReferenceSet<E> extends ReferenceCollection<E> implements Set<E> {
	
	public ReferenceSet(Set<Reference<? extends E>> coll, Function<E, Reference<? extends E>> refCreator) {
		super(coll, refCreator);
	}
}
