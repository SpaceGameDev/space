package space.engine.string.toStringHelper;

import org.jetbrains.annotations.NotNull;
import space.engine.delegate.iterator.Iteratorable;
import space.engine.indexmap.multi.IndexMultiMap.IndexMultiMapEntry;
import space.engine.string.CharSequence2D;
import space.engine.string.String2D;

import java.util.Arrays;
import java.util.Collection;
import java.util.ListIterator;

public class ToStringHelperDefault implements ToStringHelper<String> {
	
	public static final ToStringHelperDefault INSTANCE = new ToStringHelperDefault();
	
	//native
	@NotNull
	@Override
	public String toString(byte b) {
		return Byte.toString(b);
	}
	
	@NotNull
	@Override
	public String toString(short s) {
		return Short.toString(s);
	}
	
	@NotNull
	@Override
	public String toString(int i) {
		return Integer.toString(i);
	}
	
	@NotNull
	@Override
	public String toString(long l) {
		return Long.toString(l);
	}
	
	@NotNull
	@Override
	public String toString(float f) {
		return Float.toString(f);
	}
	
	@NotNull
	@Override
	public String toString(double d) {
		return Double.toString(d);
	}
	
	@NotNull
	@Override
	public String toString(boolean b) {
		return Boolean.toString(b);
	}
	
	@NotNull
	@Override
	public String toString(char c) {
		return Character.toString(c);
	}
	
	//native array
	@NotNull
	@Override
	public String toString(byte[] obj, int from, int to) {
		if (obj == null)
			return toStringNull();
		StringBuilder b = new StringBuilder();
		b.append('[');
		for (int i = from; i < to; i++) {
			b.append(toString(obj[i]));
			if (i < to - 1)
				b.append(", ");
		}
		b.append(']');
		return b.toString();
	}
	
	@NotNull
	@Override
	public String toString(short[] obj, int from, int to) {
		if (obj == null)
			return toStringNull();
		StringBuilder b = new StringBuilder();
		b.append('[');
		for (int i = from; i < to; i++) {
			b.append(toString(obj[i]));
			if (i < to - 1)
				b.append(", ");
		}
		b.append(']');
		return b.toString();
	}
	
	@NotNull
	@Override
	public String toString(int[] obj, int from, int to) {
		if (obj == null)
			return toStringNull();
		StringBuilder b = new StringBuilder();
		b.append('[');
		for (int i = from; i < to; i++) {
			b.append(toString(obj[i]));
			if (i < to - 1)
				b.append(", ");
		}
		b.append(']');
		return b.toString();
	}
	
	@NotNull
	@Override
	public String toString(long[] obj, int from, int to) {
		if (obj == null)
			return toStringNull();
		StringBuilder b = new StringBuilder();
		b.append('[');
		for (int i = from; i < to; i++) {
			b.append(toString(obj[i]));
			if (i < to - 1)
				b.append(", ");
		}
		b.append(']');
		return b.toString();
	}
	
	@NotNull
	@Override
	public String toString(float[] obj, int from, int to) {
		if (obj == null)
			return toStringNull();
		StringBuilder b = new StringBuilder();
		b.append('[');
		for (int i = from; i < to; i++) {
			b.append(toString(obj[i]));
			if (i < to - 1)
				b.append(", ");
		}
		b.append(']');
		return b.toString();
	}
	
	@NotNull
	@Override
	public String toString(double[] obj, int from, int to) {
		if (obj == null)
			return toStringNull();
		StringBuilder b = new StringBuilder();
		b.append('[');
		for (int i = from; i < to; i++) {
			b.append(toString(obj[i]));
			if (i < to - 1)
				b.append(", ");
		}
		b.append(']');
		return b.toString();
	}
	
	@NotNull
	@Override
	public String toString(boolean[] obj, int from, int to) {
		if (obj == null)
			return toStringNull();
		StringBuilder b = new StringBuilder();
		b.append('[');
		for (int i = from; i < to; i++) {
			b.append(toString(obj[i]));
			if (i < to - 1)
				b.append(", ");
		}
		b.append(']');
		return b.toString();
	}
	
	@NotNull
	@Override
	public String toString(char[] obj, int from, int to) {
		if (obj == null)
			return toStringNull();
		StringBuilder b = new StringBuilder();
		b.append('[');
		for (int i = from; i < to; i++) {
			b.append(toString(obj[i]));
			if (i < to - 1)
				b.append(", ");
		}
		b.append(']');
		return b.toString();
	}
	
	@NotNull
	@Override
	public String toString(Object[] obj, int from, int to) {
		if (obj == null)
			return toStringNull();
		StringBuilder b = new StringBuilder();
		b.append('[');
		for (int i = from; i < to; i++) {
			b.append(toString(obj[i]));
			if (i < to - 1)
				b.append(", ");
		}
		b.append(']');
		return b.toString();
	}
	
	//String
	@NotNull
	@Override
	public String toString(CharSequence str) {
		return str == null ? toStringNull() : str.toString();
	}
	
	@NotNull
	@Override
	public String toString(String str) {
		return str == null ? toStringNull() : str;
	}
	
	@NotNull
	@Override
	public String toString(CharSequence2D str) {
		return str == null ? toStringNull() : str.toString();
	}
	
	@NotNull
	@Override
	public String toString(String2D str) {
		return str == null ? toStringNull() : str.toString();
	}
	
	//null
	@NotNull
	@Override
	public String toStringNull() {
		return "null";
	}
	
	//modifier
	@NotNull
	@Override
	public String createModifier(@NotNull String modifier, Object value) {
		return modifier + " " + value;
	}
	
	//objects
	@NotNull
	@Override
	public ToStringHelperObjectsInstance<String> createObjectInstance(@NotNull Object obj) {
		return new AbstractToStringHelperObjectsInstance<>(obj, this) {
			@Override
			public String build() {
				StringBuilder b = new StringBuilder();
				b.append(obj.getClass().getName()).append("{ ");
				
				ListIterator<Entry<String>> iter = list.listIterator();
				for (Entry<String> entry : Iteratorable.toIteratorable(iter)) {
					b.append(entry.name).append(": ").append(entry.value);
					if (iter.hasNext())
						b.append(", ");
				}
				
				b.append('}');
				return b.toString();
			}
		};
	}
	
	//table
	@NotNull
	@Override
	public ToStringHelperTable<String> createTable(@NotNull String name, int dimensions) {
		return new AbstractToStringHelperTable<>(dimensions) {
			@Override
			public String build() {
				StringBuilder b = new StringBuilder();
				Collection<IndexMultiMapEntry<String>> coll = map.table();
				
				b.append(name).append("{ ");
				for (IndexMultiMapEntry<String> entry : coll)
					b.append(Arrays.toString(entry.getIndex())).append(": ").append((Object) entry.getValue()).append(", ");
				b.setLength(b.length() - 2);
				b.append('}');
				return b.toString();
			}
		};
	}
	
	//mapper
	@NotNull
	@Override
	public ToStringHelperTable<String> createMapper(@NotNull String name, @NotNull String separator, boolean align) {
		return new AbstractToStringHelperMapper<>() {
			@Override
			public String build() {
				StringBuilder b = new StringBuilder();
				
				b.append(name).append("{ ");
				int size = map.size();
				for (int i = 0; i < size; i++) {
					b.append((Object) map.get(new int[] {i, 0})).append(separator).append(Arrays.toString(new int[] {i, 1}));
					if (i + 1 < size)
						b.append(", ");
				}
				b.append('}');
				return b.toString();
			}
		};
	}
}
