package space.engine.buffer;

import space.engine.buffer.AllocatorStack.AllocatorFrame;

public interface Allocator {
	
	//default allocators
	static Allocator heap() {
		return DefaultAllocators.ALLOCATOR_HEAP;
	}
	
	static AllocatorFrame frame() {
		return DefaultAllocators.ALLOCATOR_STACK.get().frame();
	}
	
	static Allocator noop() {
		return DefaultAllocators.ALLOCATOR_NOOP;
	}
	
	//object
	long malloc(long sizeOf);
	
	long calloc(long sizeOf);
	
	void free(long address);
}
