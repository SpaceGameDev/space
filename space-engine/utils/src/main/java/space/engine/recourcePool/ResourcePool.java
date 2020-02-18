package space.engine.recourcePool;

public interface ResourcePool<E> {
	
	E allocate();
	
	E[] allocate(E[] es);
	
	void release(E e);
	
	void release(E[] es);
}
