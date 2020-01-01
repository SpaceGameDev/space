package space.engine.barrier.entity;

import space.engine.barrier.Barrier;
import space.engine.barrier.Delegate;
import space.engine.barrier.functions.ConsumerWithDelay;
import space.engine.barrier.functions.FunctionWithDelay;
import space.engine.barrier.functions.StarterWithParameter;
import space.engine.barrier.future.Future;

import static space.engine.barrier.Barrier.*;

public abstract class EntityAccessKey<E extends Entity> implements AutoCloseable {
	
	//invalid
	private boolean invalid;
	
	public void checkValid() throws IllegalStateException {
		if (invalid)
			throw new IllegalStateException("EntityAccess was invalidated!");
	}
	
	@Override
	public void close() {
		invalid = true;
	}
	
	//startOn
	public Barrier startOn(StarterWithParameter<?, E> runnable) {
		return startOn(runnable, Barrier.delegate());
	}
	
	public <B extends Barrier, C extends B> B startOn(StarterWithParameter<? extends B, E> runnable, Delegate<B, C> delegate) {
		checkValid();
		return startOn0(runnable, delegate);
	}
	
	protected abstract <B extends Barrier, C extends B> B startOn0(StarterWithParameter<? extends B, E> runnable, Delegate<B, C> delegate);
	
	//startOn wrappers
	public Barrier runOn(ConsumerWithDelay<E> runnable) {
		return startOn(e -> nowRun(() -> runnable.accept(e)));
	}
	
	public <R> Future<R> futureOn(FunctionWithDelay<E, R> runnable) {
		return startOn(e -> nowFuture(() -> runnable.apply(e)), Future.delegate());
	}
}
