package space.engine.gui.monofont.tsh.objectsCreator;

import space.engine.gui.monofont.MonofontGuiElement;
import space.engine.string.CharSequence2D;
import space.engine.string.toStringHelper.AbstractToStringHelperObjectsInstance;

public interface MonofontTSHObjectsCreator {
	
	CharSequence2D makeTable(AbstractToStringHelperObjectsInstance<MonofontGuiElement> tsh);
}
