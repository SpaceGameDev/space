package space.engine.delegate.indexmap;

import org.jetbrains.annotations.NotNull;
import space.engine.barrier.Barrier;
import space.engine.barrier.BarrierImpl;
import space.engine.delegate.collection.ObservableCollection;
import space.engine.event.Event;
import space.engine.event.EventEntry;
import space.engine.event.SequentialEventBuilder;
import space.engine.indexmap.IndexMap;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.IntStream.Builder;

/**
 * An {@link ObservableIndexMap} allows you to be notified when index got changed form the {@link IndexMap}.
 * Add a Callback to the {@link #getChangeEvent()} to get notified with a {@link Change} happening to this {@link IndexMap}.
 * <p>
 * The implementation is threadsafe if the {@link #delegate} {@link IndexMap} given to the constructor is threadsafe. It also guarantees that Event callbacks are called in order.
 */
public class ObservableIndexMap<VALUE> implements IndexMap<VALUE> {
	
	private final IndexMap<VALUE> delegate;
	private final SequentialEventBuilder<Consumer<Change<VALUE>>> changeEvent = new SequentialEventBuilder<>();
	private AtomicReference<Barrier> lastBarrier = new AtomicReference<>(Barrier.DONE_BARRIER);
	
	public ObservableIndexMap(IndexMap<VALUE> delegate) {
		this.delegate = delegate;
	}
	
	public ObservableIndexMap(IndexMap<VALUE> delegate, EventEntry<Consumer<Change<VALUE>>> listener) {
		this(delegate);
		this.changeEvent.addHook(listener);
	}
	
	public ObservableIndexMap(IndexMap<VALUE> delegate, Consumer<Change<VALUE>> listener) {
		this(delegate);
		this.changeEvent.addHook(listener);
	}
	
	public Event<Consumer<Change<VALUE>>> getChangeEvent() {
		return changeEvent;
	}
	
	//delegate event
	public void addHook(@NotNull EventEntry<Consumer<Change<VALUE>>> hook) {
		changeEvent.addHook(hook);
	}
	
	public EventEntry<Consumer<Change<VALUE>>> addHook(Consumer<Change<VALUE>> changeConsumer) {
		return changeEvent.addHook(changeConsumer);
	}
	
	public EventEntry<Consumer<Change<VALUE>>> addHook(Consumer<Change<VALUE>> changeConsumer, @NotNull EventEntry<?>... requires) {
		return changeEvent.addHook(changeConsumer, requires);
	}
	
	public EventEntry<Consumer<Change<VALUE>>> addHook(Consumer<Change<VALUE>> changeConsumer, @NotNull EventEntry<?>[] requiredBy, @NotNull EventEntry<?>... requires) {
		return changeEvent.addHook(changeConsumer, requiredBy, requires);
	}
	
	/**
	 * Adds the supplied hook and acts as if this collection would have been empty and it would fill it with the current actual value.
	 * In other words: calls the event with added() being the current value
	 */
	public void addHookAsStartedEmpty(@NotNull EventEntry<Consumer<Change<VALUE>>> hook) {
		changeEvent.addHook(hook);
		callAsStartedEmpty(hook);
	}
	
	/**
	 * Adds the supplied hook and acts as if this collection would have been empty and it would fill it with the current actual value.
	 * In other words: calls the event with added() being the current value
	 */
	public EventEntry<Consumer<Change<VALUE>>> addHookAsStartedEmpty(Consumer<Change<VALUE>> changeConsumer) {
		return callAsStartedEmpty(changeEvent.addHook(changeConsumer));
	}
	
	/**
	 * Adds the supplied hook and acts as if this collection would have been empty and it would fill it with the current actual value.
	 * In other words: calls the event with added() being the current value
	 */
	public EventEntry<Consumer<Change<VALUE>>> addHookAsStartedEmpty(Consumer<Change<VALUE>> changeConsumer, @NotNull EventEntry<?>... requires) {
		return callAsStartedEmpty(changeEvent.addHook(changeConsumer, requires));
	}
	
	/**
	 * Adds the supplied hook and acts as if this collection would have been empty and it would fill it with the current actual value.
	 * In other words: calls the event with added() being the current value
	 */
	public EventEntry<Consumer<Change<VALUE>>> addHookAsStartedEmpty(Consumer<Change<VALUE>> changeConsumer, @NotNull EventEntry<?>[] requiredBy, @NotNull EventEntry<?>... requires) {
		return callAsStartedEmpty(changeEvent.addHook(changeConsumer, requiredBy, requires));
	}
	
