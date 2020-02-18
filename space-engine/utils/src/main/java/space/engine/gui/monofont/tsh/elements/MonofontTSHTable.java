package space.engine.gui.monofont.tsh.elements;

import org.jetbrains.annotations.Nullable;
import space.engine.gui.GuiElement;
import space.engine.gui.exception.IllegalGuiElementException;
import space.engine.gui.monofont.MonofontGuiElement;
import space.engine.gui.monofont.elements.direction.MonofontTable;
import space.engine.gui.tsh.elements.GuiToStringHelperTableCreator;
import space.engine.gui.tsh.elements.GuiToStringHelperTableCreator.GuiToStringHelperTable;
import space.engine.indexmap.multi.IndexMultiMap;
import space.engine.indexmap.multi.IndexMultiMap.IndexMultiMapEntry;
import space.engine.indexmap.multi.IndexMultiMap2D;
import space.engine.string.CharSequence2D;

public class MonofontTSHTable extends MonofontTable implements GuiToStringHelperTable {
	
	public static final GuiToStringHelperTableCreator CREATOR = MonofontTSHTable::new;
	
	public String name;
	
	public MonofontTSHTable(String name, int dimensions) {
		if (dimensions > 2)
			throw new IllegalArgumentException("dimensions " + dimensions + " > 2 are not allowed");
		this.name = name;
	}
	
	@Override
	public GuiToStringHelperTableCreator getCreator() {
		return CREATOR;
	}
	
	//delegate
	@Override
	public GuiElement put(int[] pos, Object object) {
		if (!(object instanceof GuiElement))
			throw new IllegalGuiElementException(object.toString());
		return put(pos, (GuiElement) object);
	}
	
	//rebuild
	@Override
	public CharSequence2D rebuild0() {
		IndexMultiMap<@Nullable CharSequence2D> charTable = new IndexMultiMap2D<>();
		for (IndexMultiMapEntry<MonofontGuiElement> entry : table.table()) {
			MonofontGuiElement value = entry.getValue();
			charTable.put(entry.getIndex(), value != null ? value.buildSequence2D() : null);
		}
		return style.makeTable(name, this, charTable);
	}
}
