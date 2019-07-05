package space.engine.gui;

import space.engine.delegate.map.specific.GetClassOrSuperMap;
import space.engine.gui.exception.GuiElementUnsupportedException;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractGuiApi implements GuiApi {
	
	public Map<Class<? extends GuiCreator>, GuiCreator> map = new GetClassOrSuperMap<>(new HashMap<>());
	
	//add
	public void addElement(GuiCreator con, Class<? extends GuiCreator> clazz) {
		map.put(clazz, con);
	}
	
	@SafeVarargs
	public final void addElements(GuiCreator con, Class<? extends GuiCreator>... clazz) {
		for (Class<? extends GuiCreator> e : clazz)
			map.put(e, con);
	}
	
	//get
	@Override
	public <CREATOR extends GuiCreator> CREATOR get(Class<CREATOR> type) {
		GuiCreator creator = map.get(type);
		if (creator == null)
			throw new GuiElementUnsupportedException("Gui Element unsupported: " + type.getName());
		//noinspection unchecked
		return (CREATOR) creator;
	}
	
	//exists
	@Override
	public <CREATOR extends GuiCreator> boolean isSupported(Class<CREATOR> type) {
		return map.containsKey(type);
	}
}
