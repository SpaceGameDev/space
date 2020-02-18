package space.engine.delegate.indexmap;

import org.jetbrains.annotations.NotNull;
import space.engine.baseobject.Cache;
import space.engine.delegate.util.CacheUtil;
import space.engine.indexmap.IndexMap;
import space.engine.string.toStringHelper.ToStringHelper;
import space.engine.string.toStringHelper.ToStringHelper.ToStringHelperObjectsInstance;

import java.util.Collection;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * A {@link CachingIndexMap} is a {@link Map} that caches values inside it's {@link CachingIndexMap#indexMap}. If an Entry is not found it will call the {@link CachingIndexMap#def} Function in order to get the Default value and write it back into it's cache.
 * Any <code>null</code> values returned by the {@link CachingIndexMap#def} Function are properly cached and handled, so that the Function will not be recalled if such values are returned.<br>
 * <br>
 * Any Functions allowing for iteration over this {@link Map} like {@link CachingIndexMap#values()}, {@link CachingIndexMap#entrySet()}, {@link CachingIndexMap#toArray()} and {@link CachingIndexMap#toArray(Object[])} have two behaviors:
 * <ul>
 * <li>if {@link CachingIndexMap#allowIterateOverExisting}: iterates over already written values to the {@link CachingIndexMap#indexMap} cache</li>
 * <li>if <b>NOT</b> {@link CachingIndexMap#allowIterateOverExisting} <b>(default)</b>: throws an {@link UnsupportedOperationException} with message "Cache iteration not allowed!"</li>
 * </ul>
 * <br>
 * {@link CachingIndexMap} is threadsafe, if the internal {@link CachingIndexMap#indexMap} is threadsafe.
 */
public class CachingIndexMap<VALUE> extends ConvertingIndexMap.BiDirectional<VALUE, VALUE> implements Cache {
	
	public IntFunction<VALUE> def;
	public boolean allowIterateOverExisting;
	
	public CachingIndexMap(IndexMap<VALUE> indexMap, IndexMap<VALUE> def) {
		this(indexMap, def, false);
	}
	
	public CachingIndexMap(IndexMap<VALUE> indexMap, IndexMap<VALUE> def, boolean allowIterateOverExisting) {
		this(indexMap, def::get, allowIterateOverExisting);
	}
	
	public CachingIndexMap(IndexMap<VALUE> indexMap, IntFunction<VALUE> def) {
		this(indexMap, def, false);
	}
	
	public CachingIndexMap(IndexMap<VALUE> indexMap, IntFunction<VALUE> def, boolean allowIterateOverExisting) {
		super(indexMap, CacheUtil::fromCache, CacheUtil::toCache);
		this.def = def;
		this.allowIterateOverExisting = allowIterateOverExisting;
	}
	
	@Override
	public boolean replace(int index, VALUE oldValue, VALUE newValue) {
		boolean[] chg = new boolean[1];
		super.computeIfAbsent(index, () -> {
			chg[0] = true;
			VALUE defValue = def.apply(index);
			return defValue == oldValue ? newValue : defValue;
		});
		return chg[0] || super.replace(index, oldValue, newValue);
	}
	
	@Override
	public boolean replace(int index, VALUE oldValue, @NotNull Supplier<? extends VALUE> newValue) {
		boolean[] chg = new boolean[1];
		super.computeIfAbsent(index, () -> {
			chg[0] = true;
			VALUE defValue = def.apply(index);
			return defValue == oldValue ? newValue.get() : defValue;
		});
		return chg[0] || super.replace(index, oldValue, newValue);
	}
	
	@Override
	public boolean remove(int index, VALUE value) {
		boolean[] chg = new boolean[1];
		super.computeIfAbsent(index, () -> {
			chg[0] = true;
			VALUE defValue = def.apply(index);
			return defValue == value ? null : defValue;
		});
		return chg[0] || super.remove(index, value);
	}
	
	@NotNull
	@Override
	public <TSHTYPE> TSHTYPE toTSH(@NotNull ToStringHelper<TSHTYPE> api) {
		ToStringHelperObjectsInstance<TSHTYPE> tsh = api.createObjectInstance(this);
		tsh.add("indexMap", this.indexMap);
		tsh.add("def", this.def);
		return tsh.build();
	}
	
	@NotNull
	@Override
	public Collection<VALUE> values() {
		if (allowIterateOverExisting)
			return super.values();
		throw new UnsupportedOperationException("Cache iteration not allowed!");
	}
	
	@Override
	public boolean contains(int index) {
		return super.computeIfAbsent(index, () -> this.def.apply(index)) != null;
	}
	
	@Override
	public VALUE get(int index) {
		return super.computeIfAbsent(index, () -> this.def.apply(index));
	}
	
	@Override
	public VALUE[] toArray() {
		if (allowIterateOverExisting)
			return super.toArray();
		throw new UnsupportedOperationException("Cache iteration not allowed!");
	}
	
	@Override
	public VALUE[] toArray(@NotNull VALUE[] a) {
		if (allowIterateOverExisting)
			return super.toArray(a);
		throw new UnsupportedOperationException("Cache iteration not allowed!");
	}
	
	@Override
	public VALUE getOrDefault(int index, VALUE def) {
		VALUE value = super.computeIfAbsent(index, () -> this.def.apply(index));
		return value == null ? def : value;
	}
	
	@NotNull
	@Override
	public IndexMap.Entry<VALUE> getEntry(int index) {
		super.computeIfAbsent(index, () -> this.def.apply(index));
		return super.getEntry(index);
	}
	
	@Override
	public void putAllIfAbsent(@NotNull IndexMap<? extends VALUE> indexMap) {
		indexMap.entrySet().forEach(entry -> super.computeIfAbsent(entry.getIndex(), () -> {
			VALUE defValue = def.apply(entry.getIndex());
			return defValue != null ? defValue : entry.getValue();
		}));
	}
	
	@Override
	public VALUE putIfAbsent(int index, VALUE value) {
		return super.computeIfAbsent(index, () -> {
			VALUE defValue = def.apply(index);
			return defValue != null ? defValue : value;
		});
	}
	
	@Override
	public VALUE computeIfAbsent(int index, @NotNull Supplier<? extends VALUE> supplier) {
		return super.computeIfAbsent(index, () -> {
			VALUE defValue = def.apply(index);
			return defValue != null ? defValue : supplier.get();
		});
	}
	
	@Override
	public VALUE compute(int index, @NotNull ComputeFunction<? super VALUE, ? extends VALUE> function) {
		return super.compute(index, (index1, value) -> {
			if (value == null)
				value = def.apply(index1);
			return function.apply(index1, value);
		});
	}
	
	@NotNull
	@Override
	public Collection<IndexMap.Entry<VALUE>> entrySet() {
		if (allowIterateOverExisting)
			return super.entrySet();
		throw new UnsupportedOperationException("Cache iteration not allowed!");
	}
	
	@Override
	public String toString() {
		return toString0();
	}
	
	@Override
	public void clearCache() {
		super.clear();
	}
}
