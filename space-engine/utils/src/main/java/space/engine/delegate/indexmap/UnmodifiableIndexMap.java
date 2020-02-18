package space.engine.delegate.indexmap;

import org.jetbrains.annotations.NotNull;
import space.engine.delegate.collection.ConvertingCollection;
import space.engine.delegate.collection.UnmodifiableCollection;
import space.engine.delegate.indexmap.entry.UnmodifiableEntry;
import space.engine.indexmap.IndexMap;
import space.engine.string.toStringHelper.ToStringHelper;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * The {@link UnmodifiableIndexMap} makes the {@link IndexMap} unmodifiable.
 */
public class UnmodifiableIndexMap<VALUE> extends DelegatingIndexMap<VALUE> {
	
	public UnmodifiableIndexMap(IndexMap<VALUE> indexMap) {
		super(indexMap);
	}
	
	@NotNull
	@Override
	public Entry<VALUE> getEntry(int index) {
		return new UnmodifiableEntry<>(indexMap.getEntry(index));
	}
	
	@Override
	public VALUE put(int index, VALUE value) {
		throw new UnsupportedOperationException("Unmodifiable");
	}
	
	@Override
	public VALUE remove(int index) {
		throw new UnsupportedOperationException("Unmodifiable");
	}
	
	@Override
	public void putAll(@NotNull IndexMap<? extends VALUE> indexMap) {
		throw new UnsupportedOperationException("Unmodifiable");
	}
	
	@Override
	public void putAllIfAbsent(@NotNull IndexMap<? extends VALUE> indexMap) {
		throw new UnsupportedOperationException("Unmodifiable");
	}
	
	@Override
	public VALUE putIfAbsent(int index, VALUE value) {
		throw new UnsupportedOperationException("Unmodifiable");
	}
	
	@Override
	public VALUE putIfPresent(int index, VALUE value) {
		throw new UnsupportedOperationException("Unmodifiable");
	}
	
	@Override
	public boolean remove(int index, VALUE value) {
		throw new UnsupportedOperationException("Unmodifiable");
	}
	
	@Override
	public VALUE compute(int index, @NotNull ComputeFunction<? super VALUE, ? extends VALUE> function) {
		throw new UnsupportedOperationException("Unmodifiable");
	}
	
	@Override
	public VALUE computeIfAbsent(int index, @NotNull Supplier<? extends VALUE> supplier) {
		throw new UnsupportedOperationException("Unmodifiable");
	}
	
	@Override
	public boolean replace(int index, VALUE oldValue, VALUE newValue) {
		throw new UnsupportedOperationException("Unmodifiable");
	}
	
	@Override
	public boolean replace(int index, VALUE oldValue, @NotNull Supplier<? extends VALUE> newValue) {
		throw new UnsupportedOperationException("Unmodifiable");
	}
	
	@Override
	public VALUE computeIfPresent(int index, @NotNull Supplier<? extends VALUE> supplier) {
		throw new UnsupportedOperationException("Unmodifiable");
	}
	
	@Override
	public void clear() {
		throw new UnsupportedOperationException("Unmodifiable");
	}
	
	@NotNull
	@Override
	public Collection<VALUE> values() {
		return new UnmodifiableCollection<>(super.values());
	}
	
	@NotNull
	@Override
	public Collection<Entry<VALUE>> entrySet() {
		return new ConvertingCollection.BiDirectionalUnmodifiable<>(super.entrySet(), UnmodifiableEntry::new, entry -> indexMap.getEntry(entry.getIndex()));
	}
	
	@NotNull
	@Override
	public <T> T toTSH(@NotNull ToStringHelper<T> api) {
		return api.createModifier("unmodifiable", indexMap);
	}
}
