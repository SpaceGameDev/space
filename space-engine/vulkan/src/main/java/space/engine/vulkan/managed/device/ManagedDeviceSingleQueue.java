package space.engine.vulkan.managed.device;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.array.ArrayBufferFloat;
import space.engine.buffer.array.ArrayBufferPointer;
import space.engine.buffer.pointer.PointerBufferPointer;
import space.engine.vulkan.VkPhysicalDevice;
import space.engine.vulkan.VkQueueFamilyProperties;

import java.util.Collection;
import java.util.function.Function;

import static org.lwjgl.vulkan.VK10.*;
import static space.engine.Empties.EMPTY_OBJECT_ARRAY;
import static space.engine.lwjgl.LwjglStructAllocator.*;
import static space.engine.lwjgl.PointerBufferWrapper.wrapPointer;
import static space.engine.vulkan.VkException.assertVk;

public class ManagedDeviceSingleQueue extends ManagedDevice {
	
	private static final int QUEUE_REQUIRED_FLAGS = VK_QUEUE_GRAPHICS_BIT | VK_QUEUE_COMPUTE_BIT;
	
	public static ManagedDeviceSingleQueue alloc(@NotNull VkPhysicalDevice physicalDevice, @NotNull Collection<VkExtensionProperties> extensions, @Nullable VkPhysicalDeviceFeatures features, @NotNull Object[] parents) {
		@Nullable VkQueueFamilyProperties queueFamily = physicalDevice.queueProperties()
																	  .stream()
																	  .filter(family -> (family.queueFlags() & QUEUE_REQUIRED_FLAGS) == QUEUE_REQUIRED_FLAGS)
																	  .findFirst()
																	  .orElse(null);
		
		try (AllocatorFrame frame = Allocator.frame()) {
			VkDeviceCreateInfo info = mallocStruct(frame, VkDeviceCreateInfo::create, VkDeviceCreateInfo.SIZEOF).set(
					VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO,
					0,
					0,
					queueFamily != null ? allocBuffer(frame, VkDeviceQueueCreateInfo::create, VkDeviceQueueCreateInfo.SIZEOF, vkDeviceQueueCreateInfo -> vkDeviceQueueCreateInfo.set(
							VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO,
							0,
							0,
							queueFamily.index(),
							ArrayBufferFloat.alloc(frame, new float[] {1.0f}).nioBuffer()
					)) : wrapBuffer(VkDeviceQueueCreateInfo::create, 0, 0),
					null,
					wrapPointer(ArrayBufferPointer.alloc(frame, extensions.stream().map(VkExtensionProperties::extensionName).toArray(java.nio.Buffer[]::new))),
					features
			);
			
			PointerBufferPointer ptr = PointerBufferPointer.malloc(frame);
			assertVk(nvkCreateDevice(physicalDevice, info.address(), 0, ptr.address()));
			return new ManagedDeviceSingleQueue(ptr.getPointer(), physicalDevice, info, device1 -> queueFamily != null ? ManagedQueue.alloc(device1, queueFamily, 0, EMPTY_OBJECT_ARRAY) : null, parents);
		}
	}
	
	private final @Nullable ManagedQueue queue;
	
	protected ManagedDeviceSingleQueue(long handle, @NotNull VkPhysicalDevice physicalDevice, @NotNull VkDeviceCreateInfo ci, Function<ManagedDeviceSingleQueue, ManagedQueue> queue, @NotNull Object[] parents) {
		super(handle, physicalDevice, ci, parents);
		this.queue = queue.apply(this);
		init();
	}
	
	//getter
	@Override
	public @NotNull VkQueueFamilyProperties getQueueFamily(int type) throws QueueNotAvailableException {
		if (queue == null)
			throw new QueueNotAvailableException(type);
		return queue.queueFamily();
	}
	
	@Override
	public @NotNull ManagedQueue getQueue(int type, int flags) throws QueueNotAvailableException {
		if (queue == null)
			throw new QueueNotAvailableException(type, flags);
		return queue;
	}
}
