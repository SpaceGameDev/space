package space.engine.window.exception;

public class WindowException extends RuntimeException {
	
	public WindowException() {
	}
	
	public WindowException(String message) {
		super(message);
	}
	
	public WindowException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public WindowException(Throwable cause) {
		super(cause);
	}
}
