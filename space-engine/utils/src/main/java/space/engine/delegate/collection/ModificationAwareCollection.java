package space.engine.delegate.collection;

import org.jetbrains.annotations.NotNull;
import space.engine.baseobject.ToString;
import space.engine.delegate.iterator.ModificationAwareIterator;
import space.engine.string.toStringHelper.ToStringHelper;
import space.engine.string.toStringHelper.ToStringHelper.ToStringHelperObjectsInstance;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * The {@link ModificationAwareCollection} will call the {@link ModificationAwareCollection#onModification} {@link Runnable} when the {@link Collection} is modified.
 */
public class ModificationAwareCollection<E> implements Collection<E>, ToString {
	
	public Collection<E> coll;
	public Runnable onModification;
	
	public ModificationAwareCollection(Collection<E> coll, Runnable onModification) {
		this.coll = coll;
		this.onModification = onModification;
	}
	
	//access
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
		return new ModificationAwareIterator<>(coll.iterator(), onModification);
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
	
	//modify
	@Override
	public boolean add(E e) {
		boolean ret = coll.add(e);
		if (ret)
			onModification.run();
		return ret;
	}
	
	@Override
	public boolean remove(Object o) {
		boolean ret = coll.remove(o);
		if (ret)
			onModification.run();
		return ret;
	}
	
	@Override
	public boolean containsAll(@NotNull Collection<?> c) {
		return coll.containsAll(c);
	}
	
	@Override
	public boolean addAll(@NotNull Collection<? extends E> c) {
		boolean ret = coll.addAll(c);
		if (ret)
			onModification.run();
		return ret;
	}
	
	@Override
	public boolean removeAll(@NotNull Collection<?> c) {
		boolean ret = coll.removeAll(c);
		if (ret)
			onModification.run();
		return ret;
	}
	
	@Override
	public boolean removeIf(Predicate<? super E> filter) {
		boolean ret = coll.removeIf(filter);
		if (ret)
			onModification.run();
		return ret;
	}
	
	@Override
	public boolean retainAll(@NotNull Collection<?> c) {
		boolean ret = coll.retainAll(c);
		if (ret)
			onModification.run();
		return ret;
	}
	
	@Override
	public void clear() {
		coll.clear();
		onModification.run();
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
	public <TSHTYPE> TSHTYPE toTSH(@NotNull ToStringHelper<TSHTYPE> api) {
		ToStringHelperObjectsInstance<TSHTYPE> tsh = api.createObjectInstance(this);
		tsh.add("coll", this.coll);
		tsh.add("onModification", this.onModification);
		return tsh.build();
	}
}
