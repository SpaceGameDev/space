package space.engine.delegate.map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.engine.delegate.collection.UnmodifiableCollection;
import space.engine.delegate.set.ObservableSet;
import space.engine.event.Event;
import space.engine.event.EventEntry;
import space.engine.event.SequentialEventBuilder;
import space.engine.sync.barrier.Barrier;
import space.engine.sync.barrier.BarrierImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

/**
 * An {@link ObservableMap} allows you to be notified when a key is changed form the {@link Map}.
 * Add a Callback to the {@link #getChangeEvent()} to get notified with a {@link Change} happening to this {@link Map}.
 * <p>
 * The implementation is threadsafe if the {@link #delegate} {@link Map} given to the constructor is threadsafe. It also guarantees that Event callbacks are called in order.
 */
public class ObservableMap<K, V> implements Map<K, V> {
	
	private final Map<K, V> delegate;
	private final SequentialEventBuilder<Consumer<Change<K, V>>> changeEvent = new SequentialEventBuilder<>();
	private AtomicReference<Barrier> lastBarrier = new AtomicReference<>(Barrier.ALWAYS_TRIGGERED_BARRIER);
	
	public ObservableMap(Map<K, V> delegate) {
		this.delegate = delegate;
	}
	
	public Event<Consumer<Change<K, V>>> getChangeEvent() {
		return changeEvent;
	}
	
	//delegate event
	public void addHook(@NotNull EventEntry<Consumer<Change<K, V>>> hook) {
		changeEvent.addHook(hook);
	}
	
	public EventEntry<Consumer<Change<K, V>>> addHook(Consumer<Change<K, V>> changeConsumer) {
		return changeEvent.addHook(changeConsumer);
	}
	
	public EventEntry<Consumer<Change<K, V>>> addHook(Consumer<Change<K, V>> changeConsumer, @NotNull EventEntry<?>... requires) {
		return changeEvent.addHook(changeConsumer, requires);
	}
	
	public EventEntry<Consumer<Change<K, V>>> addHook(Consumer<Change<K, V>> changeConsumer, @NotNull EventEntry<?>[] requiredBy, @NotNull EventEntry<?>... requires) {
		return changeEvent.addHook(changeConsumer, requiredBy, requires);
	}
	
	/**
	 * Adds the supplied hook and acts as if this collection would have been empty and it would fill it with the current actual value.
	 * In other words: calls the event with added() being the current value
	 */
	public void addHookAsStartedEmpty(@NotNull EventEntry<Consumer<Change<K, V>>> hook) {
		changeEvent.addHook(hook);
		callAsStartedEmpty(hook);
	}
	
	/**
	 * Adds the supplied hook and acts as if this collection would have been empty and it would fill it with the current actual value.
	 * In other words: calls the event with added() being the current value
	 */
	public EventEntry<Consumer<Change<K, V>>> addHookAsStartedEmpty(Consumer<Change<K, V>> changeConsumer) {
		return callAsStartedEmpty(changeEvent.addHook(changeConsumer));
	}
	
	/**
	 * Adds the supplied hook and acts as if this collection would have been empty and it would fill it with the current actual value.
	 * In other words: calls the event with added() being the current value
	 */
	public EventEntry<Consumer<Change<K, V>>> addHookAsStartedEmpty(Consumer<Change<K, V>> changeConsumer, @NotNull EventEntry<?>... requires) {
		return callAsStartedEmpty(changeEvent.addHook(changeConsumer, requires));
	}
	
	/**
	 * Adds the supplied hook and acts as if this collection would have been empty and it would fill it with the current actual value.
	 * In other words: calls the event with added() being the current value
	 */
	public EventEntry<Consumer<Change<K, V>>> addHookAsStartedEmpty(Consumer<Change<K, V>> changeConsumer, @NotNull EventEntry<?>[] requiredBy, @NotNull EventEntry<?>... requires) {
		return callAsStartedEmpty(changeEvent.addHook(changeConsumer, requiredBy, requires));
	}
	
	private @NotNull EventEntry<Consumer<Change<K, V>>> callAsStartedEmpty(@NotNull EventEntry<Consumer<Change<K, V>>> hook) {
		hook.function.accept(new Change<>() {
			private Collection<K> added = List.copyOf(delegate.keySet());
			
			@Override
			public @NotNull ObservableMap<K, V> collection() {
				return ObservableMap.this;
			}
			
			@Override
			public @NotNull Collection<K> keysChanged() {
				return added;
			}
			
			@Override
			public boolean somethingChanged() {
				return !added.isEmpty();
			}
		});
		return hook;
	}
	
