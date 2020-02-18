package space.engine.gui.tsh.elements;

import space.engine.gui.GuiCreator;
import space.engine.gui.GuiElement;

@FunctionalInterface
public interface GuiToStringHelperArrayCreator extends GuiCreator {
	
	/**
	 * creates a new {@link ToStringHelperArray}
	 *
	 * @param type the type of array
	 * @return a new {@link ToStringHelperArray}
	 */
	ToStringHelperArray create(Class<?> type);
	
	interface ToStringHelperArray extends GuiElement {
		
		GuiElement put(int index, GuiElement element);
	}
}
