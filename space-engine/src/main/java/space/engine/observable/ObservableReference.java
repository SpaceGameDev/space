package space.engine.observable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.engine.baseobject.CanceledCheck;
import space.engine.event.EventEntry;
import space.engine.event.SequentialEventBuilder;
import space.engine.orderingGuarantee.GeneratingOrderingGuarantee;
import space.engine.sync.DelayTask;
import space.engine.sync.barrier.Barrier;
import space.engine.sync.barrier.BarrierImpl;
import space.engine.sync.future.Future;
import space.engine.sync.future.FutureNotFinishedException;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static space.engine.sync.Tasks.*;
import static space.engine.sync.barrier.Barrier.ALWAYS_TRIGGERED_BARRIER;

/**
 * An {@link ObservableReference} allows you to update a reference with {@link #set(Object)} and running an {@link space.engine.event.Event} when doing so.
 * It also guarantees ordering of Event calls independent which Thread updates the reference.
 */
public class ObservableReference<T> {
	
	//generatingReference Generator
	public static <T> ObservableReference<T> generatingReference(Generator<T> generate, ObservableReference<?>... parents) {
		return generatingReference(generate, Arrays.stream(parents));
	}
	
	public static <T> ObservableReference<T> generatingReference(Generator<T> generate, Collection<ObservableReference<?>> parents) {
		return generatingReference(generate, parents.stream());
	}
	
	public static <T> ObservableReference<T> generatingReference(Generator<T> generate, Stream<ObservableReference<?>> parents) {
		BarrierImpl initialBarrier = new BarrierImpl();
		ObservableReference<T> reference = new ObservableReference<>(initialBarrier);
		
		//activate makes sure no callbacks get processed before we calculated the initial value
		AtomicBoolean activate = new AtomicBoolean();
		EventEntry<Consumer<? super Object>> onChange = new EventEntry<>(o -> {
			if (activate.get())
				reference.set(generate);
		});
		Barrier[] hooksAdded = parents.map(parent -> parent.addHookNoInitialCallback(onChange)).toArray(Barrier[]::new);
		
		//only the initial value has additional barriers; usually you shouldn't use them as they block #ordering but here it's fine
		reference.ordering.nextInbetween(prev -> runnable(hooksAdded, () -> {
			//activate has to be true BEFORE we get the initial values otherwise we may miss updates
			activate.set(true);
			try {
				reference.t = generate.get();
				initialBarrier.triggerNow();
			} catch (DelayTask e) {
				throw new DelayTask(
						runnable(() -> {
							//noinspection unchecked
							reference.t = ((Future<T>) e.barrier).assertGet();
							initialBarrier.triggerNow();
						}).submit(e.barrier)
				);
			} catch (NoUpdate ignored) {
				throw new UnsupportedOperationException("Generator threw NoUpdate on initial value calculation!");
			}
		}).submit(prev));
		
		return reference;
	}
	
	//generatingReference GeneratorWithCancelCheck
	public static <T> ObservableReference<T> generatingReference(GeneratorWithCancelCheck<T> generate, ObservableReference<?>... parents) {
		return generatingReference(generate, Arrays.stream(parents));
	}
	
	public static <T> ObservableReference<T> generatingReference(GeneratorWithCancelCheck<T> generate, Collection<ObservableReference<?>> parents) {
		return generatingReference(generate, parents.stream());
	}
	