	private @NotNull EventEntry<Consumer<Change<VALUE>>> callAsStartedEmpty(@NotNull EventEntry<Consumer<Change<VALUE>>> hook) {
		hook.function.accept(new Change<>() {
			private int[] added = delegate.entrySet().stream().mapToInt(Entry::getIndex).toArray();
			
			@Override
			public @NotNull ObservableIndexMap<VALUE> collection() {
				return ObservableIndexMap.this;
			}
			
			@Override
			public @NotNull int[] changedIndexes() {
				return added;
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
	public boolean contains(int index) {
		return delegate.contains(index);
	}
	
	@Override
	public VALUE get(int index) {
		return delegate.get(index);
	}
	
	@Override
	public @NotNull Entry<VALUE> getEntry(int index) {
		return new Entry<>() {
			@NotNull Entry<VALUE> entry = delegate.getEntry(index);
			
			@Override
			public int getIndex() {
				return entry.getIndex();
			}
			
			@Override
			public VALUE getValue() {
				return entry.getValue();
			}
			
			@Override
			public void setValue(VALUE v) {
				entry.setValue(v);
				triggerChangeEvent(index);
			}
		};
	}
	
	@Override
	public VALUE put(int index, VALUE v) {
		VALUE ret = delegate.put(index, v);
		triggerChangeEvent(index);
		return ret;
	}
	
	@Override
	public VALUE remove(int index) {
		VALUE ret = delegate.remove(index);
		triggerChangeEvent(index);
		return ret;
	}
	
	@Override
	public VALUE[] toArray() {
		return delegate.toArray();
	}
	
	@Override
	public VALUE[] toArray(@NotNull VALUE[] array) {
		return delegate.toArray(array);
	}
	
	@Override
	public void putAll(@NotNull IndexMap<? extends VALUE> indexMap) {
		Builder b = IntStream.builder();
		for (Entry<? extends VALUE> entry : indexMap.entrySet()) {
			VALUE value = entry.getValue();
			if (value != null) {
				int index = entry.getIndex();
				b.add(index);
				put(index, value);
			}
		}
		triggerChangeEvent(b.build().toArray());
	}
	
	@Override
	public void putAllIfAbsent(@NotNull IndexMap<? extends VALUE> indexMap) {
		Builder b = IntStream.builder();
		for (Entry<? extends VALUE> entry : indexMap.entrySet()) {
			int index = entry.getIndex();
			computeIfAbsent(index, () -> {
				b.add(index);
				return entry.getValue();
			});
		}
		triggerChangeEvent(b.build().toArray());
	}
	
	@Override
	public VALUE getOrDefault(int index, VALUE def) {
		return delegate.getOrDefault(index, def);
	}
	
	@Override
	public VALUE putIfAbsent(int index, VALUE v) {
		boolean[] change = new boolean[1];
		VALUE value = delegate.computeIfAbsent(index, () -> {
			change[0] = true;
			return v;
		});
		if (change[0])
			triggerChangeEvent(index);
		return value;
	}
	
	@Override
	public VALUE putIfPresent(int index, VALUE v) {
		boolean[] change = new boolean[1];
		VALUE value = delegate.computeIfPresent(index, () -> {
			change[0] = true;
			return v;
		});
		if (change[0])
			triggerChangeEvent(index);
		return value;
	}
	
	@Override
	public boolean replace(int index, VALUE oldValue, VALUE newValue) {
		boolean ret = delegate.replace(index, oldValue, newValue);
		if (ret)
			triggerChangeEvent(index);
		return ret;
	}
	
	@Override
	public boolean replace(int index, VALUE oldValue, @NotNull Supplier<? extends VALUE> newValue) {
		boolean ret = delegate.replace(index, oldValue, newValue);
		if (ret)
			triggerChangeEvent(index);
		return ret;
	}
	
	@Override
	public boolean remove(int index, VALUE v) {
		boolean ret = delegate.remove(index, v);
		if (ret)
			triggerChangeEvent(index);
		return ret;
	}
	
	@Override
	public VALUE compute(int index, @NotNull ComputeFunction<? super VALUE, ? extends VALUE> function) {
		VALUE ret = delegate.compute(index, function);
		triggerChangeEvent(index);
		return ret;
	}
	
	@Override
	public VALUE computeIfAbsent(int index, @NotNull Supplier<? extends VALUE> supplier) {
		boolean[] change = new boolean[1];
		VALUE ret = delegate.computeIfAbsent(index, () -> {
			change[0] = true;
			return supplier.get();
		});
		if (change[0])
			triggerChangeEvent(index);
		return ret;
	}
	
	@Override
	public VALUE computeIfPresent(int index, @NotNull Supplier<? extends VALUE> supplier) {
		boolean[] change = new boolean[1];
		VALUE ret = delegate.computeIfPresent(index, () -> {
			change[0] = true;
			return supplier.get();
		});
		if (change[0])
			triggerChangeEvent(index);
		return ret;
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
	
	@Override
	public void clear() {
		Builder removed = IntStream.builder();
		delegate.entrySet().removeIf(entry -> {
			removed.add(entry.getIndex());
			return true;
		});
		triggerChangeEvent(removed.build().toArray());
	}
	
	@Override
	public @NotNull Collection<VALUE> values() {
		//values() collection is unmodifiable; no need for specfic implementation
		return delegate.values();
	}
	
	@Override
	public @NotNull Collection<Entry<VALUE>> entrySet() {
		return new ObservableCollection<>(delegate.entrySet(), change -> triggerChangeEvent(change.changed().stream().mapToInt(Entry::getIndex).toArray()));
	}
	
	//change
	protected void triggerChangeEvent(@NotNull int... indexes) {
		Change<VALUE> change = new Change<>() {
			@Override
			public @NotNull ObservableIndexMap<VALUE> collection() {
				return ObservableIndexMap.this;
			}
			
			@Override
			public @NotNull int[] changedIndexes() {
				return indexes;
			}
		};
		
		BarrierImpl newBarrier = new BarrierImpl();
		Barrier prevBarrier = lastBarrier.getAndSet(newBarrier);
		prevBarrier.thenStart(() -> {
			Barrier ret = changeEvent.submit(changeConsumer -> changeConsumer.accept(change));
			ret.addHook(newBarrier::triggerNow);
			return ret;
		});
	}
	
	public interface Change<E> {
		
		@NotNull ObservableIndexMap<E> collection();
		
		@NotNull int[] changedIndexes();
	}
}
