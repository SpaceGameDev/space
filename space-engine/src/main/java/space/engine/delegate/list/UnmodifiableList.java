package space.engine.delegate.list;

import org.jetbrains.annotations.NotNull;
import space.engine.delegate.iterator.UnmodifiableIterator;
import space.engine.delegate.list.listiterator.UnmodifiableListIterator;
import space.engine.string.toStringHelper.ToStringHelper;
import space.engine.string.toStringHelper.ToStringHelper.ToStringHelperObjectsInstance;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * The {@link UnmodifiableList} makes the {@link List} unmodifiable.
 */
public class UnmodifiableList<E> extends DelegatingList<E> {
	
	public UnmodifiableList(List<E> list) {
		super(list);
	}
	
	@NotNull
	@Override
	public Iterator<E> iterator() {
		return new UnmodifiableIterator<>(list.iterator());
	}
	
	@Override
	public boolean add(E e) {
		throw new UnsupportedOperationException("Unmodifiable");
	}
	
	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException("Unmodifiable");
	}
	
	@Override
	public boolean addAll(@NotNull Collection<? extends E> c) {
		throw new UnsupportedOperationException("Unmodifiable");
	}
	
	@Override
	public boolean addAll(int index, @NotNull Collection<? extends E> c) {
		throw new UnsupportedOperationException("Unmodifiable");
	}
	
	@Override
	public boolean removeAll(@NotNull Collection<?> c) {
		throw new UnsupportedOperationException("Unmodifiable");
	}
	
	@Override
	public boolean retainAll(@NotNull Collection<?> c) {
		throw new UnsupportedOperationException("Unmodifiable");
	}
	
	@Override
	public void replaceAll(UnaryOperator<E> operator) {
		throw new UnsupportedOperationException("Unmodifiable");
	}
	
	@Override
	public void sort(Comparator<? super E> c) {
		throw new UnsupportedOperationException("Unmodifiable");
	}
	
	@Override
	public void clear() {
		throw new UnsupportedOperationException("Unmodifiable");
	}
	
	@Override
	public E set(int index, E element) {
		throw new UnsupportedOperationException("Unmodifiable");
	}
	
	@Override
	public void add(int index, E element) {
		throw new UnsupportedOperationException("Unmodifiable");
	}
	
	@Override
	public E remove(int index) {
		throw new UnsupportedOperationException("Unmodifiable");
	}
	
	@NotNull
	@Override
	public ListIterator<E> listIterator() {
		return new UnmodifiableListIterator<>(list.listIterator());
	}
	
	@NotNull
	@Override
	public ListIterator<E> listIterator(int index) {
		return new UnmodifiableListIterator<>(list.listIterator());
	}
	
	@NotNull
	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		return new UnmodifiableList<>(super.subList(fromIndex, toIndex));
	}
	
	@Override
	public boolean removeIf(Predicate<? super E> filter) {
		throw new UnsupportedOperationException("Unmodifiable");
	}
	
	@NotNull
	@Override
	public <TSHTYPE> TSHTYPE toTSH(@NotNull ToStringHelper<TSHTYPE> api) {
		ToStringHelperObjectsInstance<TSHTYPE> tsh = api.createObjectInstance(this);
		tsh.add("list", this.list);
		return tsh.build();
	}
	
	@Override
	public String toString() {
		return toString0();
	}
}
