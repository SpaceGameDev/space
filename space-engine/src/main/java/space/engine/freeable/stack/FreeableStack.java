package space.engine.freeable.stack;

import org.jetbrains.annotations.NotNull;
import space.engine.freeable.Freeable;

public interface FreeableStack {
	
	@NotNull Frame frame();
	
	interface Frame extends Freeable, AutoCloseable {
		
		@Override
		default void close() {
			free();
		}
		
		boolean isTopFrame();
		
		default void assertTopFrame() {
			if (!isTopFrame())
				throw new IllegalStateException("this frame is not the current frame");
		}
	}
}
