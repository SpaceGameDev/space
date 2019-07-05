package space.engine.delegate.iterator;

import org.jetbrains.annotations.NotNull;
import space.engine.string.toStringHelper.ToStringHelper;
import space.engine.string.toStringHelper.ToStringHelper.ToStringHelperObjectsInstance;

import java.util.Iterator;

/**
 * The {@link ModificationAwareIterator} will call the {@link ModificationAwareIterator#onModification} {@link Runnable} when the {@link Iterator} is modified.
 */
public class ModificationAwareIterator<E> extends DelegatingIterator<E> {
	
	public Runnable onModification;
	
	public ModificationAwareIterator(Iterator<E> i, Runnable onModification) {
		super(i);
		this.onModification = onModification;
	}
	
	@Override
	public void remove() {
		super.remove();
		onModification.run();
	}
	
	@NotNull
	@Override
	public <TSHTYPE> TSHTYPE toTSH(@NotNull ToStringHelper<TSHTYPE> api) {
		ToStringHelperObjectsInstance<TSHTYPE> tsh = api.createObjectInstance(this);
		tsh.add("iter", this.iter);
		tsh.add("onModification", this.onModification);
		return tsh.build();
	}
	
	@Override
	public String toString() {
		return toString0();
	}
}
