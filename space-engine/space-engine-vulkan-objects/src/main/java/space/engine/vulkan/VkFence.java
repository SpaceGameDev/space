package space.engine.vulkan;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import space.engine.barrier.Barrier;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.array.ArrayBufferPointer;
import space.engine.buffer.pointer.PointerBufferPointer;
import space.engine.freeableStorage.Freeable;
import space.engine.freeableStorage.FreeableStorage;

import java.util.Arrays;
import java.util.function.BiFunction;

import static org.lwjgl.vulkan.VK10.*;
import static space.engine.barrier.Barrier.DONE_BARRIER;
import static space.engine.freeableStorage.Freeable.addIfNotContained;
import static space.engine.lwjgl.LwjglStructAllocator.mallocStruct;
import static space.engine.vulkan.VkException.assertVk;

public interface VkFence extends Freeable {
	
	@SuppressWarnings("unused")
	VkFence[] EMPTY_FENCE_ARRAY = new VkFence[0];
	
	//allocateDirect
	static @NotNull VkFence[] allocateDirect(@NotNull VkDevice device, int count, @NotNull Object[] parents) {
		try (AllocatorFrame frame = Allocator.frame()) {
			VkFence[] ret = new VkFence[count];
			
			VkFenceCreateInfo info = mallocStruct(frame, VkFenceCreateInfo::create, VkFenceCreateInfo.SIZEOF).set(
					VK_STRUCTURE_TYPE_FENCE_CREATE_INFO,
					0,
					0
			);
			PointerBufferPointer ptr = PointerBufferPointer.malloc(frame);
			
			for (int i = 0; i < count; i++) {
				assertVk(nvkCreateFence(device, info.address(), 0, ptr.address()));
				ret[i] = new VkFence.Default(ptr.getPointer(), device, VkFence.DestroyStorage::new, parents);
			}
			return ret;
		}
	}
	
	static @NotNull VkFence allocateDirect(@NotNull VkDevice device, @NotNull Object[] parents) {
		try (AllocatorFrame frame = Allocator.frame()) {
			VkFenceCreateInfo info = mallocStruct(frame, VkFenceCreateInfo::create, VkFenceCreateInfo.SIZEOF).set(
					VK_STRUCTURE_TYPE_FENCE_CREATE_INFO,
					0,
					0
			);
			PointerBufferPointer ptr = PointerBufferPointer.malloc(frame);
			assertVk(nvkCreateFence(device, info.address(), 0, ptr.address()));
			return new Default(ptr.getPointer(), device, DestroyStorage::new, parents);
		}
	}
	
	//wrap
	static @NotNull VkFence wrap(long address, @NotNull VkDevice device, @NotNull Object[] parents) {
		return new VkFence.Default(address, device, Freeable::createDummy, parents);
	}
	
	//parents
	@NotNull VkDevice device();
	
	//address
	long address();
	
	//reset
	default void reset() {
		try (AllocatorFrame frame = Allocator.frame()) {
			PointerBufferPointer fenceBuffer = PointerBufferPointer.alloc(frame, address());
			assertVk(nvkResetFences(device(), 1, fenceBuffer.address()));
		}
	}
	
	static void resetFences(VkFence... fences) {
		try (AllocatorFrame frame = Allocator.frame()) {
			ArrayBufferPointer fenceBuffer = ArrayBufferPointer.alloc(frame, Arrays.stream(fences).mapToLong(VkFence::address).toArray());
			assertVk(nvkResetFences(fences[0].device(), (int) fenceBuffer.length(), fenceBuffer.address()));
		}
	}
	
	//getStatus
	default boolean getStatus() {
		return assertVk(vkGetFenceStatus(device(), address())) == VK_SUCCESS;
	}
	
	class Default implements VkFence, FreeableWrapper {
		
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
	
	class DestroyStorage extends FreeableStorage {
		
		private final VkDevice device;
		private final long address;
		
		public DestroyStorage(@NotNull VkFence fence, @NotNull Object[] parents) {
			super(fence, parents);
			this.device = fence.device();
			this.address = fence.address();
		}
		
		@Override
		protected @NotNull Barrier handleFree() {
			nvkDestroyFence(device, address, 0);
			return DONE_BARRIER;
		}
	}
}
