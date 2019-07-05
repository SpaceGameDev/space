package space.engine.gui.monofont.elements.direction;

import org.jetbrains.annotations.Nullable;
import space.engine.delegate.indexmap.ModificationAwareIndexMap;
import space.engine.gui.GuiElement;
import space.engine.gui.elements.direction.GuiDirectionalCreator.GuiDirectional;
import space.engine.gui.exception.IllegalGuiElementException;
import space.engine.gui.monofont.MonofontGuiElement;
import space.engine.gui.monofont.MonofontGuiElementCaching;
import space.engine.indexmap.IndexMap;
import space.engine.indexmap.IndexMap.Entry;
import space.engine.indexmap.IndexMapArray;
import space.engine.string.CharSequence2D;

public abstract class MonofontElementList extends MonofontGuiElementCaching implements GuiDirectional {
	
	public IndexMap<MonofontGuiElement> list = new ModificationAwareIndexMap<>(new IndexMapArray<>(), this::modification);
	
	public MonofontElementList() {
	}
	
	@Override
	@Nullable
	public GuiElement put(int index, @Nullable GuiElement gui) {
		if (!(gui instanceof MonofontGuiElement))
			throw new IllegalGuiElementException(gui);
		MonofontGuiElement v = (MonofontGuiElement) gui;
		v.setParent(this);
		MonofontGuiElement old = list.put(index, v);
		if (old != null)
			old.setParent(null);
		return old;
	}
	
	@Override
	@Nullable
	public GuiElement remove(int index) {
		MonofontGuiElement old = list.remove(index);
		if (old != null)
			old.setParent(null);
		return old;
	}
	
	public IndexMap<CharSequence2D> buildList() {
		IndexMap<@Nullable CharSequence2D> charTable = new IndexMapArray<>();
		for (Entry<MonofontGuiElement> entry : list.entrySet()) {
			MonofontGuiElement value = entry.getValue();
			charTable.put(entry.getIndex(), value != null ? value.buildSequence2D() : null);
		}
		return charTable;
	}
}
