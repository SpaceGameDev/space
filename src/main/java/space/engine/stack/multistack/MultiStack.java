package space.engine.stack.multistack;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.engine.ArrayUtils;
import space.engine.stack.PointerList;

import java.util.Arrays;
import java.util.function.Consumer;

public class MultiStack<T> implements IMultiStack<T> {
	
	public static final int DEFAULT_START_SIZE = 16;
	
	public int size;
	public T[] array;
	
	@Nullable
	public Consumer<T> onDelete;
	@NotNull
	public PointerList pointerList;
	
	public MultiStack() {
		this(null);
	}
	
	public MultiStack(@Nullable Consumer<T> onDelete) {
		this(DEFAULT_START_SIZE, onDelete);
	}
	
	public MultiStack(int initSize, @Nullable Consumer<T> onDelete) {
		this(initSize, onDelete, new PointerList());
	}
	
	public MultiStack(int initSize, @Nullable Consumer<T> onDelete, @NotNull PointerList pointerList) {
		this.size = 0;
		//noinspection unchecked
		this.array = (T[]) new Object[initSize];
		this.onDelete = onDelete;
		this.pointerList = pointerList;
	}
	
	public void ensureCapacity(int capa) {
		if (array.length < capa) {
			T[] old = array;
			//noinspection unchecked
			array = (T[]) new Object[ArrayUtils.getOptimalArraySizeExpansion(old.length, capa, 1)];
			System.arraycopy(old, 0, array, 0, old.length);
		}
	}
	
	@Override
	@Contract("null -> null; !null -> !null")
	public <X extends T> X put(X t) {
		int pos = size++;
		ensureCapacity(size);
		array[pos] = t;
		return t;
	}
	
	//push
	@Override
	public void push() {
		pointerList.push(size);
	}
	
	@Override
	public long pushPointer() {
		return size;
	}
	
	//pop
	@Override
	public void pop() {
		popPointer(pointerList.pop());
	}
	
	@Override
	public void popPointer(long idl) {
		int id = (int) idl;
		if (onDelete != null)
			Arrays.stream(array, id, size).forEach(onDelete);
		
		Arrays.fill(array, id, size, null);
	}
}
