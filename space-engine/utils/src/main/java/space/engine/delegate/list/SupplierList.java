package space.engine.delegate.list;

import org.jetbrains.annotations.NotNull;
import space.engine.baseobject.ToString;
import space.engine.string.toStringHelper.ToStringHelper;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * A {@link List} delegating all calls to it's Field {@link SupplierList#list}, which is an {@link Supplier} of Type {@link List}.
 * The {@link Supplier} is provided by Constructor or set directly.
 */
public class SupplierList<E> implements ToString, List<E> {
	
	public Supplier<List<E>> list;
	
	public SupplierList(Supplier<List<E>> list) {
		this.list = list;
	}
	
	@Override
	public int size() {
		return list.get().size();
	}
	
	@Override
	public boolean isEmpty() {
		return list.get().isEmpty();
	}
	
	@Override
	public boolean contains(Object o) {
		return list.get().contains(o);
	}
	
	@NotNull
	@Override
	public Iterator<E> iterator() {
		return list.get().iterator();
	}
	
	@NotNull
	@Override
	public Object[] toArray() {
		return list.get().toArray();
	}
	
	@NotNull
	@Override
	@SuppressWarnings("SuspiciousToArrayCall")
	public <T> T[] toArray(@NotNull T[] a) {
		return list.get().toArray(a);
	}
	
	@Override
	public boolean add(E e) {
		return list.get().add(e);
	}
	
	@Override
	public boolean remove(Object o) {
		return list.get().remove(o);
	}
	
	@Override
	public boolean containsAll(@NotNull Collection<?> c) {
		return list.get().containsAll(c);
	}
	
	@Override
	public boolean addAll(@NotNull Collection<? extends E> c) {
		return list.get().addAll(c);
	}
	
	@Override
	public boolean addAll(int index, @NotNull Collection<? extends E> c) {
		return list.get().addAll(index, c);
	}
	
	@Override
	public boolean removeAll(@NotNull Collection<?> c) {
		return list.get().removeAll(c);
	}
	
	@Override
	public boolean retainAll(@NotNull Collection<?> c) {
		return list.get().retainAll(c);
	}
	
	@Override
	public void replaceAll(UnaryOperator<E> operator) {
		list.get().replaceAll(operator);
	}
	
	@Override
	public void sort(Comparator<? super E> c) {
		list.get().sort(c);
	}
	
	@Override
	public void clear() {
		list.get().clear();
	}
	
	@Override
	public E get(int index) {
		return list.get().get(index);
	}
	
	@Override
	public E set(int index, E element) {
		return list.get().set(index, element);
	}
	
	@Override
	public void add(int index, E element) {
		list.get().add(index, element);
	}
	
	@Override
	public E remove(int index) {
		return list.get().remove(index);
	}
	
	@Override
	public int indexOf(Object o) {
		return list.get().indexOf(o);
	}
	
	@Override
	public int lastIndexOf(Object o) {
		return list.get().lastIndexOf(o);
	}
	
	@NotNull
	@Override
	public ListIterator<E> listIterator() {
		return list.get().listIterator();
	}
	
	@NotNull
	@Override
	public ListIterator<E> listIterator(int index) {
		return list.get().listIterator(index);
	}
	
	@NotNull
	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		return list.get().subList(fromIndex, toIndex);
	}
	
	@Override
	public Spliterator<E> spliterator() {
		return list.get().spliterator();
	}
	
	@Override
	public boolean removeIf(Predicate<? super E> filter) {
		return list.get().removeIf(filter);
	}
	
	@Override
	public Stream<E> stream() {
		return list.get().stream();
	}
	
	@Override
	public Stream<E> parallelStream() {
		return list.get().parallelStream();
	}
	
	@Override
	public void forEach(Consumer<? super E> action) {
		list.get().forEach(action);
	}
	
	@NotNull
	@Override
	public <T> T toTSH(@NotNull ToStringHelper<T> api) {
		return api.createModifier("supplier", this.list);
	}
	
	@Override
	public String toString() {
		return toString0();
	}
}
