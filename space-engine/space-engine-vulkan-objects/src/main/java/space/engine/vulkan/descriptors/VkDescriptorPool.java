package space.engine.vulkan.descriptors;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import space.engine.barrier.Barrier;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.array.ArrayBufferLong;
import space.engine.buffer.array.ArrayBufferPointer;
import space.engine.buffer.pointer.PointerBufferLong;
import space.engine.buffer.pointer.PointerBufferPointer;
import space.engine.freeableStorage.Freeable;
import space.engine.freeableStorage.Freeable.FreeableWrapper;
import space.engine.freeableStorage.FreeableStorage;
import space.engine.vulkan.VkDevice;
import space.engine.vulkan.VkInstance;

import java.util.Arrays;
import java.util.function.BiFunction;

import static org.lwjgl.vulkan.VK10.*;
import static space.engine.freeableStorage.Freeable.addIfNotContained;
import static space.engine.lwjgl.LwjglStructAllocator.mallocStruct;
import static space.engine.vulkan.VkException.assertVk;

public class VkDescriptorPool implements FreeableWrapper {
	
	//alloc
	public static @NotNull VkDescriptorPool alloc(VkDescriptorPoolCreateInfo info, @NotNull VkDevice device, @NotNull Object[] parents) {
		try (AllocatorFrame frame = Allocator.frame()) {
			PointerBufferPointer ptr = PointerBufferPointer.malloc(frame);
			assertVk(nvkCreateDescriptorPool(device, info.address(), 0, ptr.address()));
			return create(ptr.getPointer(), device, parents);
		}
	}
	
	//create
	public static @NotNull VkDescriptorPool create(long address, @NotNull VkDevice device, @NotNull Object[] parents) {
		return new VkDescriptorPool(address, device, Storage::new, parents);
	}
	
	public static @NotNull VkDescriptorPool wrap(long address, @NotNull VkDevice device, @NotNull Object[] parents) {
		return new VkDescriptorPool(address, device, Freeable::createDummy, parents);
	}
	
	//const
	public VkDescriptorPool(long address, @NotNull VkDevice device, @NotNull BiFunction<VkDescriptorPool, Object[], Freeable> storageCreator, @NotNull Object[] parents) {
		this.address = address;
		this.device = device;
		this.storage = storageCreator.apply(this, addIfNotContained(parents, device));
	}
	
	//parents
	private final @NotNull VkDevice device;
	
	public VkDevice device() {
		return device;
	}
	
	public VkInstance instance() {
		return device.instance();
	}
	
	//address
	private final long address;
	
	public long address() {
		return address;
	}
	
	//storage
	private final @NotNull Freeable storage;
	
	@Override
	public @NotNull Freeable getStorage() {
		return storage;
	}
	
	public static class Storage extends FreeableStorage {
		
		private final @NotNull VkDevice device;
		private final long address;
		
		public Storage(@NotNull VkDescriptorPool o, @NotNull Object[] parents) {
			super(o, parents);
			this.device = o.device;
			this.address = o.address;
		}
		
		@Override
		protected @NotNull Barrier handleFree() {
			vkDestroyDescriptorPool(device, address, null);
			return Barrier.DONE_BARRIER;
		}
	}
	
	//allocateDescriptorSet
	public VkDescriptorSet allocateDescriptorSetCreate(VkDescriptorSetLayout layout, Object[] parents) {
		try (AllocatorFrame frame = Allocator.frame()) {
			PointerBufferPointer ptr = PointerBufferPointer.malloc(frame);
			nvkAllocateDescriptorSets(
					device,
					mallocStruct(frame, VkDescriptorSetAllocateInfo::create, VkDescriptorSetAllocateInfo.SIZEOF).set(
							VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO,
							0,
							this.address(),
							PointerBufferLong.alloc(frame, layout.address()).nioBuffer()
					).address(),
					ptr.address());
			return VkDescriptorSet.create(ptr.getPointer(), this, parents);
		}
	}
	
	public VkDescriptorSet[] allocateDescriptorSetsCreate(VkDescriptorSetLayout[] layouts, Object[] parents) {
		int width = layouts.length;
		try (AllocatorFrame frame = Allocator.frame()) {
			ArrayBufferPointer ptrs = ArrayBufferPointer.malloc(frame, width);
			nvkAllocateDescriptorSets(
					device,
					mallocStruct(frame, VkDescriptorSetAllocateInfo::create, VkDescriptorSetAllocateInfo.SIZEOF).set(
							VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO,
							0,
							this.address(),
							ArrayBufferLong.alloc(frame, Arrays.stream(layouts).mapToLong(VkDescriptorSetLayout::address).toArray()).nioBuffer()
					).address(),
					ptrs.address());
			return ptrs.stream().mapToObj(ptr -> VkDescriptorSet.create(ptr, this, parents)).toArray(VkDescriptorSet[]::new);
		}
	}
	
	public VkDescriptorSet allocateDescriptorSetWrap(VkDescriptorSetLayout layout, Object[] parents) {
		try (AllocatorFrame frame = Allocator.frame()) {
			PointerBufferPointer ptr = PointerBufferPointer.malloc(frame);
			nvkAllocateDescriptorSets(
					device,
					mallocStruct(frame, VkDescriptorSetAllocateInfo::create, VkDescriptorSetAllocateInfo.SIZEOF).set(
							VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO,
							0,
							this.address(),
							PointerBufferLong.alloc(frame, layout.address()).nioBuffer()
					).address(),
					ptr.address());
			return VkDescriptorSet.wrap(ptr.getPointer(), this, parents);
		}
	}
	
	public VkDescriptorSet[] allocateDescriptorSetsWrap(VkDescriptorSetLayout[] layouts, Object[] parents) {
		int width = layouts.length;
		try (AllocatorFrame frame = Allocator.frame()) {
			ArrayBufferPointer ptrs = ArrayBufferPointer.malloc(frame, width);
			nvkAllocateDescriptorSets(
					device,
					mallocStruct(frame, VkDescriptorSetAllocateInfo::create, VkDescriptorSetAllocateInfo.SIZEOF).set(
							VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO,
							0,
							this.address(),
							ArrayBufferLong.alloc(frame, Arrays.stream(layouts).mapToLong(VkDescriptorSetLayout::address).toArray()).nioBuffer()
					).address(),
					ptrs.address());
			return ptrs.stream().mapToObj(ptr -> VkDescriptorSet.wrap(ptr, this, parents)).toArray(VkDescriptorSet[]::new);
		}
	}
}
