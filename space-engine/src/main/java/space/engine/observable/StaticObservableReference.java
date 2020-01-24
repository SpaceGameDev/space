package space.engine.observable;

public class StaticObservableReference<T> extends ObservableReference<T> {
	
	public StaticObservableReference(T initial) {
		super(initial);
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
