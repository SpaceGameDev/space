package space.engine.delegate.list;

import org.jetbrains.annotations.NotNull;
import space.engine.baseobject.ToString;
import space.engine.delegate.collection.ConvertingCollection;
import space.engine.delegate.iterator.ConvertingIterator;
import space.engine.delegate.list.listiterator.ConvertingListIterator;
import space.engine.string.toStringHelper.ToStringHelper;
import space.engine.string.toStringHelper.ToStringHelper.ToStringHelperObjectsInstance;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * A {@link ConvertingList} converts <b>FROM</b> one {@link List List's} Value <b>TO</b> a different value with the help of provided {@link Function Functions} for conversion.<br>
 * It has multiple inner classes allowing for different usages for many different cases.<br>
 * All implementations are threadsafe if their underlying {@link ConvertingList#list} is also threadsafe.<br>
 * <br>
 * 3 Types of Functions:
 * <table border=1>
 * <tr><td>Function</td><td>Remap direction</td><td>Comment</td></tr>
 * <tr><td>{@link ConvertingList.OneDirectionalUnmodifiable#remap Function&lt;? super F, ? extends T&gt; remap}</td><td>F -&gt; T </td><td>Always required.</td></tr>
 * <tr><td>{@link ConvertingList.BiDirectionalSparse#reverseSparse Function&lt;? super T, ? extends F&gt; reverseSparse}</td><td>T -&gt; F </td><td>Only called when the returned value is added to this Object. If available defaults to reverse. </td></tr>
 * <tr><td>{@link ConvertingList.BiDirectional#reverse Function&lt;? super T, ? extends F&gt; reverse}</td><td>T -&gt; F </td><td>Will be called even for simple Operations, where the Result may not be stored and only used for eg. comparision. </td></tr>
 * </table>
 * <br>
 * 4 Sub-Classes for Converting with different Functions:
 * <table border=1>
 *
 * <tr>
 * <td>Class name</td>
 * <td>Modifiable?</td>
 * <td>Required Functions</td>
 * <td>Inefficient Methods</td>
 * <td>Comparision Object</td>
 * </tr>
 *
 * <tr>
 * <td>{@link ConvertingList.OneDirectionalUnmodifiable OneDirectionalUnmodifiable}</td>
 * <td>No</td>
 * <td>
 * <ul>
 * <li>{@link ConvertingList.OneDirectionalUnmodifiable#remap Function&lt;? super F, ? extends T&gt; remap}</li>
 * </ul>
 * </td>
 * <td>
 * <ul>
 * <li>{@link ConvertingList.OneDirectionalUnmodifiable#contains(Object) contains(Object)}</li>
 * <li>{@link ConvertingList.OneDirectionalUnmodifiable#containsAll(Collection) containsAll(Collection)}</li>
 * <li>{@link ConvertingList.OneDirectionalUnmodifiable#indexOf(Object) indexOf(Object)}</li>
 * <li>{@link ConvertingList.OneDirectionalUnmodifiable#lastIndexOf(Object) lastIndexOf(Object)}</li>
 * </ul>
 * </td>
 * <td>TO</td>
 * </tr>
 *
 * <tr>
 * <td>{@link ConvertingList.BiDirectionalUnmodifiable BiDirectionalUnmodifiable}</td>
 * <td>No</td>
 * <td>
 * <ul>
 * <li>{@link ConvertingList.BiDirectionalUnmodifiable#remap Function&lt;? super F, ? extends T&gt; remap}</li>
 * <li>{@link ConvertingList.BiDirectionalUnmodifiable#reverse Function&lt;? super T, ? extends F&gt; reverse}</li>
 * </ul>
 * </td>
 * <td>
 * <ul><li>none</li></ul>
 * </td>
 * <td>FROM</td>
 * </tr>
 *
 * <tr>
 * <td>{@link ConvertingList.BiDirectionalSparse BiDirectionalSparse}</td>
 * <td>Yes</td>
 * <td>
 * <ul>
 * <li>{@link ConvertingList.BiDirectionalSparse#remap Function&lt;? super F, ? extends T&gt; remap}</li>
 * <li>{@link ConvertingList.BiDirectionalSparse#reverseSparse Function&lt;? super T, ? extends F&gt; reverseSparse}</li>
 * </ul>
 * </td>
 * <td>
 * <ul>
 * <li>{@link ConvertingList.BiDirectionalSparse#contains(Object) contains(Object)}</li>
 * <li>{@link ConvertingList.BiDirectionalSparse#containsAll(Collection) containsAll(Collection)}</li>
 * <li>{@link ConvertingList.BiDirectionalSparse#indexOf(Object) indexOf(Object)}</li>
 * <li>{@link ConvertingList.BiDirectionalSparse#lastIndexOf(Object) lastIndexOf(Object)}</li>
 * <li>{@link ConvertingList.BiDirectionalSparse#remove(Object) remove(Object)}</li>
 * <li>{@link ConvertingList.BiDirectionalSparse#removeAll(Collection) removeAll(Collection)}</li>
 * <li>{@link ConvertingList.BiDirectionalSparse#retainAll(Collection) retainAll(Collection)}</li>
 * </ul>
 * </td>
 * <td>TO</td>
 * </tr>
 *
 * <tr>
 * <td>{@link ConvertingList.BiDirectional BiDirectional}</td>
 * <td>Yes</td>
 * <td>
 * <ul>
 * <li>{@link ConvertingList.BiDirectional#remap Function&lt;? super F, ? extends T&gt; remap}</li>
 * <li>{@link ConvertingList.BiDirectional#reverseSparse Function&lt;? super T, ? extends F&gt; reverseSparse} (defaults to reverse)</li>
 * <li>{@link ConvertingList.BiDirectional#reverse Function&lt;? super T, ? extends F&gt; reverse}</li>
 * </ul>
 * </td>
 * <td>
 * <ul><li>none</li></ul>
 * </td>
 * <td>FROM</td>
 * </tr>
 *
 * </table>
 * <ul>
 * <li>Inefficient Methods: Methods which are implemented inefficiently and should thus be avoided to be called. Non-marked Methods will only delegate. </li>
 * <li>Comparision Object: Object on which Comparision will be done on. Either on FROM objects or the TO objects.</li>
 * </ul>
 *
 * @param <F> the value to convert <b>FROM</b>
 * @param <T> the value to convert <b>TO</b>
 */
public abstract class ConvertingList<F, T> implements List<T>, ToString {
	
	public List<F> list;
	
	public ConvertingList(List<F> list) {
		this.list = list;
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
	
	public static class OneDirectionalUnmodifiable<F, T> extends ConvertingList<F, T> {
		
		public Function<? super F, ? extends T> remap;
		
		public OneDirectionalUnmodifiable(List<F> list, Function<? super F, ? extends T> remap) {
			super(list);
			this.remap = remap;
		}
		
		@Override
		public int size() {
			return list.size();
		}
		
		@Override
		public boolean isEmpty() {
			return list.isEmpty();
		}
		
		@Override
		public boolean contains(Object o) {
			for (F f : list)
				if (Objects.equals(remap.apply(f), o))
					return true;
			return false;
		}
		
		@NotNull
		@Override
		public Iterator<T> iterator() {
			return ConvertingIterator.createConverterOneDirectional(list.iterator(), remap);
		}
		
		@NotNull
		@Override
		public Object[] toArray() {
			//noinspection unchecked
			F[] org = (F[]) list.toArray();
			Object[] ret = new Object[org.length];
			for (int i = 0; i < org.length; i++)
				ret[i] = remap.apply(org[i]);
			return ret;
		}
		
		@NotNull
		@Override
		@SuppressWarnings("unchecked")
		public <T1> T1[] toArray(@NotNull T1[] a) {
			F[] org = (F[]) list.toArray();
			if (a.length < org.length) {
				//new instance
				T1[] ret = (T1[]) Array.newInstance(a.getClass(), org.length);
				for (int i = 0; i < org.length; i++)
					ret[i] = (T1) remap.apply(org[i]);
				return ret;
			}
			
			//use existing
			for (int i = 0; i < org.length; i++)
				a[i] = (T1) remap.apply(org[i]);
			return a;
		}
		
		//modify methods
		@Override
		public boolean add(T t) {
			throw new UnsupportedOperationException("Unmodifiable");
		}
		
		@Override
		public boolean remove(Object o) {
			throw new UnsupportedOperationException("Unmodifiable");
		}
		
		@Override
		public boolean containsAll(@NotNull Collection<?> c) {
			for (F f : list)
				if (!c.contains(remap.apply(f)))
					return false;
			return true;
		}
		
		@Override
		public boolean addAll(@NotNull Collection<? extends T> c) {
			throw new UnsupportedOperationException("Unmodifiable");
		}
		
		@Override
		public boolean addAll(int index, @NotNull Collection<? extends T> c) {
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
		public void replaceAll(UnaryOperator<T> operator) {
			throw new UnsupportedOperationException("Unmodifiable");
		}
		
		@Override
		public void sort(Comparator<? super T> c) {
			throw new UnsupportedOperationException("Unmodifiable");
		}
		
		@Override
		public void clear() {
			throw new UnsupportedOperationException("Unmodifiable");
		}
		
		@Override
		public T get(int index) {
			return remap.apply(list.get(index));
		}
		
		@Override
		public T set(int index, T element) {
			throw new UnsupportedOperationException("Unmodifiable");
		}
		
		@Override
		public void add(int index, T element) {
			throw new UnsupportedOperationException("Unmodifiable");
		}
		
		@Override
		public T remove(int index) {
			throw new UnsupportedOperationException("Unmodifiable");
		}
		
		@Override
		public int indexOf(Object o) {
			Iterator<F> iter = list.iterator();
			for (int i = 0; iter.hasNext(); i++)
				if (Objects.equals(iter.next(), o))
					return i;
			return -1;
		}
		
		@Override
		public int lastIndexOf(Object o) {
			for (int i = list.size() - 1; i >= 0; i--)
				if (Objects.equals(list.get(i), o))
					return i;
			return -1;
		}
		
		@NotNull
		@Override
		public ListIterator<T> listIterator() {
			return new ConvertingListIterator.OneDirectionalUnmodifiable<>(list.listIterator(), remap);
		}
		
		@NotNull
		@Override
		public ListIterator<T> listIterator(int index) {
			return new ConvertingListIterator.OneDirectionalUnmodifiable<>(list.listIterator(index), remap);
		}
		
		@NotNull
		@Override
		public List<T> subList(int fromIndex, int toIndex) {
			return new ConvertingList.OneDirectionalUnmodifiable<>(list.subList(fromIndex, toIndex), remap);
		}
		
		@Override
		public boolean removeIf(Predicate<? super T> filter) {
			throw new UnsupportedOperationException("Unmodifiable");
		}
		
		@Override
		public void forEach(Consumer<? super T> action) {
			list.forEach(f -> action.accept(remap.apply(f)));
		}
		
		@NotNull
		@Override
		public <TSHTYPE> TSHTYPE toTSH(@NotNull ToStringHelper<TSHTYPE> api) {
			ToStringHelperObjectsInstance<TSHTYPE> tsh = api.createObjectInstance(this);
			tsh.add("list", this.list);
			tsh.add("remap", this.remap);
			return tsh.build();
		}
	}
	
	public static class BiDirectionalUnmodifiable<F, T> extends OneDirectionalUnmodifiable<F, T> {
		
		public Function<? super T, ? extends F> reverse;
		
		public BiDirectionalUnmodifiable(List<F> list, Function<? super F, ? extends T> remap, Function<? super T, ? extends F> reverse) {
			super(list, remap);
			this.reverse = reverse;
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public boolean contains(Object o) {
			return list.contains(reverse.apply((T) o));
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public boolean containsAll(@NotNull Collection<?> c) {
			return list.containsAll(new ConvertingCollection.BiDirectionalUnmodifiable<>((Collection<T>) c, reverse, remap));
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public int indexOf(Object o) {
			return list.indexOf(reverse.apply((T) o));
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public int lastIndexOf(Object o) {
			return list.lastIndexOf(reverse.apply((T) o));
		}
		
		@NotNull
		@Override
		public List<T> subList(int fromIndex, int toIndex) {
			return new ConvertingList.BiDirectionalUnmodifiable<>(list.subList(fromIndex, toIndex), remap, reverse);
		}
	}
	
	public static class BiDirectionalSparse<F, T> extends OneDirectionalUnmodifiable<F, T> {
		
		public Function<? super T, ? extends F> reverseSparse;
		
		public BiDirectionalSparse(List<F> list, Function<? super F, ? extends T> remap, Function<? super T, ? extends F> reverseSparse) {
			super(list, remap);
			this.reverseSparse = reverseSparse;
		}
		
		@Override
		public boolean add(T t) {
			return list.add(reverseSparse.apply(t));
		}
		
		@Override
		public boolean remove(Object o) {
			ListIterator<F> iter = list.listIterator();
			while (iter.hasNext()) {
				if (Objects.equals(remap.apply(iter.next()), o)) {
					iter.remove();
					return true;
				}
			}
			return false;
		}
		
		@Override
		public boolean addAll(@NotNull Collection<? extends T> c) {
			return list.addAll(new ConvertingCollection.OneDirectionalUnmodifiable<>(c, reverseSparse));
		}
		
		@Override
		public boolean addAll(int index, @NotNull Collection<? extends T> c) {
			return list.addAll(index, new ConvertingCollection.OneDirectionalUnmodifiable<>(c, reverseSparse));
		}
		
		@Override
		public boolean removeAll(@NotNull Collection<?> c) {
			boolean mod = false;
			Iterator<F> iter = list.iterator();
			while (iter.hasNext()) {
				T t = remap.apply(iter.next());
				if (c.contains(t)) {
					iter.remove();
					mod = true;
				}
			}
			return mod;
		}
		
		@Override
		public boolean retainAll(@NotNull Collection<?> c) {
			boolean mod = false;
			Iterator<F> iter = list.iterator();
			while (iter.hasNext()) {
				T t = remap.apply(iter.next());
				if (!c.contains(t)) {
					iter.remove();
					mod = true;
				}
			}
			return mod;
		}
		
		@Override
		public void replaceAll(UnaryOperator<T> operator) {
			list.replaceAll(f1 -> {
				T t1 = remap.apply(f1);
				T t2 = operator.apply(t1);
				return t1 == t2 ? f1 : reverseSparse.apply(t2);
			});
		}
		
		@Override
		public void sort(Comparator<? super T> c) {
			list.sort((o1, o2) -> c.compare(remap.apply(o1), remap.apply(o2)));
		}
		
		@Override
		public void clear() {
			list.clear();
		}
		
		@Override
		public T set(int index, T element) {
			return remap.apply(list.set(index, reverseSparse.apply(element)));
		}
		
		@Override
		public void add(int index, T element) {
			list.add(index, reverseSparse.apply(element));
		}
		
		@Override
		public T remove(int index) {
			return remap.apply(list.remove(index));
		}
		
		@NotNull
		@Override
		public Iterator<T> iterator() {
			return ConvertingIterator.createConverterOneDirectional(list.iterator(), remap);
		}
		
		@NotNull
		@Override
		public ListIterator<T> listIterator() {
			return new ConvertingListIterator.BiDirectional<>(list.listIterator(), remap, reverseSparse);
		}
		
		@NotNull
		@Override
		public ListIterator<T> listIterator(int index) {
			return new ConvertingListIterator.BiDirectional<>(list.listIterator(index), remap, reverseSparse);
		}
		
		@NotNull
		@Override
		public List<T> subList(int fromIndex, int toIndex) {
			return new ConvertingList.BiDirectionalSparse<>(list.subList(fromIndex, toIndex), remap, reverseSparse);
		}
		
		@Override
		public boolean removeIf(Predicate<? super T> filter) {
			return list.removeIf(f -> filter.test(remap.apply(f)));
		}
	}
	
	public static class BiDirectional<F, T> extends BiDirectionalSparse<F, T> {
		
		public Function<? super T, ? extends F> reverse;
		
		public BiDirectional(List<F> list, Function<? super F, ? extends T> remap, Function<? super T, ? extends F> reverse) {
			this(list, remap, reverse, reverse);
		}
		
		public BiDirectional(List<F> list, Function<? super F, ? extends T> remap, Function<? super T, ? extends F> reverseSparse, Function<? super T, ? extends F> reverse) {
			super(list, remap, reverseSparse);
			this.reverse = reverse;
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public boolean contains(Object o) {
			return list.contains(reverse.apply((T) o));
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public boolean remove(Object o) {
			return list.remove(reverse.apply((T) o));
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public boolean removeAll(@NotNull Collection<?> c) {
			return list.removeAll(new ConvertingCollection.BiDirectionalUnmodifiable<>((Collection<T>) c, reverse, remap));
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public boolean retainAll(@NotNull Collection<?> c) {
			return list.retainAll(new ConvertingCollection.BiDirectionalUnmodifiable<>((Collection<T>) c, reverse, remap));
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public boolean containsAll(@NotNull Collection<?> c) {
			return list.containsAll(new ConvertingCollection.BiDirectionalUnmodifiable<>((Collection<T>) c, reverse, remap));
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public int indexOf(Object o) {
			return list.indexOf(reverse.apply((T) o));
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public int lastIndexOf(Object o) {
			return list.lastIndexOf(reverse.apply((T) o));
		}
		
		@NotNull
		@Override
		public List<T> subList(int fromIndex, int toIndex) {
			return new ConvertingList.BiDirectional<>(list.subList(fromIndex, toIndex), remap, reverse, reverseSparse);
		}
	}
}
