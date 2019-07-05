package space.engine.delegate.map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.engine.string.toStringHelper.ToStringHelper;
import space.engine.string.toStringHelper.ToStringHelper.ToStringHelperObjectsInstance;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A {@link BufferedMap} is a Map having a {@link BufferedMap#buffer} of entries, which can be flushed into another map with {@link BufferedMap#setSink(Map) setSink(Map)}.
 * The {@link BufferedMap} is also synchronized internally.<br>
 * <p>
 * Usage example:
 * This allows for pre-initialization {@link Map Maps} to exist and catch all data, while the Runtime {@link Map} is being created.
 * When initialization of the Runtime {@link Map} is done, all data can be flushed to it with {@link BufferedMap#setSink(Map)}.
 */
public class BufferedMap<K, V> extends DelegatingMap<K, V> {
	
	public @Nullable Map<K, V> buffer;
	
	public BufferedMap(@Nullable Map<K, V> buffer) {
		//noinspection ConstantConditions
		super(null);
		this.buffer = buffer;
	}
	
	//sink
	public synchronized void setSink(Map<K, V> sink) {
		if (buffer == null)
			throw new IllegalStateException();
		
		map = sink;
		sink.putAll(buffer);
		buffer = null;
	}
	
	public synchronized boolean hasSink() {
		return buffer == null;
	}
	
	//delegate
	@Override
	public synchronized int size() {
		return buffer == null ? super.size() : buffer.size();
	}
	
	@Override
	public synchronized boolean isEmpty() {
		return buffer == null ? super.isEmpty() : buffer.isEmpty();
	}
	
	@Override
	public synchronized boolean containsKey(Object key) {
		return buffer == null ? super.containsKey(key) : buffer.containsKey(key);
	}
	
	@Override
	public synchronized boolean containsValue(Object value) {
		return buffer == null ? super.containsValue(value) : buffer.containsValue(value);
	}
	
	@Override
	public synchronized V get(Object key) {
		return buffer == null ? super.get(key) : buffer.get(key);
	}
	
	@Override
	public synchronized V put(K key, V value) {
		return buffer == null ? super.put(key, value) : buffer.put(key, value);
	}
	
	@Override
	public synchronized V remove(Object key) {
		return buffer == null ? super.remove(key) : buffer.remove(key);
	}
	
	@Override
	public synchronized void putAll(@NotNull Map<? extends K, ? extends V> m) {
		if (buffer == null)
			super.putAll(m);
		else
			buffer.putAll(m);
	}
	
	@Override
	public synchronized void clear() {
		if (buffer == null)
			super.clear();
		else
			buffer.clear();
	}
	
	@NotNull
	@Override
	public synchronized Set<K> keySet() {
		return buffer == null ? super.keySet() : buffer.keySet();
	}
	
	@NotNull
	@Override
	public synchronized Collection<V> values() {
		return buffer == null ? super.values() : buffer.values();
	}
	
	@NotNull
	@Override
	public synchronized Set<Entry<K, V>> entrySet() {
		return buffer == null ? super.entrySet() : buffer.entrySet();
	}
	
	@Override
	public synchronized V getOrDefault(Object key, V defaultValue) {
		return buffer == null ? super.getOrDefault(key, defaultValue) : buffer.getOrDefault(key, defaultValue);
	}
	
	@Override
	public synchronized void forEach(BiConsumer<? super K, ? super V> action) {
		if (buffer == null)
			super.forEach(action);
		else
			buffer.forEach(action);
	}
	
	@Override
	public synchronized void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
		if (buffer == null)
			super.replaceAll(function);
		else
			buffer.replaceAll(function);
	}
	
	@Override
	public synchronized V putIfAbsent(K key, V value) {
		return buffer == null ? super.putIfAbsent(key, value) : buffer.putIfAbsent(key, value);
	}
	
	@Override
	public synchronized boolean remove(Object key, Object value) {
		return buffer == null ? super.remove(key, value) : buffer.remove(key, value);
	}
	
	@Override
	public synchronized boolean replace(K key, V oldValue, V newValue) {
		return buffer == null ? super.replace(key, oldValue, newValue) : buffer.replace(key, oldValue, newValue);
	}
	
	@Override
	public synchronized V replace(K key, V value) {
		return buffer == null ? super.replace(key, value) : buffer.replace(key, value);
	}
	
	@Override
	public synchronized V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
		return buffer == null ? super.computeIfAbsent(key, mappingFunction) : buffer.computeIfAbsent(key, mappingFunction);
	}
	
	@Override
	public synchronized V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		return buffer == null ? super.computeIfPresent(key, remappingFunction) : buffer.computeIfPresent(key, remappingFunction);
	}
	
	@Override
	public synchronized V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		return buffer == null ? super.compute(key, remappingFunction) : buffer.compute(key, remappingFunction);
	}
	
	@Override
	public synchronized V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
		return buffer == null ? super.merge(key, value, remappingFunction) : buffer.merge(key, value, remappingFunction);
	}
	
	@NotNull
	@Override
	public <TSHTYPE> TSHTYPE toTSH(@NotNull ToStringHelper<TSHTYPE> api) {
		ToStringHelperObjectsInstance<TSHTYPE> tsh = api.createObjectInstance(this);
		tsh.add("map", this.map);
		tsh.add("buffer", this.buffer);
		return tsh.build();
	}
}
