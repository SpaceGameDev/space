package space.engine.window.extensions;

import org.jetbrains.annotations.Nullable;
import space.engine.key.attribute.AttributeKey;
import space.engine.window.Monitor.VideoMode;
import space.engine.window.Window;

@SuppressWarnings("unused")
public interface VideoModeFullscreenExtension extends VideoModeExtension {
	
	/**
	 * null -> video mode same as Desktop
	 */
	AttributeKey<@Nullable VideoMode> FULLSCREEN_VIDEO_MODE = Window.CREATOR.createKeyWithDefault(null);
}
