package space.engine.recourcePool;

import space.engine.freeable.Freeable;

import static space.engine.Empties.EMPTY_OBJECT_ARRAY;

public interface FreeableResourcePool<E extends Freeable> extends ResourcePool<E> {
	
	E allocateParents(Object[] parents);
	
	E[] allocateParents(E[] es, Object[] parents);
	
	@Override
	default E allocate() {
		return allocateParents(EMPTY_OBJECT_ARRAY);
	}
	
	@Override
	default E[] allocate(E[] es) {
		return allocateParents(es, EMPTY_OBJECT_ARRAY);
	}
	
	@Override
	default void release(E e) {
		e.free();
	}
	
	@Override
	default void release(E[] es) {
		for (E e : es)
			e.free();
	}
}
