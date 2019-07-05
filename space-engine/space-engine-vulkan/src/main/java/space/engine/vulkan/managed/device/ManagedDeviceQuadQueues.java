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
import space.engine.lwjgl.LwjglStructAllocator;
import space.engine.vulkan.VkPhysicalDevice;
import space.engine.vulkan.VkQueueFamilyProperties;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.lwjgl.vulkan.VK10.*;
import static space.engine.Empties.EMPTY_OBJECT_ARRAY;
import static space.engine.lwjgl.LwjglStructAllocator.mallocStruct;
import static space.engine.lwjgl.PointerBufferWrapper.wrapPointer;
import static space.engine.vulkan.VkException.assertVk;

/**
 * Not working. Use {@link ManagedDeviceSingleQueue} for now.
 */
@Deprecated
public class ManagedDeviceQuadQueues extends ManagedDevice {
	
	private static final int QUEUE_TYPE_MAX = 0x3;
	private static final int QUEUE_FLAGS_MAX = 0x4;
	
	public static ManagedDeviceQuadQueues alloc(@NotNull VkPhysicalDevice physicalDevice, @NotNull Collection<VkExtensionProperties> extensions, @Nullable VkPhysicalDeviceFeatures features, boolean useRealtime, @NotNull Object[] parents) {
		try (AllocatorFrame frame = Allocator.frame()) {
			
			//family
			@Nullable VkQueueFamilyProperties familyGraphics = physicalDevice.findQueueFamilySingle(VK_QUEUE_GRAPHICS_BIT, 0)
																			 .orElse(null);
			@Nullable VkQueueFamilyProperties familyCompute = physicalDevice.findQueueFamilySingle(VK_QUEUE_COMPUTE_BIT, VK_QUEUE_GRAPHICS_BIT)
																			.or(() -> physicalDevice.findQueueFamilySingle(VK_QUEUE_COMPUTE_BIT, 0))
																			.orElse(null);
			@Nullable VkQueueFamilyProperties familyTransfer = physicalDevice.findQueueFamilySingle(VK_QUEUE_TRANSFER_BIT, VK_QUEUE_GRAPHICS_BIT | VK_QUEUE_COMPUTE_BIT)
																			 .or(() -> physicalDevice.findQueueFamilySingle(VK_QUEUE_TRANSFER_BIT, VK_QUEUE_GRAPHICS_BIT))
																			 .or(() -> physicalDevice.findQueueFamilySingle(VK_QUEUE_TRANSFER_BIT, 0))
																			 .orElse(null);
			
			VkQueueFamilyProperties[] queueFamilies = new VkQueueFamilyProperties[QUEUE_TYPE_MAX];
			queueFamilies[QUEUE_TYPE_GRAPHICS] = familyGraphics;
			queueFamilies[QUEUE_TYPE_COMPUTE] = familyCompute;
			queueFamilies[QUEUE_TYPE_TRANSFER] = familyTransfer;
			
			//queue allocation
			int[] allocated = new int[physicalDevice.queueProperties().size()];
			int queueCountGraphics;
			int queueCountCompute;
			int queueCountTransfer;
			
			{
				int queueCountGraphics2 = 0;
				int queueCountCompute2 = 0;
				int queueCountTransfer2 = 0;
				
				if (familyGraphics != null && (allocated[familyGraphics.index()] += 1) <= familyGraphics.queueCount())
					queueCountGraphics2++;
				if (familyCompute != null && (allocated[familyCompute.index()] += 1) <= familyCompute.queueCount())
					queueCountCompute2++;
				if (familyTransfer != null && (allocated[familyTransfer.index()] += 1) <= familyTransfer.queueCount())
					queueCountTransfer2++;
				if (familyTransfer != null && (allocated[familyTransfer.index()] += 1) <= familyTransfer.queueCount())
					queueCountTransfer2++;
				if (useRealtime) {
					if (familyGraphics != null && (allocated[familyGraphics.index()] += 1) <= familyGraphics.queueCount())
						queueCountGraphics2++;
					if (familyCompute != null && (allocated[familyCompute.index()] += 1) <= familyCompute.queueCount())
						queueCountCompute2++;
					if (familyTransfer != null && (allocated[familyTransfer.index()] += 2) <= familyTransfer.queueCount())
						queueCountTransfer2++;
				}
				
				queueCountGraphics = queueCountGraphics2;
				queueCountCompute = queueCountCompute2;
				queueCountTransfer = queueCountTransfer2;
			}
			
			//priority array
			@Nullable float[] priorityGraphics = new float[][] {null, {1.0f}, {0.5f, 1.0f}}[queueCountGraphics];
			@Nullable float[] priorityCompute = new float[][] {null, {1.0f}, {0.5f, 1.0f}}[queueCountCompute];
			@Nullable float[] priorityTransfer = new float[][] {null, {1.0f}, {1.0f, 1.0f}, null, {0.5f, 0.5f, 1.0f, 1.0f}}[queueCountTransfer];
			
			if (priorityGraphics == null)
				throw new IllegalStateException("graphics queue count was 0!");
			
			float[][] familyToPriority = new float[physicalDevice.queueProperties().size()][];
			int offsetGraphics = append(familyToPriority, familyGraphics, priorityGraphics);
			int offsetCompute = append(familyToPriority, familyCompute, priorityCompute);
			int offsetTransfer = append(familyToPriority, familyTransfer, priorityTransfer);
			
			//device info
			VkDeviceCreateInfo info = mallocStruct(frame, VkDeviceCreateInfo::create, VkDeviceCreateInfo.SIZEOF).set(
					VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO,
					0,
					0,
					LwjglStructAllocator.allocBuffer(
							frame, VkDeviceQueueCreateInfo::create, VkDeviceQueueCreateInfo.SIZEOF,
							physicalDevice.queueProperties()
										  .stream()
										  .filter(family -> familyToPriority[family.index()] != null)
										  .map(family -> (Consumer<VkDeviceQueueCreateInfo>) vkDeviceQueueCreateInfo -> vkDeviceQueueCreateInfo.set(
												  VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO,
												  0,
												  0,
												  family.index(),
												  ArrayBufferFloat.alloc(frame,
																		 familyToPriority[family.index()]).nioBuffer()
										  ))
										  .collect(Collectors.toList())
					),
					null,
					wrapPointer(ArrayBufferPointer.alloc(frame, extensions.stream().map(VkExtensionProperties::extensionName).toArray(java.nio.Buffer[]::new))),
					features
			);
			
			PointerBufferPointer ptr = PointerBufferPointer.malloc(frame);
			assertVk(nvkCreateDevice(physicalDevice, info.address(), 0, ptr.address()));
			return new ManagedDeviceQuadQueues(ptr.getPointer(), physicalDevice, info, queueFamilies, device1 -> {
				ManagedQueue[][] queues = new ManagedQueue[QUEUE_TYPE_MAX][QUEUE_FLAGS_MAX];
				
				//graphics
				queues[QUEUE_TYPE_GRAPHICS][0] = familyGraphics != null
						? ManagedQueue.alloc(device1, familyGraphics, offsetGraphics, EMPTY_OBJECT_ARRAY)
						: null;
				queues[QUEUE_TYPE_GRAPHICS][QUEUE_FLAG_REALTIME_BIT] = familyGraphics != null && queueCountGraphics >= 2
						? ManagedQueue.alloc(device1, familyGraphics, offsetGraphics + 1, EMPTY_OBJECT_ARRAY)
						: queues[QUEUE_TYPE_GRAPHICS][0];
				
				//compute
				queues[QUEUE_TYPE_COMPUTE][0] = familyCompute != null && queueCountCompute >= 1
						? ManagedQueue.alloc(device1, familyCompute, offsetCompute, EMPTY_OBJECT_ARRAY)
						: (familyGraphics != null && (familyGraphics.queueFlags() & VK_QUEUE_COMPUTE_BIT) == VK_QUEUE_COMPUTE_BIT ? queues[QUEUE_TYPE_GRAPHICS][0] : null);
				queues[QUEUE_TYPE_COMPUTE][QUEUE_FLAG_REALTIME_BIT] = familyCompute != null && queueCountCompute >= 2
						? ManagedQueue.alloc(device1, familyCompute, offsetCompute + 1, EMPTY_OBJECT_ARRAY)
						: queues[QUEUE_TYPE_COMPUTE][0];
				
				//transfer
				queues[QUEUE_TYPE_TRANSFER][0] = familyTransfer != null && queueCountTransfer >= 1
						? ManagedQueue.alloc(device1, familyTransfer, offsetTransfer, EMPTY_OBJECT_ARRAY)
						: (queues[QUEUE_TYPE_COMPUTE][0] != null ? queues[QUEUE_TYPE_COMPUTE][0] : queues[QUEUE_TYPE_GRAPHICS][0]);
				queues[QUEUE_TYPE_TRANSFER][QUEUE_FLAG_TRANSFER_HOST_TO_DEVICE_BIT] = familyTransfer != null && queueCountTransfer >= 2
						? ManagedQueue.alloc(device1, familyTransfer, offsetTransfer + 1, EMPTY_OBJECT_ARRAY)
						: queues[QUEUE_TYPE_TRANSFER][0];
				queues[QUEUE_TYPE_TRANSFER][QUEUE_FLAG_REALTIME_BIT] = familyTransfer != null && queueCountTransfer >= 4
						? ManagedQueue.alloc(device1, familyTransfer, offsetTransfer + 2, EMPTY_OBJECT_ARRAY)
						: queues[QUEUE_TYPE_TRANSFER][0];
				queues[QUEUE_TYPE_TRANSFER][QUEUE_FLAG_TRANSFER_HOST_TO_DEVICE_BIT | QUEUE_FLAG_REALTIME_BIT] = familyTransfer != null && queueCountTransfer >= 4
						? ManagedQueue.alloc(device1, familyTransfer, offsetTransfer + 3, EMPTY_OBJECT_ARRAY)
						: queues[QUEUE_TYPE_TRANSFER][QUEUE_FLAG_TRANSFER_HOST_TO_DEVICE_BIT];
				
				return queues;
			}, parents);
		}
	}
	
