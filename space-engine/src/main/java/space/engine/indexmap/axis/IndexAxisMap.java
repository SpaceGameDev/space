package space.engine.indexmap.axis;

import org.jetbrains.annotations.Nullable;
import space.engine.indexmap.multi.IndexMultiMap;
import space.engine.indexmap.multi.IndexMultiMap2D;

import java.util.ArrayList;
import java.util.List;

public class IndexAxisMap<VALUE> {
	
	public IndexMultiMap<List<VALUE>> table;
	
	public IndexAxisMap() {
		this(new IndexMultiMap2D<>());
	}
	
	public IndexAxisMap(IndexMultiMap<List<VALUE>> table) {
		this.table = table;
	}
	
	public int size() {
		return table.size();
	}
	
	public int size(int dim) {
		return table.size(new int[] {dim});
	}
	
	public void add(int[] pos, VALUE v) {
		int[] curr = new int[2];
		for (int i = 0; i < pos.length; i++) {
			curr[0] = i;
			curr[1] = pos[i];
			table.putIfAbsent(curr, ArrayList::new).add(v);
		}
	}
	
	@Nullable
	public List<VALUE> get(int dim, int pos) {
		return table.get(new int[] {dim, pos});
	}
	
	public void clear() {
		table.clear();
	}
}
