package space.engine.stack;

import space.engine.indexmap.IndexMapArray;

import java.util.Arrays;

public class SimpleStack<T> implements Stack<T> {
	
	private int pointer = 0;
	public IndexMapArray<T> list = new IndexMapArray<>();
	
	@Override
	public void push(T t) {
		list.put(pointer++, t);
	}
	
	@Override
	public long pushPointer(T t) {
		int ret = pointer++;
		list.put(ret, t);
		return ret;
	}
	
	@Override
	public T pop() {
		return list.remove(pointer--);
	}
	
	@Override
	public T popPointer(long id) {
		int last = pointer - 1;
		int from = (int) (id + 1);
		if (from > last)
			Arrays.fill(list.array, from, last, null);
		
		return list.remove((int) id);
	}
}
