package space.engine.gui.tsh.elements;

import space.engine.gui.GuiCreator;
import space.engine.gui.GuiElement;
import space.engine.string.toStringHelper.ToStringHelper.ToStringHelperTable;

@FunctionalInterface
public interface GuiToStringHelperTableCreator<T> extends GuiCreator {
	
	GuiToStringHelperTable create(String name, int dimensions);
	
	interface GuiToStringHelperTable extends GuiElement, ToStringHelperTable<GuiElement> {
		
		@Override
		GuiElement put(int[] pos, Object object);
		
		@Override
		default GuiElement build() {
			return this;
		}
	}
}
