package space.engine.barrier.future;

public class FutureNotFinishedException extends RuntimeException {
	
	public final Object future;
	
	public FutureNotFinishedException(Object future) {
		super("Future: " + future);
		this.future = future;
	}
}
