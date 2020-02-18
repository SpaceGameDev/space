package space.engine.key;

import org.jetbrains.annotations.NotNull;
import space.engine.delegate.indexmap.UnmodifiableIndexMap;
import space.engine.indexmap.ConcurrentIndexMap;
import space.engine.indexmap.IndexMap;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

public class KeyGenerator<KEYTYPE extends Key<?>> {
	
	private final @NotNull Class<?> keyClass;
	private AtomicInteger counter = new AtomicInteger();
	private IndexMap<KEYTYPE> keymap = new ConcurrentIndexMap<>();
	
	public KeyGenerator(@NotNull Class<?> keyClass) {
		this.keyClass = keyClass;
	}
	
	@SuppressWarnings("unchecked")
	protected int generateNextId(@NotNull Key<?> key) {
		if (!keyClass.isAssignableFrom(key.getClass())) {
			throw new IllegalKeyException(key);
		}
		int id = counter.getAndIncrement();
		keymap.put(id, (KEYTYPE) key);
		return id;
	}
	
	/**
	 * gets the key of an id
	 *
	 * @param id the id of the {@link Key}
	 * @return the {@link Key} associated with the id
	 */
	public KEYTYPE getKey(int id) {
		return keymap.get(id);
	}
	
	/**
	 * checks weather the key is from this generator
	 *
	 * @return true if the key was made by this generator and is valid
	 */
	public boolean isKeyOf(@NotNull Key<?> key) {
		return keyClass.isAssignableFrom(key.getClass()) && keymap.get(key.id) == key;
	}
	
	/**
	 * checks weather the key is from this generator
	 *
	 * @throws IllegalKeyException if the Key was not made by this generator
	 */
	public void assertKeyOf(@NotNull Key<?> key) throws IllegalKeyException {
		if (!isKeyOf(key))
			throw new IllegalKeyException();
	}
	
	/**
	 * gets all keys in an {@link IndexMap} with `key id` -> `key`
	 */
	public @NotNull IndexMap<KEYTYPE> getKeysIndexed() {
		return new UnmodifiableIndexMap<>(keymap);
	}
	
	/**
	 * gets all keys in a collection of keys
	 */
	public @NotNull Collection<KEYTYPE> getKeys() {
		return keymap.values();
	}
}
