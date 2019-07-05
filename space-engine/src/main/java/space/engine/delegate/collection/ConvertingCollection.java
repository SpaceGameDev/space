package space.engine.delegate.collection;

import org.jetbrains.annotations.NotNull;
import space.engine.baseobject.ToString;
import space.engine.delegate.iterator.ConvertingIterator;
import space.engine.string.toStringHelper.ToStringHelper;
import space.engine.string.toStringHelper.ToStringHelper.ToStringHelperObjectsInstance;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A {@link ConvertingCollection} converts <b>FROM</b> one {@link Collection Collection's} Value <b>TO</b> a different value with the help of provided {@link Function Functions} for conversion.<br>
 * It has multiple inner classes allowing for different usages for many different cases.<br>
 * All implementations are threadsafe if their underlying {@link ConvertingCollection#coll} is also threadsafe.<br>
 * <br>
 * 3 Types of Functions:
 * <table border=1>
 * <tr><td>Function</td><td>Remap direction</td><td>Comment</td></tr>
 * <tr><td>{@link ConvertingCollection.OneDirectionalUnmodifiable#remap Function&lt;? super F, ? extends T&gt; remap}</td><td>F -&gt; T </td><td>Always required.</td></tr>
 * <tr><td>{@link ConvertingCollection.BiDirectionalSparse#reverseSparse Function&lt;? super T, ? extends F&gt; reverseSparse}</td><td>T -&gt; F </td><td>Only called when the returned value is added to this Object. If available defaults to reverse. </td></tr>
 * <tr><td>{@link ConvertingCollection.BiDirectional#reverse Function&lt;? super T, ? extends F&gt; reverse}</td><td>T -&gt; F </td><td>Will be called even for simple Operations, where the Result may not be stored and only used for eg. comparision. </td></tr>
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
 * <td>Threadsafe if</td>
 * <td>Comparision Object</td>
 * </tr>
 *
 * <tr>
 * <td>{@link ConvertingCollection.OneDirectionalUnmodifiable OneDirectionalUnmodifiable}</td>
 * <td>No</td>
 * <td>
 * <ul>
 * <li>{@link ConvertingCollection.OneDirectionalUnmodifiable#remap Function&lt;? super F, ? extends T&gt; remap}</li>
 * </ul>
 * </td>
 * <td>
 * <ul>
 * <li>{@link ConvertingCollection.OneDirectionalUnmodifiable#contains(Object) contains(Object)}</li>
 * <li>{@link ConvertingCollection.OneDirectionalUnmodifiable#containsAll(Collection) containsAll(Collection)}</li>
 * </ul>
 * </td>
 * <td>{@link OneDirectionalUnmodifiable#coll coll} is threadsafe</td>
 * <td>TO</td>
 * </tr>
 *
 * <tr>
 * <td>{@link ConvertingCollection.BiDirectionalUnmodifiable BiDirectionalUnmodifiable}</td>
 * <td>No</td>
 * <td>
 * <ul>
 * <li>{@link ConvertingCollection.BiDirectionalUnmodifiable#remap Function&lt;? super F, ? extends T&gt; remap}</li>
 * <li>{@link ConvertingCollection.BiDirectionalUnmodifiable#reverse Function&lt;? super T, ? extends F&gt; reverse}</li>
 * </ul>
 * </td>
 * <td>
 * <ul><li>none</li></ul>
 * </td>
 * <td>{@link OneDirectionalUnmodifiable#coll coll} is threadsafe</td>
 * <td>FROM</td>
 * </tr>
 *
 * <tr>
 * <td>{@link ConvertingCollection.BiDirectionalSparse BiDirectionalSparse}</td>
 * <td>Yes</td>
 * <td>
 * <ul>
 * <li>{@link ConvertingCollection.BiDirectionalSparse#remap Function&lt;? super F, ? extends T&gt; remap}</li>
 * <li>{@link ConvertingCollection.BiDirectionalSparse#reverseSparse Function&lt;? super T, ? extends F&gt; reverseSparse}</li>
 * </ul>
 * </td>
 * <td>
 * <ul>
 * <li>{@link ConvertingCollection.BiDirectionalSparse#contains(Object) contains(Object)}</li>
 * <li>{@link ConvertingCollection.BiDirectionalSparse#containsAll(Collection) containsAll(Collection)}</li>
 * <li>{@link ConvertingCollection.BiDirectionalSparse#remove(Object) remove(Object)}</li>
 * <li>{@link ConvertingCollection.BiDirectionalSparse#removeAll(Collection) removeAll(Collection)}</li>
 * <li>{@link ConvertingCollection.BiDirectionalSparse#retainAll(Collection) retainAll(Collection)}</li>
 * </ul>
 * </td>
 * <td>{@link OneDirectionalUnmodifiable#coll coll} is threadsafe</td>
 * <td>TO</td>
 * </tr>
 *
 * <tr>
 * <td>{@link ConvertingCollection.BiDirectional BiDirectional}</td>
 * <td>Yes</td>
 * <td>
 * <ul>
 * <li>{@link ConvertingCollection.BiDirectional#remap Function&lt;? super F, ? extends T&gt; remap}</li>
 * <li>{@link ConvertingCollection.BiDirectional#reverseSparse Function&lt;? super T, ? extends F&gt; reverseSparse} (defaults to reverse)</li>
 * <li>{@link ConvertingCollection.BiDirectional#reverse Function&lt;? super T, ? extends F&gt; reverse}</li>
 * </ul>
 * </td>
 * <td>
 * <ul><li>none</li></ul>
 * </td>
 * <td>{@link OneDirectionalUnmodifiable#coll coll} is threadsafe</td>
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
public abstract class ConvertingCollection<F, T> implements Collection<T>, ToString {
	
	public Collection<F> coll;
	
	protected ConvertingCollection(Collection<F> coll) {
		this.coll = coll;
	}
	
	@NotNull
	@Override
	public <TSHTYPE> TSHTYPE toTSH(@NotNull ToStringHelper<TSHTYPE> api) {
		ToStringHelperObjectsInstance<TSHTYPE> tsh = api.createObjectInstance(this);
		tsh.add("coll", this.coll);
		return tsh.build();
	}
	
	@Override
	public String toString() {
		return toString0();
	}
	
	public static class OneDirectionalUnmodifiable<F, T> extends ConvertingCollection<F, T> {
		
		public Function<? super F, ? extends T> remap;
		
		public OneDirectionalUnmodifiable(Collection<F> coll, Function<? super F, ? extends T> remap) {
			super(coll);
			this.remap = remap;
		}
		
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
			for (F f : coll)
				if (Objects.equals(remap.apply(f), o))
					return true;
			return false;
		}
		
		@NotNull
		@Override
		public Iterator<T> iterator() {
			return ConvertingIterator.createConverterOneDirectionalUnmodifiable(coll.iterator(), remap);
		}
		
		@NotNull
		@Override
		public Object[] toArray() {
			//noinspection unchecked
			F[] org = (F[]) coll.toArray();
			Object[] ret = new Object[org.length];
			for (int i = 0; i < org.length; i++)
				ret[i] = remap.apply(org[i]);
			return ret;
		}
		
		@NotNull
		@Override
		@SuppressWarnings("unchecked")
		public <T1> T1[] toArray(@NotNull T1[] a) {
			F[] org = (F[]) coll.toArray();
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
			for (F f : coll)
				if (!c.contains(remap.apply(f)))
					return false;
			return true;
		}
		
		@Override
		public boolean addAll(@NotNull Collection<? extends T> c) {
			throw new UnsupportedOperationException("Unmodifiable");
		}
		
		@Override
		public boolean removeAll(@NotNull Collection<?> c) {
			throw new UnsupportedOperationException("Unmodifiable");
		}
		
		@Override
		public boolean removeIf(Predicate<? super T> filter) {
			throw new UnsupportedOperationException("Unmodifiable");
		}
		
		@Override
		public boolean retainAll(@NotNull Collection<?> c) {
			throw new UnsupportedOperationException("Unmodifiable");
		}
		
		@Override
		public void clear() {
			throw new UnsupportedOperationException("Unmodifiable");
		}
		
		@Override
		public void forEach(Consumer<? super T> action) {
			coll.forEach(f -> action.accept(remap.apply(f)));
		}
		
		@NotNull
		@Override
		public <TSHTYPE> TSHTYPE toTSH(@NotNull ToStringHelper<TSHTYPE> api) {
			ToStringHelperObjectsInstance<TSHTYPE> tsh = api.createObjectInstance(this);
			tsh.add("coll", this.coll);
			tsh.add("remap", this.remap);
			return tsh.build();
		}
	}
	
	public static class BiDirectionalUnmodifiable<F, T> extends OneDirectionalUnmodifiable<F, T> {
		
		public Function<? super T, ? extends F> reverse;
		
		public BiDirectionalUnmodifiable(Collection<F> coll, Function<? super F, ? extends T> remap, Function<? super T, ? extends F> reverse) {
			super(coll, remap);
			this.reverse = reverse;
		}
		
		//access methods
		@Override
		@SuppressWarnings("unchecked")
		public boolean contains(Object o) {
			return coll.contains(reverse.apply((T) o));
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public boolean containsAll(@NotNull Collection<?> c) {
			return coll.containsAll(new OneDirectionalUnmodifiable<>((Collection<T>) c, reverse));
		}
		
		@NotNull
		@Override
		public <TSHTYPE> TSHTYPE toTSH(@NotNull ToStringHelper<TSHTYPE> api) {
			ToStringHelperObjectsInstance<TSHTYPE> tsh = api.createObjectInstance(this);
			tsh.add("coll", this.coll);
			tsh.add("remap", this.remap);
			tsh.add("reverse", this.reverse);
			return tsh.build();
		}
	}
	
	public static class BiDirectionalSparse<F, T> extends OneDirectionalUnmodifiable<F, T> {
		
		public Function<? super T, ? extends F> reverseSparse;
		
		public BiDirectionalSparse(Collection<F> coll, Function<? super F, ? extends T> remap, Function<? super T, ? extends F> reverseSparse) {
			super(coll, remap);
			this.reverseSparse = reverseSparse;
		}
		
		@Override
		public boolean add(T t) {
			return coll.add(reverseSparse.apply(t));
		}
		
		@Override
		public boolean remove(Object o) {
			Iterator<F> iter = coll.iterator();
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
			return coll.addAll(new OneDirectionalUnmodifiable<>(c, reverseSparse));
		}
		
		@Override
		public boolean removeAll(@NotNull Collection<?> c) {
			boolean mod = false;
			Iterator<F> iter = coll.iterator();
			while (iter.hasNext()) {
				T t = remap.apply(iter.next());
				if (c.contains(t)) {
					iter.remove();
					mod = true;
				}
			}
			return mod;
		}
		
		@NotNull
		@Override
		public Iterator<T> iterator() {
			return ConvertingIterator.createConverterOneDirectional(coll.iterator(), remap);
		}
		
		@Override
		public boolean retainAll(@NotNull Collection<?> c) {
			boolean mod = false;
			Iterator<F> iter = coll.iterator();
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
		public boolean removeIf(Predicate<? super T> filter) {
			return coll.removeIf(f -> filter.test(remap.apply(f)));
		}
		
		@NotNull
		@Override
		public <TSHTYPE> TSHTYPE toTSH(@NotNull ToStringHelper<TSHTYPE> api) {
			ToStringHelperObjectsInstance<TSHTYPE> tsh = api.createObjectInstance(this);
			tsh.add("coll", this.coll);
			tsh.add("remap", this.remap);
			tsh.add("reverseSparse", this.reverseSparse);
			return tsh.build();
		}
	}
	
	public static class BiDirectional<F, T> extends BiDirectionalSparse<F, T> {
		
		public Function<? super T, ? extends F> reverse;
		
		public BiDirectional(Collection<F> coll, Function<? super F, ? extends T> remap, Function<? super T, ? extends F> reverse) {
			this(coll, remap, reverse, reverse);
		}
		
		public BiDirectional(Collection<F> coll, Function<? super F, ? extends T> remap, Function<? super T, ? extends F> reverse, Function<? super T, ? extends F> reverseSparse) {
			super(coll, remap, reverseSparse);
			this.reverse = reverse;
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public boolean remove(Object o) {
			return coll.remove(reverse.apply((T) o));
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public boolean removeAll(@NotNull Collection<?> c) {
			return coll.removeAll(new OneDirectionalUnmodifiable<>((Collection<T>) c, reverse));
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public boolean retainAll(@NotNull Collection<?> c) {
			return coll.retainAll(new OneDirectionalUnmodifiable<>((Collection<T>) c, reverse));
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public boolean contains(Object o) {
			return coll.contains(reverse.apply((T) o));
		}
		
		@NotNull
		@Override
		public <TSHTYPE> TSHTYPE toTSH(@NotNull ToStringHelper<TSHTYPE> api) {
			ToStringHelperObjectsInstance<TSHTYPE> tsh = api.createObjectInstance(this);
			tsh.add("coll", this.coll);
			tsh.add("remap", this.remap);
			tsh.add("reverseSparse", this.reverseSparse);
			tsh.add("reverse", this.reverse);
			return tsh.build();
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public boolean containsAll(@NotNull Collection<?> c) {
			return coll.containsAll(new OneDirectionalUnmodifiable<>((Collection<T>) c, reverse));
		}
	}
}
