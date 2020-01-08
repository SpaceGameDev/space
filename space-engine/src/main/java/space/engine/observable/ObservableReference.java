package space.engine.observable;

import org.jetbrains.annotations.NotNull;
import space.engine.barrier.Barrier;
import space.engine.barrier.BarrierImpl;
import space.engine.barrier.DelayTask;
import space.engine.barrier.functions.ConsumerWithDelay;
import space.engine.barrier.functions.SupplierWithDelay;
import space.engine.barrier.future.Future;
import space.engine.barrier.future.FutureNotFinishedException;
import space.engine.barrier.future.GenericFuture;
import space.engine.baseobject.CanceledCheck;
import space.engine.event.Event;
import space.engine.event.EventEntry;
import space.engine.event.SequentialEventBuilder;
import space.engine.orderingGuarantee.GeneratingOrderingGuarantee;

import java.util.function.Function;

import static space.engine.barrier.Barrier.*;

/**
 * An {@link ObservableReference} allows you to update a reference to an Object T and running an {@link Event} when doing so.
 * It also guarantees ordering of Event calls independent which Thread updates the reference.
 * <p>
 * There are multiple implementations of {@link ObservableReference}:
 * - {@link MutableObservableReference}: allows you to {@link MutableObservableReference#set(Object)}, {@link MutableObservableReference#set(Generator)} or {@link MutableObservableReference#set(GeneratorWithCancelCheck)} the Object directly.
 * - {@link GeneratingObservableReference}: generates te value out of one or multiple other ObservableReferences. Automatically updates if one of the dependencies changes.
 */
public abstract class ObservableReference<T> {
	
	//static factory supplier
	public static <T> StaticObservableReference<T> fromSupplierStatic(SupplierWithDelay<T> supplier) {
		return fromSupplier(StaticObservableReference::new, supplier);
	}
	
	public static <T> MutableObservableReference<T> fromSupplierMutable(SupplierWithDelay<T> supplier) {
		return fromSupplier(MutableObservableReference::new, supplier);
	}
	
	public static <R extends ObservableReference<T>, T> R fromSupplier(Function<Barrier, R> referenceCreator, SupplierWithDelay<T> supplier) {
		return fromFuture(referenceCreator, nowFuture(supplier));
	}
	
	//static factory future
	public static <T> StaticObservableReference<T> fromFutureStatic(Future<T> future) {
		return fromFuture(StaticObservableReference::new, future);
	}
	
	public static <T> MutableObservableReference<T> fromFutureMutable(Future<T> future) {
		return fromFuture(MutableObservableReference::new, future);
	}
	
	public static <R extends ObservableReference<T>, T> R fromFuture(Function<Barrier, R> referenceCreator, Future<T> future) {
		class S implements Generator<T> {
			
			final R reference;
			
			public S() {
				BarrierImpl initialBarrier = new BarrierImpl();
				reference = referenceCreator.apply(initialBarrier);
				future.addHook(() -> reference.setInternalAlways(this).addHook(initialBarrier::triggerNow));
			}
			
			@Override
			public T get(T previous) {
				return future.assertGet();
			}
		}
		return new S().reference;
	}
	
	public static <T> MutableObservableReference<T> mutable() {
		return new MutableObservableReference<>();
	}
	
	public static <T> MutableObservableReference<T> mutable(T initial) {
		return new MutableObservableReference<>(initial);
	}
	
	public static <T> MutableObservableReference<T> mutable(SupplierWithDelay<T> supplier) {
		try {
			return new MutableObservableReference<>(supplier.get());
		} catch (DelayTask delayTask) {
			return new MutableObservableReference<>(delayTask.barrier);
		}
	}
	
	//generator
	@FunctionalInterface
	public interface Generator<T> {
		
		/**
		 * generates a value
		 *
		 * @param previous the previous value T or null if there is none
		 * @return the new value T
		 * @throws NoUpdate  if the value should not be updated
		 * @throws DelayTask if the result requires additional computing; requires a Future of type T
		 */
		T get(T previous) throws NoUpdate, DelayTask;
		
		default Generator<T> async() {
			return previous -> {
				throw new DelayTask(nowFutureWithException(NoUpdate.class, () -> get(previous)));
			};
		}
	}
	
	@FunctionalInterface
	public interface GeneratorWithCancelCheck<T> {
		
