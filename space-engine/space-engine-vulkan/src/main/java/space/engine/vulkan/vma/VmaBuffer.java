package space.engine.vulkan.vma;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import space.engine.barrier.Barrier;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.Buffer;
import space.engine.buffer.pointer.PointerBufferPointer;
import space.engine.freeableStorage.Freeable;
import space.engine.freeableStorage.Freeable.FreeableWrapper;
import space.engine.freeableStorage.FreeableStorage;
import space.engine.vulkan.VkBuffer;
import space.engine.vulkan.managed.device.ManagedDevice;
import space.engine.vulkan.managed.device.ManagedQueue;

import java.util.function.BiFunction;

import static org.lwjgl.util.vma.Vma.*;
import static org.lwjgl.vulkan.VK10.*;
import static space.engine.Empties.EMPTY_OBJECT_ARRAY;
import static space.engine.freeableStorage.Freeable.addIfNotContained;
import static space.engine.lwjgl.LwjglStructAllocator.*;
import static space.engine.vulkan.VkException.assertVk;
import static space.engine.vulkan.managed.device.ManagedDevice.QUEUE_TYPE_TRANSFER;

public class VmaBuffer implements VkBuffer, FreeableWrapper {
	
	//alloc
	public static @NotNull VmaBuffer alloc(int flags, long sizeOf, int usage, int memFlags, int memUsage, @NotNull ManagedDevice device, @NotNull Object[] parents) {
		try (AllocatorFrame frame = Allocator.frame()) {
			PointerBufferPointer ptrBuffer = PointerBufferPointer.malloc(frame);
			PointerBufferPointer ptrAllocation = PointerBufferPointer.malloc(frame);
			assertVk(nvmaCreateBuffer(
					device.vmaAllocator().address(),
					mallocStruct(frame, VkBufferCreateInfo::create, VkBufferCreateInfo.SIZEOF).set(
							VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO,
							0,
							flags,
							sizeOf,
							usage,
							VK_SHARING_MODE_EXCLUSIVE,
							null
					).address(),
					mallocStruct(frame, VmaAllocationCreateInfo::create, VmaAllocationCreateInfo.SIZEOF).set(
							memFlags,
							memUsage,
							0,
							0,
							0,
							0,
							0
					).address(),
					ptrBuffer.address(),
					ptrAllocation.address(),
					0
			));
			return create(ptrBuffer.getPointer(), ptrAllocation.getPointer(), sizeOf, device, parents);
		}
	}
	
	//create
	public static @NotNull VmaBuffer create(long address, long vmaAllocation, long sizeOf, @NotNull ManagedDevice device, @NotNull Object[] parents) {
		return new VmaBuffer(address, vmaAllocation, sizeOf, device, Storage::new, parents);
	}
	
	public static @NotNull VmaBuffer wrap(long address, long vmaAllocation, long sizeOf, @NotNull ManagedDevice device, @NotNull Object[] parents) {
		return new VmaBuffer(address, vmaAllocation, sizeOf, device, Freeable::createDummy, parents);
	}
	
	//const
	protected VmaBuffer(long address, long vmaAllocation, long sizeOf, @NotNull ManagedDevice device, @NotNull BiFunction<? super VmaBuffer, Object[], Freeable> storageCreator, @NotNull Object[] parents) {
		this.address = address;
		this.vmaAllocation = vmaAllocation;
		this.sizeOf = sizeOf;
		this.allocator = device.vmaAllocator();
		this.storage = storageCreator.apply(this, addIfNotContained(parents, allocator));
	}
	
	//parents
	protected final @NotNull VmaAllocator allocator;
	
	public VmaAllocator allocator() {
		return allocator;
	}
	
	@Override
	public @NotNull ManagedDevice device() {
		return allocator.device();
	}
	
	//address
	protected final long address;
	protected final long vmaAllocation;
	protected final long sizeOf;
	
	@Override
	public long address() {
		return address;
	}
	
	public long vmaAllocation() {
		return vmaAllocation;
	}
	
	@Override
	public long sizeOf() {
		return sizeOf;
	}
	
	//storage
	private final @NotNull Freeable storage;
	
	@Override
	public @NotNull Freeable getStorage() {
		return storage;
	}
	
	public static class Storage extends FreeableStorage {
		
		private final @NotNull VmaAllocator allocator;
		private final long address;
		private final long allocationAddress;
		
		public Storage(@NotNull VmaBuffer o, @NotNull Object[] parents) {
			super(o, parents);
			this.allocator = o.allocator;
			this.address = o.address;
			this.allocationAddress = o.vmaAllocation;
		}
		
		@Override
		protected @NotNull Barrier handleFree() {
			vmaDestroyBuffer(allocator.address(), address, allocationAddress);
			assertVk();
			return Barrier.DONE_BARRIER;
		}
	}
	
	//uploadData
	@Override
	public @NotNull Barrier uploadData(Buffer src, long srcOffset, long dstOffset, long length) {
		ManagedDevice device = device();
		ManagedQueue transferQueue = device.getQueue(QUEUE_TYPE_TRANSFER, 0);
		
		VmaMappedBuffer mappedBuffer = VmaMappedBuffer.alloc(0, sizeOf, VK_BUFFER_USAGE_TRANSFER_SRC_BIT, 0, VMA_MEMORY_USAGE_CPU_TO_GPU, device, EMPTY_OBJECT_ARRAY);
		mappedBuffer.uploadData(src, srcOffset, dstOffset, length);
		
		Barrier barrierCopyCompleted = transferQueue.recordAndSubmit(cmd -> {
			try (AllocatorFrame frame = Allocator.frame()) {
				vkCmdCopyBuffer(
						cmd,
						mappedBuffer.address,
						this.address,
						allocBuffer(frame, VkBufferCopy::create, VkBufferCopy.SIZEOF, vkBufferCopy -> vkBufferCopy
								.srcOffset(0)
								.dstOffset(0)
								.size(sizeOf)
						)
				);
			}
		});
		barrierCopyCompleted.addHook(mappedBuffer::free);
		return barrierCopyCompleted;
	}
}
