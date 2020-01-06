package space.engine.vulkan;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferInheritanceInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import space.engine.barrier.Barrier;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.array.ArrayBufferLong;
import space.engine.buffer.array.ArrayBufferPointer;
import space.engine.buffer.pointer.PointerBufferLong;
import space.engine.buffer.pointer.PointerBufferPointer;
import space.engine.freeable.Cleaner;
import space.engine.freeable.Freeable;
import space.engine.freeable.Freeable.CleanerWrapper;
import space.engine.simpleQueue.pool.ThreadBound;
import space.engine.vulkan.VkCommandBuffer.Default;
import space.engine.vulkan.VkCommandBuffer.DestroyStorage;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.lwjgl.vulkan.VK10.*;
import static space.engine.barrier.Barrier.*;
import static space.engine.freeable.Freeable.addIfNotContained;
import static space.engine.lwjgl.LwjglStructAllocator.mallocStruct;
import static space.engine.vulkan.VkException.assertVk;

public class VkCommandPool implements CleanerWrapper {
	
	//alloc
	public static @NotNull VkCommandPool alloc(int flags, VkQueueFamilyProperties queueFamily, @NotNull VkDevice device, @NotNull Object[] parents) {
		try (AllocatorFrame frame = Allocator.frame()) {
			return alloc(mallocStruct(frame, VkCommandPoolCreateInfo::create, VkCommandPoolCreateInfo.SIZEOF).set(
					VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO,
					0,
					flags,
					queueFamily.index()
			), device, parents);
		}
	}
	
	public static @NotNull VkCommandPool alloc(VkCommandPoolCreateInfo info, @NotNull VkDevice device, @NotNull Object[] parents) {
		try (AllocatorFrame frame = Allocator.frame()) {
			PointerBufferPointer ptr = PointerBufferPointer.malloc(frame);
			assertVk(nvkCreateCommandPool(device, info.address(), 0, ptr.address()));
			return create(ptr.getPointer(), device, (info.flags() & VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT) != 0, parents);
		}
	}
	
	//create
	public static @NotNull VkCommandPool create(long address, @NotNull VkDevice device, boolean allowReset, @NotNull Object[] parents) {
		return new VkCommandPool(address, device, allowReset, Storage::new, parents);
	}
	
	public static @NotNull VkCommandPool wrap(long address, @NotNull VkDevice device, boolean allowReset, @NotNull Object[] parents) {
		return new VkCommandPool(address, device, allowReset, Freeable::createDummy, parents);
	}
	
	//const
	public VkCommandPool(long address, @NotNull VkDevice device, boolean allowReset, @NotNull BiFunction<VkCommandPool, Object[], Freeable> storageCreator, @NotNull Object[] parents) {
		this.address = address;
		this.device = device;
		this.owner = Thread.currentThread();
		this.allowReset = allowReset;
		this.storage = storageCreator.apply(this, addIfNotContained(parents, device));
	}
	
	//parents
	private final @NotNull VkDevice device;
	
	public VkDevice device() {
		return device;
	}
	
	public VkInstance instance() {
		return device.instance();
	}
	
	public final @NotNull Thread owner;
	
	public @NotNull Thread owner() {
		return owner;
	}
	
