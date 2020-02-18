package space.engine.delegate.map;

import org.jetbrains.annotations.NotNull;
import space.engine.baseobject.ToString;
import space.engine.delegate.collection.ConvertingCollection;
import space.engine.delegate.list.ConvertingList;
import space.engine.delegate.set.ConvertingSet;
import space.engine.delegate.set.UnmodifiableSet;
import space.engine.string.toStringHelper.ToStringHelper;
import space.engine.string.toStringHelper.ToStringHelper.ToStringHelperObjectsInstance;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A {@link ConvertingMap} converts <b>FROM</b> one {@link Map Map's} Value <b>TO</b> a different value with the help of provided {@link Function Functions} for conversion.<br>
 * It has multiple inner classes allowing for different usages for many different cases.<br>
 * All implementations are threadsafe if their underlying {@link ConvertingMap#map} is also threadsafe.<br>
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
 * <td>{@link OneDirectionalUnmodifiable OneDirectionalUnmodifiable}</td>
 * <td>No</td>
 * <td>
 * <ul>
 * <li>{@link OneDirectionalUnmodifiable#remap Function&lt;? super F, ? extends T&gt; remap}</li>
 * </ul>
 * </td>
 * <td>
 * <ul>
 * <li>{@link OneDirectionalUnmodifiable#containsValue(Object) containsValue(Object)}</li>
 * <li>{@link OneDirectionalUnmodifiable#values() values()}.{@link ConvertingCollection.OneDirectionalUnmodifiable#contains(Object) contains(Object)}</li>
 * <li>{@link OneDirectionalUnmodifiable#values() values()}.{@link ConvertingCollection.OneDirectionalUnmodifiable#containsAll(Collection) containsAll(Collection)}</li>
 * </ul>
 * </td>
 * <td>TO</td>
 * </tr>
 *
 * <tr>
 * <td>{@link BiDirectionalUnmodifiable BiDirectionalUnmodifiable}</td>
 * <td>No</td>
 * <td>
 * <ul>
 * <li>{@link BiDirectionalUnmodifiable#remap Function&lt;? super F, ? extends T&gt; remap}</li>
 * <li>{@link BiDirectionalUnmodifiable#remap Function&lt;? super T, ? extends F&gt; reverse}</li>
 * </ul>
 * </td>
 * <ul><li>none</li></ul>
 * <td>FORM</td>
 * </tr>
 *
 * <tr>
 * <td>{@link BiDirectionalSparse BiDirectionalSparse}</td>
 * <td>Yes</td>
 * <td>
 * <ul>
 * <li>{@link BiDirectionalSparse#remap Function&lt;? super F, ? extends T&gt; remap}</li>
 * <li>{@link BiDirectionalSparse#reverseSparse Function&lt;? super T, ? extends F&gt; reverseSparse}</li>
 * </ul>
 * </td>
 * <td>
 * <ul>
 * <li>{@link BiDirectionalSparse#containsValue(Object) containsValue(Object)}</li>
 * <li>{@link OneDirectionalUnmodifiable#values() values()}.{@link ConvertingCollection.BiDirectionalSparse#contains(Object) contains(Object)}</li>
 * <li>{@link OneDirectionalUnmodifiable#values() values()}.{@link ConvertingCollection.BiDirectionalSparse#containsAll(Collection) containsAll(Collection)}</li>
 * <li>{@link OneDirectionalUnmodifiable#values() values()}.{@link ConvertingCollection.BiDirectionalSparse#remove(Object) remove(Object)}</li>
 * <li>{@link OneDirectionalUnmodifiable#values() values()}.{@link ConvertingCollection.BiDirectionalSparse#removeAll(Collection) removeAll(Collection)}</li>
 * <li>{@link OneDirectionalUnmodifiable#values() values()}.{@link ConvertingCollection.BiDirectionalSparse#retainAll(Collection) retainAll(Collection)}</li>
 * </ul>
 * </td>
 * <td>TO</td>
 * </tr>
 *
 * <tr>
 * <td>{@link BiDirectional BiDirectional}</td>
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
public abstract class ConvertingMap<K, F, T> implements Map<K, T>, ToString {
	
	public Map<K, F> map;
	
	public ConvertingMap(Map<K, F> map) {
		this.map = map;
	}
	
	@NotNull
	@Override
	public <TSHTYPE> TSHTYPE toTSH(@NotNull ToStringHelper<TSHTYPE> api) {
		ToStringHelperObjectsInstance<TSHTYPE> tsh = api.createObjectInstance(this);
		tsh.add("map", this.map);
		return tsh.build();
	}
	
	@Override
	public String toString() {
		return toString0();
	}
	
	public static class OneDirectionalUnmodifiable<K, F, T> extends ConvertingMap<K, F, T> {
		
		public Function<? super F, ? extends T> remap;
		
		public OneDirectionalUnmodifiable(Map<K, F> map, Function<? super F, ? extends T> remap) {
			super(map);
			this.remap = remap;
		}
		
		@Override
		public int size() {
			return map.size();
		}
		
		@Override
		public boolean isEmpty() {
			return map.isEmpty();
		}
		
		@Override
		public boolean containsKey(Object key) {
			return get(key) != null;
		}
		
		@Override
		public boolean containsValue(Object value) {
			for (F f : map.values())
				if (Objects.equals(remap.apply(f), value))
					return true;
			return false;
		}
		
		@Override
		public T get(Object key) {
			return remap.apply(map.get(key));
		}
		
		@Override
		public T put(K key, T value) {
			throw new UnsupportedOperationException("unmodifiable");
		}
		
		@Override
		public T remove(Object key) {
			throw new UnsupportedOperationException("unmodifiable");
		}
		
		@Override
		public void putAll(@NotNull Map<? extends K, ? extends T> m) {
			throw new UnsupportedOperationException("unmodifiable");
		}
		
		@Override
		public void clear() {
			map.clear();
		}
		
		@NotNull
		@Override
		public Set<K> keySet() {
			return new UnmodifiableSet<>(map.keySet());
		}
		
		@NotNull
		@Override
		public Collection<T> values() {
			return new ConvertingCollection.OneDirectionalUnmodifiable<>(map.values(), remap);
		}
		
		@NotNull
		@Override
		public Set<Map.Entry<K, T>> entrySet() {
			return new ConvertingSet.BiDirectionalUnmodifiable<>(map.entrySet(), entry -> entry == null ? null : new Entry(entry), entry -> entry instanceof OneDirectionalUnmodifiable.Entry ? ((Entry) entry).entry : null);
		}
		
		@Override
		@SuppressWarnings("SuspiciousMethodCalls")
		public T getOrDefault(Object key, T defaultValue) {
			T ret = remap.apply(map.get(key));
			return ret == null ? defaultValue : ret;
		}
		
		@Override
		public void forEach(BiConsumer<? super K, ? super T> action) {
			map.forEach((k, f) -> action.accept(k, remap.apply(f)));
		}
		
		@Override
		public void replaceAll(BiFunction<? super K, ? super T, ? extends T> function) {
			throw new UnsupportedOperationException("unmodifiable");
		}
		
		@Override
		public T putIfAbsent(K key, T value) {
			throw new UnsupportedOperationException("unmodifiable");
		}
		
		@Override
		public boolean remove(Object key, Object value) {
			throw new UnsupportedOperationException("unmodifiable");
		}
		
		@Override
		public boolean replace(K key, T oldValue, T newValue) {
			throw new UnsupportedOperationException("unmodifiable");
		}
		
		@Override
		public T replace(K key, T value) {
			throw new UnsupportedOperationException("unmodifiable");
		}
		
		@Override
		public T computeIfAbsent(K key, Function<? super K, ? extends T> mappingFunction) {
			throw new UnsupportedOperationException("unmodifiable");
		}
		
		@Override
		public T computeIfPresent(K key, BiFunction<? super K, ? super T, ? extends T> remappingFunction) {
			throw new UnsupportedOperationException("unmodifiable");
		}
		
		@Override
		public T compute(K key, BiFunction<? super K, ? super T, ? extends T> remappingFunction) {
			throw new UnsupportedOperationException("unmodifiable");
		}
		
		@Override
		public T merge(K key, T value, BiFunction<? super T, ? super T, ? extends T> remappingFunction) {
			throw new UnsupportedOperationException("unmodifiable");
		}
		
		@NotNull
		@Override
		public <TSHTYPE> TSHTYPE toTSH(@NotNull ToStringHelper<TSHTYPE> api) {
			ToStringHelperObjectsInstance<TSHTYPE> tsh = api.createObjectInstance(this);
			tsh.add("map", this.map);
			tsh.add("remap", this.remap);
			return tsh.build();
		}
		
		public class Entry implements Map.Entry<K, T>, ToString {
			
			public Map.Entry<K, F> entry;
			
			public Entry(Map.Entry<K, F> entry) {
				this.entry = entry;
			}
			
			@Override
			public K getKey() {
				return entry.getKey();
			}
			
			@Override
			public T getValue() {
				return remap.apply(entry.getValue());
			}
			
			@Override
			public T setValue(T value) {
				throw new UnsupportedOperationException("unmodifiable");
			}
			
			@Override
			public int hashCode() {
				return Objects.hashCode(getKey()) ^ Objects.hashCode(getValue());
			}
			
			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (!(obj instanceof Map.Entry))
					return false;
				Map.Entry<?, ?> other = (Map.Entry<?, ?>) obj;
				return Objects.equals(getKey(), other.getKey()) && Objects.equals(getValue(), other.getValue());
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
	
	public static class BiDirectionalUnmodifiable<K, F, T> extends OneDirectionalUnmodifiable<K, F, T> {
		
		public Function<? super T, ? extends F> reverse;
		
		public BiDirectionalUnmodifiable(Map<K, F> map, Function<? super F, ? extends T> remap, Function<? super T, ? extends F> reverse) {
			super(map, remap);
			this.reverse = reverse;
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public boolean containsValue(Object value) {
			return map.containsValue(reverse.apply((T) value));
		}
		
		@NotNull
		@Override
		public Collection<T> values() {
			return new ConvertingCollection.BiDirectionalUnmodifiable<>(map.values(), remap, reverse);
		}
		
		@NotNull
		@Override
		public <TSHTYPE> TSHTYPE toTSH(@NotNull ToStringHelper<TSHTYPE> api) {
			ToStringHelperObjectsInstance<TSHTYPE> tsh = api.createObjectInstance(this);
			tsh.add("map", this.map);
			tsh.add("remap", this.remap);
			tsh.add("reverse", this.reverse);
			return tsh.build();
		}
	}
	
	public static class BiDirectionalSparse<K, F, T> extends OneDirectionalUnmodifiable<K, F, T> {
		
		public Function<? super T, ? extends F> reverseSparse;
		
		public BiDirectionalSparse(Map<K, F> map, Function<? super F, ? extends T> remap, Function<? super T, ? extends F> reverseSparse) {
			super(map, remap);
			this.reverseSparse = reverseSparse;
		}
		
		@Override
		public T put(K key, T value) {
			return remap.apply(map.put(key, reverseSparse.apply(value)));
		}
		
		@Override
		public T remove(Object key) {
			return remap.apply(map.remove(key));
		}
		
		@Override
		public void putAll(@NotNull Map<? extends K, ? extends T> m) {
			map.putAll(new ConvertingMap.OneDirectionalUnmodifiable<>(m, reverseSparse));
		}
		
		@NotNull
		@Override
		public Set<K> keySet() {
			return map.keySet();
		}
		
		@NotNull
		@Override
		public Collection<T> values() {
			return new ConvertingCollection.BiDirectionalSparse<>(map.values(), remap, reverseSparse);
		}
		
		@NotNull
		@Override
		public Set<Map.Entry<K, T>> entrySet() {
			return new ConvertingSet.BiDirectional<>(map.entrySet(),
													 entry -> entry == null ? null : new Entry(entry),
													 entry -> entry instanceof BiDirectionalSparse.Entry ? ((Entry) entry).entry : null);
		}
		
		@Override
		public void replaceAll(BiFunction<? super K, ? super T, ? extends T> function) {
			map.replaceAll((k, f) -> {
				T t = remap.apply(f);
				T tRet = function.apply(k, t);
				return t == tRet ? f : reverseSparse.apply(tRet);
			});
		}
		
		@Override
		public T putIfAbsent(K key, T value) {
			return remap.apply(map.computeIfAbsent(key, k -> reverseSparse.apply(value)));
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public boolean remove(Object key, Object value) {
			boolean[] ret = new boolean[1];
			map.compute((K) key, (k, f) -> {
				if (!Objects.equals(remap.apply(f), value))
					return f;
				ret[0] = true;
				return null;
			});
			return ret[0];
		}
		
		@Override
		public boolean replace(K key, T oldValue, T newValue) {
			boolean[] ret = new boolean[1];
			map.compute(key, (k, f) -> {
				if (!Objects.equals(remap.apply(f), oldValue))
					return f;
				ret[0] = true;
				return reverseSparse.apply(newValue);
			});
			return ret[0];
		}
		
		@Override
		public T replace(K key, T value) {
			return remap.apply(map.computeIfPresent(key, (k, f) -> reverseSparse.apply(value)));
		}
		
		@Override
		public T computeIfAbsent(K key, Function<? super K, ? extends T> mappingFunction) {
			return remap.apply(map.computeIfAbsent(key, k -> reverseSparse.apply(mappingFunction.apply(k))));
		}
		
		@Override
		public T computeIfPresent(K key, BiFunction<? super K, ? super T, ? extends T> remappingFunction) {
			return remap.apply(map.computeIfPresent(key, (k, f) -> {
				T t = remap.apply(f);
				T ret = remappingFunction.apply(k, t);
				return ret == t ? f : reverseSparse.apply(ret);
			}));
		}
		
		@Override
		public T compute(K key, BiFunction<? super K, ? super T, ? extends T> remappingFunction) {
			return remap.apply(map.computeIfPresent(key, (k, f) -> {
				T t = remap.apply(f);
				T ret = remappingFunction.apply(k, t);
				return ret == t ? f : reverseSparse.apply(ret);
			}));
		}
		
		@Override
		public T merge(K key, T value, BiFunction<? super T, ? super T, ? extends T> remappingFunction) {
			return remap.apply(map.merge(key, reverseSparse.apply(value), (f1, f2) -> {
				T t1 = remap.apply(f1);
				T t2 = remap.apply(f2);
				T ret = remappingFunction.apply(t1, t2);
				if (ret == t1)
					return f1;
				if (ret == t2)
					return f2;
				return reverseSparse.apply(ret);
			}));
		}
		
		public class Entry extends OneDirectionalUnmodifiable<K, F, T>.Entry {
			
			public Entry(Map.Entry<K, F> entry) {
				super(entry);
			}
			
			@Override
			public T setValue(T value) {
				return remap.apply(entry.setValue(reverseSparse.apply(value)));
			}
		}
	}
	
	public static class BiDirectional<K, F, T> extends BiDirectionalSparse<K, F, T> {
		
		public Function<? super T, ? extends F> reverse;
		
		public BiDirectional(Map<K, F> map, Function<? super F, ? extends T> remap, Function<? super T, ? extends F> reverse) {
			this(map, remap, reverse, reverse);
		}
		
		public BiDirectional(Map<K, F> map, Function<? super F, ? extends T> remap, Function<? super T, ? extends F> reverseSparse, Function<? super T, ? extends F> reverse) {
			super(map, remap, reverseSparse);
			this.reverse = reverse;
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public boolean containsValue(Object value) {
			return map.containsValue(reverse.apply((T) value));
		}
		
		@NotNull
		@Override
		public Collection<T> values() {
			return new ConvertingCollection.BiDirectional<>(map.values(), remap, reverse);
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public boolean remove(Object key, Object value) {
			return map.remove(key, reverse.apply((T) value));
		}
	}
}
