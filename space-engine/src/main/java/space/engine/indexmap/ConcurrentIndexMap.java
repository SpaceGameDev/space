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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Supplier;

/**
 * This implementation of {@link IndexMap} allows concurrent access with as little blocking as possible.
 * The only operation which is blocking is growing an internal array.
 *
 * @implNote This Object stores everything in an AtomicReferenceArray<VALUE>[] {@link #array}.
 * The outer array only has non-null elements, may be read at any time, but should only be written or replaced while synchronized on this (as done in {@link #ensureCapacity(int[])}).
 * The inner array (all AtomicReferenceArrays) ensures atomic access for all normal read and write operations.
 * They always has a fixed size defined in {@link #capacityInnerArray} to make location calculation simple.
 * Call {@link #calculateLocation(int)} to calculate the location in these two arrays and use it like <code>array[location[0]].operation(location[1]);</code>
 */
public class ConcurrentIndexMap<VALUE> implements IndexMap<VALUE>, ToString {
	
	public static final int DEFAULT_CAPACITY_INNER_ARRAY = 32;
	public static final int EXPAND_SHIFT_OUTER_ARRAY = 1;
	
	protected final VALUE defaultObject;
	protected final int capacityInnerArray;
	protected volatile AtomicReferenceArray<VALUE>[] array;
	
	public ConcurrentIndexMap() {
		this(null, DEFAULT_CAPACITY_INNER_ARRAY);
	}
	
	public ConcurrentIndexMap(int capacityInnerArray) {
		this(null, capacityInnerArray);
	}
	
	public ConcurrentIndexMap(IndexMap<VALUE> indexMap) {
		this(null, indexMap);
	}
	
	public ConcurrentIndexMap(@Nullable VALUE defaultObject) {
		this(defaultObject, DEFAULT_CAPACITY_INNER_ARRAY);
	}
	
	public ConcurrentIndexMap(@Nullable VALUE defaultObject, int capacityInnerArray) {
		this.defaultObject = defaultObject;
		this.capacityInnerArray = capacityInnerArray;
		
		//noinspection unchecked
		VALUE[] firstArray = (VALUE[]) new Object[capacityInnerArray];
		Arrays.fill(firstArray, defaultObject);
		//noinspection unchecked
		this.array = new AtomicReferenceArray[] {new AtomicReferenceArray<>(firstArray)};
	}
	
	public ConcurrentIndexMap(@Nullable VALUE defaultObject, IndexMap<VALUE> indexMap) {
		this(defaultObject);
		putAll(indexMap);
	}
	
	//internal
	protected int[] calculateLocation(int index) {
		if (index < 0)
			throw new IndexOutOfBoundsException("no negative index!");
		return new int[] {index / capacityInnerArray, index % capacityInnerArray};
	}
	
	//capacity
	public boolean ensureCapacity(int[] location) {
		int minCapacity = location[0] + 1;
		if (minCapacity <= array.length)
			return false;
		
		synchronized (this) {
			if (minCapacity <= array.length)
				return false;
			
			//grow array
			AtomicReferenceArray<VALUE>[] newArray = Arrays.copyOf(array, ArrayUtils.getOptimalArraySizeExpansion(array.length, minCapacity, EXPAND_SHIFT_OUTER_ARRAY));
			VALUE def = defaultObject;
			for (int i = array.length; i < newArray.length; i++) {
				if (def == null) {
					newArray[i] = new AtomicReferenceArray<>(capacityInnerArray);
				} else {
					//noinspection unchecked
					VALUE[] innerArray = (VALUE[]) new Object[capacityInnerArray];
					Arrays.fill(innerArray, def);
					newArray[i] = new AtomicReferenceArray<>(innerArray);
				}
			}
			array = newArray;
		}
		return true;
	}
	
	@Override
	public int size() {
		return array.length * capacityInnerArray;
	}
	
	//access
	@Override
	public VALUE get(int index) {
		int[] location = calculateLocation(index);
		if (location[0] >= array.length)
			return defaultObject;
		return array[location[0]].get(location[1]);
	}
	
	@NotNull
	@Override
	public IndexMap.Entry<VALUE> getEntry(int index) {
		return new Entry(index);
	}
	
	@Override
	public VALUE put(int index, @Nullable VALUE value) {
		int[] location = calculateLocation(index);
		ensureCapacity(location);
		return array[location[0]].getAndSet(index, value);
	}
	
