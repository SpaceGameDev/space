package space.engine.window.exception;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WindowErrorIdException extends WindowException {
	
	public static final String UNKNOWN_ERROR_ID = "UNKNOWN_ERROR_ID";
	
	public final int errorId;
	
	public WindowErrorIdException(int errorId) {
		this(errorId, null, null);
	}
	
	public WindowErrorIdException(int errorId, @Nullable String errorName) {
		this(errorId, errorName, null);
	}
	
	public WindowErrorIdException(int errorId, @Nullable String errorName, @Nullable String desc) {
		super(generateMessage(errorId, errorName, desc));
		this.errorId = errorId;
	}
	
	@NotNull
	private static String generateMessage(int errorId, @Nullable String errorName, @Nullable String desc) {
		StringBuilder b = new StringBuilder();
		b.append(errorName != null ? errorName : UNKNOWN_ERROR_ID);
		b.append("[0x").append(Integer.toHexString(errorId)).append(']');
		if (desc != null)
			b.append(": ").append(desc);
		return b.toString();
	}
}
