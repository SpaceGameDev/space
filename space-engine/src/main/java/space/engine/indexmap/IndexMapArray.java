package space.engine.indexmap;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.engine.ArrayUtils;
import space.engine.baseobject.ToString;
import space.engine.delegate.collection.UnmodifiableCollection;
import space.engine.delegate.iterator.Iteratorable;
import space.engine.string.toStringHelper.ToStringHelper;

import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class IndexMapArray<VALUE> implements IndexMap<VALUE>, ToString {
	
	public static final int DEFAULT_CAPACITY = 16;
	public static final int EXPAND_SHIFT = 1;
	
	protected final VALUE defaultObject;
	public VALUE[] array;
	public int length;
	
	public IndexMapArray() {
		this(null, DEFAULT_CAPACITY);
	}
	
	public IndexMapArray(int initCapacity) {
		this(null, initCapacity);
	}
	
	public IndexMapArray(IndexMap<VALUE> coll) {
		this(null, coll.toArray());
	}
	
	public IndexMapArray(VALUE[] elements) {
		this(null, elements);
	}
	
	public IndexMapArray(@Nullable VALUE defaultObject) {
		this(defaultObject, DEFAULT_CAPACITY);
	}
	
	public IndexMapArray(@Nullable VALUE defaultObject, int initCapacity) {
		this.defaultObject = defaultObject;
		//noinspection unchecked
		this.array = (VALUE[]) new Object[initCapacity];
		this.length = 0;
		
		VALUE def = this.defaultObject;
		if (def != null)
			Arrays.fill(array, def);
	}
	
	public IndexMapArray(@Nullable VALUE defaultObject, IndexMap<VALUE> coll) {
		this(defaultObject, coll.toArray());
	}
	
	public IndexMapArray(@Nullable VALUE defaultObject, VALUE[] elements) {
		this.defaultObject = defaultObject;
		//noinspection unchecked
		this.array = (VALUE[]) Arrays.copyOf(elements, elements.length, Object[].class);
		this.length = array.length;
	}
	
	//capacity
	public boolean ensureCapacity(int index) {
		int oldl = array.length;
		VALUE def = defaultObject;
		if (oldl < index + 1) {
			array = Arrays.copyOf(array, ArrayUtils.getOptimalArraySizeExpansion(oldl, index + 1, EXPAND_SHIFT));
			if (def != null)
				Arrays.fill(array, oldl, array.length, def);
			return true;
		}
		return false;
	}
	
	@Override
	public int size() {
		return length;
	}
	
	//internal methods
	protected VALUE putAndExpand(int index, @Nullable VALUE v) {
		ensureCapacity(index);
		if (index >= length)
			length = index + 1;
		
		VALUE ret = array[index];
		array[index] = v;
		return ret;
	}
	
	//access
	@Override
	public VALUE get(int index) {
		if (index < 0)
			throw new IndexOutOfBoundsException("no negative index!");
		if (index >= array.length)
			return defaultObject;
		
		return array[index];
	}
	
	@NotNull
	@Override
	public IndexMap.Entry<VALUE> getEntry(int index) {
		return new Entry(index);
	}
	
	@Override
	public VALUE put(int index, @Nullable VALUE value) {
		if (index < 0)
			throw new IndexOutOfBoundsException("no negative index!");
		return putAndExpand(index, value);
	}
	
	@Override
	public VALUE remove(int index) {
		if (index < 0)
			throw new IndexOutOfBoundsException("no negative index!");
		if (index >= array.length)
			return defaultObject;
		
		VALUE ret = array[index];
		array[index] = defaultObject;
		return ret;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public VALUE[] toArray() {
		return toArray((VALUE[]) new Object[length]);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public VALUE[] toArray(@NotNull VALUE[] array) {
		if (array.length < length)
			array = (VALUE[]) new Object[length];
		System.arraycopy(this.array, 0, array, 0, length);
		return array;
	}
	
	@Override
	public void putAll(@NotNull IndexMap<? extends VALUE> indexMap) {
		ensureCapacity(indexMap.size());
		IndexMap.super.putAll(indexMap);
	}
	
	@Override
	public void putAllIfAbsent(@NotNull IndexMap<? extends VALUE> indexMap) {
		ensureCapacity(indexMap.size());
		IndexMap.super.putAllIfAbsent(indexMap);
	}
	
	//advanced access
	@Override
	@Nullable
	@Contract("_,!null->!null")
	public VALUE getOrDefault(int index, @Nullable VALUE def) {
		if (index < 0)
			throw new IndexOutOfBoundsException("no negative index!");
		if (index >= array.length)
			return def;
		
		VALUE ret = array[index];
		return ret == defaultObject ? def : ret;
	}
	
	@Override
	@Nullable
	@Contract("_,!null->!null")
	public VALUE putIfAbsent(int index, @Nullable VALUE value) {
		VALUE curr = get(index);
		if (curr != null)
			return curr;
		
		putAndExpand(index, value);
		return value;
	}
	
	@Override
	@Nullable
	public VALUE putIfPresent(int index, @Nullable VALUE value) {
		if (get(index) == null)
			return null;
		
		putAndExpand(index, value);
		return value;
	}
	
	@Override
	public boolean replace(int index, @Nullable VALUE oldValue, @Nullable VALUE newValue) {
		if (index < 0)
			throw new IndexOutOfBoundsException("no negative index!");
		if (Objects.equals(index < array.length ? array[index] : defaultObject, oldValue))
			return false;
		
		putAndExpand(index, newValue);
		return true;
	}
	
	@Override
	public boolean replace(int index, @Nullable VALUE oldValue, @NotNull Supplier<? extends VALUE> newValue) {
		if (index < 0)
			throw new IndexOutOfBoundsException("no negative index!");
		if (Objects.equals(index < array.length ? array[index] : defaultObject, oldValue))
			return false;
		
		putAndExpand(index, newValue.get());
		return true;
	}
	
	@Override
	public boolean remove(int index, @Nullable VALUE value) {
		if (index < 0)
			throw new IndexOutOfBoundsException("no negative index!");
		if (index >= array.length)
			return false;
		if (Objects.equals(array[index], value))
			return false;
		
		array[index] = null;
		return true;
	}
	
	//compute
	@Override
	public VALUE compute(int index, @NotNull ComputeFunction<? super VALUE, ? extends VALUE> function) {
		return putAndExpand(index, function.apply(index, get(index)));
	}
	
	@Override
	public VALUE computeIfAbsent(int index, @NotNull Supplier<? extends VALUE> supplier) {
		VALUE curr = get(index);
		if (curr != null)
			return curr;
		
		VALUE value = supplier.get();
		putAndExpand(index, value);
		return value;
	}
	
	@Override
	@Nullable
	public VALUE computeIfPresent(int index, @NotNull Supplier<? extends VALUE> supplier) {
		if (get(index) == null)
			return null;
		
		VALUE value = supplier.get();
		putAndExpand(index, value);
		return value;
	}
	
	//other
	@Override
	public void clear() {
		Arrays.fill(array, 0, length, defaultObject);
	}
	
	@NotNull
	@Override
	public Collection<VALUE> values() {
		return new UnmodifiableCollection<>(Arrays.stream(array).filter(Objects::nonNull).collect(Collectors.toList()));
	}
	
	@NotNull
	@Override
	public Collection<IndexMap.Entry<VALUE>> entrySet() {
		return new AbstractCollection<>() {
			@NotNull
			@Override
			public Iterator<IndexMap.Entry<VALUE>> iterator() {
				return new Iteratorable<>() {
					int index;
					
					@Override
					public boolean hasNext() {
						return index < length;
					}
					
					@Override
					public IndexMap.Entry<VALUE> next() {
						return new Entry(index++);
					}
					
					@Override
					public void remove() {
						IndexMapArray.this.remove(index - 1);
					}
				};
			}
			
			@Override
			public int size() {
				return IndexMapArray.this.length;
			}
			
			@Override
			public boolean add(IndexMap.Entry<VALUE> entry) {
				IndexMapArray.this.put(entry.getIndex(), entry.getValue());
				return true;
			}
			
			@Override
			public boolean contains(Object o) {
				if (!(o instanceof IndexMap.Entry))
					return false;
				IndexMap.Entry<?> entry = (IndexMap.Entry<?>) o;
				return Objects.equals(get(entry.getIndex()), entry.getValue());
			}
			
			@Override
			public void clear() {
				IndexMapArray.this.clear();
			}
		};
	}
	
	@NotNull
	@Override
	public <T> T toTSH(@NotNull ToStringHelper<T> api) {
		return api.toString(array, 0, length);
	}
	
	@Override
	public String toString() {
		return toString0();
	}
	
	private class Entry implements IndexMap.Entry<VALUE> {
		
		int index;
		
		public Entry(int index) {
			this.index = index;
		}
		
		@Override
		public int getIndex() {
			return index;
		}
		
		@Override
		public VALUE getValue() {
			return get(index);
		}
		
		@Override
		public void setValue(@Nullable VALUE v) {
			put(index, v);
		}
	}
}
