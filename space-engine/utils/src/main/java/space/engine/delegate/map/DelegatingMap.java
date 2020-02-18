package space.engine.delegate.map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.engine.baseobject.ToString;
import space.engine.string.toStringHelper.ToStringHelper;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A {@link Map} delegating all calls to it's Field {@link DelegatingMap#map}, supplied with the Constructor.
 */
public class DelegatingMap<K, V> implements ToString, Map<K, V> {
	
	public @NotNull Map<K, V> map;
	
	public DelegatingMap(@NotNull Map<K, V> map) {
		this.map = map;
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
		return map.containsKey(key);
	}
	
	@Override
	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}
	
	@Override
	public V get(Object key) {
		return map.get(key);
	}
	
	@Override
	public V put(K key, V value) {
		return map.put(key, value);
	}
	
	@Override
	public V remove(Object key) {
		return map.remove(key);
	}
	
	@Override
	public void putAll(@NotNull Map<? extends K, ? extends V> m) {
		map.putAll(m);
	}
	
	@Override
	public void clear() {
		map.clear();
	}
	
	@NotNull
	@Override
	public Set<K> keySet() {
		return map.keySet();
	}
	
	@NotNull
	@Override
	public Collection<V> values() {
		return map.values();
	}
	
	@NotNull
	@Override
	public Set<Entry<K, V>> entrySet() {
		return map.entrySet();
	}
	
	@Override
	public V getOrDefault(Object key, V defaultValue) {
		return map.getOrDefault(key, defaultValue);
	}
	
	@Override
	public void forEach(BiConsumer<? super K, ? super V> action) {
		map.forEach(action);
	}
	
	@Override
	public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
		map.replaceAll(function);
	}
	
	@Override
	public V putIfAbsent(K key, V value) {
		return map.putIfAbsent(key, value);
	}
	
	@Override
	public boolean remove(Object key, Object value) {
		return map.remove(key, value);
	}
	
	@Override
	public boolean replace(K key, V oldValue, V newValue) {
		return map.replace(key, oldValue, newValue);
	}
	
	@Override
	public V replace(K key, V value) {
		return map.replace(key, value);
	}
	
	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
		return map.computeIfAbsent(key, mappingFunction);
	}
	
	@Nullable
	@Override
	public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		return map.computeIfPresent(key, remappingFunction);
	}
	
	@Override
	public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		return map.compute(key, remappingFunction);
	}
	
	@Override
	public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
		return map.merge(key, value, remappingFunction);
	}
	
	@NotNull
	@Override
	public <T> T toTSH(@NotNull ToStringHelper<T> api) {
		return api.createModifier("delegate", map);
	}
	
	@Override
	public String toString() {
		return toString0();
	}
}
