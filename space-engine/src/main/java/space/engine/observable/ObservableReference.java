package space.engine.observable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.engine.barrier.Barrier;
import space.engine.barrier.BarrierImpl;
import space.engine.barrier.DelayTask;
import space.engine.barrier.future.Future;
import space.engine.barrier.future.FutureNotFinishedException;
import space.engine.baseobject.CanceledCheck;
import space.engine.event.EventEntry;
import space.engine.event.SequentialEventBuilder;
import space.engine.orderingGuarantee.GeneratingOrderingGuarantee;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static space.engine.barrier.Barrier.*;

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
		Barrier hooksAdded = when(parents.map(parent -> parent.addHookNoInitialCallback(onChange)).toArray(Barrier[]::new));
		
		//only the initial value has additional barriers; usually you shouldn't use them as they block #ordering but here it's fine
		reference.ordering.nextInbetween(prev -> when(prev, hooksAdded).thenRun(() -> {
			//activate has to be true BEFORE we get the initial values otherwise we may miss updates
			activate.set(true);
			try {
				reference.t = generate.get();
			} catch (NoUpdate ignored) {
			
			} catch (DelayTask e) {
				throw new DelayTask(e.barrier.thenStart(() -> {
					//noinspection unchecked
					reference.t = ((Future<T>) e.barrier).assertGet();
					return done();
				}));
			}
		})).addHook(initialBarrier::triggerNow);
		
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
		Barrier hooksAdded = when(parents.map(parent -> parent.addHookNoInitialCallback(onChange)).toArray(Barrier[]::new));
		
		//only the initial value has additional barriers; usually you shouldn't use them as they block #ordering but here it's fine
		reference.ordering.nextInbetween(prev -> when(prev, hooksAdded).thenRun(() -> {
			//activate has to be true BEFORE we get the initial values otherwise we may miss updates
			activate.set(true);
			try {
				reference.t = generate.get(() -> false);
			} catch (NoUpdate ignored) {
			} catch (DelayTask e) {
				throw new DelayTask(e.barrier.thenStart(() -> {
					//noinspection unchecked
					reference.t = ((Future<T>) e.barrier).assertGet();
					return done();
				}));
			}
		})).addHook(initialBarrier::triggerNow);
		
		return reference;
	}
	
	//object
	private final GeneratingOrderingGuarantee ordering = new GeneratingOrderingGuarantee();
	protected final SequentialEventBuilder<Consumer<? super T>> changeEvent = new SequentialEventBuilder<>();
	
	private final @NotNull Future<T> initialBarrier;
	private volatile @Nullable T t;
	
	public ObservableReference() {
		this(DONE_BARRIER, null);
	}
	
	public ObservableReference(T initial) {
		this(DONE_BARRIER, initial);
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
		if (!initialBarrier.isDone())
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
		return ordering.next(prev -> prev.thenRunCancelable(canceledCheck -> {
			if (!canceledCheck.isCanceled())
				setInternal(t);
			return done();
		}));
	}
	
	public Barrier set(Generator<T> supplier) {
		return ordering.next(prev -> prev.thenRunCancelable(canceledCheck -> {
			try {
				if (canceledCheck.isCanceled())
					return done();
				
				T t;
				try {
					t = supplier.get();
				} catch (DelayTask e) {
					if (canceledCheck.isCanceled())
						return done();
					return e.barrier.thenStart(() -> {
						if (!canceledCheck.isCanceled()) {//noinspection unchecked
							setInternal(((Future<T>) e.barrier).assertGet());
						}
						return done();
					});
				}
				if (!canceledCheck.isCanceled())
					setInternal(t);
				return done();
			} catch (NoUpdate ignored) {
				return done();
			}
		}));
	}
	
	/**
	 * The supplier of this and only this method will always be executed; the result however will not be stored if canceled.
	 */
	public Barrier set(GeneratorWithCancelCheck<T> supplier) {
		return ordering.next(prev -> prev.thenRunCancelable(canceledCheck -> {
			try {
				T t;
				try {
					t = supplier.get(canceledCheck);
				} catch (DelayTask e) {
					return e.barrier.thenStart(() -> {
						//noinspection unchecked
						setInternal(((Future<T>) e.barrier).assertGet());
						return done();
					});
				}
				setInternal(t);
				return done();
			} catch (NoUpdate ignored) {
				return done();
			}
		}));
	}
	
	private void setInternal(T t) throws DelayTask {
		this.t = t;
		Barrier barrier = changeEvent.runImmediatelyIfPossible(tConsumer -> tConsumer.accept(t));
		if (barrier != DONE_BARRIER)
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
		return ordering.nextInbetween(prev -> prev.thenRun(() -> {
			hook.function.accept(t);
			changeEvent.addHook(hook);
		}));
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
		return ordering.nextInbetween(prev -> prev.thenRun(() -> {
			changeEvent.addHook(hook);
		}));
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
