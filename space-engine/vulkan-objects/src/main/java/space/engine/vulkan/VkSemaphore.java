package space.engine.vulkan;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import space.engine.barrier.Barrier;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.pointer.PointerBufferPointer;
import space.engine.freeable.Cleaner;
import space.engine.freeable.Freeable;

import java.util.function.BiFunction;

import static org.lwjgl.vulkan.VK10.*;
import static space.engine.barrier.Barrier.DONE_BARRIER;
import static space.engine.freeable.Freeable.addIfNotContained;
import static space.engine.lwjgl.LwjglStructAllocator.mallocStruct;
import static space.engine.vulkan.VkException.assertVk;

public interface VkSemaphore extends Freeable {
	
	@SuppressWarnings("unused")
	VkSemaphore[] EMPTY_SEMAPHORE_ARRAY = new VkSemaphore[0];
	
	//allocateDirect
	static @NotNull VkSemaphore[] allocateDirect(@NotNull VkDevice device, int count, @NotNull Object[] parents) {
		try (AllocatorFrame frame = Allocator.frame()) {
			VkSemaphore[] ret = new VkSemaphore[count];
			
			VkFenceCreateInfo info = mallocStruct(frame, VkFenceCreateInfo::create, VkFenceCreateInfo.SIZEOF).set(
					VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO,
					0,
					0
			);
			PointerBufferPointer ptr = PointerBufferPointer.malloc(frame);
			
			for (int i = 0; i < count; i++) {
				assertVk(nvkCreateSemaphore(device, info.address(), 0, ptr.address()));
				ret[i] = new VkSemaphore.Default(ptr.getPointer(), device, VkSemaphore.DestroyStorage::new, parents);
			}
			return ret;
		}
	}
	
	static @NotNull VkSemaphore allocateDirect(@NotNull VkDevice device, @NotNull Object[] parents) {
		try (AllocatorFrame frame = Allocator.frame()) {
			VkFenceCreateInfo info = mallocStruct(frame, VkFenceCreateInfo::create, VkFenceCreateInfo.SIZEOF).set(
					VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO,
					0,
					0
			);
			PointerBufferPointer ptr = PointerBufferPointer.malloc(frame);
			assertVk(nvkCreateSemaphore(device, info.address(), 0, ptr.address()));
			return new VkSemaphore.Default(ptr.getPointer(), device, VkSemaphore.DestroyStorage::new, parents);
		}
	}
	
	//wrap
	static @NotNull VkSemaphore wrap(long address, @NotNull VkDevice device, @NotNull Object[] parents) {
		return new VkSemaphore.Default(address, device, Freeable::createDummy, parents);
	}
	
	//parents
	@NotNull VkDevice device();
	
	//address
	long address();
	
	class Default implements VkSemaphore, CleanerWrapper {
		
		//const
		public Default(long address, @NotNull VkDevice device, @NotNull BiFunction<? super Default, Object[], Freeable> storageCreator, @NotNull Object[] parents) {
			this.device = device;
			this.address = address;
			this.storage = storageCreator.apply(this, addIfNotContained(parents, device));
		}
		
		//parents
		private final @NotNull VkDevice device;
		
		@Override
		public @NotNull VkDevice device() {
			return device;
		}
		
		//address
		private final long address;
		
		@Override
		public long address() {
			return address;
		}
		
		//storage
		private final @NotNull Freeable storage;
		
		@Override
		public @NotNull Freeable getStorage() {
			return storage;
		}
	}
	
	class DestroyStorage extends Cleaner {
		
		private final VkDevice device;
		private final long address;
		
		public DestroyStorage(@NotNull VkSemaphore semaphore, @NotNull Object[] parents) {
			super(semaphore, parents);
			this.device = semaphore.device();
			this.address = semaphore.address();
		}
		
		@Override
		protected @NotNull Barrier handleFree() {
			nvkDestroySemaphore(device, address, 0);
			return DONE_BARRIER;
		}
	}
}
