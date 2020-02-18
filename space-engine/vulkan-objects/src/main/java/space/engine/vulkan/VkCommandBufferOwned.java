package space.engine.vulkan;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkCommandBufferInheritanceInfo;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.freeable.Freeable;

import java.util.function.Function;
import java.util.function.Supplier;

import static org.lwjgl.vulkan.VK10.*;
import static space.engine.lwjgl.LwjglStructAllocator.mallocStruct;
import static space.engine.vulkan.VkException.assertVk;

public abstract class VkCommandBufferOwned extends VkCommandBuffer {
	
	public static @NotNull VkCommandBufferOwned wrap(long address, @NotNull VkCommandPool commandPool, @NotNull Object[] parents) {
		return new VkCommandBuffer.Default(address, commandPool, Freeable::createDummy, parents);
	}
	
	protected VkCommandBufferOwned(long handle, VkDevice device) {
		super(handle, device);
	}
	
	//recording
	@SuppressWarnings({"FieldCanBeLocal", "unused"})
	@Nullable
	protected Object recordingDependencies;
	
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
	
	public void record(int flags, Supplier<Object> function) {
		record(flags, null, function);
	}
	
	public void record(int flags, Function<? super VkCommandBufferOwned, Object> function) {
		record(flags, null, function);
	}
	
	public void record(int flags, @Nullable VkCommandBufferInheritanceInfo inheritanceInfo, Supplier<Object> function) {
		record(flags, inheritanceInfo, cmd -> function.get());
	}
	
	public void record(int flags, @Nullable VkCommandBufferInheritanceInfo inheritanceInfo, Function<? super VkCommandBufferOwned, Object> function) {
		begin(flags, inheritanceInfo);
		Object recordingDependencies = function.apply(this);
		end(recordingDependencies);
	}
	
	public void reset() {
		reset(0);
	}
	
	public void reset(int flags) {
		assertVk(vkResetCommandBuffer(this, flags));
		recordingDependencies = null;
	}
}
