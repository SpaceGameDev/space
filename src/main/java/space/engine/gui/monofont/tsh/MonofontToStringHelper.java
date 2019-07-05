package space.engine.gui.monofont.tsh;

import org.jetbrains.annotations.NotNull;
import space.engine.gui.GuiApi;
import space.engine.gui.elements.text.GuiText1DCreator;
import space.engine.gui.elements.text.GuiText2DCreator;
import space.engine.gui.monofont.MonofontGuiElement;
import space.engine.gui.tsh.elements.GuiToStringHelperArrayCreator;
import space.engine.gui.tsh.elements.GuiToStringHelperArrayCreator.ToStringHelperArray;
import space.engine.gui.tsh.elements.GuiToStringHelperMapperCreator;
import space.engine.gui.tsh.elements.GuiToStringHelperModifierCreator;
import space.engine.gui.tsh.elements.GuiToStringHelperObjectsCreator;
import space.engine.gui.tsh.elements.GuiToStringHelperTableCreator;
import space.engine.string.CharSequence2D;
import space.engine.string.String2D;
import space.engine.string.toStringHelper.ToStringHelper;

public class MonofontToStringHelper implements ToStringHelper<MonofontGuiElement> {
	
	public GuiApi api;
	
	public MonofontToStringHelper(GuiApi api) {
		this.api = api;
	}
	
	//native
	@NotNull
	@Override
	public MonofontGuiElement toString(byte b) {
		return (MonofontGuiElement) api.get(GuiText1DCreator.class).create(Byte.toString(b));
	}
	
	@NotNull
	@Override
	public MonofontGuiElement toString(short s) {
		return (MonofontGuiElement) api.get(GuiText1DCreator.class).create(Short.toString(s));
	}
	
	@NotNull
	@Override
	public MonofontGuiElement toString(int i) {
		return (MonofontGuiElement) api.get(GuiText1DCreator.class).create(Integer.toString(i));
	}
	
	@NotNull
	@Override
	public MonofontGuiElement toString(long l) {
		return (MonofontGuiElement) api.get(GuiText1DCreator.class).create(Long.toString(l));
	}
	
	@NotNull
	@Override
	public MonofontGuiElement toString(float f) {
		return (MonofontGuiElement) api.get(GuiText1DCreator.class).create(Float.toString(f));
	}
	
	@NotNull
	@Override
	public MonofontGuiElement toString(double d) {
		return (MonofontGuiElement) api.get(GuiText1DCreator.class).create(Double.toString(d));
	}
	
	@NotNull
	@Override
	public MonofontGuiElement toString(boolean b) {
		return (MonofontGuiElement) api.get(GuiText1DCreator.class).create(Boolean.toString(b));
	}
	
	@NotNull
	@Override
	public MonofontGuiElement toString(char c) {
		return (MonofontGuiElement) api.get(GuiText1DCreator.class).create(Character.toString(c));
	}
	
	//native array
	@NotNull
	@Override
	public MonofontGuiElement toString(byte[] obj, int from, int to) {
		if (obj == null)
			return toStringNull();
		ToStringHelperArray array = api.get(GuiToStringHelperArrayCreator.class).create(byte.class);
		for (int i = from; i < to; i++)
			array.put(i, toString(obj[i]));
		return (MonofontGuiElement) array;
	}
	
	@NotNull
	@Override
	public MonofontGuiElement toString(short[] obj, int from, int to) {
		if (obj == null)
			return toStringNull();
		ToStringHelperArray array = api.get(GuiToStringHelperArrayCreator.class).create(short.class);
		for (int i = from; i < to; i++)
			array.put(i, toString(obj[i]));
		return (MonofontGuiElement) array;
	}
	
	@NotNull
	@Override
	public MonofontGuiElement toString(int[] obj, int from, int to) {
		if (obj == null)
			return toStringNull();
		ToStringHelperArray array = api.get(GuiToStringHelperArrayCreator.class).create(int.class);
		for (int i = from; i < to; i++)
			array.put(i, toString(obj[i]));
		return (MonofontGuiElement) array;
	}
	
	@NotNull
	@Override
	public MonofontGuiElement toString(long[] obj, int from, int to) {
		if (obj == null)
			return toStringNull();
		ToStringHelperArray array = api.get(GuiToStringHelperArrayCreator.class).create(long.class);
		for (int i = from; i < to; i++)
			array.put(i, toString(obj[i]));
		return (MonofontGuiElement) array;
	}
	
