package space.engine.delegate.map.specific;

import space.engine.delegate.map.UnmodifiableMap;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class TokenMap {
	
	public static final Predicate<Field> ALL = input -> true;
	/**
	 * negative are usually error values
	 */
	public static final Predicate<Field> NEGATIVE = input -> {
		try {
			return input.getInt(null) < 0;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException();
		}
	};
	public static int PUBLIC_STATIC = Modifier.PUBLIC | Modifier.STATIC;
	public static int PUBLIC_STATIC_FINAL = Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;
	
	/**
	 * this method will search in clazzes for any int fields and maps the int to the field name
	 * useful for translating OpenGL / OpenCL / Vulkan error codes in error info
	 *
	 * @param modifier modifier for fields to have
	 * @param filter   extra filter for fields
	 * @param clazzes  all classes to search
	 * @return a {@link Map}<{@link Integer}, {@link String}>
	 */
	public static Map<Integer, String> makeErrorTokenMapInt(int modifier, Predicate<Field> filter, Class<?>... clazzes) {
		try {
			Map<Integer, String> map = new HashMap<>();
			
			for (Class<?> clazz : clazzes) {
				for (Field f : clazz.getFields()) {
					if (((f.getModifiers() & modifier) == modifier) && (f.getType() == int.class)) {
						if (filter == null || filter.test(f)) {
							int value = f.getInt(null);
							String name = f.getName();
							
							String existing = map.get(value);
							if (existing != null)
								name = existing + " | " + name;
							map.put(value, name);
						}
					}
				}
			}
			
			return new UnmodifiableMap<>(map);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
