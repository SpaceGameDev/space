package space.engine.indexmap.multi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.engine.indexmap.IndexMap;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Supplier;

public interface IndexMultiMap<VALUE> {
	
	int[] EMPTYINT = IndexMap.EMPTYINT;
	
	//expandable
	default boolean isExpandable() {
		return isExpandable(EMPTYINT);
	}
	
	boolean isExpandable(int[] pos);
	
	//size
	default int size() {
		return size(EMPTYINT);
	}
	
	int size(int[] pos);
	
	//maxsize
	default int maxSize(int depth) {
		return maxSize(EMPTYINT, depth);
	}
	
	default int maxSize(int[] pos, int depth) {
		if (depth == 0)
			return size(pos);
		
		int[] ret = new int[] {Integer.MIN_VALUE};
		maxSize(pos, depth, ret);
		return ret[0];
	}
	
	default void maxSize(int[] pos, int depth, int[] ret) {
		if (depth == 0) {
			int curr = size(pos);
			if (curr > ret[0])
				ret[0] = curr;
			return;
		}
		
		int[] temp = new int[pos.length + 1];
		System.arraycopy(pos, 0, temp, 0, pos.length);
		
		for (int i = 0; i < size(pos); i++) {
			temp[pos.length] = i;
			maxSize(temp, depth - 1, ret);
		}
	}
	
	//access
	default boolean contains(int[] pos) {
		return get(pos) != null;
	}
	
	@Nullable VALUE get(int[] pos);
	
	@NotNull IndexMultiMapEntry<? extends VALUE> getEntry(int[] pos);
	
	@Nullable VALUE put(int[] pos, VALUE v);
	
	@Nullable VALUE remove(int[] pos);
	
	//advanced access
	default VALUE getOrDefault(int[] pos, VALUE def) {
		VALUE v = get(pos);
		return v == null ? def : v;
	}
	
	@Nullable
	default VALUE putIfAbsent(int[] pos, VALUE v) {
		VALUE c = get(pos);
		if (c != null)
			return c;
		
		put(pos, c = v);
		return c;
	}
	
	default VALUE putIfAbsent(int[] pos, @NotNull Supplier<VALUE> v) {
		VALUE c = get(pos);
		if (c != null)
			return c;
		
		put(pos, c = v.get());
		return c;
	}
	
	@Nullable
	default VALUE replace(int[] pos, VALUE newValue) {
		if (contains(pos))
			return put(pos, newValue);
		return null;
	}
	
	default boolean replace(int[] pos, @Nullable VALUE oldValue, VALUE newValue) {
		if (Objects.equals(get(pos), oldValue)) {
			put(pos, newValue);
			return true;
		}
		return false;
	}
	
	default boolean replace(int[] pos, @Nullable VALUE oldValue, @NotNull Supplier<VALUE> newValue) {
		if (Objects.equals(get(pos), oldValue)) {
			put(pos, newValue.get());
			return true;
		}
		return false;
	}
	
	default boolean remove(int[] pos, @NotNull VALUE v) {
		VALUE c = get(pos);
		if (c == v) {
			remove(pos);
			return true;
		}
		return false;
	}
	
	//rest
	default void clear() {
		clear(EMPTYINT);
	}
	
	void clear(int[] pos);
	
	@NotNull Collection<VALUE> values();
	
	@NotNull
	default Collection<IndexMultiMapEntry<VALUE>> table() {
		return table(EMPTYINT);
	}
	
	@NotNull Collection<IndexMultiMapEntry<VALUE>> table(int[] pos);
	
	interface IndexMultiMapEntry<VALUE> {
		
		int[] getIndex();
		
		@Nullable VALUE getValue();
		
		void setValue(@Nullable VALUE v);
	}
	
	class IndexMultiMapTableIteratorToNormalIterator<VALUE> extends AbstractCollection<VALUE> {
		
		Collection<IndexMultiMapEntry<VALUE>> coll;
		
		public IndexMultiMapTableIteratorToNormalIterator(Collection<IndexMultiMapEntry<VALUE>> coll) {
			this.coll = coll;
		}
		
		@NotNull
		@Override
		public Iterator<VALUE> iterator() {
			return new Iterator<>() {
				Iterator<IndexMultiMapEntry<VALUE>> iter = coll.iterator();
				
				@Override
				public boolean hasNext() {
					return iter.hasNext();
				}
				
				@Nullable
				@Override
				public VALUE next() {
					return iter.next().getValue();
				}
			};
		}
		
		@Override
		public int size() {
			return coll.size();
		}
	}
}
