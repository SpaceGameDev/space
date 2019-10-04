package space.engine.vulkan;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkCommandBufferInheritanceInfo;
import space.engine.barrier.Barrier;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.freeable.Cleaner;
import space.engine.freeable.Freeable;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import static org.lwjgl.vulkan.VK10.*;
import static space.engine.barrier.Barrier.DONE_BARRIER;
import static space.engine.freeable.Freeable.addIfNotContained;
import static space.engine.lwjgl.LwjglStructAllocator.mallocStruct;
import static space.engine.vulkan.VkException.assertVk;

public abstract class VkCommandBuffer extends org.lwjgl.vulkan.VkCommandBuffer implements Freeable {
	
	@SuppressWarnings("unused")
	public static final VkCommandBuffer[] EMPTY_COMMAND_BUFFER_ARRAY = new VkCommandBuffer[0];
	
	//allocateDirect
	public static @NotNull VkCommandBuffer[] allocateDirect(@NotNull VkCommandPool commandPool, int level, int count, @NotNull Object[] parents) {
		return commandPool.allocCommandBuffers(level, count, parents);
	}
	
	public static @NotNull VkCommandBuffer allocateDirect(@NotNull VkCommandPool commandPool, int level, @NotNull Object[] parents) {
		return commandPool.allocCommandBuffer(level, parents);
	}
	
	protected VkCommandBuffer(long handle, VkDevice device) {
		super(handle, device);
	}
	
	//wrap
	public static @NotNull VkCommandBuffer wrap(long address, @NotNull VkCommandPool commandPool, @NotNull Object[] parents) {
		return new VkCommandBuffer.Default(address, commandPool, Freeable::createDummy, parents);
	}
	
	//parents
	public abstract @NotNull VkCommandPool commandPool();
	
	public @NotNull VkDevice device() {
		return commandPool().device();
	}
	
	//address
	public abstract long address();
	
	//recording
	@SuppressWarnings({"FieldCanBeLocal", "unused"})
	private @Nullable Object recordingDependencies;
	
	public void begin(int flags) {
		begin(flags, null);
	}
	
	public void begin(int flags, @Nullable VkCommandBufferInheritanceInfo inheritanceInfo) {
		try (AllocatorFrame frame = Allocator.frame()) {
			begin(mallocStruct(frame, VkCommandBufferBeginInfo::create, VkCommandBufferBeginInfo.SIZEOF).set(
					VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO,
					0,
					flags,
					inheritanceInfo
			));
		}
	}
	
	public void begin(VkCommandBufferBeginInfo info) {
		assertVk(vkBeginCommandBuffer(this, info));
	}
	
	public void end() {
		end(null);
	}
	
	public void end(@Nullable Object recordingDependencies) {
		this.recordingDependencies = recordingDependencies;
		assertVk(vkEndCommandBuffer(this));
	}
	
	public void record(int flags, Runnable function) {
		record(flags, null, function);
	}
	
	public void record(int flags, Supplier<Object> function) {
		record(flags, null, function);
	}
	
	public void record(int flags, @Nullable VkCommandBufferInheritanceInfo inheritanceInfo, Runnable function) {
		record(flags, inheritanceInfo, () -> {
			function.run();
			return null;
		});
	}
	
	public void record(int flags, @Nullable VkCommandBufferInheritanceInfo inheritanceInfo, Supplier<Object> function) {
		begin(flags, inheritanceInfo);
		Object recordingDependencies = function.get();
		end(recordingDependencies);
	}
	
	public void reset() {
		reset(0);
	}
	
	public void reset(int flags) {
		assertVk(vkResetCommandBuffer(this, flags));
		recordingDependencies = null;
	}
	
	public static class Default extends VkCommandBuffer implements CleanerWrapper {
		
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
			commandPool.releaseCommandBuffer(address);
			return DONE_BARRIER;
		}
	}
}
