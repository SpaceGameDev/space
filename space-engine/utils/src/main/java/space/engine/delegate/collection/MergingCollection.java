package space.engine.delegate.collection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.engine.baseobject.ToString;
import space.engine.delegate.iterator.MergingIterator;
import space.engine.string.toStringHelper.ToStringHelper;
import space.engine.string.toStringHelper.ToStringHelper.ToStringHelperObjectsInstance;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Merges multiple {@link Collection} to one {@link Collection}.
 * <p>
 * Use the {@link MergingCollection#createWithAddCollection(Collection, Collection[])} or the {@link MergingCollection#setAddColl(Collection)} to redirect all {@link Collection#add} and {@link Collection#addAll(Collection)} calls to the supplied {@link MergingCollection#addColl}.
 * If no {@link MergingCollection#addColl} is specified, the Methods {@link Collection#add} and {@link Collection#addAll(Collection)} throw an {@link UnsupportedOperationException}.
 */
public class MergingCollection<E> implements ToString, Collection<E> {
	
	public @Nullable AddCollection<E> addColl;
	public @NotNull Collection<Collection<E>> collections;
	
	@SafeVarargs
	public MergingCollection(Collection<E>... collections) {
		this(List.of(collections));
	}
	
	public MergingCollection(@NotNull Collection<Collection<E>> collections) {
		this.collections = collections;
	}
	
	//setAddColl
	public void setAddColl(@Nullable AddCollection<E> addColl) {
		this.addColl = addColl;
	}
	
	public void setAddColl(@Nullable Collection<E> addColl) {
		this.addColl = addColl == null ? null : addCollectionFromCollection(addColl);
	}
	
	public void removeAddColl() {
		addColl = null;
	}
	
	//size
	@Override
	public int size() {
		int cnt = 0;
		for (Collection<E> e : collections)
			cnt += e.size();
		return cnt;
	}
	
	@Override
	public boolean isEmpty() {
		for (Collection<E> e : collections)
			if (!e.isEmpty())
				return false;
		return true;
	}
	
	//access
	@Override
	public boolean contains(Object o) {
		for (Collection<E> e : collections)
			if (e.contains(o))
				return true;
		return false;
	}
	
	@NotNull
	@Override
	public Iterator<E> iterator() {
		return MergingIterator.fromIterable(collections);
	}
	
	@NotNull
	@Override
	public Object[] toArray() {
		return toArray(new Object[size()]);
	}
	
	@NotNull
	@Override
	@SuppressWarnings("SuspiciousSystemArraycopy")
	public <T> T[] toArray(@NotNull T[] a) {
		int pos = 0;
		for (Collection<E> e : collections) {
			Object[] o = e.toArray();
			int l = o.length;
			System.arraycopy(o, 0, a, pos, l);
			pos += l;
		}
		return a;
	}
	
	@Override
	public boolean add(E e) {
		if (addColl == null)
			throw new UnsupportedOperationException();
		return addColl.add(e);
	}
	
	@Override
	public boolean remove(Object o) {
		for (Collection<E> c : collections)
			if (c.remove(o))
				return true;
		return false;
	}
	
	//all
	@Override
	public boolean containsAll(@NotNull Collection<?> c) {
		for (Collection<E> e : collections)
			if (!e.containsAll(c))
				return false;
		return true;
	}
	
	@Override
	public boolean addAll(@NotNull Collection<? extends E> c) {
		if (addColl == null)
			throw new UnsupportedOperationException();
		return addColl.addAll(c);
	}
	
	@Override
	public boolean removeAll(@NotNull Collection<?> c) {
		boolean ret = false;
		for (Collection<E> e : collections)
			if (e.removeAll(c))
				ret = true;
		return ret;
	}
	
	@Override
	public boolean retainAll(@NotNull Collection<?> c) {
		boolean ret = false;
		for (Collection<E> e : collections)
			if (e.retainAll(c))
				ret = true;
		return ret;
	}
	
	//other
	@Override
	public void clear() {
		for (Collection<E> e : collections)
			e.clear();
	}
	
	@NotNull
	@Override
	public <TSHTYPE> TSHTYPE toTSH(@NotNull ToStringHelper<TSHTYPE> api) {
		ToStringHelperObjectsInstance<TSHTYPE> tsh = api.createObjectInstance(this);
		tsh.add("addColl", this.addColl);
		tsh.add("collections", this.collections);
		return tsh.build();
	}
	
	@Override
	public String toString() {
		return toString0();
	}
	
	//createWithAddCollection
	@SafeVarargs
	public static <E> MergingCollection<E> createWithAddCollection(Collection<E> addColl, Collection<E>... collections) {
		return createWithAddCollection(addColl, List.of(collections));
	}
	
	public static <E> MergingCollection<E> createWithAddCollection(Collection<E> addColl, Collection<Collection<E>> collections) {
		MergingCollection<E> ret = new MergingCollection<>(collections);
		ret.setAddColl(addColl);
		return ret;
	}
	
	@SafeVarargs
	public static <E> MergingCollection<E> createWithAddCollection(AddCollection<E> addColl, Collection<E>... collections) {
		return createWithAddCollection(addColl, List.of(collections));
	}
	
	public static <E> MergingCollection<E> createWithAddCollection(AddCollection<E> addColl, Collection<Collection<E>> collections) {
		MergingCollection<E> ret = new MergingCollection<>(collections);
		ret.setAddColl(addColl);
		return ret;
	}
	
	public static <E> AddCollection<E> addCollectionFromCollection(Collection<E> coll) {
		return new AddCollection<>() {
			@Override
			public boolean add(E e) {
				return coll.add(e);
			}
			
			@Override
			public boolean addAll(Collection<? extends E> c) {
				return coll.addAll(c);
			}
		};
	}
	
	@FunctionalInterface
	public interface AddCollection<E> {
		
		boolean add(E e);
		
		default boolean addAll(Collection<? extends E> c) {
			boolean ret = false;
			for (E e : c)
				if (add(e))
					ret = true;
			return ret;
		}
	}
}
