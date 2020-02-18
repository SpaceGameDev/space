package space.engine.key;

import org.jetbrains.annotations.NotNull;
import space.engine.baseobject.ToString;
import space.engine.delegate.collection.ConvertingCollection;
import space.engine.indexmap.IndexMap;
import space.engine.indexmap.IndexMapArray;
import space.engine.string.toStringHelper.ToStringHelper;
import space.engine.string.toStringHelper.ToStringHelper.ToStringHelperObjectsInstance;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * every {@link Key} is getting a VALUE, which is <b>INDEPENDENT</b> to the generic V of the {@link Key}
 */
public class KeyMap<VALUE> implements ToString {
	
	public IndexMap<VALUE> map;
	public KeyGenerator<?> gen;
	
	public KeyMap() {
		this(new IndexMapArray<>());
	}
	
	public KeyMap(IndexMap<VALUE> map) {
		this.map = map;
	}
	
	public KeyMap(KeyGenerator<?> gen) {
		this(new IndexMapArray<>(), gen);
	}
	
	public KeyMap(IndexMap<VALUE> map, KeyGenerator<?> gen) {
		this.map = map;
		this.gen = gen;
	}
	
	public void verifyKey(Key<?> key) {
		if (gen != null)
			gen.assertKeyOf(key);
	}
	
	//access
	
	/**
	 * gets the VALUE for a given {@link Key}
	 */
	public VALUE get(@NotNull Key<?> key) {
		verifyKey(key);
		return map.get(key.id);
	}
	
	/**
	 * sets the VALUE for a given {@link Key} to v
	 */
	public VALUE put(@NotNull Key<?> key, VALUE v) {
		verifyKey(key);
		return map.put(key.id, v);
	}
	
	/**
	 * removes (typically sets to null) the VALUE for a given {@link Key}
	 */
	public VALUE remove(@NotNull Key<?> key) {
		verifyKey(key);
		return map.remove(key.id);
	}
	
	/**
	 * gets the key or if it is null returns <code>def</code>
	 */
	@org.jetbrains.annotations.Contract("_,!null->!null")
	public VALUE getOrDefault(@NotNull Key<?> key, VALUE def) {
		verifyKey(key);
		return map.getOrDefault(key.id, def);
	}
	
	/**
	 * sets the VALUE for a given {@link Key} if the {@link Key} is not yet set (typically set to null)
	 */
	@org.jetbrains.annotations.Contract("_,!null->!null")
	public VALUE putIfAbsent(@NotNull Key<?> key, VALUE v) {
		verifyKey(key);
		return map.putIfAbsent(key.id, v);
	}
	
	/**
	 * sets the VALUE for a given {@link Key} if the {@link Key} is not yet set (typically set to null)
	 */
	public VALUE putIfAbsent(@NotNull Key<?> key, @NotNull Supplier<? extends VALUE> v) {
		verifyKey(key);
		return map.computeIfAbsent(key.id, v);
	}
	
	/**
	 * sets the VALUE for a given {@link Key} to <code>newValue</code>, if the current VALUE is equal to <code>oldValue</code>
	 */
	public boolean replace(@NotNull Key<?> key, VALUE oldValue, VALUE newValue) {
		verifyKey(key);
		return map.replace(key.id, oldValue, newValue);
	}
	
	/**
	 * sets the VALUE for a given {@link Key} to <code>newValue</code>, if the current VALUE is equal to <code>oldValue</code>
	 */
	public boolean replace(@NotNull Key<?> key, VALUE oldValue, @NotNull Supplier<? extends VALUE> newValue) {
		verifyKey(key);
		return map.replace(key.id, oldValue, newValue);
	}
	
	/**
	 * removes (typically sets to null) the VALUE for a given {@link Key}, if the current VALUE is equal to <code>v</code>
	 */
	public boolean remove(@NotNull Key<?> key, VALUE v) {
		verifyKey(key);
		return map.remove(key.id, v);
	}
	
	//others
	
	/**
	 * gets the size of this map
	 */
	public int size() {
		return map.size();
	}
	
	/**
	 * clears the map
	 */
	public void clear() {
		map.clear();
	}
	
	/**
	 * a modifiable {@link Collection} containing all VALUEs
	 */
	@NotNull
	public Collection<VALUE> values() {
		return map.values();
	}
	
	/**
	 * a modifiable {@link Collection} with {@link Entry} of all KeyPairs
	 */
	@NotNull
	public Collection<? extends Entry> table() {
		return new ConvertingCollection.BiDirectional<>(map.entrySet(), Entry::new, entry -> map.getEntry(entry.getKey().id));
	}
	
	@NotNull
	@Override
	public <TSHTYPE> TSHTYPE toTSH(@NotNull ToStringHelper<TSHTYPE> api) {
		ToStringHelperObjectsInstance<TSHTYPE> tsh = api.createObjectInstance(this);
		tsh.add("map", this.map);
		tsh.add("gen", this.gen);
		return tsh.build();
	}
	
	@Override
	public String toString() {
		return toString0();
	}
	
	public class Entry {
		
		public IndexMap.Entry<?> entry;
		
		public Entry(IndexMap.Entry<?> entry) {
			this.entry = entry;
		}
		
		@NotNull
		public Key<?> getKey() {
			return gen.getKey(entry.getIndex());
		}
		
		@org.jetbrains.annotations.Nullable
		public Object getValue() {
			return entry.getValue();
		}
	}
}
