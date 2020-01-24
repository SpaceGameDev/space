package space.engine.observable;

import space.engine.barrier.functions.SupplierWithDelay;

public class StaticObservableReference<T> extends ObservableReference<T> {
	
	public StaticObservableReference(T initial) {
		super(initial);
	}
	
	public StaticObservableReference(SupplierWithDelay<T> supplier) {
		ordering.next(prev -> prev.thenStartCancelable(
				canceledCheck -> setInternalAlways(p -> supplier.get())
		));
	}
	
	public StaticObservableReference(Generator<T> supplier) {
		ordering.next(prev -> prev.thenStartCancelable(
				canceledCheck -> setInternalAlways(supplier)
		));
	}
	
	public StaticObservableReference(GeneratorWithCancelCheck<T> supplier) {
		ordering.next(prev -> prev.thenStartCancelable(
				canceledCheck -> setInternalAlways(supplier, canceledCheck)
		));
	}
}