	@Override
	public VALUE remove(int index) {
		int[] location = calculateLocation(index);
		AtomicReferenceArray<VALUE>[] array = this.array;
		if (location[0] >= this.array.length)
			return defaultObject;
		return this.array[location[0]].getAndSet(location[1], defaultObject);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public VALUE[] toArray() {
		AtomicReferenceArray<VALUE>[] array = this.array;
		int size = array.length * capacityInnerArray;
		VALUE[] dest = (VALUE[]) new Object[size];
		for (int outer = 0; outer < array.length; outer++)
			for (int inner = 0; inner < capacityInnerArray; inner++)
				dest[outer * capacityInnerArray + inner] = array[outer].get(inner);
		return dest;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public VALUE[] toArray(@NotNull VALUE[] dest) {
		AtomicReferenceArray<VALUE>[] array = this.array;
		int size = array.length * capacityInnerArray;
		if (dest.length < size)
			dest = (VALUE[]) new Object[size];
		for (int outer = 0; outer < array.length; outer++)
			for (int inner = 0; inner < capacityInnerArray; inner++)
				dest[outer * capacityInnerArray + inner] = array[outer].get(inner);
		return dest;
	}
	
	//advanced access
	@Override
	@Nullable
	@Contract("_,!null->!null")
	public VALUE getOrDefault(int index, @Nullable VALUE def) {
		int[] location = calculateLocation(index);
		AtomicReferenceArray<VALUE>[] array = this.array;
		if (location[0] >= array.length)
			return def;
		VALUE value = array[location[0]].get(location[1]);
		return value == defaultObject ? def : value;
	}
	
	@Override
	@Nullable
	@Contract("_,!null->!null")
	public VALUE putIfAbsent(int index, @Nullable VALUE newValue) {
		int[] location = calculateLocation(index);
		ensureCapacity(location);
		AtomicReferenceArray<VALUE>[] array = this.array;
		return array[location[0]].compareAndExchange(location[1], defaultObject, newValue);
	}
	
	@Override
	@Nullable
	public VALUE putIfPresent(int index, @Nullable VALUE newValue) {
		int[] location = calculateLocation(index);
		AtomicReferenceArray<VALUE>[] array = this.array;
		if (location[0] >= array.length)
			return defaultObject;
		return array[location[0]].accumulateAndGet(location[1], newValue, (curr, value) -> curr != defaultObject ? curr : value);
	}
	
	@Override
	public boolean replace(int index, @Nullable VALUE oldValue, @Nullable VALUE newValue) {
		int[] location = calculateLocation(index);
		ensureCapacity(location);
		AtomicReferenceArray<VALUE>[] array = this.array;
		return array[location[0]].compareAndSet(location[1], oldValue, newValue);
	}
	
	@Override
	public boolean replace(int index, @Nullable VALUE oldValue, @NotNull Supplier<? extends VALUE> newValue) {
		int[] location = calculateLocation(index);
		ensureCapacity(location);
		return array[location[0]].compareAndSet(location[1], oldValue, newValue.get());
	}
	
	@Override
	public boolean remove(int index, @Nullable VALUE value) {
		int[] location = calculateLocation(index);
		AtomicReferenceArray<VALUE>[] array = this.array;
		if (location[0] >= array.length)
			return false;
		return array[location[0]].compareAndSet(location[1], value, defaultObject);
	}
	
	//compute
	@Override
	public VALUE compute(int index, @NotNull ComputeFunction<? super VALUE, ? extends VALUE> function) {
		int[] location = calculateLocation(index);
		ensureCapacity(location);
		AtomicReferenceArray<VALUE>[] array = this.array;
		return array[location[0]].accumulateAndGet(location[1], null, (value, value2) -> function.apply(index, value));
	}
	
	@Override
	public VALUE computeIfAbsent(int index, @NotNull Supplier<? extends VALUE> supplier) {
		int[] location = calculateLocation(index);
		ensureCapacity(location);
		AtomicReferenceArray<VALUE>[] array = this.array;
		return array[location[0]].compareAndExchange(location[1], defaultObject, supplier.get());
	}
	
	@Override
	@Nullable
	public VALUE computeIfPresent(int index, @NotNull Supplier<? extends VALUE> supplier) {
		int[] location = calculateLocation(index);
		AtomicReferenceArray<VALUE>[] array = this.array;
		if (location[0] >= array.length)
			return defaultObject;
		return array[location[0]].accumulateAndGet(location[1], supplier.get(), (curr, value) -> curr != defaultObject ? curr : value);
	}
	
	//other
	@Override
	public void clear() {
		AtomicReferenceArray<VALUE>[] array = this.array;
		for (AtomicReferenceArray<VALUE> innerArray : array)
			for (int inner = 0; inner < capacityInnerArray; inner++)
				innerArray.set(inner, defaultObject);
	}
	
	@NotNull
	@Override
	public Collection<VALUE> values() {
		List<VALUE> ret = new ArrayList<>();
		for (AtomicReferenceArray<VALUE> innerArray : array) {
			for (int innerIndex = 0; innerIndex < capacityInnerArray; innerIndex++) {
				VALUE value = innerArray.get(innerIndex);
				if (value != null)
					ret.add(value);
			}
		}
		return new UnmodifiableCollection<>(ret);
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
						return index < ConcurrentIndexMap.this.size();
					}
					
					@Override
					public IndexMap.Entry<VALUE> next() {
						return new Entry(index++);
					}
					
					@Override
					public void remove() {
						ConcurrentIndexMap.this.remove(index - 1);
					}
				};
			}
			
			@Override
			public int size() {
				return ConcurrentIndexMap.this.size();
			}
			
			@Override
			public boolean add(IndexMap.Entry<VALUE> entry) {
				ConcurrentIndexMap.this.put(entry.getIndex(), entry.getValue());
				return true;
			}
			
			@Override
			public boolean contains(Object o) {
				if (!(o instanceof IndexMap.Entry))
					return false;
				IndexMap.Entry entry = (IndexMap.Entry) o;
				return Objects.equals(get(entry.getIndex()), entry.getValue());
			}
			
			@Override
			public void clear() {
				ConcurrentIndexMap.this.clear();
			}
		};
	}
	
	@NotNull
	@Override
	public <T> T toTSH(@NotNull ToStringHelper<T> api) {
		return api.toString(toArray());
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
