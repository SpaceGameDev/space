package space.engine.delegate.map.entry;

import java.util.Map.Entry;

public class ModificationAwareEntry<K, V> extends DelegatingEntry<K, V> {
	
	public Runnable onModification;
	
	public ModificationAwareEntry(Entry<K, V> entry) {
		super(entry);
	}
	
	@Override
	public V setValue(V value) {
		V ret = super.setValue(value);
		onModification.run();
		return ret;
	}
}
