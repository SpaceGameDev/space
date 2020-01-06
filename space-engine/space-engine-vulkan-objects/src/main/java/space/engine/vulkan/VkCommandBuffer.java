package space.engine.vulkan;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.vulkan.VkDevice;
import space.engine.barrier.Barrier;
import space.engine.freeable.Cleaner;
import space.engine.freeable.Freeable;

import java.util.function.BiFunction;

import static space.engine.freeable.Freeable.addIfNotContained;

public abstract class VkCommandBuffer extends org.lwjgl.vulkan.VkCommandBuffer implements Freeable {
	
	@SuppressWarnings("unused")
	public static final VkCommandBuffer[] EMPTY_COMMAND_BUFFER_ARRAY = new VkCommandBuffer[0];
	
	//wrap
	public static @NotNull VkCommandBuffer wrap(long address, @NotNull VkCommandPool commandPool, @NotNull Object[] parents) {
		return new VkCommandBuffer.Default(address, commandPool, Freeable::createDummy, parents);
	}
	
	protected VkCommandBuffer(long handle, VkDevice device) {
		super(handle, device);
	}
	
	//parents
	public abstract @NotNull VkCommandPool commandPool();
	
	public @NotNull VkDevice device() {
		return commandPool().device();
	}
	
	public final @NotNull VkCommandBuffer deown() {
		return this;
	}
	
	//address
	public abstract long address();
	
	public static class Default extends VkCommandBufferOwned implements CleanerWrapper {
		
		//const
		public Default(long address, @NotNull VkCommandPool commandPool, @NotNull BiFunction<? super Default, Object[], Freeable> storageCreator, @NotNull Object[] parents) {
			super(address, commandPool.device());
			this.commandPool = commandPool;
			this.address = address;
			this.storage = storageCreator.apply(this, addIfNotContained(parents, commandPool));
		}
		
		//parents
		private final @NotNull VkCommandPool commandPool;
		
		@Override
		public @NotNull VkCommandPool commandPool() {
			return commandPool;
		}
		
		//address
		private final long address;
		
		@Override
		public long address() {
			return address;
		}
		
		//storage
		private final @NotNull Freeable storage;
		
		@Override
		public @NotNull Freeable getStorage() {
			return storage;
		}
	}
	
	public static class DestroyStorage extends Cleaner {
		
		private final @NotNull VkCommandPool commandPool;
		private final long address;
		
		public DestroyStorage(@NotNull VkCommandBuffer event, @NotNull Object[] parents) {
			super(event, parents);
			this.commandPool = event.commandPool();
			this.address = event.address();
		}
		
		@Override
		protected @NotNull Barrier handleFree() {
			return commandPool.releaseCommandBuffer(address);
		}
	}
}
