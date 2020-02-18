package space.engine.lwjgl;

import org.junit.Test;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkInstanceCreateInfo.Buffer;
import space.engine.buffer.AbstractBuffer;
import space.engine.buffer.AbstractBuffer.Storage;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.freeable.Freeable;

import static org.junit.Assert.*;
import static space.engine.lwjgl.LwjglStructAllocator.*;

public class LwjglStructAllocatorTest {
	
	@Test
	public void testStructAlloc() {
		try (AllocatorFrame frame = Allocator.frame()) {
			validateStruct(mallocStruct(frame, VkInstanceCreateInfo::create, VkInstanceCreateInfo.SIZEOF), frame);
			VkInstanceCreateInfo calloc = validateStruct(callocStruct(frame, VkInstanceCreateInfo::create, VkInstanceCreateInfo.SIZEOF), frame);
			assertEquals(calloc.enabledExtensionCount(), 0);
			
			VkInstanceCreateInfo wrap = validateStruct(wrapStruct(VkInstanceCreateInfo::create, calloc.address()), Allocator.noop());
			calloc.flags(1);
			assertEquals(wrap.flags(), 1);
		}
	}
	
	private VkInstanceCreateInfo validateStruct(VkInstanceCreateInfo vkInstanceCreateInfo, Allocator allocator) {
		assertNotNull(vkInstanceCreateInfo);
		Freeable freeable = Freeable.getFreeable(vkInstanceCreateInfo);
		assertNotNull(freeable);
		assertTrue(freeable instanceof AbstractBuffer.Storage);
		assertSame(((Storage) freeable).allocator, allocator);
		return vkInstanceCreateInfo;
	}
	
	@Test
	public void testBufferAlloc() {
		try (AllocatorFrame frame = Allocator.frame()) {
			validateBuffer(mallocBuffer(frame, VkInstanceCreateInfo::create, VkInstanceCreateInfo.SIZEOF, 5), 5, frame);
			Buffer calloc = validateBuffer(callocBuffer(frame, VkInstanceCreateInfo::create, VkInstanceCreateInfo.SIZEOF, 6), 6, frame);
			assertEquals(calloc.enabledExtensionCount(), 0);
			
			Buffer wrap = validateBuffer(wrapBuffer(VkInstanceCreateInfo::create, calloc.address(), 3), 3, Allocator.noop());
			calloc.get(2).flags(1);
			assertEquals(wrap.get(2).flags(), 1);
		}
	}
	
	private Buffer validateBuffer(Buffer buffer, int capacity, Allocator allocator) {
		assertNotNull(buffer);
		assertEquals(buffer.capacity(), capacity);
		Freeable freeable = Freeable.getFreeable(buffer);
		assertNotNull(freeable);
		assertTrue(freeable instanceof AbstractBuffer.Storage);
		assertSame(((Storage) freeable).allocator, allocator);
		return buffer;
	}
}
