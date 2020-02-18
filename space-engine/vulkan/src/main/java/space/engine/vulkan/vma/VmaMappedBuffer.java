package space.engine.vulkan.vma;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import space.engine.barrier.Barrier;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.Buffer;
import space.engine.buffer.pointer.PointerBufferPointer;
import space.engine.freeable.Cleaner;
import space.engine.freeable.Freeable;
import space.engine.freeable.stack.FreeableStack.Frame;
import space.engine.vulkan.VkMappedBuffer;
import space.engine.vulkan.managed.device.ManagedDevice;

import java.util.function.BiFunction;

import static org.lwjgl.util.vma.Vma.*;
import static org.lwjgl.vulkan.VK10.*;
import static space.engine.barrier.Barrier.DONE_BARRIER;
import static space.engine.freeable.Freeable.addIfNotContained;
import static space.engine.lwjgl.LwjglStructAllocator.mallocStruct;
import static space.engine.vulkan.VkException.assertVk;

public class VmaMappedBuffer extends VmaBuffer implements VkMappedBuffer {
	
	//alloc
	public static @NotNull VmaMappedBuffer alloc(int flags, long sizeOf, int usage, int memFlags, int memUsage, @NotNull ManagedDevice device, @NotNull Object[] parents) {
		if (memUsage == VMA_MEMORY_USAGE_GPU_ONLY)
			throw new IllegalArgumentException("Mapped buffers with memUsage of VMA_MEMORY_USAGE_GPU_ONLY are not guaranteed visible to the CPU!");
		
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
							-1,
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
	public static @NotNull VmaMappedBuffer create(long address, long vmaAllocation, long sizeOf, @NotNull ManagedDevice device, @NotNull Object[] parents) {
		return new VmaMappedBuffer(address, vmaAllocation, sizeOf, device, Storage::new, parents);
	}
	
	public static @NotNull VmaMappedBuffer wrap(long address, long vmaAllocation, long sizeOf, @NotNull ManagedDevice device, @NotNull Object[] parents) {
		return new VmaMappedBuffer(address, vmaAllocation, sizeOf, device, Freeable::createDummy, parents);
	}
	
	//object
	protected VmaMappedBuffer(long address, long vmaAllocation, long sizeOf, @NotNull ManagedDevice device, @NotNull BiFunction<? super VmaMappedBuffer, Object[], Freeable> storageCreator, @NotNull Object[] parents) {
		super(address, vmaAllocation, sizeOf, device, (vmaBuffer, objects) -> storageCreator.apply((VmaMappedBuffer) vmaBuffer, objects), parents);
	}
	
	//uploadData
	
	/**
	 * Upload will be completed as soon as this method returns. Always returns {@link Barrier#DONE_BARRIER}.
	 */
	@Override
	public @NotNull Barrier uploadData(Buffer src, long srcOffset, long dstOffset, long length) {
		try (Frame frame = Freeable.frame()) {
			MappedBuffer dst = mapMemory(new Object[] {frame});
			Buffer.copyMemory(src, srcOffset, dst, dstOffset, length);
			return DONE_BARRIER;
		}
	}
	
	//mapping
	@Override
	public MappedBuffer mapMemory(Object[] parents) {
		try (AllocatorFrame frame = Allocator.frame()) {
			PointerBufferPointer ptr = PointerBufferPointer.malloc(frame);
			assertVk(nvmaMapMemory(allocator.address(), vmaAllocation, ptr.address()));
			return new MappedBuffer(ptr.getPointer(), parents);
		}
	}
	
	public class MappedBuffer extends Buffer implements CleanerWrapper {
		
		private final long address;
		
		public MappedBuffer(long address, Object[] parents) {
			this.address = address;
			this.storage = new MappedBufferStorage(this, addIfNotContained(parents, VmaMappedBuffer.this));
		}
		
		@Override
		public long address() {
			return address;
		}
		
		@Override
		public long sizeOf() {
			return sizeOf;
		}
		
		//storage
		private final MappedBufferStorage storage;
		
		@Override
		public @NotNull Freeable getStorage() {
			return storage;
		}
		
		//getter
		public VmaMappedBuffer vkBuffer() {
			return VmaMappedBuffer.this;
		}
	}
	
	public static class MappedBufferStorage extends Cleaner {
		
		private final VmaMappedBuffer vkBuffer;
		
		public MappedBufferStorage(@NotNull MappedBuffer mappedBuffer, @NotNull Object[] parents) {
			super(mappedBuffer, parents);
			this.vkBuffer = mappedBuffer.vkBuffer();
		}
		
		@Override
		protected @NotNull Barrier handleFree() {
			vmaUnmapMemory(vkBuffer.allocator.address(), vkBuffer.vmaAllocation);
			return DONE_BARRIER;
		}
	}
}
