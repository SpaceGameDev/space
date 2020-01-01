package space.engine.barrier;

import org.jetbrains.annotations.NotNull;
import space.engine.barrier.future.Future;
import space.engine.barrier.future.FutureWithException;
import space.engine.barrier.future.GenericFuture;

/**
 * This Exception can be thrown during the execution of any Task to 'delay' the execution finish.
 * The Task catching the {@link DelayTask} should not finish it's execution until the {@link #barrier} with that the {@link DelayTask} was constructed is triggered.
 * If the Task is a {@link Future} of any kind (including {@link FutureWithException} and variations),
 * the barrier should be a Future which result is to use as it's own result (including any declared Exceptions). As it is not possible to do compile-time checks,
 * implementations should have a result of null if the barrier is not a Future or rethrow any Exception uncaught which it cannot handle.
 * It is recommended that implementations use {@link GenericFuture} as these Methods are implemented by all types of {@link Future}.
 */
public final class DelayTask extends Exception {
	
	public final @NotNull Barrier barrier;
	
	public DelayTask(@NotNull Barrier barrier) {
		super(null, null, false, false);
		this.barrier = barrier;
	}
}
