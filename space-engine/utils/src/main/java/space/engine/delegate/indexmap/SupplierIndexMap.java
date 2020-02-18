package space.engine.delegate.indexmap;

import org.jetbrains.annotations.NotNull;
import space.engine.baseobject.ToString;
import space.engine.indexmap.IndexMap;
import space.engine.string.toStringHelper.ToStringHelper;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * A {@link IndexMap} delegating all calls to it's Field {@link SupplierIndexMap#indexMap}, which is an {@link Supplier} of Type {@link IndexMap}.
 * The {@link Supplier} is provided by Constructor or set directly.
 */
public class SupplierIndexMap<VALUE> implements ToString, IndexMap<VALUE> {
	
	public Supplier<IndexMap<VALUE>> indexMap;
	
	public SupplierIndexMap(Supplier<IndexMap<VALUE>> indexMap) {
		this.indexMap = indexMap;
	}
	
	@Override
	public int size() {
		return indexMap.get().size();
	}
	
	@Override
	public boolean isEmpty() {
		return indexMap.get().isEmpty();
	}
	
	@Override
	public boolean contains(int index) {
		return indexMap.get().contains(index);
	}
	
	@Override
	public VALUE get(int index) {
		return indexMap.get().get(index);
	}
	
	@NotNull
	@Override
	public Entry<VALUE> getEntry(int index) {
		return indexMap.get().getEntry(index);
	}
	
	@Override
	public VALUE put(int index, VALUE value) {
		return indexMap.get().put(index, value);
	}
	
	@Override
	public VALUE remove(int index) {
		return indexMap.get().remove(index);
	}
	
	@Override
	public VALUE[] toArray() {
		return indexMap.get().toArray();
	}
	
	@Override
	public VALUE[] toArray(@NotNull VALUE[] array) {
		return indexMap.get().toArray(array);
	}
	
	@Override
	public VALUE putIfAbsent(int index, VALUE value) {
		return indexMap.get().putIfAbsent(index, value);
	}
	
	@Override
	public VALUE putIfPresent(int index, VALUE value) {
		return indexMap.get().putIfPresent(index, value);
	}
	
	@Override
	public boolean remove(int index, VALUE value) {
		return indexMap.get().remove(index, value);
	}
	
	@Override
	public void putAll(@NotNull IndexMap<? extends VALUE> indexMap) {
		this.indexMap.get().putAll(indexMap);
	}
	
	@Override
	public void putAllIfAbsent(@NotNull IndexMap<? extends VALUE> indexMap) {
		this.indexMap.get().putAllIfAbsent(indexMap);
	}
	
	@Override
	public VALUE getOrDefault(int index, VALUE def) {
		return indexMap.get().getOrDefault(index, def);
	}
	
	@Override
	public VALUE compute(int index, @NotNull ComputeFunction<? super VALUE, ? extends VALUE> function) {
		return indexMap.get().compute(index, function);
	}
	
	@Override
	public VALUE computeIfAbsent(int index, @NotNull Supplier<? extends VALUE> supplier) {
		return indexMap.get().computeIfAbsent(index, supplier);
	}
	
	@Override
	public boolean replace(int index, VALUE oldValue, VALUE newValue) {
		return indexMap.get().replace(index, oldValue, newValue);
	}
	
	@Override
	public boolean replace(int index, VALUE oldValue, @NotNull Supplier<? extends VALUE> newValue) {
		return indexMap.get().replace(index, oldValue, newValue);
	}
	
	@Override
	public VALUE computeIfPresent(int index, @NotNull Supplier<? extends VALUE> supplier) {
		return indexMap.get().computeIfPresent(index, supplier);
	}
	
	@Override
	public void clear() {
		indexMap.get().clear();
	}
	
	@NotNull
	@Override
	public Collection<VALUE> values() {
		return indexMap.get().values();
	}
	
	@NotNull
	@Override
	public Collection<Entry<VALUE>> entrySet() {
		return indexMap.get().entrySet();
	}
	
	@NotNull
	@Override
	public <T> T toTSH(@NotNull ToStringHelper<T> api) {
		return api.createModifier("supplier", indexMap);
	}
	
	@Override
	public String toString() {
		return toString0();
	}
}
