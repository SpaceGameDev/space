package space.engine.gui.monofont.tableCreator.multi;

import space.engine.gui.monofont.MonofontGuiElement;
import space.engine.indexmap.axis.IndexAxisMapInt;
import space.engine.indexmap.multi.IndexMultiMap;
import space.engine.indexmap.multi.IndexMultiMap.IndexMultiMapEntry;
import space.engine.string.CharSequence2D;
import space.engine.string.String2D;
import space.engine.string.StringBuilder2D;

import static space.engine.ArrayUtils.getSafeO;

/**
 * makes a table with no borders
 */
public class MonofontTableCreatorNoBorder implements MonofontTableCreator {
	
	@SuppressWarnings("unused")
	public static MonofontTableCreatorNoBorder DEFAULT = new MonofontTableCreatorNoBorder();
	
	public char fillChar;
	
	public MonofontTableCreatorNoBorder() {
		this(' ');
	}
	
	public MonofontTableCreatorNoBorder(char fillChar) {
		this.fillChar = fillChar;
	}
	
	@Override
	public CharSequence2D makeTable(String name, MonofontGuiElement guiElement, IndexMultiMap<CharSequence2D> valueTable) {
		//axis size
		int[] size = new int[] {valueTable.size(), valueTable.maxSize(1)};
		IndexAxisMapInt axis = new IndexAxisMapInt(); //content index
		for (IndexMultiMapEntry<CharSequence2D> elem : valueTable.table())
			if (elem.getValue() != null)
				axis.put(new int[] {getSafeO(elem.getIndex(), 0, 0), getSafeO(elem.getIndex(), 1, 0)}, new int[] {elem.getValue().height(), elem.getValue().maxLength()});
		
		//buffer building content
		StringBuilder2D buffer = new StringBuilder2D().setNoFillMissingSpaces();
		buffer.startEdit();
		for (IndexMultiMapEntry<CharSequence2D> elem : valueTable.table()) {
			int indexy = getSafeO(elem.getIndex(), 0, 0);
			int indexx = getSafeO(elem.getIndex(), 1, 0);
			buffer.setY(axis.getIndex(0, indexy));
			buffer.setX(axis.getIndex(1, indexx));
			int untily = axis.getIndex(0, indexy + 1);
			int untilx = axis.getIndex(1, indexx + 1);
			buffer.append(elem.getValue() == null ? String2D.EMPTY : elem.getValue(), untily, untilx, fillChar);
		}
		
		buffer.endEdit();
		return new String2D(buffer);
	}
}
