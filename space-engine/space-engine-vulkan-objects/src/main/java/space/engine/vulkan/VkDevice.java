package space.engine.vulkan;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo.Buffer;
import space.engine.barrier.Barrier;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.array.ArrayBufferFloat;
import space.engine.buffer.pointer.PointerBufferPointer;
import space.engine.freeable.Cleaner;
import space.engine.freeable.Freeable;
import space.engine.freeable.Freeable.CleanerWrapper;
import space.engine.indexmap.IndexMap;
import space.engine.indexmap.IndexMapArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static org.lwjgl.system.JNI.callPPV;
import static org.lwjgl.vulkan.VK10.*;
import static space.engine.Empties.EMPTY_OBJECT_ARRAY;
import static space.engine.freeable.Freeable.addIfNotContained;
import static space.engine.lwjgl.LwjglStructAllocator.mallocBuffer;
import static space.engine.vulkan.VkException.assertVk;

public class VkDevice extends org.lwjgl.vulkan.VkDevice implements CleanerWrapper {
	
	/**
	 * The {@link QueueRequestHandler} handles the querying of queues for a {@link VkDevice}.
	 *
	 * <ol>
	 * <li>add any QueueRequests with {@link #addRequest(VkQueueFamilyProperties, float)}.</li>
	 * <li>generate the {@link VkDeviceQueueCreateInfo.Buffer} with {@link #generateDeviceQueueRequestCreateInfoBuffer(Allocator, Object[])}<br>
	 * and add them to your {@link VkDeviceCreateInfo#pQueueCreateInfos(Buffer)}</li>
	 * <li>create your {@link VkDevice}</li>
	 * <li>call {@link #fillQueueRequestsWithQueues(VkDevice)} to fill out any QueueRequests</li>
	 * </ol>
	 */
	public static class QueueRequestHandler {
		
		private @Nullable IndexMap<List<QueueRequest>> requests = new IndexMapArray<>();
		private @Nullable QueueRequest[][] requestsFiltered;
		
		public Supplier<VkQueue> addRequest(@NotNull VkQueueFamilyProperties familyProperties, float priority) {
			if (requests == null)
				throw new IllegalStateException("already generated QueueBuffer!");
			
			QueueRequest request = new QueueRequest(familyProperties, priority);
			requests.computeIfAbsent(request.familyProperties.index(), ArrayList::new).add(request);
			return request;
		}
		
		public VkDeviceQueueCreateInfo.Buffer generateDeviceQueueRequestCreateInfoBuffer(AllocatorFrame allocator) {
			return generateDeviceQueueRequestCreateInfoBuffer(allocator, EMPTY_OBJECT_ARRAY);
		}
		
		public VkDeviceQueueCreateInfo.Buffer generateDeviceQueueRequestCreateInfoBuffer(Allocator allocator, Object[] parents) {
			if (requests == null)
				throw new IllegalStateException("already generated QueueBuffer!");
			if (requestsFiltered == null) {
				requestsFiltered = Objects.requireNonNull(requests).values().stream().filter(Objects::nonNull).filter(l -> !l.isEmpty()).map(list -> list.toArray(new QueueRequest[0])).toArray(
						QueueRequest[][]::new);
				requests = null;
			}
			
			VkDeviceQueueCreateInfo.Buffer queueInfos = mallocBuffer(allocator, VkDeviceQueueCreateInfo::create, VkDeviceQueueCreateInfo.SIZEOF, requestsFiltered.length, parents);
			for (int i = 0; i < requestsFiltered.length; i++) {
				QueueRequest[] queueRequestOfFamily = requestsFiltered[i];
				VkQueueFamilyProperties familyProperties = queueRequestOfFamily[0].familyProperties;
				ArrayBufferFloat priorities = ArrayBufferFloat.malloc(allocator, queueRequestOfFamily.length, new Object[] {queueInfos});
				for (int j = 0; j < queueRequestOfFamily.length; j++)
					priorities.putFloat(j, queueRequestOfFamily[j].priority);
				
				queueInfos.get(i).set(
						VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO,
						0,
						0,
						familyProperties.index(),
						priorities.nioBuffer()
				);
			}
			return queueInfos;
		}
		
