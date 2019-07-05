package space.engine.delegate.collection;

import org.jetbrains.annotations.NotNull;
import space.engine.delegate.util.ReferenceUtil;
import space.engine.string.toStringHelper.ToStringHelper;
import space.engine.string.toStringHelper.ToStringHelper.ToStringHelperObjectsInstance;

import java.lang.ref.Reference;
import java.util.Collection;
import java.util.function.Function;

/**
 * Remaps all Entries to a {@link Reference} of type E. These References are created by refCreator in the Constructor.<br>
 * <b>Added References have to be removed manually.</b> Otherwise References may accumulate.
 */
public class ReferenceCollection<E> extends ConvertingCollection.BiDirectionalSparse<Reference<? extends E>, E> {
	
	public ReferenceCollection(Collection<Reference<? extends E>> coll, Function<? super E, ? extends Reference<? extends E>> refCreator) {
		super(coll, ReferenceUtil::getSafe, refCreator);
	}
	
	public void setRefCreator(Function<? super E, ? extends Reference<? extends E>> refCreator) {
		this.reverseSparse = refCreator;
	}
	
	@NotNull
	@Override
	public <TSHTYPE> TSHTYPE toTSH(@NotNull ToStringHelper<TSHTYPE> api) {
		ToStringHelperObjectsInstance<TSHTYPE> tsh = api.createObjectInstance(this);
		tsh.add("coll", this.coll);
		tsh.add("refCreator", this.reverseSparse);
		return tsh.build();
	}
}
