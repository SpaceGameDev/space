package space.engine.gui.monofont.elements.text;

import space.engine.gui.elements.text.GuiText1DCreator;
import space.engine.gui.elements.text.GuiText1DCreator.GuiText1D;
import space.engine.gui.monofont.MonofontGuiElement;
import space.engine.string.CharSequence2D;
import space.engine.string.String2D;

public class MonofontText1D extends MonofontGuiElement implements GuiText1D {
	
	public static final GuiText1DCreator CREATOR = MonofontText1D::new;
	
	public CharSequence squ;
	
	public MonofontText1D(MonofontText1D text) {
		this(text.squ);
	}
	
	public MonofontText1D(CharSequence squ) {
		this.squ = squ;
	}
	
	@Override
	public GuiText1DCreator getCreator() {
		return CREATOR;
	}
	
	@Override
	public int sizeX() {
		return squ.length();
	}
	
	@Override
	public int sizeY() {
		return 1;
	}
	
	@Override
	public CharSequence2D buildSequence2D() {
		return new String2D(squ);
	}
}
