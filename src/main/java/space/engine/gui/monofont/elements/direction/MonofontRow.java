package space.engine.gui.monofont.elements.direction;

import space.engine.gui.elements.direction.GuiRowCreator;
import space.engine.gui.elements.direction.GuiRowCreator.GuiRow;
import space.engine.gui.monofont.MonofontIncluding;
import space.engine.gui.monofont.tableCreator.MonofontColumnCreator;
import space.engine.gui.monofont.tableCreator.MonofontColumnCreator.ColumnDirection;
import space.engine.gui.monofont.tableCreator.MonofontWithColumnCreator;
import space.engine.string.CharSequence2D;

public class MonofontRow extends MonofontElementList implements GuiRow, MonofontWithColumnCreator {
	
	static {
		MonofontIncluding.toIncludeList.add(MonofontRow.class);
	}
	
	public static final GuiRowCreator CREATOR = MonofontRow::new;
	
	public MonofontColumnCreator style;
	
	@Override
	public GuiRowCreator getCreator() {
		return CREATOR;
	}
	
	@Override
	public void setMonofontColumnCreator(MonofontColumnCreator style) {
		this.style = style;
	}
	
	@Override
	public CharSequence2D rebuild0() {
		return style.makeTable("", this, ColumnDirection.VERTICAL, buildList());
	}
}
