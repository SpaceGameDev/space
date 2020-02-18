package space.engine.vulkan.exception;

public class UnsupportedConfigurationException extends RuntimeException {
	
	public UnsupportedConfigurationException() {
	}
	
	public UnsupportedConfigurationException(String message) {
		super(message);
	}
	
	public UnsupportedConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public UnsupportedConfigurationException(Throwable cause) {
		super(cause);
	}
	
	public UnsupportedConfigurationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
