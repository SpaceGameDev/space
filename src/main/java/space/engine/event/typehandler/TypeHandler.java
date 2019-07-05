package space.engine.event.typehandler;

import org.jetbrains.annotations.NotNull;
import space.engine.sync.DelayTask;

/**
 * A {@link TypeHandler} handles the Execution of FUNCTIONs.
 *
 * @param <FUNCTION> the Function type
 * @see TypeHandlerParallel if this TypeHandler allows parallel execution.
 */
@FunctionalInterface
public interface TypeHandler<FUNCTION> {
	
	void accept(@NotNull FUNCTION function) throws DelayTask;
}
