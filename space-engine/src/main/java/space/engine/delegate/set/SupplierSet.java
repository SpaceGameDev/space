package space.engine.delegate.set;

import space.engine.delegate.collection.SupplierCollection;

import java.util.Set;
import java.util.function.Supplier;

/**
 * A {@link Set} delegating all calls to it's Field {@link SupplierSet#coll}, which is an {@link Supplier} of Type {@link Set}.
 * The {@link Supplier} is provided by Constructor or set directly.
 */
public class SupplierSet<E> extends SupplierCollection<E> implements Set<E> {
	
	public SupplierSet(Supplier<? extends Set<E>> coll) {
		super(coll);
	}
}
