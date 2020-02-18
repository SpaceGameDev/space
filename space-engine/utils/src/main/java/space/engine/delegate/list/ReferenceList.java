package space.engine.delegate.list;

import org.jetbrains.annotations.NotNull;
import space.engine.delegate.iterator.ReferenceIterator;
import space.engine.delegate.list.listiterator.ReferenceListIterator;
import space.engine.delegate.util.ReferenceUtil;

import java.lang.ref.Reference;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Function;

/**
 * Remaps all Entries to a {@link Reference} of type E. These References are created by refCreator in the Constructor.<br>
 * <b>Added References have to be removed manually.</b> Otherwise References may accumulate.
 */
public class ReferenceList<E> extends ConvertingList.BiDirectionalSparse<Reference<? extends E>, E> {
	
	public ReferenceList(List<Reference<? extends E>> list, Function<? super E, ? extends Reference<? extends E>> refCreator) {
		super(list, ReferenceUtil::getSafe, refCreator);
	}
	
	public void setRefCreator(Function<? super E, ? extends Reference<? extends E>> refCreator) {
		this.reverseSparse = refCreator;
	}
	
	@NotNull
	@Override
	public Iterator<E> iterator() {
		return new ReferenceIterator<>(list.iterator());
	}
	
	@NotNull
	@Override
	public ListIterator<E> listIterator() {
		return new ReferenceListIterator<>(list.listIterator(), reverseSparse);
	}
	
	@NotNull
	@Override
	public ListIterator<E> listIterator(int index) {
		return new ReferenceListIterator<>(list.listIterator(index), reverseSparse);
	}
	
	@NotNull
	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		return new ReferenceList<>(list.subList(fromIndex, toIndex), reverseSparse);
	}
}
