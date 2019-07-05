package space.engine.gui.monofont.tsh.objectsCreator;

import space.engine.delegate.iterator.Iteratorable;
import space.engine.gui.monofont.MonofontGuiElement;
import space.engine.indexmap.axis.IndexAxisMapInt;
import space.engine.math.MathUtils;
import space.engine.string.CharSequence2D;
import space.engine.string.String2D;
import space.engine.string.StringBuilder2D;
import space.engine.string.toStringHelper.AbstractToStringHelperObjectsInstance;
import space.engine.string.toStringHelper.AbstractToStringHelperObjectsInstance.Entry;

import java.util.ListIterator;

/**
 * makes a table with full borders out of single characters
 */
public class MonofontTSHObjectsCreatorImpl implements MonofontTSHObjectsCreator {
	
	public static final MonofontTSHObjectsCreator DEFAULT = new MonofontTSHObjectsCreatorImpl('|', '-', '+', ": ");
	
	public char fillChar;
	public char lineUpDown;
	public char lineLeftRight;
	public char cross;
	public String separator;
	
	public MonofontTSHObjectsCreatorImpl(char lineUpDown, char lineLeftRight, char cross, String separator) {
		this(' ', lineUpDown, lineLeftRight, cross, separator);
	}
	
	public MonofontTSHObjectsCreatorImpl(char fillChar, char lineUpDown, char lineLeftRight, char cross, String separator) {
		this.fillChar = fillChar;
		this.lineUpDown = lineUpDown;
		this.lineLeftRight = lineLeftRight;
		this.cross = cross;
		this.separator = separator;
	}
	
	public CharSequence2D makeTable(AbstractToStringHelperObjectsInstance<MonofontGuiElement> monofontObjects) {
		String className = monofontObjects.object.getClass().getName();
		
		//axis size
		IndexAxisMapInt axis = new IndexAxisMapInt();
		ListIterator<Entry<MonofontGuiElement>> iter = monofontObjects.list.listIterator();
		for (Entry<MonofontGuiElement> elem : Iteratorable.toIteratorable(iter)) {
			int index = iter.previousIndex();
			axis.put(new int[] {index, 0}, new int[] {1, elem.name.length()});
			axis.put(new int[] {index, 1}, new int[] {elem.value.sizeY(), elem.value.sizeX()});
		}
		
		//sizes
		int separatorLength = separator.length();
		int valueStart = axis.getIndex(1, 1) + separatorLength + 1;
		int valueEnd = MathUtils.max(axis.getIndex(1, 2) + separatorLength + 1, className.length() + 1);
		
		//buffer creation
		StringBuilder2D buffer = new StringBuilder2D().setNoFillMissingSpaces();
		buffer.startEdit();
		
		//class name
		buffer.setY(0).setX(0).append(cross).append(className).fill(valueEnd - className.length(), lineLeftRight).append(cross);
		
		//entries
		int index = 0;
		iter = monofontObjects.list.listIterator();
		for (Entry<MonofontGuiElement> elem : Iteratorable.toIteratorable(iter)) {
			index = iter.previousIndex();
			int starty = axis.getIndex(0, index) + 1;
			int untily = axis.getIndex(0, index + 1) + 1;
			
			//lineLeft
			buffer.setY(starty).setX(0).append(lineUpDown);
			
			//name
			buffer.append(new String2D(elem.name + separator), untily, valueStart, fillChar);
			
			//value
			buffer.setY(starty).setX(valueStart).append(elem.value.buildSequence2D(), untily, valueEnd, fillChar);
			
			//lineRight
			buffer.setY(starty).setX(valueEnd).append(lineUpDown);
		}
		
		//sides
		int lasty = axis.getIndex(0, index + 1) + 1;
		fillDown(buffer, 0, 1, lasty, lineUpDown);
		fillDown(buffer, valueEnd, 1, lasty, lineUpDown);
		
		//lastLine
		buffer.setY(lasty).setX(0).append(cross).fill(valueEnd, lineLeftRight).append(cross);
		
		buffer.endEdit();
		return buffer.toString2D();
	}
	
	@SuppressWarnings("SameParameterValue")
	protected static void fillDown(StringBuilder2D buffer, int x, int fromy, int toy, char c) {
		for (int y = fromy; y < toy; y++) {
			buffer.setY(y);
			buffer.setX(x);
			buffer.append(c);
		}
	}
}
