package space.engine.sync.barrier;

import org.jetbrains.annotations.NotNull;
import space.engine.baseobject.Cancelable;

import java.util.concurrent.TimeUnit;

public interface CancelableBarrier extends Barrier, Cancelable {
	
	@SuppressWarnings("unused")
	CancelableBarrier ALWAYS_TRIGGERED_CANCELABLE_BARRIER = new CancelableBarrier() {
		@Override
		public boolean isFinished() {
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
		
		@Override
		public Barrier dereference() {
			return ALWAYS_TRIGGERED_CANCELABLE_BARRIER;
		}
	};
}
