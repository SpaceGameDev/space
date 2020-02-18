package space.engine.vulkan.managed.device;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import space.engine.recourcePool.BlockResourcePool;
import space.engine.recourcePool.FreeableWrappedResourcePool;
import space.engine.vulkan.VkDevice;
import space.engine.vulkan.VkEvent;
import space.engine.vulkan.VkFence;
import space.engine.vulkan.VkPhysicalDevice;
import space.engine.vulkan.VkQueueFamilyProperties;
import space.engine.vulkan.VkSemaphore;
import space.engine.vulkan.vma.VmaAllocator;

import static space.engine.Empties.EMPTY_OBJECT_ARRAY;

/**
 * Manages Device with it's queues.
 */
@SuppressWarnings("unused")
public abstract class ManagedDevice extends VkDevice {
	
	protected ManagedDevice(long handle, @NotNull VkPhysicalDevice physicalDevice, @NotNull VkDeviceCreateInfo ci, @NotNull Object[] parents) {
		super(handle, physicalDevice, ci, VkDevice.Storage::new, parents);
	}
	
	public void init() {
		this.eventAwaiter = new EventAwaiter(this, new Object[] {this});
		this.vmaAllocator = VmaAllocator.alloc(this, new Object[] {this});
		this.vkFencePool = createVkFencePool();
		this.vkSemaphorePool = createVkSemaphorePool();
		this.vkEventPool = createVkEventPool();
	}
	
	//queue
	public static final int QUEUE_TYPE_GRAPHICS = 0x0;
	public static final int QUEUE_TYPE_COMPUTE = 0x1;
	public static final int QUEUE_TYPE_TRANSFER = 0x2;
	
	public static final int QUEUE_FLAG_REALTIME_BIT = 0x1;
	public static final int QUEUE_FLAG_TRANSFER_HOST_TO_DEVICE_BIT = 0x2;
	
	public abstract @NotNull VkQueueFamilyProperties getQueueFamily(int type) throws QueueNotAvailableException;
	
	public abstract @NotNull ManagedQueue getQueue(int type, int flags) throws QueueNotAvailableException;
	
	public static class QueueNotAvailableException extends IllegalArgumentException {
		
		public QueueNotAvailableException(int type, int flags) {
			super("Queue type " + type + " with flags " + flags + " not available!");
		}
		
		public QueueNotAvailableException(int type) {
			super("Queue type " + type + " not available!");
		}
		
		public QueueNotAvailableException() {
		}
		
		public QueueNotAvailableException(String s) {
			super(s);
		}
		
		public QueueNotAvailableException(String message, Throwable cause) {
			super(message, cause);
		}
		
		public QueueNotAvailableException(Throwable cause) {
			super(cause);
		}
	}
	
	//eventAwaiter
	private EventAwaiter eventAwaiter;
	
	public EventAwaiter eventAwaiter() {
		return eventAwaiter;
	}
	
	//vmaAllocator
	private VmaAllocator vmaAllocator;
	
	public VmaAllocator vmaAllocator() {
		return vmaAllocator;
	}
	
	//resourcePools
	private FreeableWrappedResourcePool<VkFence, VkFence> vkFencePool;
	private FreeableWrappedResourcePool<VkSemaphore, VkSemaphore> vkSemaphorePool;
	private FreeableWrappedResourcePool<VkEvent, VkEvent> vkEventPool;
	
	private FreeableWrappedResourcePool<VkFence, VkFence> createVkFencePool() {
		return FreeableWrappedResourcePool.withLamdba(
				BlockResourcePool.withLambda(16, count -> VkFence.allocateDirect(this, count, EMPTY_OBJECT_ARRAY)),
				(inner, storageCreator, parents) -> new VkFence.Default(inner.address(), this, storageCreator, parents),
				VkFence::reset
		);
	}
	
	private FreeableWrappedResourcePool<VkSemaphore, VkSemaphore> createVkSemaphorePool() {
		return FreeableWrappedResourcePool.withLamdba(
				BlockResourcePool.withLambda(16, count -> VkSemaphore.allocateDirect(this, count, EMPTY_OBJECT_ARRAY)),
				(inner, storageCreator, parents) -> new VkSemaphore.Default(inner.address(), this, storageCreator, parents),
				semaphore -> {}
		);
	}
	
	private FreeableWrappedResourcePool<VkEvent, VkEvent> createVkEventPool() {
		return FreeableWrappedResourcePool.withLamdba(
				BlockResourcePool.withLambda(16, count -> VkEvent.allocateDirect(this, count, EMPTY_OBJECT_ARRAY)),
				(inner, storageCreator, parents) -> new VkEvent.Default(inner.address(), this, storageCreator, parents),
				VkEvent::reset
		);
	}
	
	public FreeableWrappedResourcePool<VkFence, VkFence> vkFencePool() {
		return vkFencePool;
	}
	
	public FreeableWrappedResourcePool<VkSemaphore, VkSemaphore> vkSemaphorePool() {
		return vkSemaphorePool;
	}
	
	public FreeableWrappedResourcePool<VkEvent, VkEvent> vkEventPool() {
		return vkEventPool;
	}
}
