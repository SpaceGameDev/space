package space.engine.gui.exception;

/**
 * thrown if a {@link space.engine.gui.GuiElement} is not supported by a specifiic {@link space.engine.gui.GuiApi}
 */
public class GuiElementUnsupportedException extends RuntimeException {
	
	public GuiElementUnsupportedException() {
	}
	
	public GuiElementUnsupportedException(String message) {
		super(message);
	}
	
	public GuiElementUnsupportedException(Throwable cause) {
		super(cause);
	}
	
	public GuiElementUnsupportedException(String message, Throwable cause) {
		super(message, cause);
	}
}
