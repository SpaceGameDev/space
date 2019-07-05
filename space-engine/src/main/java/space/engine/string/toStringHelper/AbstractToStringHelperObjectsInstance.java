package space.engine.string.toStringHelper;

import org.jetbrains.annotations.NotNull;
import space.engine.string.toStringHelper.ToStringHelper.ToStringHelperObjectsInstance;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractToStringHelperObjectsInstance<T> implements ToStringHelperObjectsInstance<T> {
	
	public Object object;
	public ToStringHelper<T> helper;
	public List<Entry<T>> list = new ArrayList<>();
	
	public AbstractToStringHelperObjectsInstance(Object object, ToStringHelper<T> helper) {
		this.object = object;
		this.helper = helper;
	}
	
	@Override
	public abstract T build();
	
	@Override
	public String toString() {
		return build().toString();
	}
	
	//native
	@Override
	public void add(@NotNull String name, byte obj) {
		list.add(new Entry<>(name, helper.toString(obj)));
	}
	
	@Override
	public void add(@NotNull String name, short obj) {
		list.add(new Entry<>(name, helper.toString(obj)));
	}
	
	@Override
	public void add(@NotNull String name, int obj) {
		list.add(new Entry<>(name, helper.toString(obj)));
	}
	
	@Override
	public void add(@NotNull String name, long obj) {
		list.add(new Entry<>(name, helper.toString(obj)));
	}
	
	@Override
	public void add(@NotNull String name, float obj) {
		list.add(new Entry<>(name, helper.toString(obj)));
	}
	
	@Override
	public void add(@NotNull String name, double obj) {
		list.add(new Entry<>(name, helper.toString(obj)));
	}
	
	@Override
	public void add(@NotNull String name, boolean obj) {
		list.add(new Entry<>(name, helper.toString(obj)));
	}
	
	@Override
	public void add(@NotNull String name, char obj) {
		list.add(new Entry<>(name, helper.toString(obj)));
	}
	
	//array
	@Override
	public void add(@NotNull String name, byte[] obj, int from, int to) {
		list.add(new Entry<>(name, helper.toString(obj, from, to)));
	}
	
	@Override
	public void add(@NotNull String name, short[] obj, int from, int to) {
		list.add(new Entry<>(name, helper.toString(obj, from, to)));
	}
	
	@Override
	public void add(@NotNull String name, int[] obj, int from, int to) {
		list.add(new Entry<>(name, helper.toString(obj, from, to)));
	}
	
	@Override
	public void add(@NotNull String name, long[] obj, int from, int to) {
		list.add(new Entry<>(name, helper.toString(obj, from, to)));
	}
	
	@Override
	public void add(@NotNull String name, float[] obj, int from, int to) {
		list.add(new Entry<>(name, helper.toString(obj, from, to)));
	}
	
	@Override
	public void add(@NotNull String name, double[] obj, int from, int to) {
		list.add(new Entry<>(name, helper.toString(obj, from, to)));
	}
	
	@Override
	public void add(@NotNull String name, boolean[] obj, int from, int to) {
		list.add(new Entry<>(name, helper.toString(obj, from, to)));
	}
	
	@Override
	public void add(@NotNull String name, char[] obj, int from, int to) {
		list.add(new Entry<>(name, helper.toString(obj, from, to)));
	}
	
	//object
	@Override
	public void add(@NotNull String name, Object obj) {
		list.add(new Entry<>(name, helper.toString(obj)));
	}
	
	@Override
	public void add(@NotNull String name, Object[] obj, int from, int to) {
		list.add(new Entry<>(name, helper.toString(obj, from, to)));
	}
	
	@Override
	public void addNull(@NotNull String name) {
		list.add(new Entry<>(name, helper.toStringNull()));
	}
	
	public static class Entry<T> {
		
		public String name;
		public T value;
		
		public Entry(String name, T value) {
			this.name = name;
			this.value = value;
		}
		
		@Override
		public String toString() {
			return name + ": " + value;
		}
	}
}
