package space.engine.delegate.iterator;

import org.jetbrains.annotations.NotNull;
import space.engine.string.toStringHelper.ToStringHelper;

import java.util.Iterator;

/**
 * The {@link UnmodifiableIterator} makes the {@link Iterator} unmodifiable.
 */
public class UnmodifiableIterator<E> extends DelegatingIterator<E> {
	
	public UnmodifiableIterator(Iterator<E> i) {
		super(i);
	}
	
	@Override
	public void remove() {
		throw new UnsupportedOperationException("Unmodifiable");
	}
	
	@NotNull
	@Override
	public <T> T toTSH(@NotNull ToStringHelper<T> api) {
		return api.createModifier("synchronized", iter);
	}
}
