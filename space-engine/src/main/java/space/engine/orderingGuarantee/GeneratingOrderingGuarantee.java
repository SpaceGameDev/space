package space.engine.orderingGuarantee;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.engine.sync.barrier.Barrier;
import space.engine.sync.barrier.BarrierImpl;
import space.engine.sync.barrier.CancelableBarrier;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.function.Function;

import static space.engine.sync.barrier.CancelableBarrier.ALWAYS_TRIGGERED_CANCELABLE_BARRIER;

public class GeneratingOrderingGuarantee {
	
	private static final VarHandle LASTBARRIER;
	
	static {
		try {
			MethodHandles.Lookup l = MethodHandles.lookup();
			LASTBARRIER = l.findVarHandle(GeneratingOrderingGuarantee.class, "lastBarrier", CancelableBarrier.class);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new ExceptionInInitializerError(e);
		}
	}
	
	private @NotNull CancelableBarrier lastBarrier = ALWAYS_TRIGGERED_CANCELABLE_BARRIER;
	
	/**
	 * Cancels the previous one and executes the new one. Can be canceled by further calls to this function.
	 *
	 * @param function a function generating a new task which will start execution when supplied 'prev' {@link Barrier} is triggered and returns a {@link Barrier} triggered when execution has finished
	 * @return the Barrier returned by the function parameter
	 */
	public <B extends @NotNull CancelableBarrier> B next(Function<@NotNull ? super Barrier, B> function) {
		IntermediateCancelableBarrier next = new IntermediateCancelableBarrier();
		CancelableBarrier prev = (CancelableBarrier) LASTBARRIER.getAndSet(this, next);
		prev.cancel();
		
		B apply = function.apply(prev);
		next.setCancel(apply);
		apply.addHook(next::triggerNow);
		return apply;
	}
	
	/**
	 * Does <b>NOT</b> cancel the previous one. Will always be executed. Cannot be canceled itself.
	 * In other words: Sets in between the usual {@link CancelableBarrier} and redirects the cancel to it's previous one, making it unaffected by cancels but still guarantees ordering.
	 *
	 * @param function a function generating a new task which will start execution when supplied {@link Barrier} is triggered and returns a {@link Barrier} triggered when execution has finished
	 * @return the Barrier returned by the function parameter
	 */
	public <B extends @NotNull Barrier> B nextInbetween(Function<@NotNull ? super Barrier, B> function) {
		IntermediateCancelableBarrier next = new IntermediateCancelableBarrier();
		CancelableBarrier prev = (CancelableBarrier) LASTBARRIER.getAndSet(this, next);
		//no prev.cancel()
		
		B apply = function.apply(prev);
		next.setCancel(prev); //prev instead of apply
		apply.addHook(next::triggerNow);
		return apply;
	}
	
	/**
	 * For debugging and highly controlled testing purposes only!
	 */
	@Deprecated
	public Barrier getLatestBarrier() {
		return (Barrier) LASTBARRIER.getVolatile(this);
	}
	
	private static class IntermediateCancelableBarrier extends BarrierImpl implements CancelableBarrier {
		
		private static final CancelableBarrier CANCEL_ALREADY_CALLED_OBJECT = ALWAYS_TRIGGERED_CANCELABLE_BARRIER;
		private static final VarHandle CANCEL;
		
		static {
			try {
				MethodHandles.Lookup l = MethodHandles.lookup();
				CANCEL = l.findVarHandle(IntermediateCancelableBarrier.class, "cancel", CancelableBarrier.class);
			} catch (NoSuchFieldException | IllegalAccessException e) {
				throw new ExceptionInInitializerError(e);
			}
		}
		
		@SuppressWarnings("unused")
		private @Nullable CancelableBarrier cancel;
		
		public void setCancel(CancelableBarrier cancel) {
			CancelableBarrier old = (CancelableBarrier) CANCEL.compareAndExchange(this, null, cancel);
			if (old == CANCEL_ALREADY_CALLED_OBJECT) {
				cancel.cancel();
			} else if (old != null) {
				throw new IllegalStateException("cancel already set!");
			}
		}
		
		@Override
		public void cancel() {
			CancelableBarrier old = (CancelableBarrier) CANCEL.compareAndExchange(this, null, CANCEL_ALREADY_CALLED_OBJECT);
			if (old != null && old != CANCEL_ALREADY_CALLED_OBJECT)
				old.cancel();
		}
	}
}
