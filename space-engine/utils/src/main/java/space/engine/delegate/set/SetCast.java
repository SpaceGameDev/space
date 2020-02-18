package space.engine.delegate.set;

import org.jetbrains.annotations.NotNull;
import space.engine.delegate.collection.DelegatingCollection;
import space.engine.string.toStringHelper.ToStringHelper;

import java.util.Collection;
import java.util.Set;

/**
 * Casts a {@link Collection} to a {@link Set}<br>
 * <b>Does not ensure uniqueness of entries</b>
 */
public class SetCast<E> extends DelegatingCollection<E> implements Set<E> {
	
	public SetCast(Collection<E> coll) {
		super(coll);
	}
	
	@NotNull
	@Override
	public <T> T toTSH(@NotNull ToStringHelper<T> api) {
		return api.createModifier("cast(Set)", coll);
	}
}
