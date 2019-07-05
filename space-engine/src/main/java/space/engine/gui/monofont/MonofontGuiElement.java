package space.engine.gui.monofont;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.engine.baseobject.ToString;
import space.engine.gui.GuiElement;
import space.engine.string.CharSequence2D;
import space.engine.string.ToString2D;
import space.engine.string.toStringHelper.ToStringHelper;

public abstract class MonofontGuiElement implements ToString, GuiElement, ToString2D {
	
	public @Nullable MonofontGuiElement parent;
	
	//parent
	public void setParent(@Nullable MonofontGuiElement parent) {
		this.parent = parent;
	}
	
	//api
	@Override
	public MonofontGuiApi getApi() {
		return MonofontGuiApi.INSTANCE;
	}
	
	//size
	public abstract int sizeX();
	
	public abstract int sizeY();
	
	//toString
	public abstract CharSequence2D buildSequence2D();
	
	@NotNull
	@Override
	public CharSequence2D toString2D() {
		return buildSequence2D();
	}
	
	@Override
	public String toString() {
		return buildSequence2D().toString();
	}
	
	@NotNull
	@Override
	public <T> T toTSH(@NotNull ToStringHelper<T> api) {
		if (api instanceof MonofontGuiApi)
			//noinspection unchecked
			return (T) this;
		return api.toString(buildSequence2D());
	}
}
