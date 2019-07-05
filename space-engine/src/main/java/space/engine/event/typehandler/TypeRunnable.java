package space.engine.event.typehandler;

import org.jetbrains.annotations.NotNull;

/**
 * Use {@link #INSTANCE} instead of creating a new {@link TypeRunnable}.
 * Allows parallel calls.
 */
public class TypeRunnable implements TypeHandlerParallel<Runnable> {
	
	public static final TypeRunnable INSTANCE = new TypeRunnable();
	
	private TypeRunnable() {
	}
	
	@Override
	public void accept(@NotNull Runnable runnable) {
		runnable.run();
	}
}
