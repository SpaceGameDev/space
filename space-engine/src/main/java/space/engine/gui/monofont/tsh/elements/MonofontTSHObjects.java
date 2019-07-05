package space.engine.gui.monofont.tsh.elements;

import org.jetbrains.annotations.NotNull;
import space.engine.gui.GuiCreator;
import space.engine.gui.GuiElement;
import space.engine.gui.monofont.MonofontGuiElement;
import space.engine.gui.monofont.MonofontGuiElementCaching;
import space.engine.gui.monofont.tsh.objectsCreator.MonofontTSHObjectsCreator;
import space.engine.gui.monofont.tsh.objectsCreator.MonofontTSHObjectsCreatorImpl;
import space.engine.gui.tsh.elements.GuiToStringHelperObjectsCreator;
import space.engine.gui.tsh.elements.GuiToStringHelperObjectsCreator.ToStringHelperObjects;
import space.engine.string.CharSequence2D;
import space.engine.string.String2D;
import space.engine.string.toStringHelper.AbstractToStringHelperObjectsInstance;
import space.engine.string.toStringHelper.ToStringHelper;

public class MonofontTSHObjects extends MonofontGuiElementCaching implements ToStringHelperObjects {
	
	public static final GuiToStringHelperObjectsCreator CREATOR = MonofontTSHObjects::new;
	
	public Object obj;
	public ToStringHelper<? extends GuiElement> helper;
	public AbstractToStringHelperObjectsInstance<MonofontGuiElement> tshObjectsInstance;
	public MonofontTSHObjectsCreator style = MonofontTSHObjectsCreatorImpl.DEFAULT;
	
	public MonofontTSHObjects(Object obj, ToStringHelper<? extends GuiElement> helper) {
		this.obj = obj;
		this.helper = helper;
		
		//noinspection unchecked
		tshObjectsInstance = new AbstractToStringHelperObjectsInstance<>(obj, (ToStringHelper<MonofontGuiElement>) helper) {
			@Override
			public MonofontGuiElement build() {
				return MonofontTSHObjects.this;
			}
		};
	}
	
	@Override
	public GuiCreator getCreator() {
		return CREATOR;
	}
	
	@Override
	public CharSequence2D rebuild0() {
		return style.makeTable(tshObjectsInstance);
	}
	
	//delegate
	@Override
	public MonofontGuiElement build() {
		return this;
	}
	
	@Override
	public void add(@NotNull String name, byte obj) {
		tshObjectsInstance.add(name, obj);
	}
	
	@Override
	public void add(@NotNull String name, short obj) {
		tshObjectsInstance.add(name, obj);
	}
	
	@Override
	public void add(@NotNull String name, int obj) {
		tshObjectsInstance.add(name, obj);
	}
	
	@Override
	public void add(@NotNull String name, long obj) {
		tshObjectsInstance.add(name, obj);
	}
	
	@Override
	public void add(@NotNull String name, float obj) {
		tshObjectsInstance.add(name, obj);
	}
	
	@Override
	public void add(@NotNull String name, double obj) {
		tshObjectsInstance.add(name, obj);
	}
	
	@Override
	public void add(@NotNull String name, boolean obj) {
		tshObjectsInstance.add(name, obj);
	}
	
	@Override
	public void add(@NotNull String name, char obj) {
		tshObjectsInstance.add(name, obj);
	}
	
	@Override
	public void add(@NotNull String name, byte[] obj, int from, int to) {
		tshObjectsInstance.add(name, obj, from, to);
	}
	
	@Override
	public void add(@NotNull String name, short[] obj, int from, int to) {
		tshObjectsInstance.add(name, obj, from, to);
	}
	
	@Override
	public void add(@NotNull String name, int[] obj, int from, int to) {
		tshObjectsInstance.add(name, obj, from, to);
	}
	
	@Override
	public void add(@NotNull String name, long[] obj, int from, int to) {
		tshObjectsInstance.add(name, obj, from, to);
	}
	
	@Override
	public void add(@NotNull String name, float[] obj, int from, int to) {
		tshObjectsInstance.add(name, obj, from, to);
	}
	
	@Override
	public void add(@NotNull String name, double[] obj, int from, int to) {
		tshObjectsInstance.add(name, obj, from, to);
	}
	
	@Override
	public void add(@NotNull String name, boolean[] obj, int from, int to) {
		tshObjectsInstance.add(name, obj, from, to);
	}
	
	@Override
	public void add(@NotNull String name, char[] obj, int from, int to) {
		tshObjectsInstance.add(name, obj, from, to);
	}
	
	@Override
	public void add(@NotNull String name, Object obj) {
		tshObjectsInstance.add(name, obj);
	}
	
	@Override
	public void add(@NotNull String name, Object[] obj, int from, int to) {
		tshObjectsInstance.add(name, obj, from, to);
	}
	
	@Override
	public void addNull(@NotNull String name) {
		tshObjectsInstance.addNull(name);
	}
	
	@Override
	public void add(@NotNull String name, byte[] obj) {
		tshObjectsInstance.add(name, obj);
	}
	
	@Override
	public void add(@NotNull String name, short[] obj) {
		tshObjectsInstance.add(name, obj);
	}
	
	@Override
	public void add(@NotNull String name, int[] obj) {
		tshObjectsInstance.add(name, obj);
	}
	
	@Override
	public void add(@NotNull String name, long[] obj) {
		tshObjectsInstance.add(name, obj);
	}
	
	@Override
	public void add(@NotNull String name, float[] obj) {
		tshObjectsInstance.add(name, obj);
	}
	
	@Override
	public void add(@NotNull String name, double[] obj) {
		tshObjectsInstance.add(name, obj);
	}
	
	@Override
	public void add(@NotNull String name, boolean[] obj) {
		tshObjectsInstance.add(name, obj);
	}
	
	@Override
	public void add(@NotNull String name, char[] obj) {
		tshObjectsInstance.add(name, obj);
	}
	
	@Override
	public void add(@NotNull String name, Object[] obj) {
		tshObjectsInstance.add(name, obj);
	}
	
	@Override
	public void add(@NotNull String name, String obj) {
		tshObjectsInstance.add(name, obj);
	}
	
	@Override
	public void add(@NotNull String name, CharSequence obj) {
		tshObjectsInstance.add(name, obj);
	}
	
	@Override
	public void add(@NotNull String name, String2D obj) {
		tshObjectsInstance.add(name, obj);
	}
	
	@Override
	public void add(@NotNull String name, CharSequence2D obj) {
		tshObjectsInstance.add(name, obj);
	}
}
