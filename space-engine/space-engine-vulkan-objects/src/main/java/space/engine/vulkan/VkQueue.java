package space.engine.vulkan;

import org.jetbrains.annotations.NotNull;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.pointer.PointerBufferPointer;
import space.engine.freeableStorage.Freeable;
import space.engine.freeableStorage.Freeable.FreeableWrapper;

import java.util.function.BiFunction;

import static org.lwjgl.vulkan.VK10.nvkGetDeviceQueue;
import static space.engine.freeableStorage.Freeable.addIfNotContained;

public class VkQueue extends org.lwjgl.vulkan.VkQueue implements FreeableWrapper {
	
	//alloc
	public static @NotNull VkQueue alloc(@NotNull VkDevice device, @NotNull VkQueueFamilyProperties family, int queueIndex, @NotNull Object[] parents) {
		try (AllocatorFrame frame = Allocator.frame()) {
			PointerBufferPointer ptr = PointerBufferPointer.malloc(frame);
			nvkGetDeviceQueue(device, family.index(), queueIndex, ptr.address());
			return VkQueue.wrap(ptr.getPointer(), device, family, parents);
		}
	}
	
	//create
	public static @NotNull VkQueue wrap(long address, @NotNull VkDevice device, @NotNull VkQueueFamilyProperties queueFamily, @NotNull Object[] parents) {
		return new VkQueue(address, device, queueFamily, Freeable::createDummy, parents);
	}
	
	//const
	protected VkQueue(long address, @NotNull VkDevice device, @NotNull VkQueueFamilyProperties queueFamily, @NotNull BiFunction<VkQueue, Object[], Freeable> storageCreator, @NotNull Object[] parents) {
		this(address, device, queueFamily);
		init(storageCreator, parents);
	}
	
	protected VkQueue(long address, @NotNull VkDevice device, @NotNull VkQueueFamilyProperties queueFamily) {
		super(address, device);
		this.device = device;
		this.queueFamily = queueFamily;
	}
	
	protected void init(@NotNull BiFunction<VkQueue, Object[], Freeable> storageCreator, @NotNull Object[] parents) {
		this.storage = storageCreator.apply(this, addIfNotContained(parents, device));
	}
	
	//parents
	private final @NotNull VkDevice device;
	
	public VkDevice device() {
		return device;
	}
	
	@Override
	@Deprecated
	public @NotNull VkDevice getDevice() {
		return device;
	}
	
	public VkInstance instance() {
		return device.instance();
	}
	
	private final @NotNull VkQueueFamilyProperties queueFamily;
	
	public VkQueueFamilyProperties queueFamily() {
		return queueFamily;
	}
	
	//storage
	@SuppressWarnings("NullableProblems")
	private @NotNull Freeable storage;
	
	@Override
	public @NotNull Freeable getStorage() {
		return storage;
	}
}
