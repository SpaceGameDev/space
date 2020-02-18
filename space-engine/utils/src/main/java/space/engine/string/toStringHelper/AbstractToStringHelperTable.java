package space.engine.string.toStringHelper;

import org.jetbrains.annotations.Nullable;
import space.engine.indexmap.multi.IndexMultiMap;
import space.engine.string.toStringHelper.ToStringHelper.ToStringHelperTable;

public abstract class AbstractToStringHelperTable<T> implements ToStringHelperTable<T> {
	
	public IndexMultiMap<T> map;
	
	public AbstractToStringHelperTable(int dimensions) {
		map = ToStringHelper.getOptimalMultiMap(dimensions);
	}
	
	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public T put(int[] pos, Object object) {
		return map.put(pos, (T) object);
	}
	
	@Override
	public abstract T build();
}
