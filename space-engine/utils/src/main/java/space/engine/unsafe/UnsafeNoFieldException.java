package space.engine.unsafe;

public class UnsafeNoFieldException extends RuntimeException {
	
	public UnsafeNoFieldException() {
	}
	
	public UnsafeNoFieldException(String message) {
		super(message);
	}
	
	public UnsafeNoFieldException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public UnsafeNoFieldException(Throwable cause) {
		super(cause);
	}
}
