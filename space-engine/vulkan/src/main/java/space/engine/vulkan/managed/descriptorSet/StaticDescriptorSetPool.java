package space.engine.vulkan.managed.descriptorSet;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.array.ArrayBufferLong;
import space.engine.freeable.Freeable;
import space.engine.freeable.Freeable.CleanerWrapper;
import space.engine.lwjgl.LwjglStructAllocator;
import space.engine.vulkan.VkInstance;
import space.engine.vulkan.descriptors.VkDescriptorPool;
import space.engine.vulkan.descriptors.VkDescriptorSet;
import space.engine.vulkan.descriptors.VkDescriptorSetBinding;
import space.engine.vulkan.descriptors.VkDescriptorSetLayout;
import space.engine.vulkan.managed.device.ManagedDevice;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.lwjgl.vulkan.VK10.*;
import static space.engine.lwjgl.LwjglStructAllocator.mallocStruct;

/**
 * A {@link VkDescriptorPool} allocated for a specific {@link VkDescriptorSetLayout} and {@link VkDescriptorSet} count.
 * {@link VkDescriptorSet VkDescriptorSets} will be allocated immediately from the pool and kept alive until the {@link StaticDescriptorSetPool} is destroyed.
 */
public class StaticDescriptorSetPool implements CleanerWrapper {
	
	public StaticDescriptorSetPool(ManagedDevice device, VkDescriptorSetLayout layout, int setCount, Object[] parents) {
		this.device = device;
		this.storage = Freeable.createDummy(this, parents);
		
		@NotNull VkDescriptorSetBinding[] bindings = layout.bindings();
		int[] descriptorTypes = Arrays.stream(bindings).mapToInt(b -> b.descriptorType).distinct().toArray();
		int[] descriptorTypesCount = Arrays.stream(descriptorTypes).map(type -> (int) Arrays.stream(bindings).filter(b -> b.descriptorType == type).count()).toArray();
		
		try (AllocatorFrame frame = Allocator.frame()) {
			this.pool = VkDescriptorPool.alloc(mallocStruct(frame, VkDescriptorPoolCreateInfo::create, VkDescriptorPoolCreateInfo.SIZEOF).set(
					VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO,
					0,
					0,
					setCount,
					LwjglStructAllocator.allocBuffer(frame, VkDescriptorPoolSize::create, VkDescriptorPoolSize.SIZEOF, IntStream
							.range(0, descriptorTypes.length)
							.mapToObj(i -> (Consumer<VkDescriptorPoolSize>) vkDescriptorPoolSize -> vkDescriptorPoolSize.set(
									descriptorTypes[i], descriptorTypesCount[i] * setCount
							))
							.collect(Collectors.toUnmodifiableList())
					)
			), device, new Object[] {this});
			
			ArrayBufferLong ptrs = ArrayBufferLong.malloc(frame, setCount);
			nvkAllocateDescriptorSets(device, mallocStruct(frame, VkDescriptorSetAllocateInfo::create, VkDescriptorSetAllocateInfo.SIZEOF).set(
					VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO,
					0,
					pool.address(),
					ArrayBufferLong.alloc(frame, IntStream.range(0, setCount).mapToLong(i -> layout.address()).toArray()).nioBuffer()
			).address(), ptrs.address());
			this.sets = ptrs.stream().mapToObj(ptr -> VkDescriptorSet.wrap(ptr, pool, new Object[] {this})).toArray(VkDescriptorSet[]::new);
		}
	}
	
	//parents
	private final ManagedDevice device;
	
	public ManagedDevice device() {
		return device;
	}
	
	public VkInstance instance() {
		return device.instance();
	}
	
	//storage
	private final @NotNull Freeable storage;
	
	@Override
	public @NotNull Freeable getStorage() {
		return storage;
	}
	
	//pool
	private final @NotNull VkDescriptorPool pool;
	private final @NotNull VkDescriptorSet[] sets;
	
	public @NotNull VkDescriptorPool pool() {
		return pool;
	}
	
	public VkDescriptorSet[] sets() {
		return sets;
	}
}
