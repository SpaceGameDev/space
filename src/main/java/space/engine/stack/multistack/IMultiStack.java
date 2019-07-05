package space.engine.stack.multistack;

import org.jetbrains.annotations.Contract;

/**
 * Works <b><i>exactly</i></b> like a stack, you can {@link IMultiStack#put(Object)} values on it
 * and use the {@link IMultiStack#push()}) and {@link IMultiStack#pop()} operations to add more layers and remove them (with their values).
 * You have the option of just {@link IMultiStack#push()}-ing and {@link IMultiStack#pop()}-ing
 * or you can do a {@link IMultiStack#pushPointer()} and {@link IMultiStack#popPointer(long)}, if you want to ensure the equilibrium of {@link IMultiStack#push()} and {@link IMultiStack#pop()}s manually.
 */
public interface IMultiStack<T> {
	
	@Contract("null -> null; !null -> !null")
	<X extends T> X put(X t);
	
	void push();
	
	long pushPointer();
	
	void pop();
	
	void popPointer(long pointer);
}