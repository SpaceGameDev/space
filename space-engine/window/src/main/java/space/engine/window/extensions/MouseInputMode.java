package space.engine.window.extensions;

import org.jetbrains.annotations.NotNull;
import space.engine.key.attribute.AttributeKey;

import static space.engine.window.Window.CREATOR;

public interface MouseInputMode {
	
	AttributeKey<@NotNull Modes> MOUSE_MODE = CREATOR.createKeyWithDefault(Modes.CURSOR_NORMAL);
	
	@SuppressWarnings("unused")
	enum Modes {
		
		CURSOR_NORMAL,
		CURSOR_HIDDEN,
		CURSOR_DISABLED
	}
}
