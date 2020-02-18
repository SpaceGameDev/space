package space.engine.gui.monofont.tableCreator.multi;

import space.engine.gui.monofont.MonofontGuiElement;
import space.engine.indexmap.multi.IndexMultiMap;
import space.engine.string.CharSequence2D;

@FunctionalInterface
public interface MonofontTableCreator {
	
	/**
	 * creates a nice table String2D from a {@link IndexMultiMap}
	 *
	 * @param name       the name of the Column, may be displayed
	 * @param guiElement the {@link MonofontGuiElement} generating
	 * @param valueTable contains all entries, may contain null elements supposed to treated like empty strings
	 * @return a nice table
	 */
	CharSequence2D makeTable(String name, MonofontGuiElement guiElement, IndexMultiMap<CharSequence2D> valueTable);
}
