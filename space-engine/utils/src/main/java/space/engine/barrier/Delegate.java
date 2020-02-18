package space.engine.barrier;

import space.engine.barrier.future.Future;

/**
 * Interface for delegating specialized Barriers
 *
 * @param <B> BASE: the base class of the specialized {@link Barrier}
 * @param <C> COMPLETABLE: the class of the Completable made for the BASE class
 */
public interface Delegate<B extends Barrier, C extends B> {
	
	/**
	 * Creates a new COMPLETABLE, an Object extends BASE which can be completed by calling {@link #complete(Barrier, Barrier)}
	 *
	 * @return a new COMPLETABLE
	 */
	C createCompletable();
	
	/**
	 * Completes a created Completable. The Completable should be finished with any additional parameters gotten from the #delegate param.
	 * The #delegate will be finished when this function is called so functions like {@link Future#assertGet()} can be called.
	 *
	 * @param completable the completable to complete
	 * @param delegate    the delegate to get any additional required parameters from
	 */
	void complete(C completable, B delegate);
	
	/**
	 * Adds a hook to #delegate to call {@link #complete(Barrier, Barrier)} when it finishes.
	 */
	default void addHookAndComplete(C completable, B delegate) {
		delegate.addHook(() -> complete(completable, delegate));
	}
}
