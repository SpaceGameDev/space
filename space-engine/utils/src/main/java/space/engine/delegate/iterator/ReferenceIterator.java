package space.engine.delegate.iterator;

import org.jetbrains.annotations.NotNull;
import space.engine.delegate.util.ReferenceUtil;
import space.engine.string.toStringHelper.ToStringHelper;
import space.engine.string.toStringHelper.ToStringHelper.ToStringHelperObjectsInstance;

import java.lang.ref.Reference;
import java.util.Iterator;

/**
 * Remaps all Entries to a {@link Reference} of type E.<br>
 */
public class ReferenceIterator<E> extends ConvertingIterator.OneDirectional<Reference<? extends E>, E> {
	
	public ReferenceIterator(Iterator<Reference<? extends E>> iter) {
		super(iter, ReferenceUtil::getSafe);
	}
	
	@NotNull
	@Override
	public <TSHTYPE> TSHTYPE toTSH(@NotNull ToStringHelper<TSHTYPE> api) {
		ToStringHelperObjectsInstance<TSHTYPE> tsh = api.createObjectInstance(this);
		tsh.add("iter", this.iter);
		return tsh.build();
	}
}