	//delegate
	@Override
	public int size() {
		return delegate.size();
	}
	
	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}
	
	@Override
	public V getOrDefault(Object key, V defaultValue) {
		return delegate.getOrDefault(key, defaultValue);
	}
	
	@Override
	public void forEach(BiConsumer<? super K, ? super V> action) {
		delegate.forEach(action);
	}
	
	@Override
	public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
		Collection<K> changed = new ArrayList<>();
		delegate.replaceAll((k, v) -> {
			V newV = function.apply(k, v);
			if (v != newV)
				changed.add(k);
			return newV;
		});
		triggerChangeEvent(changed);
	}
	
	@Override
	public V putIfAbsent(K key, V value) {
		boolean[] change = new boolean[1];
		V ret = delegate.computeIfAbsent(key, key1 -> {
			change[0] = true;
			return value;
		});
		if (change[0])
			triggerChangeEvent(key);
		return ret;
	}
	
	@Override
	public boolean remove(Object key, Object value) {
		boolean ret = delegate.remove(key, value);
		if (ret)
			triggerChangeEvent(key);
		return ret;
	}
	
	@Override
	public boolean replace(K key, V oldValue, V newValue) {
		boolean ret = delegate.replace(key, oldValue, newValue);
		if (ret)
			triggerChangeEvent(key);
		return ret;
	}
	
	@Nullable
	@Override
	public V replace(K key, V value) {
		boolean[] change = new boolean[1];
		V ret = delegate.computeIfPresent(key, (key1, old) -> {
			change[0] = true;
			return value;
		});
		if (change[0])
			triggerChangeEvent(key);
		return ret;
	}
	
	@Override
	public V compute(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		V ret = delegate.compute(key, remappingFunction);
		triggerChangeEvent(key);
		return ret;
	}
	
	@Override
	public V computeIfAbsent(K key, @NotNull Function<? super K, ? extends V> mappingFunction) {
		boolean[] change = new boolean[1];
		V ret = delegate.computeIfAbsent(key, (key1) -> {
			change[0] = true;
			return mappingFunction.apply(key1);
		});
		if (change[0])
			triggerChangeEvent(key);
		return ret;
	}
	
	@Override
	public V computeIfPresent(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		boolean[] change = new boolean[1];
		V ret = delegate.computeIfPresent(key, (key1, value) -> {
			change[0] = true;
			return remappingFunction.apply(key1, value);
		});
		if (change[0])
			triggerChangeEvent(key);
		//noinspection ConstantConditions
		return ret;
	}
	
	@Override
	public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
		V ret = delegate.merge(key, value, remappingFunction);
		triggerChangeEvent(key);
		return ret;
	}
	
	@Override
	public boolean containsKey(Object key) {
		return delegate.containsKey(key);
	}
	
	@Override
	public boolean containsValue(Object value) {
		return delegate.containsValue(value);
	}
	
	@Override
	public V get(Object key) {
		return delegate.get(key);
	}
	
	@Nullable
	@Override
	public V put(K key, V value) {
		V ret = delegate.put(key, value);
		triggerChangeEvent(key);
		return ret;
	}
	
	@Override
	public V remove(Object key) {
		V ret = delegate.remove(key);
		triggerChangeEvent(key);
		return ret;
	}
	
	@Override
	public void putAll(@NotNull Map<? extends K, ? extends V> m) {
		Builder<K> b = Stream.builder();
		for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
			V value = entry.getValue();
			if (value != null) {
				K key = entry.getKey();
				b.add(key);
				put(key, value);
			}
		}
		triggerChangeEvent(b.build().collect(Collectors.toUnmodifiableList()));
	}
	
	@Override
	public void clear() {
		Collection<K> removed = new ArrayList<>();
		delegate.replaceAll((key, value) -> {
			removed.add(key);
			return null;
		});
		triggerChangeEvent(removed);
	}
	
	@NotNull
	@Override
	public Set<K> keySet() {
		return new ObservableSet<>(delegate.keySet(), change -> triggerChangeEvent(change.changed()));
	}
	
	@NotNull
	@Override
	public Collection<V> values() {
		return new UnmodifiableCollection<>(delegate.values());
	}
	
	@NotNull
	@Override
	public Set<Entry<K, V>> entrySet() {
		return new ObservableSet<>(delegate.entrySet(), change -> triggerChangeEvent(change.changed().stream().map(Entry::getKey).collect(Collectors.toUnmodifiableList())));
	}
	
	@Override
	public int hashCode() {
		return delegate.hashCode();
	}
	
	//change
	@SuppressWarnings("unchecked")
	protected void triggerChangeEvent(Object keysChanged) {
		triggerChangeEvent(List.of((K) keysChanged));
	}
	
	protected void triggerChangeEvent(@NotNull Collection<K> keysChanged) {
		Change<K, V> change = new Change<>() {
			@Override
			public @NotNull ObservableMap<K, V> collection() {
				return ObservableMap.this;
			}
			
			@Override
			public @NotNull Collection<K> keysChanged() {
				return keysChanged;
			}
			
			@Override
			public boolean somethingChanged() {
				return !keysChanged.isEmpty();
			}
		};
		
		BarrierImpl newBarrier = new BarrierImpl();
		Barrier prevBarrier = lastBarrier.getAndSet(newBarrier);
		changeEvent.runImmediatelyIfPossible(changeConsumer -> changeConsumer.accept(change), prevBarrier).addHook(newBarrier::triggerNow);
	}
	
	public interface Change<K, V> {
		
		@NotNull ObservableMap<K, V> collection();
		
		@NotNull Collection<K> keysChanged();
		
		boolean somethingChanged();
	}
}
