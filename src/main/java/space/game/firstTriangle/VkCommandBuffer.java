package space.game.firstTriangle;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import space.engine.buffer.AllocatorStack.Frame;
import space.engine.freeableStorage.Freeable;
import space.engine.freeableStorage.Freeable.FreeableWrapper;
import space.engine.freeableStorage.FreeableStorage;
import space.engine.sync.barrier.Barrier;

import java.util.function.BiFunction;

import static org.lwjgl.vulkan.VK10.*;
import static space.engine.buffer.Allocator.allocatorStack;
import static space.engine.freeableStorage.Freeable.addIfNotContained;
import static space.game.firstTriangle.VkException.assertVk;

public class VkCommandBuffer extends org.lwjgl.vulkan.VkCommandBuffer implements FreeableWrapper {
	
	//create
	public static @NotNull VkCommandBuffer create(long address, @NotNull VkDevice device, @NotNull VkCommandPool commandPool, @NotNull Object[] parents) {
		return new VkCommandBuffer(address, device, commandPool, Storage::new, parents);
	}
	
	public static @NotNull VkCommandBuffer wrap(long address, @NotNull VkDevice device, @NotNull VkCommandPool commandPool, @NotNull Object[] parents) {
		return new VkCommandBuffer(address, device, commandPool, Freeable::createDummy, parents);
	}
	
	//const
	public VkCommandBuffer(long address, @NotNull VkDevice device, @NotNull VkCommandPool commandPool, @NotNull BiFunction<VkCommandBuffer, Object[], Freeable> storageCreator, @NotNull Object[] parents) {
		super(address, device);
		this.address = address;
		this.device = device;
		this.commandPool = commandPool;
		this.storage = storageCreator.apply(this, addIfNotContained(parents, device, commandPool));
	}
	
	//parents
	private final @NotNull VkDevice device;
	
	public VkDevice device() {
		return device;
	}
	
	public VkInstance instance() {
		return device.instance();
	}
	
	private final @NotNull VkCommandPool commandPool;
	
	public VkCommandPool commandPool() {
		return commandPool;
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
		private final @NotNull VkCommandPool commandPool;
		private final long address;
		
		public Storage(@NotNull VkCommandBuffer o, @NotNull Object[] parents) {
			super(o, parents);
			this.device = o.device;
			this.commandPool = o.commandPool;
			this.address = o.address;
		}
		
		@Override
		protected @NotNull Barrier handleFree() {
			try (Frame frame = allocatorStack().frame()) {
				nvkFreeCommandBuffers(device, commandPool.address(), 1, address);
			}
			return Barrier.ALWAYS_TRIGGERED_BARRIER;
		}
	}
	
	//methods
	public void beginCommandBuffer(VkCommandBufferBeginInfo info) {
		assertVk(vkBeginCommandBuffer(this, info));
	}
	
	public void endCommandBuffer() {
		assertVk(vkEndCommandBuffer(this));
	}
}
