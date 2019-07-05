package space.engine.delegate.map.entry;

import java.util.Map.Entry;

public class UnmodifiableEntry<K, V> extends DelegatingEntry<K, V> {
	
	public UnmodifiableEntry(Entry<K, V> entry) {
		super(entry);
	}
	
	@Override
	public V setValue(V value) {
		throw new UnsupportedOperationException("Unmodifiable");
	}
}
