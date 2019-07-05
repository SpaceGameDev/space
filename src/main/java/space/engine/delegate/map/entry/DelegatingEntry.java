package space.engine.delegate.map.entry;

import java.util.Map;
import java.util.Objects;

public class DelegatingEntry<K, V> implements Map.Entry<K, V> {
	
	public Map.Entry<K, V> entry;
	
	public DelegatingEntry(Map.Entry<K, V> entry) {
		this.entry = entry;
	}
	
	@Override
	public K getKey() {
		return entry.getKey();
	}
	
	@Override
	public V getValue() {
		return entry.getValue();
	}
	
	@Override
	public V setValue(V value) {
		return entry.setValue(value);
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(getKey()) ^ Objects.hashCode(getValue());
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Map.Entry))
			return false;
		Map.Entry other = (Map.Entry) obj;
		return Objects.equals(getKey(), other.getKey()) && Objects.equals(getValue(), other.getValue());
	}
}
