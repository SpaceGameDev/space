package space.engine.delegate.map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A {@link Map} delegating all calls to it's Field {@link SupplierMap#map}, which is an {@link Supplier} of Type {@link Map}.
 * The {@link Supplier} is provided by Constructor or set directly.
 */
public class SupplierMap<K, V> implements Map<K, V> {
	
	public Supplier<Map<K, V>> map;
	
	public SupplierMap(Supplier<Map<K, V>> map) {
		this.map = map;
	}
	
	@Override
	public int size() {
		return map.get().size();
	}
	
	@Override
	public boolean isEmpty() {
		return map.get().isEmpty();
	}
	
	@Override
	public boolean containsKey(Object key) {
		return map.get().containsKey(key);
	}
	
	@Override
	public boolean containsValue(Object value) {
		return map.get().containsValue(value);
	}
	
	@Override
	public V get(Object key) {
		return map.get().get(key);
	}
	
	@Override
	public V put(K key, V value) {
		return map.get().put(key, value);
	}
	
	@Override
	public V remove(Object key) {
		return map.get().remove(key);
	}
	
	@Override
	public void putAll(@NotNull Map<? extends K, ? extends V> m) {
		map.get().putAll(m);
	}
	
	@Override
	public void clear() {
		map.get().clear();
	}
	
	@NotNull
	@Override
	public Set<K> keySet() {
		return map.get().keySet();
	}
	
	@NotNull
	@Override
	public Collection<V> values() {
		return map.get().values();
	}
	
	@NotNull
	@Override
	public Set<Entry<K, V>> entrySet() {
		return map.get().entrySet();
	}
	
	@Override
	public V getOrDefault(Object key, V defaultValue) {
		return map.get().getOrDefault(key, defaultValue);
	}
	
	@Override
	public void forEach(BiConsumer<? super K, ? super V> action) {
		map.get().forEach(action);
	}
	
	@Override
	public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
		map.get().replaceAll(function);
	}
	
	@Override
	public V putIfAbsent(K key, V value) {
		return map.get().putIfAbsent(key, value);
	}
	
	@Override
	public boolean remove(Object key, Object value) {
		return map.get().remove(key, value);
	}
	
	@Override
	public boolean replace(K key, V oldValue, V newValue) {
		return map.get().replace(key, oldValue, newValue);
	}
	
	@Override
	public V replace(K key, V value) {
		return map.get().replace(key, value);
	}
	
	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
		return map.get().computeIfAbsent(key, mappingFunction);
	}
	
	@Nullable
	@Override
	public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		return map.get().computeIfPresent(key, remappingFunction);
	}
	
	@Override
	public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		return map.get().compute(key, remappingFunction);
	}
	
	@Override
	public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
		return map.get().merge(key, value, remappingFunction);
	}
}
