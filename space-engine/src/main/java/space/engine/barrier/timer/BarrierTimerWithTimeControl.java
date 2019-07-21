package space.engine.barrier.timer;

/**
 * A {@link BarrierTimer} with extensive methods for manipulating speed and warping time
 */
public class BarrierTimerWithTimeControl extends BarrierTimer {
	
	private double speedNanos;
	private long offsetNanos;
	
	/**
	 * Timer with normal speed starting at the point in time this Constructor is called
	 */
	public BarrierTimerWithTimeControl(Object[] parents) {
		this(1.0, -System.nanoTime(), parents);
	}
	
	/**
	 * Timer with normal speed starting at a specified point in time
	 */
	public BarrierTimerWithTimeControl(long offsetNanos, Object[] parents) {
		this(1.0, offsetNanos, parents);
	}
	
	/**
	 * Timer with variable speed starting at a point in time
	 */
	public BarrierTimerWithTimeControl(double speedNanos, long offsetNanos, Object[] parents) {
		super(parents);
		this.speedNanos = speedNanos;
		this.offsetNanos = offsetNanos;
	}
	
	//override
	@Override
	public long timeFunction(long input) {
		return (long) (input / speedNanos) - offsetNanos;
	}
	
	@Override
	public long currTime() {
		return (long) ((System.nanoTime() + offsetNanos) * speedNanos);
	}
	
	@Override
	public double currTimeFraction() {
		return (System.nanoTime() + offsetNanos) * speedNanos;
	}
	
	@Override
	public double currSpeed() {
		return speedNanos;
	}
	
	//speed
	public void setSpeed(double speedNanos) {
		this.speedNanos = speedNanos;
		recalculateTimer();
	}
	
	public void pause() {
		setSpeed(0);
	}
	
	public void normalSpeed() {
		setSpeed(1);
	}
	
	//offset
	public void warpTo(long offsetTime) {
		warpToNanos(timeFunction(offsetTime));
	}
	
	public void warpToNanos(long offsetNanos) {
		if (offsetNanos < this.offsetNanos)
			throw new IllegalStateException("warping to the past!");
		this.offsetNanos = offsetNanos;
		recalculateTimer();
	}
	
	public void warpBy(long deltaTime) {
		warpToNanos(timeFunction(deltaTime) + offsetNanos);
	}
	
	public void warpByNanos(long deltaNanos) {
		warpToNanos(deltaNanos + offsetNanos);
	}
}
