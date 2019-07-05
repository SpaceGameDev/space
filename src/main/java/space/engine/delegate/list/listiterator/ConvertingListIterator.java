package space.engine.delegate.list.listiterator;

import org.jetbrains.annotations.NotNull;
import space.engine.baseobject.ToString;
import space.engine.string.toStringHelper.ToStringHelper;
import space.engine.string.toStringHelper.ToStringHelper.ToStringHelperObjectsInstance;

import java.util.ListIterator;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A {@link ConvertingListIterator} converts <b>FROM</b> one {@link ListIterator ListIterator's} Value <b>TO</b> a different value with the help of provided {@link Function Functions} for conversion.<br>
 * It has multiple inner classes allowing for different usages for many different cases.<br>
 * All implementations are threadsafe if their underlying {@link ConvertingListIterator#listIterator} is also threadsafe.<br>
 * <br>
 * 2 Types of Functions:
 * <table border=1>
 * <tr><td>Function</td><td>Remap direction</td><td>Comment</td></tr>
 * <tr><td>{@link ConvertingListIterator.OneDirectionalUnmodifiable#remap Function&lt;? super F, ? extends T&gt; remap}</td><td>F -&gt; T </td><td>Always required.</td></tr>
 * <tr><td>{@link ConvertingListIterator.BiDirectional#reverse Function&lt;? super T, ? extends F&gt; reverse}</td><td>T -&gt; F </td><td>Will be called even for simple Operations, where the Result may not be stored and only used for eg. comparision. <br><b>Implementation-specific: The Result will always be added. </b></td></tr>
 * </table>
 * <br>
 * 2 Sub-Classes for Converting with different Functions:
 * <table border=1>
 *
 * <tr>
 * <td>Class name</td>
 * <td>Modifiable?</td>
 * <td>Required Functions</td>
 * <td>Inefficient Methods</td>
 * <td>Comparision Object</td>
 * </tr>
 *
 * <tr>
 * <td>{@link ConvertingListIterator.OneDirectionalUnmodifiable OneDirectionalUnmodifiable}</td>
 * <td>No</td>
 * <td>
 * <ul>
 * <li>{@link ConvertingListIterator.OneDirectionalUnmodifiable#remap Function&lt;? super F, ? extends T&gt; remap}</li>
 * </ul>
 * </td>
 * <td>
 * <ul><li>none</li></ul>
 * </td>
 * <td>not used</td>
 * </tr>
 *
 * <tr>
 * <td>{@link ConvertingListIterator.BiDirectional BiDirectional}</td>
 * <td>Yes</td>
 * <td>
 * <ul>
 * <li>{@link ConvertingListIterator.BiDirectional#remap Function&lt;? super F, ? extends T&gt; remap}</li>
 * <li>{@link ConvertingListIterator.BiDirectional#reverse Function&lt;? super T, ? extends F&gt; reverse}</li>
 * </ul>
 * </td>
 * <td>
 * <ul><li>none</li></ul>
 * </td>
 * <td>not used</td>
 * </tr>
 *
 * </table>
 * <ul>
 * <li>Inefficient Methods: Methods which are implemented inefficiently and should thus be avoided to be called. Non-marked Methods will only delegate. </li>
 * <li>Comparision Object: Object on which Comparision will be done on. Either on FROM objects or the TO objects.</li>
 * </ul>
 *
 * @param <F> the value to convert <b>FROM</b>
 * @param <T> the value to convert <b>TO</b>
 */
public abstract class ConvertingListIterator<F, T> implements ListIterator<T>, ToString {
	
	public ListIterator<F> listIterator;
	
	public ConvertingListIterator(ListIterator<F> listIterator) {
		this.listIterator = listIterator;
	}
	
	@NotNull
	@Override
	public <TSHTYPE> TSHTYPE toTSH(@NotNull ToStringHelper<TSHTYPE> api) {
		ToStringHelperObjectsInstance<TSHTYPE> tsh = api.createObjectInstance(this);
		tsh.add("listIterator", this.listIterator);
		return tsh.build();
	}
	
	@Override
	public String toString() {
		return toString0();
	}
	
	public static class OneDirectionalUnmodifiable<F, T> extends ConvertingListIterator<F, T> {
		
		public Function<? super F, ? extends T> remap;
		
		public OneDirectionalUnmodifiable(ListIterator<F> listIterator, Function<? super F, ? extends T> remap) {
			super(listIterator);
			this.remap = remap;
		}
		
		@Override
		public boolean hasNext() {
			return listIterator.hasNext();
		}
		
		@Override
		public T next() {
			return remap.apply(listIterator.next());
		}
		
		@Override
		public boolean hasPrevious() {
			return listIterator.hasPrevious();
		}
		
		@Override
		public T previous() {
			return remap.apply(listIterator.previous());
		}
		
		@Override
		public int nextIndex() {
			return listIterator.nextIndex();
		}
		
		@Override
		public int previousIndex() {
			return listIterator.previousIndex();
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException("Unmodifiable");
		}
		
		@Override
		public void set(T t) {
			throw new UnsupportedOperationException("Unmodifiable");
		}
		
		@Override
		public void add(T t) {
			throw new UnsupportedOperationException("Unmodifiable");
		}
		
		@Override
		public void forEachRemaining(Consumer<? super T> action) {
			listIterator.forEachRemaining(f -> action.accept(remap.apply(f)));
		}
		
		@NotNull
		@Override
		public <TSHTYPE> TSHTYPE toTSH(@NotNull ToStringHelper<TSHTYPE> api) {
			ToStringHelperObjectsInstance<TSHTYPE> tsh = api.createObjectInstance(this);
			tsh.add("listIterator", this.listIterator);
			tsh.add("remap", this.remap);
			return tsh.build();
		}
	}
	
	public static class BiDirectional<F, T> extends OneDirectionalUnmodifiable<F, T> {
		
		public Function<? super T, ? extends F> reverse;
		
		public BiDirectional(ListIterator<F> listIterator, Function<? super F, ? extends T> remap, Function<? super T, ? extends F> reverse) {
			super(listIterator, remap);
			this.reverse = reverse;
		}
		
		@Override
		public void remove() {
			listIterator.remove();
		}
		
		@Override
		public void set(T t) {
			listIterator.set(reverse.apply(t));
		}
		
		@Override
		public void add(T t) {
			listIterator.add(reverse.apply(t));
		}
	}
}
