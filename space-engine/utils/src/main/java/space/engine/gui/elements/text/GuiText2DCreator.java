package space.engine.gui.elements.text;

import space.engine.gui.GuiCreator;
import space.engine.gui.GuiElement;
import space.engine.string.CharSequence2D;

public interface GuiText2DCreator extends GuiCreator {
	
	/**
	 * creates a simple 2D Text
	 *
	 * @param text the text, in {@link CharSequence2D}
	 * @return a new {@link GuiElement} of set 2D text
	 */
	GuiText2D create(CharSequence2D text);
	
	interface GuiText2D extends GuiElement {
	
	}
}
