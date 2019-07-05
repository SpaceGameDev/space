package space.engine.string.toStringHelper;

import org.jetbrains.annotations.Nullable;
import space.engine.indexmap.multi.IndexMultiMap;
import space.engine.indexmap.multi.IndexMultiMap2D;
import space.engine.string.toStringHelper.ToStringHelper.ToStringHelperTable;

import java.util.Arrays;

public abstract class AbstractToStringHelperMapper<T> implements ToStringHelperTable<T> {
	
	public IndexMultiMap<T> map = new IndexMultiMap2D<>(IndexMultiMap2D.DEFAULT_HEIGHT, 2);
	
	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public T put(int[] pos, Object object) {
		if (pos.length != 2 || (pos[1] == 0 || pos[1] == 1))
			throw new IllegalArgumentException(Arrays.toString(pos));
		return map.put(pos, (T) object);
	}
	
	@Override
	public abstract T build();
}
