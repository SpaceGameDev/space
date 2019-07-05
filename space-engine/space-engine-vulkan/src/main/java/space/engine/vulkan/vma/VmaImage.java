package space.engine.vulkan.vma;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.VkBufferImageCopy;
import org.lwjgl.vulkan.VkExtent3D;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkImageSubresourceLayers;
import org.lwjgl.vulkan.VkOffset3D;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.Buffer;
import space.engine.buffer.pointer.PointerBufferPointer;
import space.engine.freeableStorage.Freeable;
import space.engine.freeableStorage.Freeable.FreeableWrapper;
import space.engine.freeableStorage.FreeableStorage;
import space.engine.sync.barrier.Barrier;
import space.engine.vulkan.VkImage;
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

public class VmaImage implements VkImage, FreeableWrapper {
	
	//alloc
	public static @NotNull VmaImage alloc(int flags, int imageType, int imageFormat, int width, int height, int depth, int mipLevels, int arrayLayers, int samples, int tiling, int usage, int initialLayout, int memFlags, int memUsage, @NotNull ManagedDevice device, @NotNull Object[] parents) {
		try (AllocatorFrame frame = Allocator.frame()) {
			PointerBufferPointer ptrImage = PointerBufferPointer.malloc(frame);
			PointerBufferPointer ptrAllocation = PointerBufferPointer.malloc(frame);
			assertVk(nvmaCreateImage(
					device.vmaAllocator().address(),
					mallocStruct(frame, VkImageCreateInfo::create, VkImageCreateInfo.SIZEOF).set(
							VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO,
							0,
							flags,
							imageType,
							imageFormat,
							mallocStruct(frame, VkExtent3D::create, VkExtent3D.SIZEOF)
									.width(width)
									.height(height)
									.depth(depth),
							mipLevels,
							arrayLayers,
							samples,
							tiling,
							usage,
							VK_SHARING_MODE_EXCLUSIVE,
							null,
							initialLayout
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
					ptrImage.address(),
					ptrAllocation.address(),
					0
			));
			return create(ptrImage.getPointer(), ptrAllocation.getPointer(), imageType, imageFormat, width, height, depth, mipLevels, arrayLayers, samples, tiling, device, parents);
		}
	}
	
	//create
	public static @NotNull VmaImage create(long address, long vmaAllocation, int imageType, int imageFormat, int width, int height, int depth, int mipLevels, int arrayLayers, int samples, int tiling, @NotNull ManagedDevice device, @NotNull Object[] parents) {
		return new VmaImage(address, vmaAllocation, imageType, imageFormat, width, height, depth, mipLevels, arrayLayers, samples, tiling, device, Storage::new, parents);
	}
	
	public static @NotNull VmaImage wrap(long address, long vmaAllocation, int imageType, int imageFormat, int width, int height, int depth, int mipLevels, int arrayLayers, int samples, int tiling, @NotNull ManagedDevice device, @NotNull Object[] parents) {
		return new VmaImage(address, vmaAllocation, imageType, imageFormat, width, height, depth, mipLevels, arrayLayers, samples, tiling, device, Freeable::createDummy, parents);
	}
	
	//const
	protected VmaImage(long address, long vmaAllocation, int imageType, int imageFormat, int width, int height, int depth, int mipLevels, int arrayLayers, int samples, int tiling, @NotNull ManagedDevice device, @NotNull BiFunction<? super VmaImage, Object[], Freeable> storageCreator, @NotNull Object[] parents) {
		this.address = address;
		this.vmaAllocation = vmaAllocation;
		this.imageType = imageType;
		this.imageFormat = imageFormat;
		this.width = width;
		this.height = height;
		this.depth = depth;
		this.mipLevels = mipLevels;
		this.arrayLayers = arrayLayers;
		this.samples = samples;
		this.tiling = tiling;
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
	protected final int imageType, imageFormat, width, height, depth, mipLevels, arrayLayers, samples, tiling;
	
	@Override
	public long address() {
		return address;
	}
	
	public long vmaAllocation() {
		return vmaAllocation;
	}
	
	@Override
	public int imageType() {
		return imageType;
	}
	
	@Override
	public int imageFormat() {
		return imageFormat;
	}
	
	@Override
	public int width() {
		return width;
	}
	
	@Override
	public int height() {
		return height;
	}
	
	@Override
	public int depth() {
		return depth;
	}
	
	@Override
	public int mipLevels() {
		return mipLevels;
	}
	
	@Override
	public int arrayLayers() {
		return arrayLayers;
	}
	
	@Override
	public int samples() {
		return samples;
	}
	
	@Override
	public int tiling() {
		return tiling;
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
		
		public Storage(@NotNull VmaImage o, @NotNull Object[] parents) {
			super(o, parents);
			this.allocator = o.allocator;
			this.address = o.address;
			this.allocationAddress = o.vmaAllocation;
		}
		
		@Override
		protected @NotNull Barrier handleFree() {
			vmaDestroyImage(allocator.address(), address, allocationAddress);
			return Barrier.ALWAYS_TRIGGERED_BARRIER;
		}
	}
	
	//uploadData
	@Override
	public @NotNull Barrier uploadData(@NotNull Buffer data, int bufferRowLength, int bufferImageHeight, int dstImageLayout, int aspectMask, int mipLevel, int baseArrayLayer, int layerCount) {
		ManagedDevice device = device();
		ManagedQueue transferQueue = device.getQueue(QUEUE_TYPE_TRANSFER, 0);
		
		VmaMappedBuffer mappedBuffer = VmaMappedBuffer.alloc(0, data.sizeOf(), VK_BUFFER_USAGE_TRANSFER_SRC_BIT, 0, VMA_MEMORY_USAGE_CPU_TO_GPU, device, EMPTY_OBJECT_ARRAY);
		mappedBuffer.uploadData(data);
		
		Barrier barrierCopyCompleted = transferQueue.recordAndSubmit(cmd -> {
			try (AllocatorFrame frame = Allocator.frame()) {
				vkCmdCopyBufferToImage(
						cmd,
						0,
						this.address,
						dstImageLayout,
						allocBuffer(frame, VkBufferImageCopy::create, VkBufferImageCopy.SIZEOF, vkBufferImageCopy -> vkBufferImageCopy.set(
								0,
								bufferRowLength,
								bufferImageHeight,
								mallocStruct(frame, VkImageSubresourceLayers::create, VkImageSubresourceLayers.SIZEOF).set(
										aspectMask,
										mipLevel,
										baseArrayLayer,
										layerCount
								),
								mallocStruct(frame, VkOffset3D::create, VkOffset3D.SIZEOF).set(
										0, 0, 0
								),
								mallocStruct(frame, VkExtent3D::create, VkExtent3D.SIZEOF).set(
										width, height, depth
								)
						))
				);
			}
		});
		barrierCopyCompleted.addHook(mappedBuffer::free);
		return barrierCopyCompleted;
	}
}