		/**
		 * generates a value
		 *
		 * @param previous      the previous value T or null if there is none
		 * @param canceledCheck to check whether the generation of the new value was canceled
		 * @return the new value T
		 * @throws NoUpdate  if the value should not be updated
		 * @throws DelayTask if the result requires additional computing; requires a Future of type T
		 */
		T get(T previous, @NotNull CanceledCheck canceledCheck) throws NoUpdate, DelayTask;
		
		default GeneratorWithCancelCheck<T> async() {
			return (previous, canceledCheck) -> {
				throw new DelayTask(nowFutureWithException(NoUpdate.class, () -> get(previous, canceledCheck)));
			};
		}
	}
	
	protected final GeneratingOrderingGuarantee ordering = new GeneratingOrderingGuarantee();
	protected final SequentialEventBuilder<ConsumerWithDelay<? super T>> changeEvent = new SequentialEventBuilder<>();
	
	private final @NotNull Future<T> initialBarrier;
	private volatile T t;
	
	@SuppressWarnings("ConstantConditions")
	protected ObservableReference() {
		this(done(), null);
	}
	
	protected ObservableReference(T initial) {
		this(done(), initial);
	}
	
	@SuppressWarnings("ConstantConditions")
	protected ObservableReference(Barrier initialBarrier) {
		this(initialBarrier, null);
	}
	
	protected ObservableReference(@NotNull Barrier initialBarrier, T t) {
		this.initialBarrier = initialBarrier.dereference().toFuture(() -> this.t);
		this.t = t;
	}
	
	//get
	
	/**
	 * Calling this function outside of a callback may cause it to suddenly return a different value.
	 * When using this method query the value once and use it over your entire lifespan so it won't change on the fly.
	 * <p>
	 * Calling this is the same as calling {@link #future()}.{@link Future#assertGet() assertGet()}
	 *
	 * @return the current T
	 * @throws FutureNotFinishedException if the initial calculation of t has not yet completed
	 */
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
	public @NotNull Future<T> future() {
		return initialBarrier;
	}
	
	//setInternalMayCancel
	
	/**
	 * requires callee to own {@link #ordering}
	 * <p>
	 * all #setInternalMayCancel may never compute or set the value and just return if canceled
	 */
	protected Barrier setInternalMayCancel(T t, CanceledCheck canceledCheck) {
		if (canceledCheck.isCanceled())
			return done();
		return setInternalAlways(t);
	}
	
	/**
	 * requires callee to own {@link #ordering}
	 * <p>
	 * all #setInternalMayCancel may never compute or set the value and just return if canceled
	 */
	protected Barrier setInternalMayCancel(Generator<T> supplier, CanceledCheck canceledCheck) {
		if (canceledCheck.isCanceled())
			return done();
		
		try {
			T t = supplier.get(this.t);
			if (canceledCheck.isCanceled())
				return done();
			return setInternalAlways(t);
		} catch (DelayTask delay) {
			if (canceledCheck.isCanceled())
				return done();
			return delay.barrier.thenStart(() -> {
				if (canceledCheck.isCanceled())
					return done();
				return setInternalMayCancel(previous -> {
					try {
						//noinspection unchecked
						return ((GenericFuture<T>) delay.barrier).assertGetAnyException();
					} catch (NoUpdate e) {
						throw e;
					} catch (Throwable e) {
						throw GenericFuture.newUnexpectedException(e);
					}
				}, canceledCheck);
			});
		} catch (NoUpdate ignored) {
			return done();
		}
	}
	
	//setInternalAlways
	
	/**
	 * requires callee to own {@link #ordering}
	 * <p>
	 * all #setInternalAlways() always call the supplier (if any) and set the new value regardless of the {@link CanceledCheck}
	 */
	protected Barrier setInternalAlways(GeneratorWithCancelCheck<T> supplier, CanceledCheck canceledCheck) {
		try {
			T t = supplier.get(this.t, canceledCheck);
			return setInternalAlways(t);
		} catch (DelayTask delay) {
			return delay.barrier.thenStart(() -> {
				//noinspection CodeBlock2Expr
				return setInternalAlways((previous, canceledCheck1) -> {
					try {
						//noinspection unchecked
						return ((GenericFuture<T>) delay.barrier).assertGetAnyException();
					} catch (NoUpdate e) {
						throw e;
					} catch (Throwable e) {
						throw GenericFuture.newUnexpectedException(e);
					}
				}, canceledCheck);
			});
		} catch (NoUpdate ignored) {
			return done();
		}
	}
	