	private static int append(@NotNull float[][] array, @Nullable VkQueueFamilyProperties family, @Nullable float[] append) {
		if (family == null)
			return 0;
		int i = family.index();
		if (array[i] == null) {
			array[i] = append;
			return 0;
		}
		if (append == null) {
			return 0;
		}
		
		int oldLength = array[i].length;
		array[i] = Arrays.copyOf(array[i], oldLength + append.length);
		System.arraycopy(append, 0, array[i], oldLength, append.length);
		return oldLength;
	}
	
	private final VkQueueFamilyProperties[] queueFamilies;
	private final ManagedQueue[][] queues;
	
	protected ManagedDeviceQuadQueues(long handle, @NotNull VkPhysicalDevice physicalDevice, @NotNull VkDeviceCreateInfo ci, VkQueueFamilyProperties[] queueFamilies, Function<ManagedDeviceQuadQueues, ManagedQueue[][]> queues, @NotNull Object[] parents) {
		super(handle, physicalDevice, ci, parents);
		this.queueFamilies = queueFamilies;
		this.queues = queues.apply(this);
		init();
	}
	
	@Override
	public @NotNull VkQueueFamilyProperties getQueueFamily(int type) throws QueueNotAvailableException {
		if (type <= QUEUE_TYPE_MAX) {
			VkQueueFamilyProperties family = queueFamilies[type];
			if (family != null)
				return family;
		}
		throw new QueueNotAvailableException(type);
	}
	
	@Override
	public @NotNull ManagedQueue getQueue(int type, int flags) throws QueueNotAvailableException {
		if (type <= QUEUE_TYPE_MAX && flags <= QUEUE_FLAGS_MAX) {
			ManagedQueue vkQueue = queues[type][flags];
			if (vkQueue != null)
				return vkQueue;
		}
		throw new QueueNotAvailableException(type, flags);
	}
}
