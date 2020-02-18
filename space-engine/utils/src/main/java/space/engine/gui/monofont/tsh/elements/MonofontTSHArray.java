package space.engine.gui.monofont.tsh.elements;

import space.engine.gui.GuiCreator;
import space.engine.gui.monofont.elements.direction.MonofontElementList;
import space.engine.gui.monofont.tableCreator.MonofontColumnCreator;
import space.engine.gui.monofont.tableCreator.MonofontColumnCreator.ColumnDirection;
import space.engine.gui.monofont.tableCreator.MonofontColumnCreatorArray;
import space.engine.gui.tsh.elements.GuiToStringHelperArrayCreator;
import space.engine.gui.tsh.elements.GuiToStringHelperArrayCreator.ToStringHelperArray;
import space.engine.string.CharSequence2D;

import java.util.function.Supplier;

public class MonofontTSHArray extends MonofontElementList implements ToStringHelperArray {
	
	public static final GuiToStringHelperArrayCreator CREATOR = MonofontTSHArray::new;
	public static Supplier<MonofontColumnCreatorArray> DEFAULT = () -> MonofontColumnCreatorArray.INSTANCE;
	
	public Class<?> type;
	public MonofontColumnCreator style = DEFAULT.get();
	
	public MonofontTSHArray(Class<?> type) {
		this.type = type;
	}
	
	@Override
	public GuiCreator getCreator() {
		return CREATOR;
	}
	
	@Override
	public CharSequence2D rebuild0() {
		return style.makeTable(type.getName() + "[]", this, ColumnDirection.VERTICAL, buildList());
	}
}
