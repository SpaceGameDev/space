package space.engine.window.extensions;

import org.jetbrains.annotations.NotNull;
import space.engine.key.attribute.AttributeKey;
import space.engine.window.Window;

public interface ResizeableExtension extends WindowExtension {
	
	AttributeKey<@NotNull Boolean> RESIZEABLE = Window.CREATOR.createKeyWithDefault(false);
	AttributeKey<@NotNull Integer> MINX = Window.CREATOR.createKeyWithDefault(0);
	AttributeKey<@NotNull Integer> MINY = Window.CREATOR.createKeyWithDefault(0);
	AttributeKey<@NotNull Integer> MAXX = Window.CREATOR.createKeyWithDefault(Integer.MAX_VALUE);
	AttributeKey<@NotNull Integer> MAXY = Window.CREATOR.createKeyWithDefault(Integer.MAX_VALUE);
}
