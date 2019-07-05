package space.engine.gui.monofont.tsh.elements;

import space.engine.gui.GuiElement;
import space.engine.gui.exception.IllegalGuiElementException;
import space.engine.gui.monofont.MonofontGuiElement;
import space.engine.gui.monofont.MonofontGuiElementCaching;
import space.engine.gui.monofont.tableCreator.multi.MonofontTableCreator;
import space.engine.gui.monofont.tableCreator.multi.MonofontTableCreatorNoBorder;
import space.engine.gui.tsh.elements.GuiToStringHelperMapperCreator;
import space.engine.gui.tsh.elements.GuiToStringHelperMapperCreator.GuiToStringHelperMapper;
import space.engine.indexmap.IndexMap;
import space.engine.indexmap.IndexMapArray;
import space.engine.indexmap.multi.IndexMultiMap;
import space.engine.indexmap.multi.IndexMultiMap2D;
import space.engine.string.CharSequence2D;
import space.engine.string.StringBuilder2D;

public class MonofontTSHMapper extends MonofontGuiElementCaching implements GuiToStringHelperMapper {
	
	public static final GuiToStringHelperMapperCreator CREATOR = MonofontTSHMapper::new;
	
	public String name;
	public CharSequence2D separator;
	public boolean align;
	public MonofontTableCreator style = MonofontTableCreatorNoBorder.DEFAULT;
	public IndexMap<Entry> table = new IndexMapArray<>();
	
	public MonofontTSHMapper(String name, CharSequence2D separator, boolean align) {
		this.name = name;
		this.separator = separator;
		this.align = align;
	}
	
	@Override
	public GuiToStringHelperMapperCreator getCreator() {
		return CREATOR;
	}
	
	@Override
	public GuiElement put(int[] pos, Object object) {
		if (!(object instanceof MonofontGuiElement))
			throw new IllegalGuiElementException();
		MonofontGuiElement gui = (MonofontGuiElement) object;
		
		Entry ent = table.computeIfAbsent(pos[0], MonofontTSHMapper.Entry::new);
		MonofontGuiElement old;
		if (pos[1] == 0) {
			old = ent.key;
			ent.key = gui;
		} else if (pos[1] == 1) {
			old = ent.value;
			ent.value = gui;
		} else
			throw new IllegalArgumentException("pos[1] " + pos[1] + " == 0 OR 1");
		return old;
	}
	
	//rebuild
	@Override
	public CharSequence2D rebuild0() {
		IndexMultiMap<CharSequence2D> charTable = new IndexMultiMap2D<>();
		for (IndexMap.Entry<Entry> entry : table.entrySet()) {
			if (entry == null)
				continue;
			
			int index = entry.getIndex();
			Entry value = entry.getValue();
			if (value.key == null || value.value == null)
				throw new NullPointerException("Entry contained null: key: " + value.key + ", value: " + value.value);
			
			if (align) {
				charTable.put(new int[] {index, 0}, value.key.buildSequence2D());
				charTable.put(new int[] {index, 1}, separator);
				charTable.put(new int[] {index, 2}, value.value.buildSequence2D());
			} else {
				charTable.put(new int[] {index, 0}, new StringBuilder2D().append(value.key.buildSequence2D()).append(separator));
				charTable.put(new int[] {index, 1}, value.value.buildSequence2D());
			}
		}
		return style.makeTable(name, this, charTable);
	}
	
	public static class Entry {
		
		public MonofontGuiElement key;
		public MonofontGuiElement value;
		
		public Entry() {
		}
		
		public Entry(MonofontGuiElement key, MonofontGuiElement value) {
			this.key = key;
			this.value = value;
		}
		
		public boolean isEmpty() {
			return key == null && value == null;
		}
	}
}
