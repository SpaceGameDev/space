package space.engine.barrier;

import space.engine.baseobject.Cancelable;

public interface CancelableBarrier extends Barrier, Cancelable {
	
	class CancelableDoneBarrier extends Barrier.DoneBarrier implements CancelableBarrier {
		
		@Override
		public void cancel() {
		
		}
	}
	
	CancelableBarrier CANCELABLE_DONE_BARRIER = new CancelableDoneBarrier();
	
	static CancelableBarrier cancelableDone() {
		return CANCELABLE_DONE_BARRIER;
	}
	
}
