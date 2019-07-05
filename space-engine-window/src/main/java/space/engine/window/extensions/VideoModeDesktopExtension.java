package space.engine.window.extensions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.engine.key.attribute.AttributeKey;
import space.engine.window.Window;

@SuppressWarnings("unused")
public interface VideoModeDesktopExtension extends VideoModeExtension {
	
	AttributeKey<@NotNull Integer> WIDTH = VideoModeExtension.WIDTH;
	AttributeKey<@NotNull Integer> HEIGHT = VideoModeExtension.HEIGHT;
	AttributeKey<@Nullable Integer> POS_X = Window.CREATOR.createKeyWithDefault(null);
	AttributeKey<@Nullable Integer> POS_Y = Window.CREATOR.createKeyWithDefault(null);
	AttributeKey<@NotNull Boolean> HAS_TRANSPARENCY = Window.CREATOR.createKeyWithDefault(false);
}
