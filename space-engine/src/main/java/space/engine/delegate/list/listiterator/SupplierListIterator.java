package space.engine.delegate.list.listiterator;

import org.jetbrains.annotations.NotNull;
import space.engine.baseobject.ToString;
import space.engine.string.toStringHelper.ToStringHelper;

import java.util.ListIterator;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A {@link ListIterator} delegating all calls to it's Field {@link SupplierListIterator#iter}, which is an {@link Supplier} of Type {@link ListIterator}.
 * The {@link Supplier} is provided by Constructor or set directly.
 */
public class SupplierListIterator<E> implements ToString, ListIterator<E> {
	
	public Supplier<ListIterator<E>> iter;
	
	public SupplierListIterator(Supplier<ListIterator<E>> iter) {
		this.iter = iter;
	}
	
	@Override
	public boolean hasNext() {
		return iter.get().hasNext();
	}
	
	@Override
	public E next() {
		return iter.get().next();
	}
	
	@Override
	public boolean hasPrevious() {
		return iter.get().hasPrevious();
	}
	
	@Override
	public E previous() {
		return iter.get().previous();
	}
	
	@Override
	public int nextIndex() {
		return iter.get().nextIndex();
	}
	
	@Override
	public int previousIndex() {
		return iter.get().previousIndex();
	}
	
	@Override
	public void remove() {
		iter.get().remove();
	}
	
	@Override
	public void set(E e) {
		iter.get().set(e);
	}
	
	@Override
	public void add(E e) {
		iter.get().add(e);
	}
	
	@Override
	public void forEachRemaining(Consumer<? super E> action) {
		iter.get().forEachRemaining(action);
	}
	
	@NotNull
	@Override
	public <T> T toTSH(@NotNull ToStringHelper<T> api) {
		return api.createModifier("supplier", iter);
	}
	
	@Override
	public String toString() {
		return toString0();
	}
}
