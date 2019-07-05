package space.engine.gui.monofont.elements.direction;

import org.jetbrains.annotations.Nullable;
import space.engine.gui.GuiCreator;
import space.engine.gui.GuiElement;
import space.engine.gui.elements.direction.GuiTableCreator;
import space.engine.gui.elements.direction.GuiTableCreator.GuiTable;
import space.engine.gui.exception.IllegalGuiElementException;
import space.engine.gui.monofont.MonofontGuiElement;
import space.engine.gui.monofont.MonofontGuiElementCaching;
import space.engine.gui.monofont.MonofontIncluding;
import space.engine.gui.monofont.tableCreator.multi.MonofontTableCreator;
import space.engine.gui.monofont.tableCreator.multi.MonofontTableCreatorDefault;
import space.engine.gui.monofont.tableCreator.multi.MonofontWithTableCreator;
import space.engine.indexmap.multi.IndexMultiMap;
import space.engine.indexmap.multi.IndexMultiMap.IndexMultiMapEntry;
import space.engine.indexmap.multi.IndexMultiMap2D;
import space.engine.string.CharSequence2D;

public class MonofontTable extends MonofontGuiElementCaching implements GuiTable, MonofontWithTableCreator {
	
	static {
		MonofontIncluding.toIncludeList.add(MonofontTable.class);
	}
	
	public static final GuiTableCreator CREATOR = MonofontTable::new;
	
	public MonofontTableCreator style = MonofontTableCreatorDefault.STYLE_DEFAULT;
	public IndexMultiMap<MonofontGuiElement> table = new IndexMultiMap2D<>();
	
	@Override
	public void setMonofontTableCreator(MonofontTableCreator style) {
		this.style = style;
	}
	
	@Override
	public GuiCreator getCreator() {
		return CREATOR;
	}
	
	@Nullable
	public GuiElement put(int[] pos, GuiElement gui) {
		if (!(gui instanceof MonofontGuiElement))
			throw new IllegalGuiElementException(gui);
		MonofontGuiElement v = (MonofontGuiElement) gui;
		v.setParent(this);
		MonofontGuiElement old = table.put(pos, v);
		if (old != null)
			old.setParent(null);
		return old;
	}
	
	@Nullable
	public GuiElement remove(int[] pos) {
		MonofontGuiElement old = table.remove(pos);
		if (old != null)
			old.setParent(null);
		return old;
	}
	
	//rebuild
	@Override
	public CharSequence2D rebuild0() {
		IndexMultiMap<@Nullable CharSequence2D> charTable = new IndexMultiMap2D<>();
		for (IndexMultiMapEntry<MonofontGuiElement> entry : table.table()) {
			MonofontGuiElement value = entry.getValue();
			charTable.put(entry.getIndex(), value != null ? value.buildSequence2D() : null);
		}
		return style.makeTable("", this, charTable);
	}
}
