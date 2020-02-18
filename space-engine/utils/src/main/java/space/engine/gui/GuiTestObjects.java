package space.engine.gui;

import org.jetbrains.annotations.NotNull;
import space.engine.baseobject.ToString;
import space.engine.gui.monofont.MonofontGuiApi;
import space.engine.string.String2D;
import space.engine.string.toStringHelper.ToStringHelper;
import space.engine.string.toStringHelper.ToStringHelper.ToStringHelperObjectsInstance;

public class GuiTestObjects {
	
	public static void main(String[] args) {
		ToStringHelper.setDefault(MonofontGuiApi.TSH);
//		System.out.println(new SimpleDependency("test", new String[] {"before"}, new String[] {"after", "last"}));
		TestObj1 th = new TestObj1("Name thing", new String2D(new String[] {"Hello there!", "I'm a Name Thing"}));
		System.out.println(th);
	}
	
	public static class TestObj1 implements ToString {
		
		String name;
		String2D name2D;
		
		public TestObj1(String name, String2D name2D) {
			this.name = name;
			this.name2D = name2D;
		}
		
		@NotNull
		@Override
		public <TSHTYPE> TSHTYPE toTSH(@NotNull ToStringHelper<TSHTYPE> api) {
			ToStringHelperObjectsInstance<TSHTYPE> tsh = api.createObjectInstance(this);
			tsh.add("name", this.name);
			tsh.add("name2D", this.name2D);
			return tsh.build();
		}
		
		@Override
		public String toString() {
			return toString0();
		}
	}
}
