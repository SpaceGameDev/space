package space.engine.delegate.indexmap.entry;

import org.jetbrains.annotations.NotNull;
import space.engine.indexmap.IndexMap.Entry;
import space.engine.string.toStringHelper.ToStringHelper;

public class UnmodifiableEntry<VALUE> extends DelegatingEntry<VALUE> {
	
	public UnmodifiableEntry(Entry<VALUE> entry) {
		super(entry);
	}
	
	@Override
	public void setValue(VALUE v) {
		throw new UnsupportedOperationException("Unmodifiable");
	}
	
	@NotNull
	@Override
	public <TSHTYPE> TSHTYPE toTSH(@NotNull ToStringHelper<TSHTYPE> api) {
		return api.createModifier("unmodifiable", entry);
	}
}
