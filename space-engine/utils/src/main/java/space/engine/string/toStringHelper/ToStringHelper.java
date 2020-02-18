package space.engine.string.toStringHelper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.engine.baseobject.ToString;
import space.engine.indexmap.IndexMapArray;
import space.engine.indexmap.multi.IndexMultiMap;
import space.engine.indexmap.multi.IndexMultiMap2D;
import space.engine.indexmap.multi.IndexMultiMapFrom1DIndexMap;
import space.engine.string.CharSequence2D;
import space.engine.string.String2D;

import java.util.function.Supplier;

/**
 * a helper class for creating {@link String}s for {@link Object}s
 *
 * @param <T> type of return from the {@link ToStringHelper}
 */
public interface ToStringHelper<T> {
	
	@NotNull
	static ToStringHelper<?> getDefault() {
		return ToStringHelperGetter.DEFAULT.get();
	}
	
	static void setDefault(@NotNull Supplier<ToStringHelper<?>> tsh) {
		ToStringHelperGetter.DEFAULT = tsh;
	}
	
	static void setDefault(@NotNull ToStringHelper<?> tsh) {
		ToStringHelperGetter.DEFAULT = () -> tsh;
	}
	
	//native
	@NotNull T toString(byte b);
	
	@NotNull T toString(short s);
	
	@NotNull T toString(int i);
	
	@NotNull T toString(long l);
	
	@NotNull T toString(float f);
	
	@NotNull T toString(double d);
	
	@NotNull T toString(boolean b);
	
	@NotNull T toString(char c);
	
	//array
	@NotNull
	default T toString(@Nullable byte[] obj) {
		return obj == null ? toStringNull() : toString(obj, 0, obj.length);
	}
	
	@NotNull
	default T toString(@Nullable short[] obj) {
		return obj == null ? toStringNull() : toString(obj, 0, obj.length);
	}
	
	@NotNull
	default T toString(@Nullable int[] obj) {
		return obj == null ? toStringNull() : toString(obj, 0, obj.length);
	}
	
	@NotNull
	default T toString(@Nullable long[] obj) {
		return obj == null ? toStringNull() : toString(obj, 0, obj.length);
	}
	
	@NotNull
	default T toString(@Nullable float[] obj) {
		return obj == null ? toStringNull() : toString(obj, 0, obj.length);
	}
	
	@NotNull
	default T toString(@Nullable double[] obj) {
		return obj == null ? toStringNull() : toString(obj, 0, obj.length);
	}
	
	@NotNull
	default T toString(@Nullable boolean[] obj) {
		return obj == null ? toStringNull() : toString(obj, 0, obj.length);
	}
	
	@NotNull
	default T toString(@Nullable char[] obj) {
		return obj == null ? toStringNull() : toString(obj, 0, obj.length);
	}
	
	//array from to
	@NotNull T toString(@Nullable byte[] obj, int from, int to);
	
	@NotNull T toString(@Nullable short[] obj, int from, int to);
	
	@NotNull T toString(@Nullable int[] obj, int from, int to);
	
	@NotNull T toString(@Nullable long[] obj, int from, int to);
	
	@NotNull T toString(@Nullable float[] obj, int from, int to);
	
	@NotNull T toString(@Nullable double[] obj, int from, int to);
	
	@NotNull T toString(@Nullable boolean[] obj, int from, int to);
	
	@NotNull T toString(@Nullable char[] obj, int from, int to);
	
	//array object
	@NotNull
	default T toString(@Nullable Object[] obj) {
		return obj == null ? toStringNull() : toString(obj, 0, obj.length);
	}
	
	@NotNull T toString(@Nullable Object[] obj, int from, int to);
	
	//String
	@NotNull T toString(CharSequence str);
	
	@NotNull T toString(String str);
	
	@NotNull T toString(CharSequence2D str);
	
	@NotNull T toString(String2D str);
	
	//null
	@NotNull T toStringNull();
	
	//object
	default T toString(@Nullable Object o) {
		//null
		if (o == null)
			return toStringNull();
		
		//string
		if (o instanceof String)
			return toString((String) o);
		if (o instanceof CharSequence)
			return toString((CharSequence) o);
		if (o instanceof String2D)
			return toString((String2D) o);
		if (o instanceof CharSequence2D)
			return toString((CharSequence2D) o);
		
		//native autoboxing
		if (o instanceof Byte)
			return toString(((Byte) o).byteValue());
		if (o instanceof Short)
			return toString(((Short) o).shortValue());
		if (o instanceof Integer)
			return toString(((Integer) o).intValue());
		if (o instanceof Long)
			return toString(((Long) o).longValue());
		if (o instanceof Float)
			return toString(((Float) o).floatValue());
		if (o instanceof Double)
			return toString(((Double) o).doubleValue());
		if (o instanceof Boolean)
			return toString(((Boolean) o).booleanValue());
		if (o instanceof Character)
			return toString(((Character) o).charValue());
		
		//native array
		if (o instanceof byte[])
			return toString((byte[]) o);
		if (o instanceof short[])
			return toString((short[]) o);
		if (o instanceof int[])
			return toString((int[]) o);
		if (o instanceof long[])
			return toString((long[]) o);
		if (o instanceof float[])
			return toString((float[]) o);
		if (o instanceof double[])
			return toString((double[]) o);
		if (o instanceof boolean[])
			return toString((boolean[]) o);
		if (o instanceof char[])
			return toString((char[]) o);
		
		return ToString.toTSH(this, o);
	}
	