	/**
	 * requires callee to own {@link #ordering}
	 * <p>
	 * all #setInternalAlways() always call the supplier (if any) and set the new value regardless of the {@link CanceledCheck}
	 */
	protected Barrier setInternalAlways(Generator<T> supplier) {
		try {
			T t = supplier.get(this.t);
			return setInternalAlways(t);
		} catch (DelayTask e) {
			return e.barrier.thenStart(() -> {
				//noinspection unchecked
				return setInternalAlways(((Future<T>) e.barrier).assertGet());
			});
		} catch (NoUpdate ignored) {
			return done();
		}
	}
	
	/**
	 * requires callee to own {@link #ordering}
	 * <p>
	 * all #setInternalAlways() always call the supplier (if any) and set the new value regardless of the {@link CanceledCheck}
	 */
	protected Barrier setInternalAlways(T t) {
		this.t = t;
		return changeEvent.runImmediatelyIfPossible(tConsumer -> tConsumer.accept(t));
	}
	
	/**
	 * For debugging and highly controlled testing purposes only!
	 */
	@Deprecated
	public Barrier getLatestBarrier() {
		return ordering.getLatestBarrier();
	}
	
	//addHook
	public Barrier addHook(@NotNull EventEntry<? extends ConsumerWithDelay<? super T>> hook) {
		return ordering.nextInbetween(prev -> prev.thenStart(() -> {
			changeEvent.addHook(hook);
			hook.function.accept(t);
			return done();
		}));
	}
	
	public AddHookResult<T> addHook(ConsumerWithDelay<? super T> changeConsumer) {
		EventEntry<ConsumerWithDelay<? super T>> entry = new EventEntry<>(changeConsumer);
		return new AddHookResult<>(entry, addHookNoInitialCallback(entry));
	}
	
	public AddHookResult<T> addHook(ConsumerWithDelay<? super T> changeConsumer, @NotNull EventEntry<?>... requires) {
		EventEntry<ConsumerWithDelay<? super T>> entry = new EventEntry<>(changeConsumer, requires);
		return new AddHookResult<>(entry, addHookNoInitialCallback(entry));
	}
	
	public AddHookResult<T> addHook(ConsumerWithDelay<? super T> changeConsumer, @NotNull EventEntry<?>[] requiredBy, @NotNull EventEntry<?>... requires) {
		EventEntry<ConsumerWithDelay<? super T>> entry = new EventEntry<>(changeConsumer, requiredBy, requires);
		return new AddHookResult<>(entry, addHookNoInitialCallback(entry));
	}
	
	//addHookNoInitialCallback
	public Barrier addHookNoInitialCallback(@NotNull EventEntry<? extends ConsumerWithDelay<? super T>> hook) {
		return ordering.nextInbetween(prev -> prev.thenStart(() -> {
			changeEvent.addHook(hook);
			return done();
		}));
	}
	
	public AddHookResult<T> addHookNoInitialCallback(ConsumerWithDelay<? super T> changeConsumer) {
		EventEntry<ConsumerWithDelay<? super T>> entry = new EventEntry<>(changeConsumer);
		return new AddHookResult<>(entry, addHookNoInitialCallback(entry));
	}
	
	public AddHookResult<T> addHookNoInitialCallback(ConsumerWithDelay<? super T> changeConsumer, @NotNull EventEntry<?>... requires) {
		EventEntry<ConsumerWithDelay<? super T>> entry = new EventEntry<>(changeConsumer, requires);
		return new AddHookResult<>(entry, addHookNoInitialCallback(entry));
	}
	
	public AddHookResult<T> addHookNoInitialCallback(ConsumerWithDelay<? super T> changeConsumer, @NotNull EventEntry<?>[] requiredBy, @NotNull EventEntry<?>... requires) {
		EventEntry<ConsumerWithDelay<? super T>> entry = new EventEntry<>(changeConsumer, requiredBy, requires);
		return new AddHookResult<>(entry, addHookNoInitialCallback(entry));
	}
	
	public static class AddHookResult<T> {
		
		public final EventEntry<ConsumerWithDelay<? super T>> entry;
		public final Barrier barrier;
		
		public AddHookResult(EventEntry<ConsumerWithDelay<? super T>> entry, Barrier barrier) {
			this.entry = entry;
			this.barrier = barrier;
		}
	}
}
