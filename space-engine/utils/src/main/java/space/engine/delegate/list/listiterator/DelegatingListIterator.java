package space.engine.delegate.list.listiterator;

import org.jetbrains.annotations.NotNull;
import space.engine.baseobject.ToString;
import space.engine.string.toStringHelper.ToStringHelper;

import java.util.ListIterator;
import java.util.function.Consumer;

/**
 * A {@link ListIterator} delegating all calls to it's Field {@link DelegatingListIterator#iter}, supplied with the Constructor.
 */
public class DelegatingListIterator<E> implements ToString, ListIterator<E> {
	
	public ListIterator<E> iter;
	
	public DelegatingListIterator(ListIterator<E> iter) {
		this.iter = iter;
	}
	
	//methods
	@Override
	public boolean hasNext() {
		return iter.hasNext();
	}
	
	@Override
	public E next() {
		return iter.next();
	}
	
	@Override
	public boolean hasPrevious() {
		return iter.hasPrevious();
	}
	
	@Override
	public E previous() {
		return iter.previous();
	}
	
	@Override
	public int nextIndex() {
		return iter.nextIndex();
	}
	
	@Override
	public int previousIndex() {
		return iter.previousIndex();
	}
	
	@Override
	public void remove() {
		iter.remove();
	}
	
	@Override
	public void set(E e) {
		iter.set(e);
	}
	
	@Override
	public void add(E e) {
		iter.add(e);
	}
	
	@Override
	public void forEachRemaining(Consumer<? super E> action) {
		iter.forEachRemaining(action);
	}
	
	//toString
	@NotNull
	@Override
	public <T> T toTSH(@NotNull ToStringHelper<T> api) {
		return api.createModifier("delegate", iter);
	}
	
	@Override
	public String toString() {
		return toString0();
	}
}