	//modifier
	@NotNull T createModifier(@NotNull String modifier, @Nullable Object value);
	
	//objects
	
	/**
	 * an object mapper mapping names of fields to values
	 */
	@NotNull ToStringHelperObjectsInstance<T> createObjectInstance(@NotNull Object obj);
	
	/**
	 * an object mapper mapping names of fields to values
	 */
	interface ToStringHelperObjectsInstance<T> {
		
		//primitive
		void add(@NotNull String name, byte obj);
		
		void add(@NotNull String name, short obj);
		
		void add(@NotNull String name, int obj);
		
		void add(@NotNull String name, long obj);
		
		void add(@NotNull String name, float obj);
		
		void add(@NotNull String name, double obj);
		
		void add(@NotNull String name, boolean obj);
		
		void add(@NotNull String name, char obj);
		
		//array
		default void add(@NotNull String name, @Nullable byte[] obj) {
			if (obj == null)
				addNull(name);
			else
				add(name, obj, 0, obj.length);
		}
		
		default void add(@NotNull String name, @Nullable short[] obj) {
			if (obj == null)
				addNull(name);
			else
				add(name, obj, 0, obj.length);
		}
		
		default void add(@NotNull String name, @Nullable int[] obj) {
			if (obj == null)
				addNull(name);
			else
				add(name, obj, 0, obj.length);
		}
		
		default void add(@NotNull String name, @Nullable long[] obj) {
			if (obj == null)
				addNull(name);
			else
				add(name, obj, 0, obj.length);
		}
		
		default void add(@NotNull String name, @Nullable float[] obj) {
			if (obj == null)
				addNull(name);
			else
				add(name, obj, 0, obj.length);
		}
		
		default void add(@NotNull String name, @Nullable double[] obj) {
			if (obj == null)
				addNull(name);
			else
				add(name, obj, 0, obj.length);
		}
		
		default void add(@NotNull String name, @Nullable boolean[] obj) {
			if (obj == null)
				addNull(name);
			else
				add(name, obj, 0, obj.length);
		}
		
		default void add(@NotNull String name, @Nullable char[] obj) {
			if (obj == null)
				addNull(name);
			else
				add(name, obj, 0, obj.length);
		}
		
		//array from to
		void add(@NotNull String name, @Nullable byte[] obj, int from, int to);
		
		void add(@NotNull String name, @Nullable short[] obj, int from, int to);
		
		void add(@NotNull String name, @Nullable int[] obj, int from, int to);
		
		void add(@NotNull String name, @Nullable long[] obj, int from, int to);
		
		void add(@NotNull String name, @Nullable float[] obj, int from, int to);
		
		void add(@NotNull String name, @Nullable double[] obj, int from, int to);
		
		void add(@NotNull String name, @Nullable boolean[] obj, int from, int to);
		
		void add(@NotNull String name, @Nullable char[] obj, int from, int to);
		
		//object
		void add(@NotNull String name, @Nullable Object obj);
		
		void addNull(@NotNull String name);
		
		default void add(@NotNull String name, @Nullable Object[] obj) {
			if (obj == null)
				addNull(name);
			else
				add(name, obj, 0, obj.length);
		}
		
		void add(@NotNull String name, @Nullable Object[] obj, int from, int to);
		
		//extra
		default void add(@NotNull String name, String obj) {
			add(name, (CharSequence) obj);
		}
		
		default void add(@NotNull String name, CharSequence obj) {
			add(name, (Object) obj);
		}
		
		default void add(@NotNull String name, String2D obj) {
			add(name, (CharSequence2D) obj);
		}
		
		default void add(@NotNull String name, CharSequence2D obj) {
			add(name, (Object) obj);
		}
		
		T build();
	}
	
	//mapper
	
	/**
	 * mappers map one thing (pos[1] = 0) to another (pos[1] = 1)
	 */
	@NotNull ToStringHelperTable<T> createMapper(@NotNull String name, @NotNull String separator, boolean align);
	
	//tables
	
	/**
	 * tables can be infinite in any of their (infinite) dimensions
	 */
	@NotNull ToStringHelperTable<T> createTable(@NotNull String name, int dimensions);
	
	interface ToStringHelperTable<T> {
		
		@Nullable Object put(int[] pos, @Nullable Object object);
		
		T build();
	}
	
	@NotNull
	static <VALUE> IndexMultiMap<VALUE> getOptimalMultiMap(int dimensions) {
		if (dimensions <= 0)
			throw new IllegalArgumentException("Dimension " + dimensions + " <= 0");
		
		switch (dimensions) {
			case 1:
				return new IndexMultiMapFrom1DIndexMap<>(new IndexMapArray<>());
			case 2:
				return new IndexMultiMap2D<>();
			default:
				throw new UnsupportedOperationException("Dimension " + dimensions + " not supported!");
		}
	}
}
