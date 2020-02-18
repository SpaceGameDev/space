package space.engine.gui;

public interface GuiApi {
	
	<CREATOR extends GuiCreator> CREATOR get(Class<CREATOR> type);
	
	<CREATOR extends GuiCreator> boolean isSupported(Class<CREATOR> type);
	
	Class<?> getBaseElementClass();
}
