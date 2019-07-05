package space.engine.window.exception;

public class WindowUnsupportedApiTypeException extends WindowException {
	
	public final Object apiType;
	
	public WindowUnsupportedApiTypeException(Object apiType) {
		super("Api type " + apiType + " unsupported!");
		this.apiType = apiType;
	}
}
