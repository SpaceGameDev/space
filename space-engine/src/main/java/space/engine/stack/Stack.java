package space.engine.stack;

/**
 * Works <b><i>similar</i></b> to a stack, you can push() values on it and can retrieve them with pop().
 * You have the option of just push()-ing and pop()-ing
 * or you can do a pushPointer() and popPointer(long), if you want to ensure the equilibrium of push() and pop()s manually.
 */
public interface Stack<T> {
	
	void push(T t);
	
	long pushPointer(T t);
	
	T pop();
	
	T popPointer(long pointer);
}
