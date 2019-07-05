package space.engine.delegate.map.specific;

import org.jetbrains.annotations.Nullable;
import space.engine.delegate.map.DelegatingMap;

import java.util.Map;

/**
 * If you {@link GetClassOrSuperMap#get(Object)} with a Class, and the class does not exist, it will try again with it's superclass, until a value is found or no superclass exists.
 * Only works with {@link GetClassOrSuperMap#get(Object)}, {@link GetClassOrSuperMap#containsKey(Object)} and {@link GetClassOrSuperMap#getOrDefault(Object, Object)}
 */
public class GetClassOrSuperMap<K extends Class<?>, V> extends DelegatingMap<K, V> {
	
	public GetClassOrSuperMap(Map<K, V> map) {
		super(map);
	}
	
	@Override
	public boolean containsKey(Object key) {
		if (!(key instanceof Class<?>))
			return false;
		
		for (Class<?> clazz = (Class<?>) key; clazz != null; clazz = clazz.getSuperclass())
			if (map.containsKey(clazz))
				return true;
		return false;
	}
	
	@Nullable
	@Override
	public V get(Object key) {
		if (!(key instanceof Class<?>))
			return null;
		
		for (Class<?> clazz = (Class<?>) key; clazz != null; clazz = clazz.getSuperclass()) {
			V ret = map.get(clazz);
			if (ret != null)
				return ret;
		}
		return null;
	}
	
	@Override
	@SuppressWarnings("SuspiciousMethodCalls")
	public V getOrDefault(Object key, V defaultValue) {
		if (!(key instanceof Class<?>))
			return defaultValue;
		
		for (Class<?> clazz = (Class<?>) key; clazz != null; clazz = clazz.getSuperclass()) {
			V ret = map.get(clazz);
			if (ret != null)
				return ret;
		}
		return defaultValue;
	}
}
