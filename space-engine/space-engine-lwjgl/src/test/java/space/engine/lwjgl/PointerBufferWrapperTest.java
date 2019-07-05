package space.engine.lwjgl;

import org.junit.Assert;
import org.junit.Test;
import org.lwjgl.PointerBuffer;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.array.ArrayBufferPointer;
import space.engine.buffer.pointer.PointerBufferPointer;
import space.engine.freeableStorage.Freeable;

import static org.junit.Assert.assertEquals;
import static space.engine.Empties.EMPTY_OBJECT_ARRAY;

public class PointerBufferWrapperTest {
	
	@Test
	public void testWrapPointerBufferPointer() {
		try (AllocatorFrame frame = Allocator.frame()) {
			PointerBufferPointer calloc = PointerBufferPointer.calloc(frame, EMPTY_OBJECT_ARRAY);
			calloc.putPointer(42);
			PointerBuffer wrap = PointerBufferWrapper.wrapPointer(calloc);
			assertEquals(wrap.capacity(), 1);
			assertEquals(wrap.get(0), 42);
			Assert.assertNotNull(Freeable.getFreeable(wrap));
		}
	}
	
	@Test
	public void testWrapArrayBufferPointer() {
		try (AllocatorFrame frame = Allocator.frame()) {
			ArrayBufferPointer calloc = ArrayBufferPointer.calloc(frame, 5, EMPTY_OBJECT_ARRAY);
			calloc.putPointer(2, 42);
			assertEquals(0, calloc.getPointer(0));
			assertEquals(42, calloc.getPointer(2));
			PointerBuffer wrap = PointerBufferWrapper.wrapPointer(calloc);
			assertEquals(wrap.capacity(), 5);
			assertEquals(0, wrap.get(0));
			assertEquals(42, wrap.get(2));
			Assert.assertNotNull(Freeable.getFreeable(wrap));
		}
	}
}
