package space.engine.delegate.util;

import org.jetbrains.annotations.Nullable;

import java.lang.ref.Reference;
import java.util.function.Function;

public class ReferenceUtil {
	
	public static final Function<?, Reference<?>> defRefCreator = e -> {
		throw new IllegalStateException("No Reference Creator!");
	};
	
	@SuppressWarnings("unchecked")
	public static <E> Function<E, Reference<? extends E>> defRefCreator() {
		return (Function<E, Reference<? extends E>>) (Object) defRefCreator;
	}
	
	@Nullable
	public static <E> E getSafe(Reference<E> ref) {
		return ref == null ? null : ref.get();
	}
}
