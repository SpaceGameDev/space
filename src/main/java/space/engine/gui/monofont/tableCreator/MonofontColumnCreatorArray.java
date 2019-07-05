package space.engine.gui.monofont.tableCreator;

import space.engine.gui.monofont.MonofontGuiElement;
import space.engine.indexmap.IndexMap;
import space.engine.indexmap.IndexMap.Entry;
import space.engine.indexmap.axis.IndexAxisMapInt;
import space.engine.math.MathUtils;
import space.engine.string.CharSequence2D;
import space.engine.string.StringBuilder2D;

import java.util.Collection;

/**
 * makes a table with full borders out of single characters
 */
public class MonofontColumnCreatorArray implements MonofontColumnCreator {
	
	@SuppressWarnings("unused")
	public static MonofontColumnCreatorArray INSTANCE = new MonofontColumnCreatorArray('[', ']', "|", '-');
	
	public char fillChar;
	public char leftBound;
	public char rightBound;
	public String separator;
	public char line;
	
	public MonofontColumnCreatorArray(char leftBound, char rightBound, String separator, char line) {
		this(' ', leftBound, rightBound, separator, line);
	}
	
	public MonofontColumnCreatorArray(char fillChar, char leftBound, char rightBound, String separator, char line) {
		this.fillChar = fillChar;
		this.leftBound = leftBound;
		this.rightBound = rightBound;
		this.separator = separator;
		this.line = line;
	}
	
	@Override
	public CharSequence2D makeTable(String className, MonofontGuiElement guiElement, ColumnDirection direction, IndexMap<CharSequence2D> elements) {
		//axis size
		IndexAxisMapInt axis = new IndexAxisMapInt();
		Collection<Entry<CharSequence2D>> iter = elements.entrySet();
		for (Entry<CharSequence2D> elem : iter) {
			int index = elem.getIndex();
			CharSequence2D value = elem.getValue();
			axis.put(new int[] {0, index}, new int[] {value.height(), value.maxLength()});
		}
		
		//sizes
		int separatorLength = separator.length();
		int maxY = axis.getIndex(0, 1) + 1;
		int maxIndexX = axis.size(1);
		int maxXEntry = axis.getIndex(1, maxIndexX) + maxIndexX * separatorLength;
		int maxX = MathUtils.max(maxXEntry, className.length() + 1);
		
		//buffer creation
		StringBuilder2D buffer = new StringBuilder2D().setNoFillMissingSpaces();
		buffer.startEdit();
		
		//first line
		buffer.setY(0).setX(0).append(leftBound).append(className).fill(maxX - className.length(), line).append(rightBound);
		
		//entries
		iter = elements.entrySet();
		for (Entry<CharSequence2D> elem : iter) {
			int index = elem.getIndex();
			int startx = axis.getIndex(1, index) + index * separatorLength + 1;
			int untilx = axis.getIndex(1, index + 1) + (index + 1) * separatorLength;
			
			buffer.setY(1).setX(startx).append(elem.getValue(), maxY, untilx, fillChar);
		}
		
		//after entries
		if (maxXEntry < maxX)
			buffer.setY(1).setX(maxXEntry).fill(maxX - maxXEntry + 1, fillChar);
		
		//sides
		fillDown(buffer, 0, 0, maxY, leftBound);
		fillDown(buffer, maxX, 0, maxY, rightBound);
		
		//separator
		for (int i = 0; i < maxIndexX - 1; i++)
			fillDown(buffer, axis.getIndex(1, i + 1) + i * separatorLength + 1, 1, maxY, separator);
		
		//last line
		buffer.setY(maxY).setX(0).append(leftBound).fill(maxX, line).append(rightBound);
		buffer.endEdit();
		return buffer.toString2D();
	}
	
	//fillDown
	@SuppressWarnings("SameParameterValue")
	protected static void fillDown(StringBuilder2D buffer, int x, int fromy, int toy, char c) {
		for (int y = fromy; y < toy; y++) {
			buffer.setY(y);
			buffer.setX(x);
			buffer.append(c);
		}
	}
	
	@SuppressWarnings("SameParameterValue")
	protected static void fillDown(StringBuilder2D buffer, int x, int fromy, int toy, String c) {
		for (int y = fromy; y < toy; y++) {
			buffer.setY(y);
			buffer.setX(x);
			buffer.append(c);
		}
	}
}
