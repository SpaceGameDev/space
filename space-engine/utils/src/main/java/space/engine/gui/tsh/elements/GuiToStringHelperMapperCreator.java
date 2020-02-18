package space.engine.gui.tsh.elements;

import space.engine.gui.GuiCreator;
import space.engine.gui.GuiElement;
import space.engine.string.CharSequence2D;
import space.engine.string.toStringHelper.ToStringHelper.ToStringHelperTable;

@FunctionalInterface
public interface GuiToStringHelperMapperCreator extends GuiCreator {
	
	GuiToStringHelperMapper create(String name, CharSequence2D separator, boolean align);
	
	interface GuiToStringHelperMapper extends GuiElement, ToStringHelperTable<GuiElement> {
		
		@Override
		GuiElement put(int[] pos, Object object);
		
		@Override
		default GuiElement build() {
			return this;
		}
	}
}
