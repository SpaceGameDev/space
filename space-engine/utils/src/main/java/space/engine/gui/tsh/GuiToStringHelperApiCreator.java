package space.engine.gui.tsh;

import space.engine.gui.GuiCreator;
import space.engine.string.toStringHelper.ToStringHelper;

@FunctionalInterface
public interface GuiToStringHelperApiCreator<T> extends GuiCreator {
	
	ToStringHelper<T> create();
}
