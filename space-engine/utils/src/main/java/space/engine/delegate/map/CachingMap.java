package space.engine.delegate.map;

import org.jetbrains.annotations.NotNull;
import space.engine.baseobject.Cache;
import space.engine.delegate.util.CacheUtil;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A {@link CachingMap} is a {@link Map} that caches values inside it's {@link CachingMap#map}. If an Entry is not found it will call the {@link CachingMap#def} Function in order to get the Default value and write it back into it's cache.
 * Any <code>null</code> values returned by the {@link CachingMap#def} Function are properly cached and handled, so that the Function will not be recalled if such values are returned.<br>
 * <br>
 * Any Functions allowing for iteration over this {@link Map} like {@link CachingMap#keySet()}, {@link CachingMap#values()}, {@link CachingMap#entrySet()} and {@link CachingMap#replaceAll(BiFunction)} have two behaviors:
 * <ul>
 * <li>if {@link CachingMap#allowIterateOverExisting}: iterates over already written values to the {@link CachingMap#map} cache</li>
 * <li>if <b>NOT</b> {@link CachingMap#allowIterateOverExisting} <b>(default)</b>: throws an {@link UnsupportedOperationException} with message "Cache iteration not allowed!"</li>
 * </ul>
 * <br>
 * {@link CachingMap} is threadsafe, if the internal {@link CachingMap#map} is threadsafe. See {@link java.util.concurrent.ConcurrentHashMap}.
 */
public class CachingMap<K, V> extends ConvertingMap.BiDirectional<K, V, V> implements Cache {
	
	public Function<K, V> def;
	public boolean allowIterateOverExisting;
	
	public CachingMap(Map<K, V> indexMap, Map<K, V> def) {
		this(indexMap, def, false);
	}
	
	public CachingMap(Map<K, V> indexMap, Map<K, V> def, boolean allowIterateOverExisting) {
		this(indexMap, def::get, allowIterateOverExisting);
	}
	
	public CachingMap(Map<K, V> indexMap, Function<K, V> def) {
		this(indexMap, def, false);
	}
	
	public CachingMap(Map<K, V> indexMap, Function<K, V> def, boolean allowIterateOverExisting) {
		super(indexMap, CacheUtil::fromCache, CacheUtil::toCache);
		this.def = def;
		this.allowIterateOverExisting = allowIterateOverExisting;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean containsKey(Object key) {
		return super.computeIfAbsent((K) key, def) != null;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public V get(Object key) {
		return super.computeIfAbsent((K) key, def);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public V getOrDefault(Object key, V defaultValue) {
		V ret = super.computeIfAbsent((K) key, def);
		return ret == null ? defaultValue : ret;
	}
	
	@NotNull
	@Override
	public Set<K> keySet() {
		if (allowIterateOverExisting)
			return super.keySet();
		throw new UnsupportedOperationException("Cache iteration not allowed!");
	}
	
	@NotNull
	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		if (allowIterateOverExisting)
			return super.entrySet();
		throw new UnsupportedOperationException("Cache iteration not allowed!");
	}
	
	@Override
	public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
		if (allowIterateOverExisting)
			super.replaceAll(function);
		throw new UnsupportedOperationException("Cache iteration not allowed!");
	}
	
	@Override
	public V putIfAbsent(K key, V value) {
		super.computeIfAbsent(key, def);
		return super.putIfAbsent(key, value);
	}
	
	@Override
	public boolean replace(K key, V oldValue, V newValue) {
		super.computeIfAbsent(key, def);
		return super.replace(key, oldValue, newValue);
	}
	
	@Override
	public V replace(K key, V value) {
		super.computeIfAbsent(key, def);
		return super.replace(key, value);
	}
	
	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
		return super.computeIfAbsent(key, k -> {
			V defValue = def.apply(k);
			return defValue != null ? defValue : mappingFunction.apply(k);
		});
	}
	
	@Override
	public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		return map.compute(key, (k, v) -> {
			V v2 = (v == null) ? def.apply(k) : CacheUtil.fromCache(v);
			return v2 == null ? CacheUtil.nullObject() : CacheUtil.toCache(remappingFunction.apply(k, v2));
		});
	}
	
	@Override
	public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		return map.compute(key, (k, v) -> {
			V v2 = (v == null) ? def.apply(k) : CacheUtil.fromCache(v);
			return CacheUtil.toCache(remappingFunction.apply(k, v2));
		});
	}
	
	@Override
	public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
		return map.compute(key, (k, v) -> {
			V v2 = (v == null) ? def.apply(k) : CacheUtil.fromCache(v);
			return CacheUtil.toCache(remappingFunction.apply(v2, value));
		});
	}
	
	@NotNull
	@Override
	public Collection<V> values() {
		if (allowIterateOverExisting)
			return super.values();
		throw new UnsupportedOperationException("Cache iteration not allowed!");
	}
	
	@Override
	public void clearCache() {
		map.clear();
	}
}
