package space.engine.orderingGuarantee;

import org.jetbrains.annotations.NotNull;
import space.engine.sync.barrier.Barrier;
import space.engine.sync.barrier.BarrierImpl;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.function.Function;

import static space.engine.sync.barrier.Barrier.ALWAYS_TRIGGERED_BARRIER;

public class SequentialOrderingGuarantee {
	
	private static final VarHandle LASTBARRIER;
	
	static {
		try {
			MethodHandles.Lookup l = MethodHandles.lookup();
			LASTBARRIER = l.findVarHandle(SequentialOrderingGuarantee.class, "lastBarrier", Barrier.class);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new ExceptionInInitializerError(e);
		}
	}
	
	private Barrier lastBarrier = ALWAYS_TRIGGERED_BARRIER;
	
	/**
	 * Enqueues a new function to be executed in the sequence.
	 *
	 * @param function a function generating a new task which will start execution when supplied {@link Barrier} is triggered and returns a {@link Barrier} triggered when execution has finished
	 * @return the Barrier returned by the function parameter
	 */
	public <B extends @NotNull Barrier> B next(Function<@NotNull ? super Barrier, B> function) {
		BarrierImpl next = new BarrierImpl();
		Barrier prev = (Barrier) LASTBARRIER.getAndSet(this, next);
		
		B apply = function.apply(prev);
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
}
