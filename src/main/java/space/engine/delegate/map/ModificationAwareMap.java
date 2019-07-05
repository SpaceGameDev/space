package space.engine.delegate.map;

import org.jetbrains.annotations.NotNull;
import space.engine.delegate.collection.ModificationAwareCollection;
import space.engine.delegate.map.entry.ModificationAwareEntry;
import space.engine.delegate.set.ConvertingSet;
import space.engine.delegate.set.ModificationAwareSet;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * The {@link ModificationAwareMap} will call the {@link ModificationAwareMap#onModification} {@link Runnable} when the {@link Map} is modified.
 */
public class ModificationAwareMap<K, V> extends DelegatingMap<K, V> {
	
	public Runnable onModification;
	
	public ModificationAwareMap(Map<K, V> map, Runnable onModification) {
		super(map);
		this.onModification = onModification;
	}
	
	@Override
	public V put(K key, V value) {
		V ret = map.put(key, value);
		onModification.run();
		return ret;
	}
	
	@Override
	public V remove(Object key) {
		V ret = map.remove(key);
		onModification.run();
		return ret;
	}
	
	@Override
	public void putAll(@NotNull Map<? extends K, ? extends V> m) {
		map.putAll(m);
		if (m.size() != 0)
			onModification.run();
	}
	
	@Override
	public void clear() {
		map.clear();
		if (map.size() != 0)
			onModification.run();
	}
	
	@NotNull
	@Override
	public Set<K> keySet() {
		return new ModificationAwareSet<>(map.keySet(), onModification);
	}
	
	@NotNull
	@Override
	public Collection<V> values() {
		return new ModificationAwareCollection<>(map.values(), onModification);
	}
	
	@NotNull
	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		return new ModificationAwareSet<>(new ConvertingSet.BiDirectional<>(map.entrySet(), entry -> entry == null ? null : new ModificationAwareEntry<>(entry), entry -> entry instanceof ModificationAwareEntry ? ((ModificationAwareEntry<K, V>) entry).entry : null), onModification);
	}
	
	@Override
	public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
		boolean[] mod = new boolean[1];
		map.replaceAll((k, v) -> {
			V ret = function.apply(k, v);
			if (ret != v)
				mod[0] = true;
			return ret;
		});
		if (mod[0])
			onModification.run();
	}
	
	@Override
	public V putIfAbsent(K key, V value) {
		boolean[] mod = new boolean[1];
		V ret = map.computeIfAbsent(key, k -> {
			mod[0] = true;
			return value;
		});
		if (mod[0])
			onModification.run();
		return ret;
	}
	
	@Override
	public boolean remove(Object key, Object value) {
		if (get(key) != value)
			return false;
		remove(key);
		onModification.run();
		return true;
	}
	
	@Override
	public boolean replace(K key, V oldValue, V newValue) {
		if (get(key) != oldValue)
			return false;
		put(key, newValue);
		onModification.run();
		return true;
	}
	
	@Override
	public V replace(K key, V value) {
		V ret = map.replace(key, value);
		onModification.run();
		return ret;
	}
	
	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
		boolean[] mod = new boolean[1];
		V ret = map.computeIfAbsent(key, k -> {
			mod[0] = true;
			return mappingFunction.apply(k);
		});
		if (mod[0])
			onModification.run();
		return ret;
	}
	
	@Override
	public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		boolean[] mod = new boolean[1];
		V ret = map.computeIfPresent(key, (k, v) -> {
			mod[0] = true;
			return remappingFunction.apply(k, v);
		});
		if (mod[0])
			onModification.run();
		return ret;
	}
	
	@Override
	public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		V ret = map.compute(key, remappingFunction);
		onModification.run();
		return ret;
	}
	
	@Override
	public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
		boolean[] mod = new boolean[1];
		V ret = map.merge(key, value, (v, v2) -> {
			mod[0] = true;
			return remappingFunction.apply(v, v2);
		});
		if (mod[0])
			onModification.run();
		return ret;
	}
}
