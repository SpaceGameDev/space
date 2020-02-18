package space.engine.delegate.collection;

import org.jetbrains.annotations.NotNull;
import space.engine.baseobject.ToString;
import space.engine.string.toStringHelper.ToStringHelper;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A {@link Collection} delegating all calls to it's Field {@link SupplierCollection#coll}, which is an {@link Supplier} of Type {@link Collection}, allowing for unique usages. The {@link Supplier} is provided by Constructor or set directly.
 */
public class SupplierCollection<E> implements ToString, Collection<E> {
	
	public Supplier<? extends Collection<E>> coll;
	
	public SupplierCollection(Supplier<? extends Collection<E>> coll) {
		this.coll = coll;
	}
	
	@Override
	public int size() {
		return coll.get().size();
	}
	
	@Override
	public boolean isEmpty() {
		return coll.get().isEmpty();
	}
	
	@Override
	public boolean contains(Object o) {
		return coll.get().contains(o);
	}
	
	@NotNull
	@Override
	public Iterator<E> iterator() {
		return coll.get().iterator();
	}
	
	@NotNull
	@Override
	public Object[] toArray() {
		return coll.get().toArray();
	}
	
	@NotNull
	@Override
	public <T> T[] toArray(@NotNull T[] a) {
		return coll.get().toArray(a);
	}
	
	@Override
	public boolean add(E e) {
		return coll.get().add(e);
	}
	
	@Override
	public boolean remove(Object o) {
		return coll.get().remove(o);
	}
	
	@Override
	public boolean containsAll(@NotNull Collection<?> c) {
		return coll.get().containsAll(c);
	}
	
	@Override
	public boolean addAll(@NotNull Collection<? extends E> c) {
		return coll.get().addAll(c);
	}
	
	@Override
	public boolean removeAll(@NotNull Collection<?> c) {
		return coll.get().removeAll(c);
	}
	
	@Override
	public boolean removeIf(Predicate<? super E> filter) {
		return coll.get().removeIf(filter);
	}
	
	@Override
	public boolean retainAll(@NotNull Collection<?> c) {
		return coll.get().retainAll(c);
	}
	
	@Override
	public void clear() {
		coll.get().clear();
	}
	
	@Override
	public Spliterator<E> spliterator() {
		return coll.get().spliterator();
	}
	
	@Override
	public Stream<E> stream() {
		return coll.get().stream();
	}
	
	@Override
	public Stream<E> parallelStream() {
		return coll.get().parallelStream();
	}
	
	@Override
	public void forEach(Consumer<? super E> action) {
		coll.get().forEach(action);
	}
	
	@NotNull
	@Override
	public <T> T toTSH(@NotNull ToStringHelper<T> api) {
		return api.createModifier("supplier", coll);
	}
	
	@Override
	public String toString() {
		return toString0();
	}
}
