package space.engine.barrier;

import org.jetbrains.annotations.NotNull;
import space.engine.baseobject.Cancelable;

import java.util.concurrent.TimeUnit;

public interface CancelableBarrier extends Barrier, Cancelable {
	
	@SuppressWarnings("unused")
	CancelableBarrier CANCELABLE_DONE_BARRIER = new CancelableBarrier() {
		@Override
		public boolean isDone() {
			return true;
		}
		
		@Override
		public void addHook(@NotNull Runnable run) {
			run.run();
		}
		
		@Override
		public void removeHook(@NotNull Runnable run) {
		
		}
		
		@Override
		public void await() {
		
		}
		
		@Override
		public void await(long time, TimeUnit unit) {
		
		}
		
		@Override
		public void cancel() {
		
		}
	};
	
	static CancelableBarrier cancelableDone() {
		return CANCELABLE_DONE_BARRIER;
	}
	
}