		public void fillQueueRequestsWithQueues(VkDevice device) {
			if (requestsFiltered == null)
				throw new IllegalStateException("already filled out QueueRequests");
			
			try (AllocatorFrame frame = Allocator.frame()) {
				PointerBufferPointer queue = PointerBufferPointer.malloc(frame);
				for (QueueRequest[] queueRequestByFamily : requestsFiltered) {
					for (int j = 0; j < queueRequestByFamily.length; j++) {
						QueueRequest queueRequest = queueRequestByFamily[j];
						nvkGetDeviceQueue(device, queueRequest.familyProperties.index(), j, queue.address());
						queueRequest.queue = VkQueue.wrap(queue.getPointer(), device, queueRequest.familyProperties, new Object[] {device});
					}
				}
			}
			requestsFiltered = null;
		}
	}
	
	private static class QueueRequest implements Supplier<VkQueue> {
		
		private final @NotNull VkQueueFamilyProperties familyProperties;
		private final float priority;
		private @Nullable VkQueue queue;
		
		public QueueRequest(@NotNull VkQueueFamilyProperties familyProperties, float priority) {
			this.familyProperties = familyProperties;
			this.priority = priority;
		}
		
		@Override
		public VkQueue get() {
			if (queue == null)
				throw new RuntimeException("queue queried before device was created!");
			return queue;
		}
	}
	
	//alloc
	public static VkDevice alloc(@NotNull VkDeviceCreateInfo info, @NotNull VkPhysicalDevice physicalDevice, @NotNull Object[] parents) {
		try (AllocatorFrame frame = Allocator.frame()) {
			PointerBufferPointer device = PointerBufferPointer.malloc(frame);
			assertVk(nvkCreateDevice(physicalDevice, info.address(), 0, device.address()));
			return create(device.getPointer(), physicalDevice, info, parents);
		}
	}
	
	//create
	public static VkDevice create(long handle, @NotNull VkPhysicalDevice physicalDevice, @NotNull VkDeviceCreateInfo ci, @NotNull Object[] parents) {
		return new VkDevice(handle, physicalDevice, ci, Storage::new, parents);
	}
	
	public static VkDevice wrap(long handle, @NotNull VkPhysicalDevice physicalDevice, @NotNull VkDeviceCreateInfo ci, @NotNull Object[] parents) {
		return new VkDevice(handle, physicalDevice, ci, Freeable::createDummy, parents);
	}
	
	//const
	public VkDevice(long handle, @NotNull VkPhysicalDevice physicalDevice, @NotNull VkDeviceCreateInfo ci, @NotNull BiFunction<VkDevice, Object[], Freeable> storageCreator, @NotNull Object[] parents) {
		super(handle, physicalDevice, ci);
		this.physicalDevice = physicalDevice;
		this.storage = storageCreator.apply(this, addIfNotContained(parents, physicalDevice));
	}
	
	//parents
	private final @NotNull VkPhysicalDevice physicalDevice;
	
	public VkInstance instance() {
		return physicalDevice.instance();
	}
	
	public @NotNull VkPhysicalDevice physicalDevice() {
		return physicalDevice;
	}
	
	@Override
	@Deprecated
	public @NotNull VkPhysicalDevice getPhysicalDevice() {
		return physicalDevice;
	}
	
	//storage
	private final @NotNull Freeable storage;
	
	public static class Storage extends Cleaner {
		
		private final long function_vkDestroyDevice;
		private final long device;
		
		public Storage(@NotNull VkDevice device, @NotNull Object[] parents) {
			super(device, parents);
			function_vkDestroyDevice = device.getCapabilities().vkDestroyDevice;
			this.device = device.address();
		}
		
		@Override
		protected @NotNull Barrier handleFree() {
			//vkDestroyDevice
			callPPV(function_vkDestroyDevice, device, 0);
			return Barrier.DONE_BARRIER;
		}
	}
	
	@Override
	public @NotNull Freeable getStorage() {
		return storage;
	}
}