	public static <T> ObservableReference<T> generatingReference(GeneratorWithCancelCheck<T> generate, Stream<ObservableReference<?>> parents) {
		BarrierImpl initialBarrier = new BarrierImpl();
		ObservableReference<T> reference = new ObservableReference<>(initialBarrier);
		
		//activate makes sure no callbacks get processed before we calculated the initial value
		AtomicBoolean activate = new AtomicBoolean();
		EventEntry<Consumer<? super Object>> onChange = new EventEntry<>(o -> {
			if (activate.get())
				reference.set(generate);
		});
		Barrier[] hooksAdded = parents.map(parent -> parent.addHookNoInitialCallback(onChange)).toArray(Barrier[]::new);
		
		//only the initial value has additional barriers; usually you shouldn't use them as they block #ordering but here it's fine
		reference.ordering.nextInbetween(prev -> runnable(hooksAdded, () -> {
			//activate has to be true BEFORE we get the initial values otherwise we may miss updates
			activate.set(true);
			try {
				reference.t = generate.get(() -> false);
				initialBarrier.triggerNow();
			} catch (DelayTask e) {
				throw new DelayTask(
						runnable(() -> {
							//noinspection unchecked
							reference.t = ((Future<T>) e.barrier).assertGet();
							initialBarrier.triggerNow();
						}).submit(e.barrier)
				);
			} catch (NoUpdate ignored) {
				throw new UnsupportedOperationException("Generator threw NoUpdate on initial value calculation!");
			}
		}).submit(prev));
		
		return reference;
	}
	
	//object
	private final GeneratingOrderingGuarantee ordering = new GeneratingOrderingGuarantee();
	protected final SequentialEventBuilder<Consumer<? super T>> changeEvent = new SequentialEventBuilder<>();
	
	private final @NotNull Future<T> initialBarrier;
	private volatile @Nullable T t;
	
	public ObservableReference() {
		this(ALWAYS_TRIGGERED_BARRIER, null);
	}
	
	public ObservableReference(T initial) {
		this(ALWAYS_TRIGGERED_BARRIER, initial);
	}
	
	public ObservableReference(Barrier initialBarrier) {
		this(initialBarrier, null);
	}
	
	protected ObservableReference(@NotNull Barrier initialBarrier, @Nullable T t) {
		this.initialBarrier = initialBarrier.dereference().toFuture(() -> this.t);
		this.t = t;
	}
	
	//get
	
	/**
	 * Calling this function outside of a callback may cause it to suddenly return a different value.
	 * When using this method query the value once and use it over your entire lifespan so it won't change on the fly.
	 * <p>
	 * Calling this is the same as calling {@link #getFuture()}.{@link Future#assertGet() assertGet()}
	 *
	 * @return the current T
	 * @throws FutureNotFinishedException if the initial calculation of t has not yet completed
	 */
	@SuppressWarnings("ConstantConditions")
	public T assertGet() throws FutureNotFinishedException {
		if (!initialBarrier.isFinished())
			throw new FutureNotFinishedException(this);
		return t;
	}
	
	/**
	 * Calling this function outside of a callback may cause it to suddenly return a different value.
	 * When using this method query the value once and use it over your entire lifespan so it won't change on the fly.
	 * <p>
	 *
	 * @return a Future which is finished when the initial value is calculated
	 */
	public @NotNull Future<T> getFuture() {
		return initialBarrier;
	}
	
	//set
	public Barrier set(T t) {
		return ordering.next(prev -> runnableCancelable(canceledCheck -> {
			if (canceledCheck.isCanceled())
				return;
			setInternal(t);
		}).submit(prev));
	}
	
	public Barrier set(Generator<T> supplier) {
		return ordering.next(prev -> runnableCancelable(canceledCheck -> {
			try {
				if (canceledCheck.isCanceled())
					return;
				
				T t;
				try {
					t = supplier.get();
				} catch (DelayTask e) {
					if (canceledCheck.isCanceled())
						return;
					throw new DelayTask(
							runnable(() -> {
								if (canceledCheck.isCanceled())
									return;
								//noinspection unchecked
								setInternal(((Future<T>) e.barrier).assertGet());
							}).submit(e.barrier)
					);
				}
				if (canceledCheck.isCanceled())
					return;
				setInternal(t);
			} catch (NoUpdate ignored) {
			
			}
		}).submit(prev));
	}
	
