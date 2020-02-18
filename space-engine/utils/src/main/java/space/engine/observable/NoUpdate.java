package space.engine.observable;

/**
 * Thrown when a {@link ObservableReference} should not be changed
 */
public class NoUpdate extends Exception {
	
	public NoUpdate() {
		super(null, null, false, false);
	}
}
