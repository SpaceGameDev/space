package space.game.firstTriangle;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.vulkan.VkSubmitInfo;
import space.engine.freeableStorage.Freeable;
import space.engine.freeableStorage.Freeable.FreeableWrapper;

import java.util.function.BiFunction;

import static org.lwjgl.vulkan.VK10.vkQueueSubmit;
import static space.engine.freeableStorage.Freeable.addIfNotContained;
import static space.game.firstTriangle.VkException.assertVk;

public class VkQueue extends org.lwjgl.vulkan.VkQueue implements FreeableWrapper {
	
	//create
	public static @NotNull VkQueue wrap(long address, @NotNull VkDevice device, @NotNull VkQueueFamilyProperties queueFamily, @NotNull Object[] parents) {
		return new VkQueue(address, device, queueFamily, Freeable::createDummy, parents);
	}
	
	//const
	public VkQueue(long address, @NotNull VkDevice device, @NotNull VkQueueFamilyProperties queueFamily, @NotNull BiFunction<VkQueue, Object[], Freeable> storageCreator, @NotNull Object[] parents) {
		super(address, device);
		this.address = address;
		this.device = device;
		this.queueFamily = queueFamily;
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
	
	//methods
	public void submit(VkSubmitInfo info) {
		assertVk(vkQueueSubmit(this, info, 0));
	}
	
	public void submit(VkSubmitInfo.Buffer info) {
		assertVk(vkQueueSubmit(this, info, 0));
	}
}
