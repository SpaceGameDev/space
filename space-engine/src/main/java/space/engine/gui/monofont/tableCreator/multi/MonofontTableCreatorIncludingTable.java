package space.engine.gui.monofont.tableCreator.multi;

import space.engine.gui.monofont.MonofontGuiElement;
import space.engine.gui.monofont.MonofontIncluding;
import space.engine.indexmap.axis.IndexAxisMapInt;
import space.engine.indexmap.multi.IndexMultiMap;
import space.engine.indexmap.multi.IndexMultiMap.IndexMultiMapEntry;
import space.engine.string.CharSequence2D;
import space.engine.string.String2D;
import space.engine.string.StringBuilder2D;

import static space.engine.ArrayUtils.getSafeO;

/**
 * makes a table out of characters like {@link MonofontTableCreatorSingleCharTable}, but attaches it to the parent table if the parent is a table
 */
public class MonofontTableCreatorIncludingTable extends MonofontTableCreatorSingleCharTable {
	
	@SuppressWarnings("unused")
	public static MonofontTableCreator DEFAULT = new MonofontTableCreatorIncludingTable('|', '-', '+');
	
	public boolean allowIncluding = true;
	
	public MonofontTableCreatorIncludingTable(char lineUpDown, char lineLeftRight, char cross) {
		super(lineUpDown, lineLeftRight, cross);
	}
	
	public MonofontTableCreatorIncludingTable(char fillChar, char lineUpDown, char lineLeftRight, char cross) {
		super(fillChar, lineUpDown, lineLeftRight, cross);
	}
	
	public void setAllowIncluding(boolean allowIncluding) {
		this.allowIncluding = allowIncluding;
	}
	
	@Override
	public CharSequence2D makeTable(String name, MonofontGuiElement guiElement, IndexMultiMap<CharSequence2D> valueTable) {
		return (allowIncluding && guiElement.parent != null && MonofontIncluding.toIncludeList.contains(guiElement.parent.getClass())) ? makeTableNoBorder(valueTable) : super.makeTable(name, guiElement, valueTable);
	}
	
	public CharSequence2D makeTableNoBorder(IndexMultiMap<CharSequence2D> valueTable) {
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
			buffer.setY(axis.getIndex(0, indexy) + indexy);
			buffer.setX(axis.getIndex(1, indexx) + indexx);
			int untily = axis.getIndex(0, indexy + 1) + indexy;
			int untilx = axis.getIndex(1, indexx + 1) + indexx;
			buffer.append(elem.getValue() == null ? String2D.EMPTY : elem.getValue(), untily, untilx, fillChar);
		}
		
		//filling left right
		for (int indexy = 1; indexy < size[0]; indexy++) {
			buffer.setY(axis.getIndex(0, indexy) + indexy - 1);
			buffer.setX(0);
			buffer.fill(axis.getIndex(1, size[1]) + size[1], lineLeftRight);
		}
		
		//filling up down
		for (int indexy = 0; indexy < axis.getIndex(0, size[0]) + size[0] - 1; indexy++) {
			buffer.setY(indexy);
			for (int indexx = 1; indexx < size[1]; indexx++) {
				buffer.setX(axis.getIndex(1, indexx) + indexx - 1);
				buffer.append(lineUpDown);
			}
		}
		
		//filling cross
		for (int indexy = 1; indexy < size[0]; indexy++) {
			buffer.setY(axis.getIndex(0, indexy) + indexy - 1);
			for (int indexx = 1; indexx < size[1]; indexx++) {
				buffer.setX(axis.getIndex(1, indexx) + indexx - 1);
				buffer.append(cross);
			}
		}
		
		buffer.endEdit();
		return new String2D(buffer);
	}
}
