package space.engine.event.typehandler;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * The {@link #result()} will be the first {@link Function} returning something != null.
 * Sequential only.
 */
public class TypeHandlerFirstFunction<T, R> implements TypeHandler<Function<T, R>> {
	
	private final T input;
	private R result;
	
	public TypeHandlerFirstFunction(T input) {
		this.input = input;
	}
	
	@Override
	public void accept(@NotNull Function<T, R> task) {
		if (result == null)
			result = task.apply(input);
	}
	
	public R result() {
		return result;
	}
}
