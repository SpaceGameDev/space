package space.engine.gui.monofont.tableCreator;

import space.engine.gui.monofont.MonofontGuiElement;
import space.engine.gui.monofont.tableCreator.multi.MonofontTableCreator;
import space.engine.indexmap.IndexMap;
import space.engine.indexmap.multi.IndexMultiMapFrom1DIndexMap;
import space.engine.string.CharSequence2D;

public class MonofontColumnFromTable implements MonofontColumnCreator {
	
	public MonofontTableCreator tableCreator;
	public int depth;
	
	public MonofontColumnFromTable(MonofontTableCreator tableCreator) {
		this(tableCreator, 0);
	}
	
	public MonofontColumnFromTable(MonofontTableCreator tableCreator, int depth) {
		this.tableCreator = tableCreator;
		this.depth = depth;
	}
	
	public static MonofontColumnFromTable createHorizontal(MonofontTableCreator tableCreator) {
		return new MonofontColumnFromTable(tableCreator, 0);
	}
	
	public static MonofontColumnFromTable createVertical(MonofontTableCreator tableCreator) {
		return new MonofontColumnFromTable(tableCreator, 1);
	}
	
	@Override
	public CharSequence2D makeTable(String name, MonofontGuiElement guiElement, ColumnDirection direction, IndexMap<CharSequence2D> valueTable) {
		return tableCreator.makeTable(name, guiElement, new IndexMultiMapFrom1DIndexMap<>(valueTable, false, depth));
	}
}
