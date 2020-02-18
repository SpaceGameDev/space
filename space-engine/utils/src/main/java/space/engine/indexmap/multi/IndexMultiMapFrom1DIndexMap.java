package space.engine.indexmap.multi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.engine.delegate.collection.ConvertingCollection;
import space.engine.delegate.collection.UnmodifiableCollection;
import space.engine.indexmap.IndexMap;
import space.engine.indexmap.IndexMapArray;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static space.engine.ArrayUtils.getSafeO;

public class IndexMultiMapFrom1DIndexMap<VALUE> implements IndexMultiMap<VALUE> {
	
	public IndexMap<VALUE> indexMap;
	public boolean modifiable;
	public int[] relativePos;
	
	public IndexMultiMapFrom1DIndexMap() {
		this(new IndexMapArray<>());
	}
	
	public IndexMultiMapFrom1DIndexMap(IndexMap<VALUE> indexMap) {
		this(indexMap, false);
	}
	
	public IndexMultiMapFrom1DIndexMap(IndexMap<VALUE> indexMap, boolean modifiable) {
		this(indexMap, modifiable, EMPTYINT);
	}
	
	public IndexMultiMapFrom1DIndexMap(IndexMap<VALUE> indexMap, boolean modifiable, int dimensionalDepth) {
		this(indexMap, modifiable, new int[dimensionalDepth]);
	}
	
	public IndexMultiMapFrom1DIndexMap(IndexMap<VALUE> indexMap, boolean modifiable, int[] relativePos) {
		this.indexMap = indexMap;
		this.modifiable = modifiable;
		this.relativePos = relativePos;
	}
	
	//basic
	public int getListIndex(int[] pos) {
		return isListIndexValid(pos) ? getSafeO(pos, relativePos.length, 0) : -1;
	}
	
	/**
	 * checks if position in inside the indexMap.
	 * (kinda like Arrays.equals(pos, relativePos))
	 */
	public boolean isListIndexValid(int[] pos) {
		if (pos.length != relativePos.length)
			return false;
		for (int i = 0; i < relativePos.length; i++)
			if (getSafeO(pos, i, 0) != relativePos[i])
				return false;
		return true;
	}
	
	public void checkModificationAllowed() {
		if (!modifiable)
			throw new UnsupportedOperationException("not modifiable!");
	}
	
	//size
	@Override
	public boolean isExpandable(int[] pos) {
		return modifiable && isListIndexValid(pos);
	}
	
	@Override
	public int size(int[] pos) {
		return isListIndexValid(pos) ? indexMap.size() : 1;
	}
	
	//access
	@Nullable
	@Override
	public VALUE get(int[] pos) {
		int i = getListIndex(pos);
		if (i == -1)
			return null;
		return indexMap.get(i);
	}
	
	@NotNull
	@Override
	public IndexMultiMapEntry<? extends VALUE> getEntry(int[] pos) {
		int i = getListIndex(pos);
		if (i == -1)
			throw new IllegalArgumentException();
		return new Entry<>(indexMap.getEntry(i));
	}
	
	@Override
	public VALUE put(int[] pos, VALUE v) {
		checkModificationAllowed();
		int i = getListIndex(pos);
		if (i == -1)
			return null;
		return indexMap.put(i, v);
	}
	
	@Override
	public VALUE remove(int[] pos) {
		checkModificationAllowed();
		int i = getListIndex(pos);
		if (i == -1)
			return null;
		return indexMap.remove(i);
	}
	
	//clear
	@Override
	public void clear() {
		checkModificationAllowed();
		indexMap.clear();
	}
	
	@Override
	public void clear(int[] pos) {
		checkModificationAllowed();
		int i = getListIndex(pos);
		if (i == -1)
			return;
		indexMap.clear();
	}
	
	//values
	@NotNull
	@Override
	public Collection<VALUE> values() {
		return modifiable ? indexMap.values() : new UnmodifiableCollection<>(indexMap.values());
	}
	
	@NotNull
	@Override
	public Collection<IndexMultiMapEntry<VALUE>> table(int[] pos) {
		if (pos.length <= relativePos.length) {
			for (int i = 0; i < pos.length; i++)
				if (pos[i] != relativePos[i])
					return Collections.emptySet();
			return tableComplete();
		} else {
			for (int i = 0; i < pos.length; i++)
				if (pos[i] != getSafeO(relativePos, i, 0))
					return Collections.emptyList();
			return Collections.singleton(new Entry<>(indexMap.getEntry(pos[relativePos.length])));
		}
	}
	
	public Collection<IndexMultiMapEntry<VALUE>> tableComplete() {
		return new ConvertingCollection.BiDirectional<>(indexMap.entrySet(),
														IndexMultiMapFrom1DIndexMap.Entry::new,
														entry -> entry instanceof IndexMultiMapFrom1DIndexMap.Entry ? ((Entry<VALUE>) entry).entry : null);
	}
	
	protected class Entry<V> implements IndexMultiMapEntry<V> {
		
		private final IndexMap.Entry<V> entry;
		
		public Entry(IndexMap.Entry<V> entry) {
			this.entry = entry;
		}
		
		@Override
		public int[] getIndex() {
			int[] array = Arrays.copyOf(relativePos, relativePos.length + 1);
			array[relativePos.length] = entry.getIndex();
			return array;
		}
		
		@Override
		public V getValue() {
			return entry.getValue();
		}
		
		@Override
		public void setValue(V v) {
			checkModificationAllowed();
			entry.setValue(v);
		}
	}
}
