package space.engine.event.typehandler;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Allows parallel calls.
 */
public class TypeConsumer<T> implements TypeHandlerParallel<Consumer<T>> {
	
	public T obj;
	
	public TypeConsumer(T obj) {
		this.obj = obj;
	}
	
	@Override
	public void accept(@NotNull Consumer<T> c) {
		c.accept(obj);
	}
}
