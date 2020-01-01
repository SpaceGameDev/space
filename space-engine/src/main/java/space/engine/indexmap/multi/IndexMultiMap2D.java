package space.engine.indexmap.multi;

import org.jetbrains.annotations.NotNull;
import space.engine.ArrayUtils;

import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class IndexMultiMap2D<VALUE> implements IndexMultiMap<VALUE> {
	
	public static final int DEFAULT_CAPACITY = 16;
	public static final int DEFAULT_HEIGHT = 4;
	public static final int EXPAND_SHIFT_HEIGHT = 1;
	public static final int EXPAND_SHIFT = 1;
	
	public VALUE[][] buffer;
	public int[] length;
	public int height;
	
	public IndexMultiMap2D() {
		this(DEFAULT_HEIGHT, DEFAULT_CAPACITY);
	}
	
	public IndexMultiMap2D(int sizeHeight, int sizeCapacity) {
		//noinspection unchecked
		this((VALUE[][]) new Object[sizeHeight][sizeCapacity], false);
	}
	
	public IndexMultiMap2D(VALUE[][] buffer) {
		this(buffer, true);
	}
	
	public IndexMultiMap2D(VALUE[][] buffer, boolean isFilled) {
		this(buffer, isFilled ? calcLengths(buffer) : new int[buffer.length], isFilled ? buffer.length : 0);
	}
	
	public IndexMultiMap2D(VALUE[][] buffer, int[] length, int height) {
		this.buffer = buffer;
		this.length = length;
		this.height = height;
	}
	
	public static int[] calcLengths(Object[][] buffer) {
		int l = buffer.length;
		int[] ret = new int[l];
		for (int i = 0; i < l; i++)
			ret[i] = buffer[i].length;
		return ret;
	}
	
	public boolean ensureHeightIndex(int index) {
		return ensureHeight(index + 1);
	}
	
	public boolean ensureHeight(int capa) {
		int l = buffer.length;
		if (l < capa) {
			int newsize = ArrayUtils.getOptimalArraySizeExpansion(l, capa, EXPAND_SHIFT_HEIGHT);
			
			VALUE[][] old = buffer;
			//noinspection unchecked
			buffer = (VALUE[][]) new Object[newsize][];
			System.arraycopy(old, 0, buffer, 0, l);
			
			int[] oldl = length;
			length = new int[newsize];
			System.arraycopy(oldl, 0, length, 0, l);
			
			return true;
		}
		return false;
	}
	
	public boolean ensureCapacityExists(int height) {
		return ensureCapacity(height, 0);
	}
	
	public boolean ensureCapacityIndex(int height, int index) {
		return ensureCapacity(height, index + 1);
	}
	
	public boolean ensureCapacity(int height, int capa) {
		VALUE[] buf = buffer[height];
		
		if (buf == null) {
			//noinspection unchecked
			buffer[height] = (VALUE[]) new Object[ArrayUtils.getOptimalArraySizeStart(DEFAULT_CAPACITY, capa)];
			return true;
		}
		
		int l = buf.length;
		if (l < capa) {
			//noinspection unchecked
			VALUE[] n = (VALUE[]) new Object[ArrayUtils.getOptimalArraySizeExpansion(l, capa, EXPAND_SHIFT)];
			System.arraycopy(buf, 0, n, 0, l);
			buffer[height] = n;
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean isExpandable() {
		return true;
	}
	
	@Override
	public boolean isExpandable(int[] pos) {
		return true;
	}
	
	@Override
	public int size(int[] pos) {
		if (pos.length == 0)
			return height;
		return length[pos[0]];
	}
	
	@Override
	public boolean contains(int[] pos) {
		int h = 0 < pos.length ? pos[0] : 0;
		if (h >= height)
			return false;
		
		VALUE[] buf = buffer[h];
		if (buf == null)
			return false;
		
		int l = length[h];
		int x = 1 < pos.length ? pos[1] : 0;
		return x < l && buf[x] != null;
	}
	
	@Override
	public VALUE get(int[] pos) {
		int h = 0 < pos.length ? pos[0] : 0;
		if (h >= height)
			return null;
		
		VALUE[] buf = buffer[h];
		if (buf == null)
			return null;
		
		int l = length[h];
		int x = 1 < pos.length ? pos[1] : 0;
		if (x >= l)
			return null;
		
		return buf[x];
	}
	
	@NotNull
	@Override
	public IndexMultiMapEntry<? extends VALUE> getEntry(int[] pos) {
		return new Entry(pos);
	}
	
	@Override
	public VALUE remove(int[] pos) {
		int h = 0 < pos.length ? pos[0] : 0;
		if (h >= height)
			return null;
		
		VALUE[] buf = buffer[h];
		if (buf == null)
			return null;
		
		int l = length[h];
		int x = 1 < pos.length ? pos[1] : 0;
		if (x >= l)
			return null;
		
		VALUE ret = buf[x];
		buf[x] = null;
		return ret;
	}
	
	@Override
	public VALUE put(int[] pos, VALUE v) {
		int h = 0 < pos.length ? pos[0] : 0;
		ensureHeightIndex(h);
		if (h >= height)
			height = h + 1;
		
		int x = 1 < pos.length ? pos[1] : 0;
		ensureCapacityIndex(h, x);
		if (x >= length[h])
			length[h] = x + 1;
		VALUE[] buf = buffer[h];
		
		VALUE ret = buf[x];
		buf[x] = v;
		return ret;
	}
	
	@Override
	public void clear() {
		Arrays.fill(buffer, null);
		Arrays.fill(length, 0);
		height = 0;
	}
	
	@Override
	public void clear(int[] pos) {
		if (pos.length == 0) {
			clear();
		} else {
			int h = pos[0];
			if (h >= height)
				return;
			Arrays.fill(buffer[h], null);
		}
	}
	
	public IndexMultiMap2D<VALUE> copy() {
		return new IndexMultiMap2D<>(buffer.clone(), length.clone(), height);
	}
	
	public void trimAll() {
		trim();
		for (int i = 0; i < height; i++)
			trim(i);
	}
	
	public void trim() {
		if (buffer.length == height)
			return;
		
		VALUE[][] old = buffer;
		//noinspection unchecked
		buffer = (VALUE[][]) new Object[height][];
		System.arraycopy(old, 0, buffer, 0, height);
	}
	
	public void trim(int h) {
		ensureCapacityExists(h);
		VALUE[] old = buffer[h];
		int l = length[h];
		
		if (old.length == l)
			return;
		
		//noinspection unchecked
		VALUE[] n = (VALUE[]) new Object[l];
		System.arraycopy(old, 0, n, 0, l);
		buffer[h] = n;
	}
	
	@NotNull
	@Override
	public Collection<VALUE> values() {
		return new IndexMultiMap.IndexMultiMapTableIteratorToNormalIterator<>(table());
	}
	
	@NotNull
	@Override
	public Collection<IndexMultiMapEntry<VALUE>> table(int[] pos) {
		return new Table();
	}
	
	public class Table extends AbstractCollection<IndexMultiMapEntry<VALUE>> {
		
		@NotNull
		@Override
		public Iterator<IndexMultiMapEntry<VALUE>> iterator() {
			return new EntryIterator();
		}
		
		@Override
		public int size() {
			int size = 0;
			for (int i = 0; i < height; i++)
				size += length[i];
			return size;
		}
	}
	
	public class Entry implements IndexMultiMapEntry<VALUE> {
		
		public int[] pos;
		
		public Entry(int[] pos) {
			this.pos = pos;
		}
		
		@Override
		public int[] getIndex() {
			return pos;
		}
		
		@Override
		public VALUE getValue() {
			return get(pos);
		}
		
		@Override
		public void setValue(VALUE v) {
			put(pos, v);
		}
	}
	
	public class EntryIterator implements Iterator<IndexMultiMapEntry<VALUE>> {
		
		public boolean hasNext = true;
		
		//current
		public int h;
		public int x;
		
		//next
		public int nh;
		public int nx = -1;
		
		public EntryIterator() {
			calcNext();
		}
		
		@Override
		public boolean hasNext() {
			return hasNext;
		}
		
		@Override
		public IndexMultiMapEntry<VALUE> next() {
			if (!hasNext)
				throw new NoSuchElementException();
			
			calcNext();
			return new Entry(new int[] {h, x});
		}
		
		public void calcNext() {
			//shift over
			h = nh;
			x = nx++;
			
			//nextLine
			while (nx >= length[nh]) {
				nh++;
				nx = 0;
				
				//endOfLine
				if (nh >= height) {
					hasNext = false;
					break;
				}
			}
		}
	}
}
