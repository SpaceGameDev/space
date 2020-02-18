package space.engine.barrier;

import space.engine.baseobject.CanceledCheck;

public class CancelableBarrierImpl extends BarrierImpl implements CancelableBarrier, CanceledCheck {
	
	private volatile boolean canceled;
	
	public CancelableBarrierImpl() {
	}
	
	public CancelableBarrierImpl(boolean initialTriggerState) {
		super(initialTriggerState);
	}
	
	@Override
	public void cancel() {
		canceled = true;
	}
	
	@Override
	public boolean isCanceled() {
		return canceled;
	}
}
