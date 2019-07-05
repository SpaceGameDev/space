package space.engine.delegate.collection;

import org.jetbrains.annotations.NotNull;
import space.engine.baseobject.ToString;
import space.engine.string.toStringHelper.ToStringHelper;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A {@link Collection} delegating all calls to it's Field {@link DelegatingCollection#coll}, supplied with the Constructor.
 */
public class DelegatingCollection<E> implements ToString, Collection<E> {
	
	public Collection<E> coll;
	
	public DelegatingCollection(Collection<E> coll) {
		this.coll = coll;
	}
	
	//delegate
	@Override
	public int size() {
		return coll.size();
	}
	
	@Override
	public boolean isEmpty() {
		return coll.isEmpty();
	}
	
	@Override
	public boolean contains(Object o) {
		return coll.contains(o);
	}
	
	@NotNull
	@Override
	public Iterator<E> iterator() {
		return coll.iterator();
	}
	
	@NotNull
	@Override
	public Object[] toArray() {
		return coll.toArray();
	}
	
	@NotNull
	@Override
	@SuppressWarnings("SuspiciousToArrayCall")
	public <T> T[] toArray(@NotNull T[] a) {
		return coll.toArray(a);
	}
	
	@Override
	public boolean add(E e) {
		return coll.add(e);
	}
	
	@Override
	public boolean remove(Object o) {
		return coll.remove(o);
	}
	
	@Override
	public boolean containsAll(@NotNull Collection<?> c) {
		return coll.containsAll(c);
	}
	
	@Override
	public boolean addAll(@NotNull Collection<? extends E> c) {
		return coll.addAll(c);
	}
	
	@Override
	public boolean removeAll(@NotNull Collection<?> c) {
		return coll.removeAll(c);
	}
	
	@Override
	public boolean removeIf(Predicate<? super E> filter) {
		return coll.removeIf(filter);
	}
	
	@Override
	public boolean retainAll(@NotNull Collection<?> c) {
		return coll.retainAll(c);
	}
	
	@Override
	public void clear() {
		coll.clear();
	}
	
	@Override
	public Spliterator<E> spliterator() {
		return coll.spliterator();
	}
	
	@Override
	public Stream<E> stream() {
		return coll.stream();
	}
	
	@Override
	public Stream<E> parallelStream() {
		return coll.parallelStream();
	}
	
	@Override
	public void forEach(Consumer<? super E> action) {
		coll.forEach(action);
	}
	
	@NotNull
	@Override
	public <T> T toTSH(@NotNull ToStringHelper<T> api) {
		return api.createModifier("delegate", coll);
	}
	
	@Override
	public String toString() {
		return toString0();
	}
}