	/**
	 * The supplier of this and only this method will always be executed; the result however will not be stored if canceled.
	 */
	public Barrier set(GeneratorWithCancelCheck<T> supplier) {
		return ordering.next(prev -> runnableCancelable(canceledCheck -> {
			try {
				T t;
				try {
					t = supplier.get(canceledCheck);
				} catch (DelayTask e) {
					throw new DelayTask(
							runnable(() -> {
								//noinspection unchecked
								setInternal(((Future<T>) e.barrier).assertGet());
							}).submit(e.barrier)
					);
				}
				setInternal(t);
			} catch (NoUpdate ignored) {
			
			}
		}).submit(prev));
	}
	
	private void setInternal(T t) throws DelayTask {
		this.t = t;
		Barrier barrier = changeEvent.runImmediatelyIfPossible(tConsumer -> tConsumer.accept(t));
		if (barrier != ALWAYS_TRIGGERED_BARRIER)
			throw new DelayTask(barrier);
	}
	
	/**
	 * For debugging and highly controlled testing purposes only!
	 */
	@Deprecated
	public Barrier getLatestBarrier() {
		return ordering.getLatestBarrier();
	}
	
	//addHook
	public Barrier addHook(@NotNull EventEntry<? extends Consumer<? super T>> hook) {
		return ordering.nextInbetween(prev -> runnable(() -> {
			hook.function.accept(t);
			changeEvent.addHook(hook);
		}).submit(prev));
	}
	
	public EventEntry<Consumer<? super T>> addHook(Consumer<? super T> changeConsumer) {
		EventEntry<Consumer<? super T>> entry = new EventEntry<>(changeConsumer);
		addHook(entry);
		return entry;
	}
	
	public EventEntry<Consumer<? super T>> addHook(Consumer<? super T> changeConsumer, @NotNull EventEntry<?>... requires) {
		EventEntry<Consumer<? super T>> entry = new EventEntry<>(changeConsumer, requires);
		addHook(entry);
		return entry;
	}
	
	public EventEntry<Consumer<? super T>> addHook(Consumer<? super T> changeConsumer, @NotNull EventEntry<?>[] requiredBy, @NotNull EventEntry<?>... requires) {
		EventEntry<Consumer<? super T>> entry = new EventEntry<>(changeConsumer, requiredBy, requires);
		addHook(entry);
		return entry;
	}
	
	//addHookNoInitialCallback
	public Barrier addHookNoInitialCallback(@NotNull EventEntry<? extends Consumer<? super T>> hook) {
		return ordering.nextInbetween(prev -> runnable(() -> changeEvent.addHook(hook)).submit(prev));
	}
	
	public EventEntry<Consumer<? super T>> addHookNoInitialCallback(Consumer<? super T> changeConsumer) {
		EventEntry<Consumer<? super T>> entry = new EventEntry<>(changeConsumer);
		addHookNoInitialCallback(entry);
		return entry;
	}
	
	public EventEntry<Consumer<? super T>> addHookNoInitialCallback(Consumer<? super T> changeConsumer, @NotNull EventEntry<?>... requires) {
		EventEntry<Consumer<? super T>> entry = new EventEntry<>(changeConsumer, requires);
		addHookNoInitialCallback(entry);
		return entry;
	}
	
	public EventEntry<Consumer<? super T>> addHookNoInitialCallback(Consumer<? super T> changeConsumer, @NotNull EventEntry<?>[] requiredBy, @NotNull EventEntry<?>... requires) {
		EventEntry<Consumer<? super T>> entry = new EventEntry<>(changeConsumer, requiredBy, requires);
		addHookNoInitialCallback(entry);
		return entry;
	}
	
	//generator
	@FunctionalInterface
	public interface Generator<T> {
		
		T get() throws NoUpdate, DelayTask;
	}
	
	@FunctionalInterface
	public interface GeneratorWithCancelCheck<T> {
		
		T get(CanceledCheck canceledCheck) throws NoUpdate, DelayTask;
	}
}
