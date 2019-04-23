package space.game.firstTriangle;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkQueue;
import space.engine.buffer.AllocatorStack.Frame;
import space.engine.buffer.Buffer;
import space.engine.buffer.StringConverter;
import space.engine.buffer.array.ArrayBufferFloat;
import space.engine.buffer.array.ArrayBufferPointer;
import space.engine.buffer.pointer.PointerBufferPointer;
import space.engine.freeableStorage.Freeable;
import space.engine.freeableStorage.Freeable.FreeableWrapper;
import space.engine.freeableStorage.FreeableStorage;
import space.engine.indexmap.IndexMap;
import space.engine.indexmap.IndexMapArray;
import space.engine.sync.barrier.Barrier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.lwjgl.system.JNI.callPPV;
import static org.lwjgl.vulkan.VK10.*;
import static space.engine.buffer.Allocator.*;
import static space.engine.freeableStorage.Freeable.addIfNotContained;
import static space.engine.lwjgl.LwjglStructAllocator.*;
import static space.engine.lwjgl.PointerBufferWrapper.wrapPointer;

public class VkDevice extends org.lwjgl.vulkan.VkDevice implements FreeableWrapper {
	
	//builder
	public static Builder builder(VkPhysicalDevice vkPhysicalDevice) {
		return new Builder(vkPhysicalDevice);
	}
	
	public static class Builder {
		
		private final VkPhysicalDevice physicalDevice;
		
		public Builder(VkPhysicalDevice physicalDevice) {
			this.physicalDevice = physicalDevice;
		}
		
		public VkPhysicalDevice getPhysicalDevice() {
			return physicalDevice;
		}
		
		//features
		private @Nullable VkPhysicalDeviceFeatures features;
		
		public Builder setFeatures(VkPhysicalDeviceFeatures features) {
			this.features = features;
			return this;
		}
		
		public @Nullable VkPhysicalDeviceFeatures getFeatures() {
			return features;
		}
		
		//queues
		private @NotNull IndexMap<List<QueueRequest>> queueRequests = new IndexMapArray<>();
		
		public Supplier<VkQueue> addQueueRequest(VkQueueFamilyProperties familyProperties, float priority) {
			QueueRequest request = new QueueRequest(familyProperties, priority);
			queueRequests.computeIfAbsent(request.familyProperties.index(), ArrayList::new).add(request);
			return request;
		}
		
		private static class QueueRequest implements Supplier<VkQueue> {
			
			private final VkQueueFamilyProperties familyProperties;
			private final float priority;
			private VkQueue queue;
			
			public QueueRequest(VkQueueFamilyProperties familyProperties, float priority) {
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
		
		public @NotNull IndexMap<List<QueueRequest>> getQueueRequests() {
			return queueRequests;
		}
		
		//extensions
		private @NotNull Collection<VkExtensionProperties> extensions = new ArrayList<>();
		
		public Builder addExtension(VkExtensionProperties extension) {
			this.extensions.add(Objects.requireNonNull(extension));
			return this;
		}
		
		public Builder addExtension(Collection<VkExtensionProperties> extension) {
			extension.forEach(Objects::requireNonNull);
			this.extensions.addAll(extension);
			return this;
		}
		
		public @NotNull Collection<VkExtensionProperties> getExtensions() {
			return extensions;
		}
		
		public void validate() {
		
		}
		
		//build
		public VkDevice build(Object[] parents) {
			validate();
			try (Frame frame = allocatorStack().frame()) {
				Collection<VkQueueFamilyProperties> queueProperties = physicalDevice.queueProperties();
				List<List<QueueRequest>> queueRequests = this.queueRequests.values().stream().filter(Objects::nonNull).filter(l -> !l.isEmpty()).collect(Collectors.toList());
				VkDeviceQueueCreateInfo.Buffer queueInfos = mallocBuffer(allocatorHeap(), VkDeviceQueueCreateInfo::create, VkDeviceQueueCreateInfo.SIZEOF, queueRequests.size(), new Object[] {frame});
				for (int i = 0; i < queueRequests.size(); i++) {
					List<QueueRequest> queueRequestOfFamily = queueRequests.get(i);
					VkQueueFamilyProperties familyProperties = queueRequestOfFamily.get(0).familyProperties;
					ArrayBufferFloat priorities = ArrayBufferFloat.malloc(frame, queueRequestOfFamily.size());
					for (int j = 0; j < queueRequestOfFamily.size(); j++)
						priorities.putFloat(j, queueRequestOfFamily.get(i).priority);
					
					queueInfos.get(i).set(
							VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO,
							0,
							0,
							familyProperties.index(),
							priorities.nioBuffer()
					);
				}
				
				VkDeviceCreateInfo info = callocStruct(frame, VkDeviceCreateInfo::create, VkDeviceCreateInfo.SIZEOF).set(
						VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO,
						0,
						0,
						queueInfos,
						null /* deprecated */,
						wrapPointer(ArrayBufferPointer.alloc(frame, extensions
								.stream()
								.map(ex -> StringConverter.stringToUTF8(allocatorHeap(), ex.extensionNameString(), true, new Object[] {frame}))
								.toArray(Buffer[]::new))),
						features
				);
				
				PointerBufferPointer device = PointerBufferPointer.malloc(frame);
				nvkCreateDevice(physicalDevice, info.address(), 0, device.address());
				VkDevice vkDevice = create(device.getPointer(), physicalDevice, info, parents);
				
				PointerBufferPointer queue = PointerBufferPointer.malloc(frame);
				for (List<QueueRequest> queueRequestByFamily : queueRequests) {
					for (int j = 0; j < queueRequestByFamily.size(); j++) {
						QueueRequest queueRequest = queueRequestByFamily.get(j);
						nvkGetDeviceQueue(vkDevice, queueRequest.familyProperties.index(), j, queue.address());
						queueRequest.queue = new VkQueue(queue.getPointer(), vkDevice);
					}
				}
				
				return vkDevice;
			}
		}
	}
	
	//create
	public static VkDevice create(long handle, VkPhysicalDevice physicalDevice, VkDeviceCreateInfo ci, Object[] parents) {
		return new VkDevice(handle, physicalDevice, ci, Storage::new, parents);
	}
	
	public static VkDevice wrap(long handle, VkPhysicalDevice physicalDevice, VkDeviceCreateInfo ci, Object[] parents) {
		return new VkDevice(handle, physicalDevice, ci, Freeable::createDummy, parents);
	}
	
	//const
	public VkDevice(long handle, VkPhysicalDevice physicalDevice, VkDeviceCreateInfo ci, BiFunction<VkDevice, Object[], Freeable> storageCreator, Object[] parents) {
		super(handle, physicalDevice, ci);
		this.physicalDevice = physicalDevice;
		this.storage = storageCreator.apply(this, addIfNotContained(parents, physicalDevice));
	}
	
	//parents
	private final VkPhysicalDevice physicalDevice;
	
	public VkInstance instance() {
		return physicalDevice.instance();
	}
	
	public VkPhysicalDevice physicalDevice() {
		return physicalDevice;
	}
	
	@Override
	@Deprecated
	public VkPhysicalDevice getPhysicalDevice() {
		return physicalDevice;
	}
	
	//storage
	private final Freeable storage;
	
	public static class Storage extends FreeableStorage {
		
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
			return Barrier.ALWAYS_TRIGGERED_BARRIER;
		}
	}
	
	@Override
	public @NotNull Freeable getStorage() {
		return storage;
	}
}