	public void validateThread() {
		Thread currThread = Thread.currentThread();
		if (currThread != owner)
			throw new IllegalCallerException("Owned by Thread " + owner + " but was called from a different Thread " + currThread);
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
	
	public static class Storage extends Cleaner {
		
		private final @NotNull VkDevice device;
		private final long address;
		
		public Storage(@NotNull VkCommandPool o, @NotNull Object[] parents) {
			super(o, parents);
			this.device = o.device;
			this.address = o.address;
		}
		
		@Override
		protected @NotNull Barrier handleFree() {
			vkDestroyCommandPool(device, address, null);
			return Barrier.DONE_BARRIER;
		}
	}
	
	//allocCommandBuffer
	private final boolean allowReset;
	
	public boolean allowReset() {
		return allowReset;
	}
	
	//alloc
	public @NotNull VkCommandBufferOwned allocCommandBuffer(int level, @NotNull Object[] parents) {
		validateThread();
		try (AllocatorFrame frame = Allocator.frame()) {
			VkCommandBufferAllocateInfo info = mallocStruct(frame, VkCommandBufferAllocateInfo::create, VkCommandBufferAllocateInfo.SIZEOF).set(
					VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO,
					0,
					this.address(),
					level,
					1
			);
			PointerBufferPointer ptr = PointerBufferPointer.malloc(frame);
			synchronized (this) {
				assertVk(nvkAllocateCommandBuffers(device(), info.address(), ptr.address()));
			}
			return new Default(ptr.getPointer(), this, allowReset ? DestroyStorage::new : Freeable::createDummy, parents);
		}
	}
	
	public @NotNull VkCommandBuffer[] allocCommandBuffers(int level, int count, @NotNull Object[] parents) {
		validateThread();
		try (AllocatorFrame frame = Allocator.frame()) {
			VkCommandBuffer[] ret = new VkCommandBuffer[count];
			
			VkCommandBufferAllocateInfo info = mallocStruct(frame, VkCommandBufferAllocateInfo::create, VkCommandBufferAllocateInfo.SIZEOF).set(
					VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO,
					0,
					this.address(),
					level,
					count
			);
			ArrayBufferPointer ptr = ArrayBufferPointer.malloc(frame, count);
			synchronized (this) {
				assertVk(nvkAllocateCommandBuffers(device(), info.address(), ptr.address()));
			}
			for (int i = 0; i < count; i++)
				ret[i] = new Default(ptr.getPointer(i), this, allowReset ? DestroyStorage::new : Freeable::createDummy, parents);
			return ret;
		}
	}
	
	//allocAndRecord
	public @NotNull VkCommandBuffer allocAndRecordCommandBuffer(int level, Object[] parents, int recordFlags, Function<? super VkCommandBufferOwned, Object> function) {
		return allocAndRecordCommandBuffer(level, parents, recordFlags, null, function);
	}
	
	public @NotNull VkCommandBuffer allocAndRecordCommandBuffer(int level, Object[] parents, int recordFlags, @Nullable VkCommandBufferInheritanceInfo inheritanceInfo, Function<? super VkCommandBufferOwned, Object> record) {
		VkCommandBufferOwned cmd = allocCommandBuffer(level, parents);
		cmd.record(recordFlags, inheritanceInfo, record);
		return cmd.deown();
	}
	
	//release
	public Barrier releaseCommandBuffer(@NotNull VkCommandBuffer commandBuffer) {
		return releaseCommandBuffer(commandBuffer.address());
	}
	
	public Barrier releaseCommandBuffer(long commandBuffer) {
		if (Thread.currentThread() == owner) {
			releaseCommandBufferInternal(commandBuffer);
			return done();
		} else {
			return nowRun(ThreadBound.getQueue(owner), () -> releaseCommandBufferInternal(commandBuffer));
		}
	}
	
	private void releaseCommandBufferInternal(long commandBuffer) {
		try (AllocatorFrame frame = Allocator.frame()) {
			PointerBufferLong ptr = PointerBufferLong.alloc(frame, commandBuffer);
			nvkFreeCommandBuffers(device, this.address(), 1, ptr.address());
			assertVk();
		}
	}
	
	public Barrier releaseCommandBuffers(@NotNull VkCommandBuffer[] commandBuffers) {
		return releaseCommandBuffers(Arrays.stream(commandBuffers).mapToLong(VkCommandBuffer::address).toArray());
	}
	
	public Barrier releaseCommandBuffers(long[] commandBuffers) {
		if (Thread.currentThread() == owner) {
			releaseCommandBuffersInternal(commandBuffers);
			return done();
		} else {
			return nowRun(ThreadBound.getQueue(owner), () -> releaseCommandBuffersInternal(commandBuffers));
		}
	}
	
	private void releaseCommandBuffersInternal(long[] commandBuffers) {
		try (AllocatorFrame frame = Allocator.frame()) {
			ArrayBufferLong ptrs = ArrayBufferLong.alloc(frame, commandBuffers);
			nvkFreeCommandBuffers(device, this.address(), commandBuffers.length, ptrs.address());
			assertVk();
		}
	}
}
