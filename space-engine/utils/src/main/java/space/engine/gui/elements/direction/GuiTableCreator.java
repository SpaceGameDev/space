package space.engine.gui.elements.direction;

import space.engine.gui.GuiCreator;
import space.engine.gui.GuiElement;

public interface GuiTableCreator extends GuiCreator {
	
	/**
	 * creates a {@link GuiTable}.
	 * Use the Methods {@link GuiTable#put(int[], GuiElement)} and {@link GuiTable#remove(int[])}
	 * to config it like a {@link space.engine.indexmap.multi.IndexMultiMap}.
	 *
	 * @return a new {@link GuiTable} to fill a table within
	 */
	GuiTable create();
	
	interface GuiTable extends GuiElement {
		
		GuiElement put(int[] pos, GuiElement v);
		
		GuiElement remove(int[] pos);
	}
}
