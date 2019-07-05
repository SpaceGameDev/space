package space.engine.gui.monofont.tableCreator;

import space.engine.gui.monofont.MonofontGuiElement;
import space.engine.indexmap.IndexMap;
import space.engine.string.CharSequence2D;

@FunctionalInterface
public interface MonofontColumnCreator {
	
	/**
	 * creates a nice table String2D from a {@link IndexMap}
	 *
	 * @param name       the name of the Column, may be displayed
	 * @param guiElement the {@link MonofontGuiElement} generating
	 * @param direction  the {@link ColumnDirection} in which to create a Column, may be ignored
	 * @param valueTable contains all entries, may contain null elements supposed to treated like empty strings
	 * @return a nice table
	 */
	CharSequence2D makeTable(String name, MonofontGuiElement guiElement, ColumnDirection direction, IndexMap<CharSequence2D> valueTable);
	
	enum ColumnDirection {
		
		HORIZONTAL(0),
		VERTICAL(1);
		
		static {
			HORIZONTAL.opposite = VERTICAL;
			VERTICAL.opposite = HORIZONTAL;
		}
		
		public final int dimensionalDepth;
		private ColumnDirection opposite;
		
		ColumnDirection(int dimensionalDepth) {
			this.dimensionalDepth = dimensionalDepth;
		}
		
		public ColumnDirection opposite() {
			return opposite;
		}
	}
}
