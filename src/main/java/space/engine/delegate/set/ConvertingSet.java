package space.engine.delegate.set;

import space.engine.delegate.collection.ConvertingCollection;

import java.util.Set;
import java.util.function.Function;

public class ConvertingSet {
	
	public static class OneDirectionalUnmodifiable<F, T> extends ConvertingCollection.OneDirectionalUnmodifiable<F, T> implements Set<T> {
		
		public OneDirectionalUnmodifiable(Set<F> coll, Function<? super F, ? extends T> remap) {
			super(coll, remap);
		}
	}
	
	public static class BiDirectionalUnmodifiable<F, T> extends ConvertingCollection.BiDirectionalUnmodifiable<F, T> implements Set<T> {
		
		public BiDirectionalUnmodifiable(Set<F> coll, Function<? super F, ? extends T> remap, Function<? super T, ? extends F> reverse) {
			super(coll, remap, reverse);
		}
	}
	
	public static class BiDirectionalSparse<F, T> extends ConvertingCollection.BiDirectionalSparse<F, T> implements Set<T> {
		
		public BiDirectionalSparse(Set<F> coll, Function<? super F, ? extends T> remap, Function<? super T, ? extends F> reverseSparse) {
			super(coll, remap, reverseSparse);
		}
	}
	
	public static class BiDirectional<F, T> extends ConvertingCollection.BiDirectional<F, T> implements Set<T> {
		
		public BiDirectional(Set<F> coll, Function<? super F, ? extends T> remap, Function<? super T, ? extends F> reverse) {
			super(coll, remap, reverse);
		}
		
		public BiDirectional(Set<F> coll, Function<? super F, ? extends T> remap, Function<? super T, ? extends F> reverse, Function<? super T, ? extends F> reverseSparse) {
			super(coll, remap, reverse, reverseSparse);
		}
	}
}
