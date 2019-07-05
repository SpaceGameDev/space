package space.engine.vulkan.descriptors;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.array.ArrayBufferLong;
import space.engine.buffer.pointer.PointerBufferPointer;
import space.engine.freeableStorage.Freeable;
import space.engine.freeableStorage.Freeable.FreeableWrapper;
import space.engine.freeableStorage.FreeableStorage;
import space.engine.sync.barrier.Barrier;
import space.engine.vulkan.VkDevice;
import space.engine.vulkan.VkInstance;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.lwjgl.vulkan.VK10.*;
import static space.engine.freeableStorage.Freeable.addIfNotContained;
import static space.engine.lwjgl.LwjglStructAllocator.*;
import static space.engine.vulkan.VkException.assertVk;

public class VkDescriptorSetLayout implements FreeableWrapper {
	
	//alloc
	public static @NotNull VkDescriptorSetLayout alloc(@NotNull VkDevice device, int flags, @NotNull VkDescriptorSetBinding[] bindings, @NotNull Object[] parents) {
		try (AllocatorFrame frame = Allocator.frame()) {
			return alloc(mallocStruct(frame, VkDescriptorSetLayoutCreateInfo::create, VkDescriptorSetLayoutCreateInfo.SIZEOF).set(
					VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO,
					0,
					flags,
					allocBuffer(frame, VkDescriptorSetLayoutBinding::create, VkDescriptorSetLayoutBinding.SIZEOF,
								Arrays.stream(bindings)
									  .map(b -> (Consumer<VkDescriptorSetLayoutBinding>) vkDescriptorSetLayoutBinding -> vkDescriptorSetLayoutBinding.set(
											  b.binding,
											  b.descriptorType,
											  b.descriptorCount,
											  b.stageFlags,
											  b.immutableSamplers != null ? ArrayBufferLong.alloc(frame, b.immutableSamplers).nioBuffer() : null
									  ))
									  .collect(Collectors.toUnmodifiableList())
					)
			), device, bindings, parents);
		}
	}
	
	public static @NotNull VkDescriptorSetLayout alloc(VkDescriptorSetLayoutCreateInfo info, @NotNull VkDevice device, @NotNull VkDescriptorSetBinding[] bindings, @NotNull Object[] parents) {
		try (AllocatorFrame frame = Allocator.frame()) {
			PointerBufferPointer ptr = PointerBufferPointer.malloc(frame);
			assertVk(nvkCreateDescriptorSetLayout(device, info.address(), 0, ptr.address()));
			return create(ptr.getPointer(), device, bindings, parents);
		}
	}
	
	//create
	public static @NotNull VkDescriptorSetLayout create(long address, @NotNull VkDevice device, @NotNull VkDescriptorSetBinding[] bindings, @NotNull Object[] parents) {
		return new VkDescriptorSetLayout(address, device, bindings, Storage::new, parents);
	}
	
	public static @NotNull VkDescriptorSetLayout wrap(long address, @NotNull VkDevice device, @NotNull VkDescriptorSetBinding[] bindings, @NotNull Object[] parents) {
		return new VkDescriptorSetLayout(address, device, bindings, Freeable::createDummy, parents);
	}
	
	//const
	public VkDescriptorSetLayout(long address, @NotNull VkDevice device, @NotNull VkDescriptorSetBinding[] bindings, @NotNull BiFunction<VkDescriptorSetLayout, Object[], Freeable> storageCreator, @NotNull Object[] parents) {
		this.address = address;
		this.device = device;
		this.bindings = bindings;
		this.storage = storageCreator.apply(this, addIfNotContained(parents, device));
	}
	
	//parents
	private final @NotNull VkDevice device;
	private final @NotNull VkDescriptorSetBinding[] bindings;
	
	public @NotNull VkDevice device() {
		return device;
	}
	
	public @NotNull VkInstance instance() {
		return device.instance();
	}
	
	public @NotNull VkDescriptorSetBinding[] bindings() {
		return bindings;
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
		
		public Storage(@NotNull VkDescriptorSetLayout o, @NotNull Object[] parents) {
			super(o, parents);
			this.device = o.device;
			this.address = o.address;
		}
		
		@Override
		protected @NotNull Barrier handleFree() {
			vkDestroyDescriptorSetLayout(device, address, null);
			return Barrier.ALWAYS_TRIGGERED_BARRIER;
		}
	}
}
