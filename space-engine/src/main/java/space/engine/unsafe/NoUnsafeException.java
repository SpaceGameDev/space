package space.engine.unsafe;

public class NoUnsafeException extends RuntimeException {
	
	public NoUnsafeException() {
	}
	
	public NoUnsafeException(String message) {
		super(message);
	}
	
	public NoUnsafeException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public NoUnsafeException(Throwable cause) {
		super(cause);
	}
}
