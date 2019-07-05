package space.engine.gui.monofont.tsh.elements;

import space.engine.gui.GuiCreator;
import space.engine.gui.GuiElement;
import space.engine.gui.exception.IllegalGuiElementException;
import space.engine.gui.monofont.MonofontGuiElement;
import space.engine.gui.tsh.elements.GuiToStringHelperModifierCreator;
import space.engine.gui.tsh.elements.GuiToStringHelperModifierCreator.GuiToStringHelperModifier;
import space.engine.string.CharSequence2D;
import space.engine.string.StringBuilder2D;

import static space.engine.math.MathUtils.max;

public class MonofontTSHModifier extends MonofontGuiElement implements GuiToStringHelperModifier {
	
	public static final GuiToStringHelperModifierCreator CREATOR = MonofontTSHModifier::new;
	
	public String modifier;
	public MonofontGuiElement value;
	
	public MonofontTSHModifier(String modifier, String separator, GuiElement value) {
		this(modifier + separator, value);
	}
	
	public MonofontTSHModifier(String modifier, GuiElement value) {
		if (!(value instanceof MonofontGuiElement))
			throw new IllegalGuiElementException(value);
		
		this.modifier = modifier;
		this.value = (MonofontGuiElement) value;
	}
	
	@Override
	public int sizeX() {
		return modifier.length() + value.sizeX();
	}
	
	@Override
	public int sizeY() {
		return max(1, value.sizeY());
	}
	
	@Override
	public CharSequence2D buildSequence2D() {
		StringBuilder2D b = new StringBuilder2D();
		b.append(modifier).append(value.buildSequence2D());
		return b;
	}
	
	@Override
	public GuiCreator getCreator() {
		return CREATOR;
	}
}
