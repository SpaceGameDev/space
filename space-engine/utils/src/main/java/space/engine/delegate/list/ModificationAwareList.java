package space.engine.delegate.list;

import org.jetbrains.annotations.NotNull;
import space.engine.delegate.iterator.ModificationAwareIterator;
import space.engine.delegate.list.listiterator.ModificationAwareListIterator;
import space.engine.string.toStringHelper.ToStringHelper;
import space.engine.string.toStringHelper.ToStringHelper.ToStringHelperObjectsInstance;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * The {@link ModificationAwareList} will call the {@link ModificationAwareList#onModification} {@link Runnable} when the {@link List} is modified.
 */
public class ModificationAwareList<E> extends DelegatingList<E> {
	
	public Runnable onModification;
	
	public ModificationAwareList(List<E> list, Runnable onModification) {
		super(list);
		this.onModification = onModification;
	}
	
	@NotNull
	@Override
	public Iterator<E> iterator() {
		return new ModificationAwareIterator<>(list.iterator(), onModification);
	}
	
	@Override
	public boolean add(E e) {
		list.add(e);
		onModification.run();
		return true;
	}
	
	@Override
	public boolean remove(Object o) {
		boolean ret = list.remove(o);
		if (ret)
			onModification.run();
		return ret;
	}
	
	@Override
	public boolean addAll(@NotNull Collection<? extends E> c) {
		boolean ret = list.addAll(c);
		if (ret)
			onModification.run();
		return ret;
	}
	
	@Override
	public boolean addAll(int index, @NotNull Collection<? extends E> c) {
		boolean ret = list.addAll(index, c);
		if (ret)
			onModification.run();
		return ret;
	}
	
	@Override
	public boolean removeAll(@NotNull Collection<?> c) {
		onModification.run();
		return list.removeAll(c);
	}
	
	@Override
	public boolean retainAll(@NotNull Collection<?> c) {
		boolean ret = list.retainAll(c);
		if (ret)
			onModification.run();
		return ret;
	}
	
	@Override
	public void replaceAll(UnaryOperator<E> operator) {
		boolean[] mod = new boolean[1];
		list.replaceAll(e -> {
			E ret = operator.apply(e);
			if (ret != e)
				mod[0] = true;
			return ret;
		});
		if (mod[0])
			onModification.run();
	}
	
	@Override
	public void sort(Comparator<? super E> c) {
		list.sort(c);
		onModification.run();
	}
	
	@Override
	public void clear() {
		list.clear();
		if (list.size() != 0)
			onModification.run();
	}
	
	@Override
	public E set(int index, E element) {
		E ret = list.set(index, element);
		onModification.run();
		return ret;
	}
	
	@Override
	public void add(int index, E element) {
		list.add(index, element);
		onModification.run();
	}
	
	@Override
	public E remove(int index) {
		E ret = list.remove(index);
		onModification.run();
		return ret;
	}
	
	@NotNull
	@Override
	public ListIterator<E> listIterator() {
		return new ModificationAwareListIterator<>(list.listIterator(), onModification);
	}
	
	@NotNull
	@Override
	public ListIterator<E> listIterator(int index) {
		return new ModificationAwareListIterator<>(list.listIterator(), onModification);
	}
	
	@NotNull
	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		return new ModificationAwareList<>(super.subList(fromIndex, toIndex), onModification);
	}
	
	@Override
	public Spliterator<E> spliterator() {
		return list.spliterator();
	}
	
	@Override
	public boolean removeIf(Predicate<? super E> filter) {
		boolean ret = super.removeIf(filter);
		if (ret)
			onModification.run();
		return ret;
	}
	
	@Override
	public Stream<E> stream() {
		return list.stream();
	}
	
	@Override
	public Stream<E> parallelStream() {
		return list.parallelStream();
	}
	
	@NotNull
	@Override
	public <TSHTYPE> TSHTYPE toTSH(@NotNull ToStringHelper<TSHTYPE> api) {
		ToStringHelperObjectsInstance<TSHTYPE> tsh = api.createObjectInstance(this);
		tsh.add("list", this.list);
		tsh.add("onModification", this.onModification);
		return tsh.build();
	}
}
