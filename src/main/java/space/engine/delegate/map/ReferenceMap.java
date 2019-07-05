package space.engine.delegate.map;

import org.jetbrains.annotations.NotNull;
import space.engine.delegate.collection.ReferenceCollection;
import space.engine.delegate.util.ReferenceUtil;

import java.lang.ref.Reference;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

/**
 * Remaps all Entries to a {@link Reference} of type V. These References are created by refCreator in the Constructor.<br>
 * <b>Added References have to be removed manually.</b> Otherwise References may accumulate.
 */
public class ReferenceMap<K, V> extends ConvertingMap.BiDirectionalSparse<K, Reference<? extends V>, V> {
	
	public ReferenceMap(Map<K, Reference<? extends V>> map, Function<? super V, ? extends Reference<? extends V>> refCreator) {
		super(map, ReferenceUtil::getSafe, refCreator);
	}
	
	public void setRefCreator(Function<? super V, ? extends Reference<? extends V>> refCreator) {
		this.reverseSparse = refCreator;
	}
	
	@NotNull
	@Override
	public Collection<V> values() {
		return new ReferenceCollection<>(map.values(), reverseSparse);
	}
}
