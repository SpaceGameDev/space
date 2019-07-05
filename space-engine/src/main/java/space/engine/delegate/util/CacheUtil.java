package space.engine.delegate.util;

import org.jetbrains.annotations.Nullable;

public class CacheUtil {
	
	public static final Object nullObject = new Object();
	
	@SuppressWarnings("unchecked")
	public static <T> T nullObject() {
		return (T) nullObject;
	}
	
	public static @Nullable <T> T fromCache(T t) {
		return t == nullObject ? null : t;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T toCache(T t) {
		return t == null ? (T) nullObject : t;
	}
}
