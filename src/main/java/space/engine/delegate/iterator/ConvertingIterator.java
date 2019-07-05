package space.engine.delegate.iterator;

import org.jetbrains.annotations.NotNull;
import space.engine.baseobject.ToString;
import space.engine.string.toStringHelper.ToStringHelper;
import space.engine.string.toStringHelper.ToStringHelper.ToStringHelperObjectsInstance;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A {@link ConvertingIterator} converts <b>FROM</b> one {@link Iterator Iterator's} Value <b>TO</b> a different value with the help of provided {@link Function Functions} for conversion.<br>
 * It has multiple inner classes allowing for different usages for many different cases.<br>
 * All implementations are threadsafe if their underlying {@link ConvertingIterator#iter} is also threadsafe.<br>
 * <br>
 * 1 Types of Functions:
 * <table border=1>
 * <tr><td>Function</td><td>Remap direction</td><td>Comment</td></tr>
 * <tr><td>{@link ConvertingIterator.OneDirectional#remap Function&lt;? super F, ? extends T&gt; remap}</td><td>F -&gt; T </td><td>Always required.</td></tr>
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
 * <td>{@link ConvertingIterator.OneDirectionalUnmodifiable OneDirectionalUnmodifiable}</td>
 * <td>No</td>
 * <td>
 * <ul>
 * <li>{@link ConvertingIterator.OneDirectionalUnmodifiable#remap Function&lt;? super F, ? extends T&gt; remap}</li>
 * </ul>
 * </td>
 * <td>
 * <ul><li>none</li></ul>
 * </td>
 * <td>not used</td>
 * </tr>
 *
 * <tr>
 * <td>{@link ConvertingIterator.OneDirectional OneDirectional}</td>
 * <td>Yes</td>
 * <td>
 * <ul>
 * <li>{@link ConvertingIterator.OneDirectional#remap Function&lt;? super F, ? extends T&gt; remap}</li>
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
 * <li>Comparision Object: Not used here</li>
 * </ul>
 *
 * @param <F> the value to convert <b>FROM</b>
 * @param <T> the value to convert <b>TO</b>
 */
public abstract class ConvertingIterator<F, T> implements Iterator<T>, ToString {
	
	public Iterator<F> iter;
	
	public ConvertingIterator(Iterator<F> iter) {
		this.iter = iter;
	}
	
	@NotNull
	@Override
	public <TSHTYPE> TSHTYPE toTSH(@NotNull ToStringHelper<TSHTYPE> api) {
		ToStringHelperObjectsInstance<TSHTYPE> tsh = api.createObjectInstance(this);
		tsh.add("iter", this.iter);
		return tsh.build();
	}
	
	@Override
	public String toString() {
		return toString0();
	}
	
	public static <F, T> OneDirectionalUnmodifiable<F, T> createConverterOneDirectionalUnmodifiable(Iterator<F> iter, Function<? super F, ? extends T> remap) {
		return new OneDirectionalUnmodifiable<>(iter, remap);
	}
	
	public static <F, T> OneDirectional<F, T> createConverterOneDirectional(Iterator<F> iter, Function<? super F, ? extends T> remap) {
		return new OneDirectional<>(iter, remap);
	}
	
	public static class OneDirectionalUnmodifiable<F, T> extends ConvertingIterator<F, T> {
		
		public Function<? super F, ? extends T> remap;
		
		public OneDirectionalUnmodifiable(Iterator<F> iter, Function<? super F, ? extends T> remap) {
			super(iter);
			this.remap = remap;
		}
		
		@Override
		public boolean hasNext() {
			return iter.hasNext();
		}
		
		@Override
		public T next() {
			return remap.apply(iter.next());
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException("Unmodifiable");
		}
		
		@Override
		public void forEachRemaining(Consumer<? super T> action) {
			iter.forEachRemaining(f -> action.accept(remap.apply(f)));
		}
		
		@NotNull
		@Override
		public <TSHTYPE> TSHTYPE toTSH(@NotNull ToStringHelper<TSHTYPE> api) {
			ToStringHelperObjectsInstance<TSHTYPE> tsh = api.createObjectInstance(this);
			tsh.add("iter", this.iter);
			tsh.add("remap", this.remap);
			return tsh.build();
		}
	}
	
	public static class OneDirectional<F, T> extends OneDirectionalUnmodifiable<F, T> {
		
		public OneDirectional(Iterator<F> iter, Function<? super F, ? extends T> remap) {
			super(iter, remap);
		}
		
		@Override
		public void remove() {
			iter.remove();
		}
	}
}
