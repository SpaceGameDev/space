package space.engine.delegate.map;

import org.jetbrains.annotations.NotNull;
import space.engine.delegate.collection.UnmodifiableCollection;
import space.engine.delegate.map.entry.UnmodifiableEntry;
import space.engine.delegate.set.ConvertingSet;
import space.engine.delegate.set.UnmodifiableSet;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * The {@link UnmodifiableMap} makes the {@link Map} unmodifiable.
 */
public class UnmodifiableMap<K, V> extends DelegatingMap<K, V> {
	
	public UnmodifiableMap(Map<K, V> map) {
		super(map);
	}
	
	@Override
	public V put(K key, V value) {
		throw new UnsupportedOperationException("Unmodifiable");
	}
	
	@Override
	public V remove(Object key) {
		throw new UnsupportedOperationException("Unmodifiable");
	}
	
	@Override
	public void putAll(@NotNull Map<? extends K, ? extends V> m) {
		throw new UnsupportedOperationException("Unmodifiable");
	}
	
	@Override
	public void clear() {
		throw new UnsupportedOperationException("Unmodifiable");
	}
	
	@Override
	public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
		throw new UnsupportedOperationException("Unmodifiable");
	}
	
	@Override
	public V putIfAbsent(K key, V value) {
		throw new UnsupportedOperationException("Unmodifiable");
	}
	
	@Override
	public boolean remove(Object key, Object value) {
		throw new UnsupportedOperationException("Unmodifiable");
	}
	
	@Override
	public boolean replace(K key, V oldValue, V newValue) {
		throw new UnsupportedOperationException("Unmodifiable");
	}
	
	@Override
	public V replace(K key, V value) {
		throw new UnsupportedOperationException("Unmodifiable");
	}
	
	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
		throw new UnsupportedOperationException("Unmodifiable");
	}
	
	@Override
	public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		throw new UnsupportedOperationException("Unmodifiable");
	}
	
	@Override
	public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		throw new UnsupportedOperationException("Unmodifiable");
	}
	
	@Override
	public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
		throw new UnsupportedOperationException("Unmodifiable");
	}
	
	@NotNull
	@Override
	public Set<K> keySet() {
		return new UnmodifiableSet<>(map.keySet());
	}
	
	@NotNull
	@Override
	public Collection<V> values() {
		return new UnmodifiableCollection<>(map.values());
	}
	
	@NotNull
	@Override
	public Set<Entry<K, V>> entrySet() {
		return new ConvertingSet.BiDirectionalUnmodifiable<>(map.entrySet(),
															 UnmodifiableEntry::new,
															 entry -> entry instanceof UnmodifiableEntry ? ((UnmodifiableEntry<K, V>) entry).entry : null);
	}
}
