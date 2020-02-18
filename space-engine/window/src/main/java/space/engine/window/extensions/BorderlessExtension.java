package space.engine.window.extensions;

import org.jetbrains.annotations.NotNull;
import space.engine.key.attribute.AttributeKey;

import static space.engine.window.Window.CREATOR;

public interface BorderlessExtension extends WindowExtension {
	
	AttributeKey<@NotNull Boolean> BORDERLESS = CREATOR.createKeyWithDefault(Boolean.FALSE);
}