	@NotNull
	@Override
	public MonofontGuiElement toString(float[] obj, int from, int to) {
		if (obj == null)
			return toStringNull();
		ToStringHelperArray array = api.get(GuiToStringHelperArrayCreator.class).create(float.class);
		for (int i = from; i < to; i++)
			array.put(i, toString(obj[i]));
		return (MonofontGuiElement) array;
	}
	
	@NotNull
	@Override
	public MonofontGuiElement toString(double[] obj, int from, int to) {
		if (obj == null)
			return toStringNull();
		ToStringHelperArray array = api.get(GuiToStringHelperArrayCreator.class).create(double.class);
		for (int i = from; i < to; i++)
			array.put(i, toString(obj[i]));
		return (MonofontGuiElement) array;
	}
	
	@NotNull
	@Override
	public MonofontGuiElement toString(boolean[] obj, int from, int to) {
		if (obj == null)
			return toStringNull();
		ToStringHelperArray array = api.get(GuiToStringHelperArrayCreator.class).create(boolean.class);
		for (int i = from; i < to; i++)
			array.put(i, toString(obj[i]));
		return (MonofontGuiElement) array;
	}
	
	@NotNull
	@Override
	public MonofontGuiElement toString(char[] obj, int from, int to) {
		if (obj == null)
			return toStringNull();
		ToStringHelperArray array = api.get(GuiToStringHelperArrayCreator.class).create(char.class);
		for (int i = from; i < to; i++)
			array.put(i, toString(obj[i]));
		return (MonofontGuiElement) array;
	}
	
	//array object
	@NotNull
	@Override
	public MonofontGuiElement toString(Object[] obj, int from, int to) {
		if (obj == null)
			return toStringNull();
		ToStringHelperArray array = api.get(GuiToStringHelperArrayCreator.class).create(obj.getClass().getComponentType());
		for (int i = from; i < to; i++)
			array.put(i, toString(obj[i]));
		return (MonofontGuiElement) array;
	}
	
	//string
	@NotNull
	@Override
	public MonofontGuiElement toString(CharSequence str) {
		return (MonofontGuiElement) api.get(GuiText1DCreator.class).create(str.toString());
	}
	
	@NotNull
	@Override
	public MonofontGuiElement toString(String str) {
		return (MonofontGuiElement) api.get(GuiText1DCreator.class).create(str);
	}
	
	@NotNull
	@Override
	public MonofontGuiElement toString(CharSequence2D str) {
		return (MonofontGuiElement) api.get(GuiText2DCreator.class).create(str);
	}
	
	@NotNull
	@Override
	public MonofontGuiElement toString(String2D str) {
		return (MonofontGuiElement) api.get(GuiText2DCreator.class).create(str);
	}
	
	//null
	@NotNull
	@Override
	public MonofontGuiElement toStringNull() {
		return (MonofontGuiElement) api.get(GuiText1DCreator.class).create("null");
	}
	
	//modifier
	@NotNull
	@Override
	public MonofontGuiElement createModifier(@NotNull String modifier, Object value) {
		return (MonofontGuiElement) api.get(GuiToStringHelperModifierCreator.class).create(modifier, toString(value));
	}
	
	//object
	@NotNull
	@Override
	@SuppressWarnings("unchecked")
	public ToStringHelperObjectsInstance<MonofontGuiElement> createObjectInstance(@NotNull Object obj) {
		return (ToStringHelperObjectsInstance<MonofontGuiElement>) (Object) api.get(GuiToStringHelperObjectsCreator.class).create(obj, this);
	}
	
	//mapper
	@NotNull
	@Override
	@SuppressWarnings("unchecked")
	public ToStringHelperTable<MonofontGuiElement> createMapper(@NotNull String name, @NotNull String separator, boolean align) {
		return (ToStringHelperTable<MonofontGuiElement>) (Object) api.get(GuiToStringHelperMapperCreator.class).create(name, new String2D(separator), align);
	}
	
	//table
	@NotNull
	@Override
	@SuppressWarnings("unchecked")
	public ToStringHelperTable<MonofontGuiElement> createTable(@NotNull String name, int dimensions) {
		return (ToStringHelperTable<MonofontGuiElement>) (Object) api.get(GuiToStringHelperTableCreator.class).create(name, dimensions);
	}
}
