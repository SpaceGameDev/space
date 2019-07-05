package space.engine.indexmap.axis;

import space.engine.indexmap.multi.IndexMultiMap2DInt;

public class IndexAxisMapInt {
	
	public IndexMultiMap2DInt table;
	
	public IndexAxisMapInt() {
		this(new IndexMultiMap2DInt());
	}
	
	public IndexAxisMapInt(IndexMultiMap2DInt table) {
		this.table = table;
	}
	
	public int size() {
		return table.size();
	}
	
	public int size(int dim) {
		return table.size(new int[] {dim});
	}
	
	public void put(int[] pos, int[] size) {
		if (pos.length != size.length)
			throw new IllegalArgumentException();
		
		int[] curr = new int[2];
		for (int i = 0; i < pos.length; i++) {
			curr[0] = i;
			curr[1] = pos[i];
			if (size[i] > table.get(curr))
				table.put(curr, size[i]);
		}
	}
	
	public int get(int dim, int pos) {
		return table.get(new int[] {dim, pos});
	}
	
	public int getIndex(int dim, int pos) {
		int ret = 0;
		for (int i = 0; i < pos; i++)
			ret += table.get(new int[] {dim, i});
		return ret;
	}
	
	public IndexAxisMapInt copy() {
		return new IndexAxisMapInt(table.copy());
	}
	
	public void clear() {
		table.clear();
	}
}
