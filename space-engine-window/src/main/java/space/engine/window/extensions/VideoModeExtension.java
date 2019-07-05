package space.engine.window.extensions;

import org.jetbrains.annotations.NotNull;
import space.engine.key.attribute.AttributeKey;
import space.engine.window.Window;

@SuppressWarnings("unused")
public interface VideoModeExtension extends WindowExtension {
	
	AttributeKey<@NotNull Integer> WIDTH = Window.CREATOR.createKeyWithDefault(1920);
	AttributeKey<@NotNull Integer> HEIGHT = Window.CREATOR.createKeyWithDefault(1080);
}
