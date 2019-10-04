package space.engine.buffer;

import org.jetbrains.annotations.NotNull;
import space.engine.freeable.stack.FreeableStack;

public interface AllocatorStack extends FreeableStack {
	
	@Override
	@NotNull AllocatorFrame frame();
	
	interface AllocatorFrame extends FreeableStack.Frame, Allocator {
	
	}
}
