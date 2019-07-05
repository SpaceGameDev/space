package space.engine.delegate.list.listiterator;

import org.jetbrains.annotations.NotNull;
import space.engine.string.toStringHelper.ToStringHelper;

import java.util.ListIterator;

/**
 * The {@link UnmodifiableListIterator} makes the {@link ListIterator} unmodifiable.
 */
public class UnmodifiableListIterator<E> extends DelegatingListIterator<E> {
	
	public UnmodifiableListIterator(ListIterator<E> iterator) {
		super(iterator);
	}
	
	@Override
	public void remove() {
		throw new UnsupportedOperationException("Unmodifiable");
	}
	
	@Override
	public void set(E e) {
		throw new UnsupportedOperationException("Unmodifiable");
	}
	
	@Override
	public void add(E e) {
		throw new UnsupportedOperationException("Unmodifiable");
	}
	
	@NotNull
	@Override
	public <T> T toTSH(@NotNull ToStringHelper<T> api) {
		return api.createModifier("unmodifiable", iter);
	}
}
