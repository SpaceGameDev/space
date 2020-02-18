package space.engine.indexmap.multi;

import space.engine.ArrayUtils;

import java.util.Arrays;

public class IndexMultiMap2DInt {
	
	public static int defaultCapacity = 16;
	public static int defaultHeight = 1;
	public static int expandShiftHeight = 1;
	public static int expandShift = 1;
	
	public int[][] buffer;
	public int[] length;
	public int height;
	
	public IndexMultiMap2DInt() {
		this(defaultHeight, defaultCapacity);
	}
	
	public IndexMultiMap2DInt(int sizeHeight, int sizeCapacity) {
		this(new int[sizeHeight][sizeCapacity], false);
	}
	
	public IndexMultiMap2DInt(int[][] buffer) {
		this(buffer, true);
	}
	
	public IndexMultiMap2DInt(int[][] buffer, boolean isFilled) {
		this(buffer, isFilled ? calcLengths(buffer) : new int[buffer.length], isFilled ? buffer.length : 0);
	}
	
	public IndexMultiMap2DInt(int[][] buffer, int[] length, int height) {
		this.buffer = buffer;
		this.length = length;
		this.height = height;
	}
	
	public static int[] calcLengths(int[][] buffer) {
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
			int newsize = ArrayUtils.getOptimalArraySizeExpansion(l, capa, expandShiftHeight);
			
			int[][] old = buffer;
			buffer = new int[newsize][];
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
		int[] buf = buffer[height];
		
		if (buf == null) {
			buffer[height] = new int[ArrayUtils.getOptimalArraySizeStart(defaultCapacity, capa)];
			return true;
		}
		
		int l = buf.length;
		if (l < capa) {
			int[] n = new int[ArrayUtils.getOptimalArraySizeExpansion(l, capa, expandShift)];
			System.arraycopy(buf, 0, n, 0, l);
			buffer[height] = n;
			return true;
		}
		
		return false;
	}
	
	public int size() {
		return height;
	}
	
	public int size(int[] pos) {
		if (pos.length == 0)
			return height;
		return pos[0] < length.length ? length[pos[0]] : 0;
	}
	
	public boolean contains(int[] pos) {
		int h = 0 < pos.length ? pos[0] : 0;
		if (h >= height)
			return false;
		
		int[] buf = buffer[h];
		if (buf == null)
			return false;
		
		int l = length[h];
		int x = 1 < pos.length ? pos[1] : 0;
		return x < l;
	}
	
	public int get(int[] pos) {
		int h = 0 < pos.length ? pos[0] : 0;
		if (h >= height)
			return 0;
		
		int[] buf = buffer[h];
		if (buf == null)
			return 0;
		
		int l = length[h];
		int x = 1 < pos.length ? pos[1] : 0;
		if (x >= l)
			return 0;
		
		return buf[x];
	}
	
	public int remove(int[] pos) {
		int h = 0 < pos.length ? pos[0] : 0;
		if (h >= height)
			return 0;
		
		int[] buf = buffer[h];
		if (buf == null)
			return 0;
		
		int l = length[h];
		int x = 1 < pos.length ? pos[1] : 0;
		if (x >= l)
			return 0;
		
		int ret = buf[x];
		buf[x] = 0;
		return ret;
	}
	
	public int put(int[] pos, int v) {
		int h = 0 < pos.length ? pos[0] : 0;
		ensureHeightIndex(h);
		if (h >= height)
			height = h + 1;
		
		int x = 1 < pos.length ? pos[1] : 0;
		ensureCapacityIndex(h, x);
		if (x >= length[h])
			length[h] = x + 1;
		int[] buf = buffer[h];
		
		int ret = buf[x];
		buf[x] = v;
		return ret;
	}
	
	public void clear() {
		Arrays.fill(buffer, null);
		Arrays.fill(length, 0);
		height = 0;
	}
	
	public IndexMultiMap2DInt copy() {
		return new IndexMultiMap2DInt(buffer.clone(), length.clone(), height);
	}
	
	public void trimAll() {
		trim();
		for (int i = 0; i < height; i++)
			trim(i);
	}
	
	public void trim() {
		if (buffer.length == height)
			return;
		
		int[][] old = buffer;
		buffer = new int[height][];
		System.arraycopy(old, 0, buffer, 0, height);
	}
	
	public void trim(int h) {
		ensureCapacityExists(h);
		int[] old = buffer[h];
		int l = length[h];
		
		if (old.length == l)
			return;
		
		int[] n = new int[l];
		System.arraycopy(old, 0, n, 0, l);
		buffer[h] = n;
	}
}
