package space.engine.window;

import org.jetbrains.annotations.NotNull;
import space.engine.barrier.future.Future;

public interface WindowFrameworkCreator {
	
	@NotNull Future<WindowFramework> createFramework();
}
