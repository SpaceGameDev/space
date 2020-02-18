package space.engine.unsafe;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class UnsafeInstance {
	
	private static final Unsafe UNSAFE;
	
	static {
		Unsafe u = null;
		try {
			Field f = Unsafe.class.getDeclaredField("theUnsafe");
			f.setAccessible(true);
			u = (Unsafe) f.get(null);
		} catch (Exception ignore) {
			
		}
		UNSAFE = u;
	}
	
	public static Unsafe getUnsafe() throws NoUnsafeException {
		if (UNSAFE == null)
			throw new NoUnsafeException("Unsafe is not avaible!");
		return UNSAFE;
	}
	
	public static long objectFieldOffset(Class<?> clazz, String name) throws UnsafeNoFieldException {
		try {
			return getUnsafe().objectFieldOffset(clazz.getDeclaredField(name));
		} catch (NoSuchFieldException e) {
			throw new UnsafeNoFieldException("Field " + clazz.getName() + "." + name, e);
		}
	}
	
	public static long objectFieldOffsetWithSuper(Class<?> clazz, String name) throws UnsafeNoFieldException {
		Unsafe unsafe = getUnsafe();
		Class<?> c = clazz;
		while (c != null) {
			try {
				return unsafe.objectFieldOffset(c.getDeclaredField(name));
			} catch (NoSuchFieldException ignored) {
			
			}
			c = c.getSuperclass();
		}
		throw new UnsafeNoFieldException("Field " + clazz.getName() + "." + name);
	}
}
