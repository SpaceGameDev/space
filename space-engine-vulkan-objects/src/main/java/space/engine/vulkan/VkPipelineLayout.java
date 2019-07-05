package space.engine.vulkan;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.array.ArrayBufferLong;
import space.engine.buffer.pointer.PointerBufferPointer;
import space.engine.freeableStorage.Freeable;
import space.engine.freeableStorage.Freeable.FreeableWrapper;
import space.engine.freeableStorage.FreeableStorage;
import space.engine.sync.barrier.Barrier;
import space.engine.vulkan.descriptors.VkDescriptorSetLayout;

import java.util.Arrays;
import java.util.function.BiFunction;

import static org.lwjgl.vulkan.VK10.*;
import static space.engine.freeableStorage.Freeable.addIfNotContained;
import static space.engine.lwjgl.LwjglStructAllocator.mallocStruct;
import static space.engine.vulkan.VkException.assertVk;

public class VkPipelineLayout implements FreeableWrapper {
	
	//alloc
	public static @NotNull VkPipelineLayout alloc(@NotNull VkDevice device, @NotNull VkDescriptorSetLayout[] descriptorSetLayouts, @NotNull Object[] parents) {
		try (AllocatorFrame frame = Allocator.frame()) {
			return alloc(mallocStruct(frame, VkPipelineLayoutCreateInfo::create, VkPipelineLayoutCreateInfo.SIZEOF).set(
					VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO,
					0,
					0,
					ArrayBufferLong.alloc(frame, Arrays.stream(descriptorSetLayouts).mapToLong(VkDescriptorSetLayout::address).toArray()).nioBuffer(),
					null
			), device, descriptorSetLayouts, parents);
		}
	}
	
	public static @NotNull VkPipelineLayout alloc(VkPipelineLayoutCreateInfo info, @NotNull VkDevice device, @NotNull VkDescriptorSetLayout[] descriptorSetLayouts, @NotNull Object[] parents) {
		try (AllocatorFrame frame = Allocator.frame()) {
			PointerBufferPointer ptr = PointerBufferPointer.malloc(frame);
			assertVk(nvkCreatePipelineLayout(device, info.address(), 0, ptr.address()));
			return create(ptr.getPointer(), device, descriptorSetLayouts, parents);
		}
	}
	
	//create
	public static @NotNull VkPipelineLayout create(long address, @NotNull VkDevice device, @NotNull VkDescriptorSetLayout[] descriptorSetLayouts, @NotNull Object[] parents) {
		return new VkPipelineLayout(address, device, descriptorSetLayouts, Storage::new, parents);
	}
	
	public static @NotNull VkPipelineLayout wrap(long address, @NotNull VkDevice device, @NotNull VkDescriptorSetLayout[] descriptorSetLayouts, @NotNull Object[] parents) {
		return new VkPipelineLayout(address, device, descriptorSetLayouts, Freeable::createDummy, parents);
	}
	
	//const
	public VkPipelineLayout(long address, @NotNull VkDevice device, @NotNull VkDescriptorSetLayout[] descriptorSetLayouts, @NotNull BiFunction<VkPipelineLayout, Object[], Freeable> storageCreator, @NotNull Object[] parents) {
		this.address = address;
		this.device = device;
		this.descriptorSetLayouts = descriptorSetLayouts;
		this.storage = storageCreator.apply(this, addIfNotContained(parents, device));
	}
	
	//parents
	private final @NotNull VkDevice device;
	private final @NotNull VkDescriptorSetLayout[] descriptorSetLayouts;
	
	public VkDevice device() {
		return device;
	}
	
	public VkInstance instance() {
		return device.instance();
	}
	
	public VkDescriptorSetLayout[] descriptorSetLayouts() {
		return descriptorSetLayouts;
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
		
		public Storage(@NotNull VkPipelineLayout o, @NotNull Object[] parents) {
			super(o, parents);
			this.device = o.device;
			this.address = o.address;
		}
		
		@Override
		protected @NotNull Barrier handleFree() {
			vkDestroyPipelineLayout(device, address, null);
			return Barrier.ALWAYS_TRIGGERED_BARRIER;
		}
	}
}
