package space.engine.delegate.indexmap;

import org.jetbrains.annotations.NotNull;
import space.engine.baseobject.ToString;
import space.engine.delegate.collection.ConvertingCollection;
import space.engine.indexmap.IndexMap;
import space.engine.string.toStringHelper.ToStringHelper;
import space.engine.string.toStringHelper.ToStringHelper.ToStringHelperObjectsInstance;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A {@link ConvertingIndexMap} converts <b>FROM</b> one {@link IndexMap IndexMap's} Value <b>TO</b> a different value with the help of provided {@link Function Functions} for conversion.<br>
 * It has multiple inner classes allowing for different usages for many different cases.<br>
 * All implementations are threadsafe if their underlying {@link ConvertingIndexMap#indexMap} is also threadsafe.<br>
 * <br>
 * 3 Types of Functions:
 * <table border=1>
 * <tr><td>Function</td><td>Remap direction</td><td>Comment</td></tr>
 * <tr><td>{@link ConvertingIndexMap.OneDirectionalUnmodifiable#remap Function&lt;? super F, ? extends T&gt; remap}</td><td>F -&gt; T </td><td>Always required.</td></tr>
 * <tr><td>{@link ConvertingIndexMap.BiDirectionalSparse#reverseSparse Function&lt;? super T, ? extends F&gt; reverseSparse}</td><td>T -&gt; F </td><td>Only called when the returned value is added to this Object. If available defaults to reverse. </td></tr>
 * <tr><td>{@link ConvertingIndexMap.BiDirectional#reverse Function&lt;? super T, ? extends F&gt; reverse}</td><td>T -&gt; F </td><td>Will be called even for simple Operations, where the Result may not be stored and only used for eg. comparision. </td></tr>
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
 * <td>{@link ConvertingIndexMap.OneDirectionalUnmodifiable OneDirectionalUnmodifiable}</td>
 * <td>No</td>
 * <td>
 * <ul>
 * <li>{@link ConvertingIndexMap.OneDirectionalUnmodifiable#remap Function&lt;? super F, ? extends T&gt; remap}</li>
 * </ul>
 * </td>
 * <td>
 * <ul>
 * <li>{@link ConvertingIndexMap.OneDirectionalUnmodifiable#values() values()}.{@link ConvertingCollection.OneDirectionalUnmodifiable#contains(Object) contains(Object)}</li>
 * <li>{@link ConvertingIndexMap.OneDirectionalUnmodifiable#values() values()}.{@link ConvertingCollection.OneDirectionalUnmodifiable#containsAll(Collection) containsAll(Collection)}</li>
 * </ul>
 * </td>
 * <td>TO</td>
 * </tr>
 *
 * <tr>
 * <td>{@link ConvertingIndexMap.BiDirectionalUnmodifiable BiDirectionalUnmodifiable}</td>
 * <td>No</td>
 * <td>
 * <ul>
 * <li>{@link ConvertingIndexMap.BiDirectionalUnmodifiable#remap Function&lt;? super F, ? extends T&gt; remap}</li>
 * <li>{@link ConvertingIndexMap.BiDirectionalUnmodifiable#reverse Function&lt;? super T, ? extends F&gt; reverse}</li>
 * </ul>
 * </td>
 * <td>
 * <ul><li>none</li></ul>
 * </td>
 * <td>FROM</td>
 * </tr>
 *
 * <tr>
 * <td>{@link ConvertingIndexMap.BiDirectionalSparse BiDirectionalSparse}</td>
 * <td>Yes</td>
 * <td>
 * <ul>
 * <li>{@link ConvertingIndexMap.BiDirectionalSparse#remap Function&lt;? super F, ? extends T&gt; remap}</li>
 * <li>{@link ConvertingIndexMap.BiDirectionalSparse#reverseSparse Function&lt;? super T, ? extends F&gt; reverseSparse}</li>
 * </ul>
 * </td>
 * <td>
 * <ul>
 * <li>{@link ConvertingIndexMap.BiDirectionalSparse#values() values()}.{@link ConvertingCollection.OneDirectionalUnmodifiable#contains(Object) contains(Object)}</li>
 * <li>{@link ConvertingIndexMap.BiDirectionalSparse#values() values()}.{@link ConvertingCollection.OneDirectionalUnmodifiable#containsAll(Collection) containsAll(Collection)}</li>
 * </ul>
 * </td>
 * <td>TO</td>
 * </tr>
 *
 * <tr>
 * <td>{@link ConvertingIndexMap.BiDirectional BiDirectional}</td>
 * <td>Yes</td>
 * <td>
 * <ul>
 * <li>{@link ConvertingIndexMap.BiDirectional#remap Function&lt;? super F, ? extends T&gt; remap}</li>
 * <li>{@link ConvertingIndexMap.BiDirectional#reverseSparse Function&lt;? super T, ? extends F&gt; reverseSparse} (defaults to reverse)</li>
 * <li>{@link ConvertingIndexMap.BiDirectional#reverse Function&lt;? super T, ? extends F&gt; reverse}</li>
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
public abstract class ConvertingIndexMap<F, T> implements IndexMap<T>, ToString {
	
	public IndexMap<F> indexMap;
	
	public ConvertingIndexMap(IndexMap<F> indexMap) {
		this.indexMap = indexMap;
	}
	
	@NotNull
	@Override
	public <TSHTYPE> TSHTYPE toTSH(@NotNull ToStringHelper<TSHTYPE> api) {
		ToStringHelperObjectsInstance<TSHTYPE> tsh = api.createObjectInstance(this);
		tsh.add("indexMap", this.indexMap);
		return tsh.build();
	}
	
	@Override
	public String toString() {
		return toString0();
	}
	
	public static class OneDirectionalUnmodifiable<F, T> extends ConvertingIndexMap<F, T> {
		
		public Function<? super F, ? extends T> remap;
		
		public OneDirectionalUnmodifiable(IndexMap<F> indexMap, Function<? super F, ? extends T> remap) {
			super(indexMap);
			this.remap = remap;
		}
		
		@Override
		public int size() {
			return indexMap.size();
		}
		
		@Override
		public boolean contains(int index) {
			return get(index) != null;
		}
		
		@Override
		public T get(int index) {
			return remap.apply(indexMap.get(index));
		}
		
		@Override
		public boolean isEmpty() {
			return indexMap.isEmpty();
		}
		
		@Override
		public T put(int index, T value) {
			throw new UnsupportedOperationException("unmodifiable");
		}
		
		@Override
		public T remove(int index) {
			throw new UnsupportedOperationException("unmodifiable");
		}
		
		@Override
		public T[] toArray() {
			F[] org = indexMap.toArray();
			//noinspection unchecked
			T[] ret = (T[]) new Object[org.length];
			for (int i = 0; i < org.length; i++)
				ret[i] = remap.apply(org[i]);
			return ret;
		}
		
		@NotNull
		@Override
		public IndexMap.Entry<T> getEntry(int index) {
			return new Entry(indexMap.getEntry(index));
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public T[] toArray(@NotNull T[] a) {
			F[] org = indexMap.toArray();
			if (a.length < org.length) {
				//new instance
				T[] ret = (T[]) Array.newInstance(a.getClass(), org.length);
				for (int i = 0; i < org.length; i++)
					ret[i] = remap.apply((org[i]));
				return ret;
			}
			
			//use existing
			for (int i = 0; i < org.length; i++)
				a[i] = remap.apply((org[i]));
			return a;
		}
		
		@Override
		public void putAll(@NotNull IndexMap<? extends T> indexMap) {
			throw new UnsupportedOperationException("unmodifiable");
		}
		
		@Override
		public void putAllIfAbsent(@NotNull IndexMap<? extends T> indexMap) {
			throw new UnsupportedOperationException("unmodifiable");
		}
		
		@Override
		public T getOrDefault(int index, T def) {
			T apply = remap.apply(indexMap.get(index));
			return apply == null ? def : apply;
		}
		
		@Override
		public T putIfAbsent(int index, T value) {
			throw new UnsupportedOperationException("unmodifiable");
		}
		
		@Override
		public T putIfPresent(int index, T t) {
			throw new UnsupportedOperationException("unmodifiable");
		}
		
		@Override
		public boolean replace(int index, T oldValue, T newValue) {
			throw new UnsupportedOperationException("unmodifiable");
		}
		
		@Override
		public boolean replace(int index, T oldValue, @NotNull Supplier<? extends T> newValue) {
			throw new UnsupportedOperationException("unmodifiable");
		}
		
		@Override
		public boolean remove(int index, T value) {
			throw new UnsupportedOperationException("unmodifiable");
		}
		
		@Override
		public void clear() {
			throw new UnsupportedOperationException("unmodifiable");
		}
		
		@Override
		public T compute(int index, @NotNull ComputeFunction<? super T, ? extends T> function) {
			throw new UnsupportedOperationException("unmodifiable");
		}
		
		@Override
		public T computeIfAbsent(int index, @NotNull Supplier<? extends T> supplier) {
			throw new UnsupportedOperationException("unmodifiable");
		}
		
		@Override
		public T computeIfPresent(int index, @NotNull Supplier<? extends T> supplier) {
			throw new UnsupportedOperationException("unmodifiable");
		}
		
		@NotNull
		@Override
		public Collection<T> values() {
			return new ConvertingCollection.OneDirectionalUnmodifiable<>(indexMap.values(), remap);
		}
		
		@NotNull
		@Override
		public Collection<IndexMap.Entry<T>> entrySet() {
			return new ConvertingCollection.BiDirectionalUnmodifiable<>(indexMap.entrySet(),
																		entry -> entry == null ? null : new Entry(entry),
																		entry -> entry instanceof OneDirectionalUnmodifiable.Entry ? ((Entry) entry).entry : null);
		}
		
		@NotNull
		@Override
		public <TSHTYPE> TSHTYPE toTSH(@NotNull ToStringHelper<TSHTYPE> api) {
			ToStringHelperObjectsInstance<TSHTYPE> tsh = api.createObjectInstance(this);
			tsh.add("indexMap", this.indexMap);
			tsh.add("remap", this.remap);
			return tsh.build();
		}
		
		public class Entry implements IndexMap.Entry<T>, ToString {
			
			public IndexMap.Entry<F> entry;
			
			public Entry(IndexMap.Entry<F> entry) {
				this.entry = entry;
			}
			
			@Override
			public int getIndex() {
				return entry.getIndex();
			}
			
			@Override
			public T getValue() {
				return remap.apply(entry.getValue());
			}
			
			public void setValue(T v) {
				throw new UnsupportedOperationException("unmodifiable");
			}
			
			@Override
			public void remove() {
				throw new UnsupportedOperationException("unmodifiable");
			}
			
			@Override
			public int hashCode() {
				return Integer.hashCode(this.getIndex()) ^ Objects.hashCode(this.getValue());
			}
			
			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (!(obj instanceof IndexMap.Entry))
					return false;
				IndexMap.Entry other = (IndexMap.Entry) obj;
				return this.getIndex() == other.getIndex() && Objects.equals(this.getValue(), other.getValue());
			}
			
			@Override
			public String toString() {
				return toString0();
			}
			
			@NotNull
			@Override
			public <TSH> TSH toTSH(@NotNull ToStringHelper<TSH> api) {
				ToStringHelperObjectsInstance<TSH> tsh = api.createObjectInstance(this);
				tsh.add("entry", this.entry);
				return tsh.build();
			}
		}
	}
	
	public static class BiDirectionalUnmodifiable<F, T> extends OneDirectionalUnmodifiable<F, T> {
		
		public Function<? super T, ? extends F> reverse;
		
		public BiDirectionalUnmodifiable(IndexMap<F> indexMap, Function<? super F, ? extends T> remap, Function<? super T, ? extends F> reverse) {
			super(indexMap, remap);
			this.reverse = reverse;
		}
		
		@NotNull
		@Override
		public Collection<T> values() {
			return new ConvertingCollection.BiDirectionalUnmodifiable<>(indexMap.values(), remap, reverse);
		}
		
		@NotNull
		@Override
		public <TSHTYPE> TSHTYPE toTSH(@NotNull ToStringHelper<TSHTYPE> api) {
			ToStringHelperObjectsInstance<TSHTYPE> tsh = api.createObjectInstance(this);
			tsh.add("indexMap", this.indexMap);
			tsh.add("remap", this.remap);
			tsh.add("reverse", this.reverse);
			return tsh.build();
		}
	}
	
	public static class BiDirectionalSparse<F, T> extends OneDirectionalUnmodifiable<F, T> {
		
		public Function<? super T, ? extends F> reverseSparse;
		
		public BiDirectionalSparse(IndexMap<F> indexMap, Function<? super F, ? extends T> remap, Function<? super T, ? extends F> reverseSparse) {
			super(indexMap, remap);
			this.reverseSparse = reverseSparse;
		}
		
		@Override
		public T put(int index, T value) {
			return remap.apply(indexMap.put(index, reverseSparse.apply(value)));
		}
		
		@Override
		public T remove(int index) {
			return remap.apply(indexMap.remove(index));
		}
		
		@NotNull
		@Override
		public IndexMap.Entry<T> getEntry(int index) {
			return new Entry(indexMap.getEntry(index));
		}
		
		@Override
		public void putAll(@NotNull IndexMap<? extends T> indexMap) {
			this.indexMap.putAll(new ConvertingIndexMap.OneDirectionalUnmodifiable<>(indexMap, reverseSparse));
		}
		
		@Override
		public void putAllIfAbsent(@NotNull IndexMap<? extends T> indexMap) {
			this.indexMap.putAllIfAbsent(new OneDirectionalUnmodifiable<>(indexMap, reverseSparse));
		}
		
		@Override
		public T putIfAbsent(int index, T value) {
			return remap.apply(indexMap.computeIfAbsent(index, () -> reverseSparse.apply(value)));
		}
		
		@Override
		public T putIfPresent(int index, T value) {
			return remap.apply(indexMap.computeIfPresent(index, () -> reverseSparse.apply(value)));
		}
		
		@Override
		public boolean replace(int index, T oldValue, T newValue) {
			boolean[] ret = new boolean[1];
			indexMap.compute(index, (index1, currValue) -> (ret[0] = Objects.equals(remap.apply(currValue), oldValue)) ? reverseSparse.apply(newValue) : currValue);
			return ret[0];
		}
		
		@Override
		public boolean replace(int index, T oldValue, @NotNull Supplier<? extends T> newValue) {
			boolean[] ret = new boolean[1];
			indexMap.compute(index, (index1, currValue) -> (ret[0] = Objects.equals(remap.apply(currValue), oldValue)) ? reverseSparse.apply(newValue.get()) : currValue);
			return ret[0];
		}
		
		@Override
		public boolean remove(int index, T value) {
			boolean[] ret = new boolean[1];
			indexMap.compute(index, (index1, currValue) -> (ret[0] = Objects.equals(remap.apply(currValue), value)) ? null : currValue);
			return ret[0];
		}
		
		@Override
		public T compute(int index, @NotNull ComputeFunction<? super T, ? extends T> function) {
			return remap.apply(indexMap.compute(index, (index1, f1) -> {
				T t1 = remap.apply(f1);
				T t2 = function.apply(index, t1);
				return t1 == t2 ? f1 : reverseSparse.apply(t2);
			}));
		}
		
		@Override
		public T computeIfAbsent(int index, @NotNull Supplier<? extends T> supplier) {
			return remap.apply(indexMap.computeIfAbsent(index, () -> reverseSparse.apply(supplier.get())));
		}
		
		@Override
		public T computeIfPresent(int index, @NotNull Supplier<? extends T> supplier) {
			return remap.apply(indexMap.computeIfPresent(index, () -> reverseSparse.apply(supplier.get())));
		}
		
		@Override
		public void clear() {
			indexMap.clear();
		}
		
		@NotNull
		@Override
		public Collection<IndexMap.Entry<T>> entrySet() {
			return new ConvertingCollection.BiDirectional<>(indexMap.entrySet(),
															entry -> entry == null ? null : new Entry(entry),
															entry -> entry instanceof BiDirectionalSparse.Entry ? ((Entry) entry).entry : null);
		}
		
		@NotNull
		@Override
		public <TSHTYPE> TSHTYPE toTSH(@NotNull ToStringHelper<TSHTYPE> api) {
			ToStringHelperObjectsInstance<TSHTYPE> tsh = api.createObjectInstance(this);
			tsh.add("indexMap", this.indexMap);
			tsh.add("remap", this.remap);
			tsh.add("reverseSparse", this.reverseSparse);
			return tsh.build();
		}
		
		public class Entry extends OneDirectionalUnmodifiable<F, T>.Entry {
			
			public Entry(IndexMap.Entry<F> entry) {
				super(entry);
			}
			
			@Override
			public void setValue(T v) {
				entry.setValue(reverseSparse.apply(v));
			}
			
			@Override
			public void remove() {
				entry.remove();
			}
		}
	}
	
	public static class BiDirectional<F, T> extends BiDirectionalSparse<F, T> {
		
		public Function<? super T, ? extends F> reverse;
		
		public BiDirectional(IndexMap<F> indexMap, Function<? super F, ? extends T> remap, Function<? super T, ? extends F> reverse) {
			this(indexMap, remap, reverse, reverse);
		}
		
		public BiDirectional(IndexMap<F> indexMap, Function<? super F, ? extends T> remap, Function<? super T, ? extends F> reverseSparse, Function<? super T, ? extends F> reverse) {
			super(indexMap, remap, reverseSparse);
			this.reverse = reverse;
		}
		
		@Override
		public boolean replace(int index, T oldValue, T newValue) {
			return indexMap.replace(index, reverse.apply(oldValue), () -> reverseSparse.apply(newValue));
		}
		
		@Override
		public boolean replace(int index, T oldValue, @NotNull Supplier<? extends T> newValue) {
			return indexMap.replace(index, reverse.apply(oldValue), () -> reverseSparse.apply(newValue.get()));
		}
		
		@Override
		public boolean remove(int index, T value) {
			return indexMap.remove(index, reverse.apply(value));
		}
		
		@NotNull
		@Override
		public <TSHTYPE> TSHTYPE toTSH(@NotNull ToStringHelper<TSHTYPE> api) {
			ToStringHelperObjectsInstance<TSHTYPE> tsh = api.createObjectInstance(this);
			tsh.add("indexMap", this.indexMap);
			tsh.add("remap", this.remap);
			tsh.add("reverseSparse", this.reverseSparse);
			tsh.add("reverse", this.reverse);
			return tsh.build();
		}
		
		@NotNull
		@Override
		public Collection<T> values() {
			return new ConvertingCollection.BiDirectionalUnmodifiable<>(indexMap.values(), remap, reverse);
		}
	}
}
