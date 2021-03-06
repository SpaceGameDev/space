package space.engine.freeable.stack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.engine.barrier.Barrier;
import space.engine.freeable.CleanerDependencyList;
import space.engine.freeable.stack.AbstractFreeableStack.Frame;

public abstract class AbstractFreeableStack<FRAME extends Frame> implements FreeableStack {
	
	protected @Nullable FRAME current;
	
	protected abstract @NotNull FRAME createFrame(@Nullable FRAME prev);
	
	@Override
	public final @NotNull FRAME frame() {
		return current = createFrame(current);
	}
	
	public class Frame implements FreeableStack.Frame {
		
		protected @Nullable FRAME prev;
		private @Nullable CleanerDependencyList subList;
		
		public Frame(@Nullable FRAME prev) {
			this.prev = prev;
		}
		
		//topFrame
		public boolean isTopFrame() {
			return current == this;
		}
		
		//free
		@Override
		public @NotNull Barrier free() {
			assertTopFrame();
			current = prev;
			prev = null;
			return subList != null ? subList.free() : Barrier.DONE_BARRIER;
		}
		
		@Override
		public boolean isFreed() {
			return prev == null;
		}
		
		@Override
		public @NotNull CleanerDependencyList getSubList() {
			CleanerDependencyList subList = this.subList;
			if (subList != null)
				return subList;
			return this.subList = new CleanerDependencyList();
		}
	}
}
