package space.engine.delegate.set;

import org.jetbrains.annotations.NotNull;
import space.engine.barrier.Barrier;
import space.engine.barrier.BarrierImpl;
import space.engine.delegate.collection.MergingCollection;
import space.engine.delegate.collection.UnmodifiableCollection;
import space.engine.event.Event;
import space.engine.event.EventEntry;
import space.engine.event.SequentialEventBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An {@link ObservableSet} allows you to be notified when something got added or removed form the {@link Collection}.
 * Add a Callback to the {@link #getChangeEvent()} to get notified with a {@link Change} happening to this {@link Collection}.
 * <p>
 * The implementation is threadsafe if the {@link #delegate} {@link Collection} given to the constructor is threadsafe. It also guarantees that Event callbacks are called in order.
 */
public class ObservableSet<E> implements Set<E> {
	
	private final Set<E> delegate;
	private final SequentialEventBuilder<Consumer<Change<E>>> changeEvent = new SequentialEventBuilder<>();
	private AtomicReference<Barrier> lastBarrier = new AtomicReference<>(Barrier.DONE_BARRIER);
	
	public ObservableSet(Set<E> delegate) {
		this.delegate = delegate;
	}
	
	public ObservableSet(Set<E> delegate, EventEntry<Consumer<Change<E>>> listener) {
		this(delegate);
		this.changeEvent.addHook(listener);
	}
	
	public ObservableSet(Set<E> delegate, Consumer<Change<E>> listener) {
		this(delegate);
		this.changeEvent.addHook(listener);
	}
	
	public Event<Consumer<Change<E>>> getChangeEvent() {
		return changeEvent;
	}
	
	//delegate event
	public void addHook(@NotNull EventEntry<Consumer<Change<E>>> hook) {
		changeEvent.addHook(hook);
	}
	
	public EventEntry<Consumer<Change<E>>> addHook(Consumer<Change<E>> changeConsumer) {
		return changeEvent.addHook(changeConsumer);
	}
	
	public EventEntry<Consumer<Change<E>>> addHook(Consumer<Change<E>> changeConsumer, @NotNull EventEntry<?>... requires) {
		return changeEvent.addHook(changeConsumer, requires);
	}
	
	public EventEntry<Consumer<Change<E>>> addHook(Consumer<Change<E>> changeConsumer, @NotNull EventEntry<?>[] requiredBy, @NotNull EventEntry<?>... requires) {
		return changeEvent.addHook(changeConsumer, requiredBy, requires);
	}
	
	/**
	 * Adds the supplied hook and acts as if this collection would have been empty and it would fill it with the current actual value.
	 * In other words: calls the event with added() being the current value
	 */
	public void addHookAsStartedEmpty(@NotNull EventEntry<Consumer<Change<E>>> hook) {
		changeEvent.addHook(hook);
		callAsStartedEmpty(hook);
	}
	
	/**
	 * Adds the supplied hook and acts as if this collection would have been empty and it would fill it with the current actual value.
	 * In other words: calls the event with added() being the current value
	 */
	public EventEntry<Consumer<Change<E>>> addHookAsStartedEmpty(Consumer<Change<E>> changeConsumer) {
		return callAsStartedEmpty(changeEvent.addHook(changeConsumer));
	}
	
	/**
	 * Adds the supplied hook and acts as if this collection would have been empty and it would fill it with the current actual value.
	 * In other words: calls the event with added() being the current value
	 */
	public EventEntry<Consumer<Change<E>>> addHookAsStartedEmpty(Consumer<Change<E>> changeConsumer, @NotNull EventEntry<?>... requires) {
		return callAsStartedEmpty(changeEvent.addHook(changeConsumer, requires));
	}
	
	/**
	 * Adds the supplied hook and acts as if this collection would have been empty and it would fill it with the current actual value.
	 * In other words: calls the event with added() being the current value
	 */
	public EventEntry<Consumer<Change<E>>> addHookAsStartedEmpty(Consumer<Change<E>> changeConsumer, @NotNull EventEntry<?>[] requiredBy, @NotNull EventEntry<?>... requires) {
		return callAsStartedEmpty(changeEvent.addHook(changeConsumer, requiredBy, requires));
	}
	
	private @NotNull EventEntry<Consumer<Change<E>>> callAsStartedEmpty(@NotNull EventEntry<Consumer<Change<E>>> hook) {
		hook.function.accept(new Change<>() {
			private List<E> added = List.copyOf(delegate);
			
			@Override
			public @NotNull ObservableSet<E> collection() {
				return ObservableSet.this;
			}
			
			@Override
			public @NotNull Collection<E> added() {
				return added;
			}
			
			@Override
			public @NotNull Collection<E> removed() {
				return List.of();
			}
			
			@Override
			public @NotNull Collection<E> changed() {
				return added;
			}
			
			@Override
			public boolean somethingAdded() {
				return !added.isEmpty();
			}
			
			@Override
			public boolean somethingRemoved() {
				return false;
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
	public boolean contains(Object o) {
		return delegate.contains(o);
	}
	
	@NotNull
	@Override
	public Iterator<E> iterator() {
		return new Iterator<>() {
			Iterator<E> iter = delegate.iterator();
			E prev = null;
			
			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}
			
			@Override
			public E next() {
				return prev = iter.next();
			}
			
			@Override
			public void remove() {
				ObservableSet.this.remove(prev);
			}
		};
	}
	
	@NotNull
	@Override
	public Object[] toArray() {
		return delegate.toArray();
	}
	
	@NotNull
	@Override
	@SuppressWarnings("SuspiciousToArrayCall")
	public <T> T[] toArray(@NotNull T[] a) {
		return delegate.toArray(a);
	}
	
	@Override
	public boolean add(E e) {
		boolean ret = delegate.add(e);
		if (ret)
			triggerChangeEvent(List.of(e), List.of());
		return ret;
	}
	
	@Override
	public boolean remove(Object o) {
		boolean ret = delegate.remove(o);
		if (ret)
			//noinspection unchecked
			triggerChangeEvent(List.of((E) o), List.of());
		return ret;
	}
	
	@Override
	public boolean containsAll(@NotNull Collection<?> c) {
		return delegate.containsAll(c);
	}
	
	@Override
	public boolean addAll(@NotNull Collection<? extends E> c) {
		List<E> added = c.stream().filter(delegate::add).collect(Collectors.toUnmodifiableList());
		if (added.isEmpty())
			return false;
		triggerChangeEvent(added, List.of());
		return true;
	}
	
	@Override
	public boolean removeAll(@NotNull Collection<?> c) {
		//noinspection unchecked,SuspiciousMethodCalls
		List<E> removed = c.stream().filter(delegate::remove).map(o -> (E) o).collect(Collectors.toUnmodifiableList());
		if (removed.isEmpty())
			return false;
		triggerChangeEvent(List.of(), removed);
		return true;
	}
	
	@Override
	public boolean retainAll(@NotNull Collection<?> c) {
		return this.removeIf(e -> !c.contains(e));
	}
	
	@Override
	public void clear() {
		delegate.clear();
		triggerChangeEvent(List.of(), new UnmodifiableCollection<>(delegate));
	}
	
	@Override
	public boolean removeIf(Predicate<? super E> filter) {
		List<E> removed = new ArrayList<>();
		boolean ret = delegate.removeIf(e -> {
			if (filter.test(e)) {
				removed.add(e);
				return true;
			}
			return false;
		});
		if (!removed.isEmpty())
			//noinspection unchecked
			triggerChangeEvent(List.of(), (List<E>) List.of(removed.toArray()));
		return ret;
	}
	
	@Override
	public Spliterator<E> spliterator() {
		return delegate.spliterator();
	}
	
	@Override
	public Stream<E> stream() {
		return delegate.stream();
	}
	
	@Override
	public Stream<E> parallelStream() {
		return delegate.parallelStream();
	}
	
	@Override
	public void forEach(Consumer<? super E> action) {
		delegate.forEach(action);
	}
	
	@Override
	public int hashCode() {
		return delegate.hashCode();
	}
	
	@Override
	@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	public boolean equals(Object obj) {
		return delegate.equals(obj);
	}
	
	//change
	protected void triggerChangeEvent(@NotNull Collection<E> added, @NotNull Collection<E> removed) {
		Change<E> change = new Change<>() {
			@Override
			public @NotNull ObservableSet<E> collection() {
				return ObservableSet.this;
			}
			
			@Override
			public @NotNull Collection<E> added() {
				return added;
			}
			
			@Override
			public @NotNull Collection<E> removed() {
				return removed;
			}
			
			@Override
			public @NotNull Collection<E> changed() {
				return added.isEmpty() ? removed : removed.isEmpty() ? added : new MergingCollection<>(added, removed);
			}
			
			@Override
			public boolean somethingAdded() {
				return !added.isEmpty();
			}
			
			@Override
			public boolean somethingRemoved() {
				return !removed.isEmpty();
			}
			
			@Override
			public boolean somethingChanged() {
				return !added.isEmpty() || !removed.isEmpty();
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
		
		@NotNull ObservableSet<E> collection();
		
		@NotNull Collection<E> added();
		
		@NotNull Collection<E> removed();
		
		/**
		 * added() + removed()
		 */
		@NotNull Collection<E> changed();
		
		boolean somethingAdded();
		
		boolean somethingRemoved();
		
		boolean somethingChanged();
	}
}
