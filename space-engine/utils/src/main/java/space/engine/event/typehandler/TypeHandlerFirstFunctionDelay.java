package space.engine.event.typehandler;

import org.jetbrains.annotations.NotNull;
import space.engine.barrier.DelayTask;
import space.engine.barrier.functions.FunctionWithDelay;

import java.util.function.Function;

/**
 * The {@link #result()} will be the first {@link Function} returning something != null.
 * Sequential only.
 */
public class TypeHandlerFirstFunctionDelay<T, R> implements TypeHandler<FunctionWithDelay<T, R>> {
	
	private final T input;
	private R result;
	
	public TypeHandlerFirstFunctionDelay(T input) {
		this.input = input;
	}
	
	@Override
	public void accept(@NotNull FunctionWithDelay<T, R> task) throws DelayTask {
		if (result == null)
			result = task.apply(input);
	}
	
	public R result() {
		return result;
	}
}
