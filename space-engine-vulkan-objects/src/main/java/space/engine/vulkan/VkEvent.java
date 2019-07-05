package space.engine.vulkan;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.pointer.PointerBufferPointer;
import space.engine.freeableStorage.Freeable;
import space.engine.freeableStorage.FreeableStorage;
import space.engine.sync.barrier.Barrier;

import java.util.function.BiFunction;

import static org.lwjgl.vulkan.VK10.*;
import static space.engine.freeableStorage.Freeable.addIfNotContained;
import static space.engine.lwjgl.LwjglStructAllocator.mallocStruct;
import static space.engine.sync.barrier.Barrier.ALWAYS_TRIGGERED_BARRIER;
import static space.engine.vulkan.VkException.assertVk;

public interface VkEvent extends Freeable {
	
	@SuppressWarnings("unused")
	VkEvent[] EMPTY_EVENT_ARRAY = new VkEvent[0];
	
	//allocateDirect
	static @NotNull VkEvent[] allocateDirect(@NotNull VkDevice device, int count, @NotNull Object[] parents) {
		try (AllocatorFrame frame = Allocator.frame()) {
			VkEvent[] ret = new VkEvent[count];
			
			VkFenceCreateInfo info = mallocStruct(frame, VkFenceCreateInfo::create, VkFenceCreateInfo.SIZEOF).set(
					VK_STRUCTURE_TYPE_FENCE_CREATE_INFO,
					0,
					0
			);
			PointerBufferPointer ptr = PointerBufferPointer.malloc(frame);
			
			for (int i = 0; i < count; i++) {
				assertVk(nvkCreateEvent(device, info.address(), 0, ptr.address()));
				ret[i] = new VkEvent.Default(ptr.getPointer(), device, VkEvent.DestroyStorage::new, parents);
			}
			return ret;
		}
	}
	
	static @NotNull VkEvent allocateDirect(@NotNull VkDevice device, @NotNull Object[] parents) {
		try (AllocatorFrame frame = Allocator.frame()) {
			VkFenceCreateInfo info = mallocStruct(frame, VkFenceCreateInfo::create, VkFenceCreateInfo.SIZEOF).set(
					VK_STRUCTURE_TYPE_FENCE_CREATE_INFO,
					0,
					0
			);
			PointerBufferPointer ptr = PointerBufferPointer.malloc(frame);
			assertVk(nvkCreateEvent(device, info.address(), 0, ptr.address()));
			return new VkEvent.Default(ptr.getPointer(), device, VkEvent.DestroyStorage::new, parents);
		}
	}
	
	//wrap
	static @NotNull VkEvent wrap(long address, @NotNull VkDevice device, @NotNull Object[] parents) {
		return new VkEvent.Default(address, device, Freeable::createDummy, parents);
	}
	
	//parents
	@NotNull VkDevice device();
	
	//address
	long address();
	
	//set
	default void set() {
		assertVk(vkSetEvent(device(), address()));
	}
	
	default void cmdSet(VkCommandBuffer cmd, int stageMask) {
		vkCmdSetEvent(cmd, address(), stageMask);
	}
	
	//reset
	default void reset() {
		assertVk(vkResetEvent(device(), address()));
	}
	
	default void cmdReset(VkCommandBuffer cmd, int stageMask) {
		vkCmdResetEvent(cmd, address(), stageMask);
	}
	
	//getStatus
	default boolean getStatus() {
		return assertVk(vkGetEventStatus(device(), address())) == VK_EVENT_SET;
	}
	
	class Default implements VkEvent, FreeableWrapper {
		
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
		
		public DestroyStorage(@NotNull VkEvent event, @NotNull Object[] parents) {
			super(event, parents);
			this.device = event.device();
			this.address = event.address();
		}
		
		@Override
		protected @NotNull Barrier handleFree() {
			nvkDestroyEvent(device, address, 0);
			return ALWAYS_TRIGGERED_BARRIER;
		}
	}
}
