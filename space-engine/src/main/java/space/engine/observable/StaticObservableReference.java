package space.engine.observable;

import org.jetbrains.annotations.NotNull;
import space.engine.barrier.Barrier;

public class StaticObservableReference<T> extends ObservableReference<T> {
	
	public StaticObservableReference() {
	}
	
	public StaticObservableReference(T initial) {
		super(initial);
	}
	
	public StaticObservableReference(Barrier initialBarrier) {
		super(initialBarrier);
	}
	
	public StaticObservableReference(@NotNull Barrier initialBarrier, T t) {
		super(initialBarrier, t);
	}
}
