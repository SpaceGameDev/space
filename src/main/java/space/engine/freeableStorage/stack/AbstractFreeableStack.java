package space.engine.freeableStorage.stack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.engine.freeableStorage.FreeableList;
import space.engine.freeableStorage.stack.AbstractFreeableStack.Frame;
import space.engine.sync.barrier.Barrier;

public abstract class AbstractFreeableStack<FRAME extends Frame> implements FreeableStack {
	
	protected @Nullable FRAME current;
	
	protected abstract @NotNull FRAME createFrame(@Nullable FRAME prev);
	
	@Override
	public final @NotNull FRAME frame() {
		return current = createFrame(current);
	}
	
	public class Frame implements FreeableStack.Frame {
		
		protected @Nullable FRAME prev;
		private @Nullable FreeableList subList;
		
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
			return subList != null ? subList.free() : Barrier.ALWAYS_TRIGGERED_BARRIER;
		}
		
		@Override
		public boolean isFreed() {
			return prev == null;
		}
		
		@Override
		public @NotNull FreeableList getSubList() {
			FreeableList subList = this.subList;
			if (subList != null)
				return subList;
			return this.subList = new FreeableList();
		}
	}
}
