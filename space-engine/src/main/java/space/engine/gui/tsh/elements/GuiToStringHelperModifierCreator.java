package space.engine.gui.tsh.elements;

import space.engine.gui.GuiCreator;
import space.engine.gui.GuiElement;
import space.engine.gui.exception.IllegalGuiElementException;

@FunctionalInterface
public interface GuiToStringHelperModifierCreator extends GuiCreator {
	
	/**
	 * creates a modifier to a value in this or a similar way:
	 * <code>modifier + " " + value</code>
	 *
	 * @param modifier the modifier applied to the value (eg. synchronized, delegate etc.)
	 * @param value    the value as a {@link GuiElement}
	 * @return the new {@link GuiToStringHelperModifier}
	 * @throws IllegalGuiElementException if a supplied {@link GuiElement} is illegal (eg. wrong type)
	 */
	default GuiToStringHelperModifier create(String modifier, GuiElement value) throws IllegalGuiElementException {
		return create(modifier, " ", value);
	}
	
	/**
	 * creates a modifier to a value in this or a similar way:
	 * <code>modifier + separator + value</code>
	 *
	 * @param modifier  the modifier applied to the value (eg. synchronized, delegate etc.)
	 * @param separator the separator between the modifier and the value
	 * @param value     the value as a {@link GuiElement}
	 * @return the new {@link GuiToStringHelperModifier}
	 * @throws IllegalGuiElementException if a supplied {@link GuiElement} is illegal (eg. wrong type)
	 */
	GuiToStringHelperModifier create(String modifier, String separator, GuiElement value) throws IllegalGuiElementException;
	
	interface GuiToStringHelperModifier extends GuiElement {
	
	}
}
