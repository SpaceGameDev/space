package space.engine.simpleQueue.pool;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.stream.Stream;

public interface Executor extends java.util.concurrent.Executor {
	
	@Override
	void execute(@NotNull Runnable command);
	
	default void executeAll(@NotNull Collection<@NotNull Runnable> commands) {
		commands.forEach(this::execute);
	}
	
	default void executeAll(@NotNull Runnable[] commands) {
		for (Runnable command : commands)
			execute(command);
	}
	
	default void executeAll(@NotNull Stream<@NotNull Runnable> commands) {
		commands.forEach(this::execute);
	}
}
