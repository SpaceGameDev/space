package space.engine.key.attribute;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.engine.key.Key;

public interface AbstractEntry<V> {
	
	@NotNull Key<V> getKey();
	
	//value
	@Nullable Object getValueDirect();
}
