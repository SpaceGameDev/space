package space.engine.freeable.stack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FreeableStackImpl extends AbstractFreeableStack<AbstractFreeableStack.Frame> {
	
	@NotNull
	@Override
	protected AbstractFreeableStack.Frame createFrame(@Nullable AbstractFreeableStack.Frame prev) {
		return new Frame(prev);
	}
}
