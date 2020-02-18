package space.engine.delegate.indexmap;

import org.jetbrains.annotations.NotNull;
import space.engine.delegate.collection.ConvertingCollection;
import space.engine.delegate.collection.ModificationAwareCollection;
import space.engine.delegate.indexmap.entry.ModificationAwareEntry;
import space.engine.indexmap.IndexMap;
import space.engine.string.toStringHelper.ToStringHelper;
import space.engine.string.toStringHelper.ToStringHelper.ToStringHelperObjectsInstance;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * The {@link ModificationAwareIndexMap} will call the {@link ModificationAwareIndexMap#onModification} {@link Runnable} when the {@link IndexMap} is modified.
 */
public class ModificationAwareIndexMap<VALUE> extends DelegatingIndexMap<VALUE> {
	
	public Runnable onModification;
	
	public ModificationAwareIndexMap(IndexMap<VALUE> indexMap, Runnable onModification) {
		super(indexMap);
		this.onModification = onModification;
	}
	
	@Override
	public VALUE put(int index, VALUE value) {
		VALUE ret = super.put(index, value);
		onModification.run();
		return ret;
	}
	
	@Override
	public VALUE remove(int index) {
		VALUE ret = super.remove(index);
		onModification.run();
		return ret;
	}
	
	@Override
	public void putAll(@NotNull IndexMap<? extends VALUE> indexMap) {
		super.putAll(indexMap);
		if (!indexMap.isEmpty())
			onModification.run();
	}
	
	@Override
	public void putAllIfAbsent(@NotNull IndexMap<? extends VALUE> indexMap) {
		super.putAllIfAbsent(indexMap);
		if (!indexMap.isEmpty())
			onModification.run();
	}
	
	@Override
	public VALUE putIfAbsent(int index, VALUE value) {
		VALUE ret = super.putIfAbsent(index, value);
		if (ret != value)
			onModification.run();
		return ret;
	}
	
	@Override
	public VALUE putIfPresent(int index, VALUE value) {
		VALUE ret = super.putIfPresent(index, value);
		if (ret != value)
			onModification.run();
		return ret;
	}
	
	@Override
	public boolean remove(int index, VALUE value) {
		boolean ret = super.remove(index, value);
		if (ret)
			onModification.run();
		return ret;
	}
	
	@Override
	public VALUE compute(int index, @NotNull ComputeFunction<? super VALUE, ? extends VALUE> function) {
		VALUE ret = super.compute(index, function);
		onModification.run();
		return ret;
	}
	
	@Override
	public VALUE computeIfAbsent(int index, @NotNull Supplier<? extends VALUE> supplier) {
		boolean[] mod = new boolean[1];
		VALUE ret = super.computeIfAbsent(index, () -> {
			mod[0] = true;
			return supplier.get();
		});
		if (mod[0])
			onModification.run();
		return ret;
	}
	
	@Override
	public boolean replace(int index, VALUE oldValue, VALUE newValue) {
		boolean ret = super.replace(index, oldValue, newValue);
		if (ret)
			onModification.run();
		return ret;
	}
	
	@Override
	public boolean replace(int index, VALUE oldValue, @NotNull Supplier<? extends VALUE> newValue) {
		boolean ret = super.replace(index, oldValue, newValue);
		if (ret)
			onModification.run();
		return ret;
	}
	
	@Override
	public VALUE computeIfPresent(int index, @NotNull Supplier<? extends VALUE> supplier) {
		boolean[] mod = new boolean[1];
		VALUE ret = super.computeIfAbsent(index, () -> {
			mod[0] = true;
			return supplier.get();
		});
		if (mod[0])
			onModification.run();
		return ret;
	}
	
	@Override
	public void clear() {
		super.clear();
		onModification.run();
	}
	
	@NotNull
	@Override
	public Entry<VALUE> getEntry(int index) {
		return new ModificationAwareEntry<>(super.getEntry(index), onModification);
	}
	
	@NotNull
	@Override
	public Collection<VALUE> values() {
		return new ModificationAwareCollection<>(super.values(), onModification);
	}
	
	@NotNull
	@Override
	public Collection<Entry<VALUE>> entrySet() {
		return new ModificationAwareCollection<>(new ConvertingCollection.BiDirectional<>(super.entrySet(),
																						  entry -> new ModificationAwareEntry<>(entry, onModification),
																						  modEntry -> modEntry instanceof ModificationAwareEntry ? ((ModificationAwareEntry<VALUE>) modEntry).entry : null),
												 onModification);
	}
	
	@NotNull
	@Override
	public <TSHTYPE> TSHTYPE toTSH(@NotNull ToStringHelper<TSHTYPE> api) {
		ToStringHelperObjectsInstance<TSHTYPE> tsh = api.createObjectInstance(this);
		tsh.add("indexMap", this.indexMap);
		tsh.add("onModification", this.onModification);
		return tsh.build();
	}
	
	@Override
	public String toString() {
		return toString0();
	}
}
