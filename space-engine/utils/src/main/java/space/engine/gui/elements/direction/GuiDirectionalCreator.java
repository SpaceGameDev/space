package space.engine.gui.elements.direction;

import org.jetbrains.annotations.Nullable;
import space.engine.gui.GuiCreator;
import space.engine.gui.GuiElement;

/**
 * when using this, the resulting element should go in the inverse direction of the parent (row - column - row - ...)
 */
@FunctionalInterface
public interface GuiDirectionalCreator extends GuiCreator {
	
	/**
	 * Creates a {@link GuiDirectional}.
	 * Use the Methods {@link GuiDirectional#put(int, GuiElement)} and {@link GuiDirectional#remove(int)} to config it like a {@link space.engine.indexmap.IndexMap}.
	 *
	 * @return a new {@link GuiDirectional} to fill a table within
	 */
	GuiDirectional create();
	
	interface GuiDirectional extends GuiElement {
		
		@Nullable GuiElement put(int index, @Nullable GuiElement v);
		
		@Nullable GuiElement remove(int index);
	}
}
