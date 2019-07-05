package space.engine.gui.tsh.elements;

import space.engine.gui.GuiCreator;
import space.engine.gui.GuiElement;
import space.engine.string.toStringHelper.ToStringHelper;
import space.engine.string.toStringHelper.ToStringHelper.ToStringHelperObjectsInstance;

public interface GuiToStringHelperObjectsCreator extends GuiCreator {
	
	/**
	 * creates a new {@link ToStringHelperObjects} Instance
	 *
	 * @param obj the {@link Object} to create an Instance for
	 * @return the new {@link ToStringHelperObjects}
	 */
	ToStringHelperObjects create(Object obj, ToStringHelper<? extends GuiElement> helper);
	
	interface ToStringHelperObjects extends GuiElement, ToStringHelperObjectsInstance<GuiElement> {
	
	}
}
