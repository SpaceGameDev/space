package space.engine.gui.monofont;

import space.engine.gui.AbstractGuiApi;
import space.engine.gui.elements.direction.GuiColumnCreator;
import space.engine.gui.elements.direction.GuiDirectionalCreator;
import space.engine.gui.elements.direction.GuiRowCreator;
import space.engine.gui.elements.direction.GuiTableCreator;
import space.engine.gui.elements.text.GuiText1DCreator;
import space.engine.gui.elements.text.GuiText2DCreator;
import space.engine.gui.monofont.elements.direction.MonofontColumn;
import space.engine.gui.monofont.elements.direction.MonofontDirectional;
import space.engine.gui.monofont.elements.direction.MonofontRow;
import space.engine.gui.monofont.elements.direction.MonofontTable;
import space.engine.gui.monofont.elements.text.MonofontText1D;
import space.engine.gui.monofont.elements.text.MonofontText2D;
import space.engine.gui.monofont.tsh.MonofontToStringHelper;
import space.engine.gui.monofont.tsh.elements.MonofontTSHArray;
import space.engine.gui.monofont.tsh.elements.MonofontTSHMapper;
import space.engine.gui.monofont.tsh.elements.MonofontTSHModifier;
import space.engine.gui.monofont.tsh.elements.MonofontTSHObjects;
import space.engine.gui.monofont.tsh.elements.MonofontTSHTable;
import space.engine.gui.tsh.GuiToStringHelperApiCreator;
import space.engine.gui.tsh.elements.GuiToStringHelperArrayCreator;
import space.engine.gui.tsh.elements.GuiToStringHelperMapperCreator;
import space.engine.gui.tsh.elements.GuiToStringHelperModifierCreator;
import space.engine.gui.tsh.elements.GuiToStringHelperObjectsCreator;
import space.engine.gui.tsh.elements.GuiToStringHelperTableCreator;
import space.engine.string.toStringHelper.ToStringHelper;

public class MonofontGuiApi extends AbstractGuiApi {
	
	public static final MonofontGuiApi INSTANCE = new MonofontGuiApi();
	public static final ToStringHelper<?> TSH = new MonofontToStringHelper(INSTANCE);
	
	protected MonofontGuiApi() {
		//direction
		addElements(MonofontColumn.CREATOR, GuiColumnCreator.class);
		addElements(MonofontRow.CREATOR, GuiRowCreator.class);
		addElements(MonofontDirectional.CREATOR, GuiDirectionalCreator.class);
		addElements(MonofontTable.CREATOR, GuiTableCreator.class);
		
		//text
		addElements(MonofontText1D.CREATOR, GuiText1DCreator.class);
		addElements(MonofontText2D.CREATOR, GuiText2DCreator.class);
		
		//tsh
		addElement((GuiToStringHelperApiCreator) () -> TSH, GuiToStringHelperApiCreator.class);
		addElement(MonofontTSHArray.CREATOR, GuiToStringHelperArrayCreator.class);
		addElement(MonofontTSHMapper.CREATOR, GuiToStringHelperMapperCreator.class);
		addElement(MonofontTSHModifier.CREATOR, GuiToStringHelperModifierCreator.class);
		addElement(MonofontTSHObjects.CREATOR, GuiToStringHelperObjectsCreator.class);
		addElement(MonofontTSHTable.CREATOR, GuiToStringHelperTableCreator.class);
	}
	
	@Override
	public Class<MonofontGuiElement> getBaseElementClass() {
		return MonofontGuiElement.class;
	}
}
