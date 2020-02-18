package space.engine.gui.elements.direction;

@FunctionalInterface
public interface GuiColumnCreator extends GuiDirectionalCreator {
	
	@Override
	GuiColumn create();
	
	interface GuiColumn extends GuiDirectional {
	
	}
}
