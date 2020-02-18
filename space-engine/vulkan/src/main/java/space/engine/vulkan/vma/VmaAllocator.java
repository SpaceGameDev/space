package space.engine.vulkan.vma;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.util.vma.VmaAllocatorCreateInfo;
import org.lwjgl.util.vma.VmaVulkanFunctions;
import space.engine.barrier.Barrier;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.pointer.PointerBufferPointer;
import space.engine.freeable.Cleaner;
import space.engine.freeable.Freeable;
import space.engine.freeable.Freeable.CleanerWrapper;
import space.engine.vulkan.VkInstance;
import space.engine.vulkan.managed.device.ManagedDevice;

import java.util.function.BiFunction;

import static org.lwjgl.util.vma.Vma.*;
import static space.engine.freeable.Freeable.addIfNotContained;
import static space.engine.lwjgl.LwjglStructAllocator.mallocStruct;
import static space.engine.vulkan.VkException.assertVk;

public class VmaAllocator implements CleanerWrapper {
	
	//alloc
	public static @NotNull VmaAllocator alloc(@NotNull ManagedDevice device, @NotNull Object[] parents) {
		try (AllocatorFrame frame = Allocator.frame()) {
			return alloc(mallocStruct(frame, VmaAllocatorCreateInfo::create, VmaAllocatorCreateInfo.SIZEOF).set(
					0,
					device.physicalDevice(),
					device,
					0,
					null,
					null,
					0,
					null,
					mallocStruct(frame, VmaVulkanFunctions::create, VmaVulkanFunctions.SIZEOF).set(
							device.instance(), device
					),
					null
			), device, parents);
		}
	}
	
	public static @NotNull VmaAllocator alloc(VmaAllocatorCreateInfo info, @NotNull ManagedDevice device, @NotNull Object[] parents) {
		try (AllocatorFrame frame = Allocator.frame()) {
			PointerBufferPointer ptr = PointerBufferPointer.malloc(frame);
			assertVk(nvmaCreateAllocator(info.address(), ptr.address()));
			return create(ptr.getPointer(), device, parents);
		}
	}
	
	//create
	public static @NotNull VmaAllocator create(long address, @NotNull ManagedDevice device, @NotNull Object[] parents) {
		return new VmaAllocator(address, device, Storage::new, parents);
	}
	
	public static @NotNull VmaAllocator wrap(long address, @NotNull ManagedDevice device, @NotNull Object[] parents) {
		return new VmaAllocator(address, device, Freeable::createDummy, parents);
	}
	
	//const
	protected VmaAllocator(long address, @NotNull ManagedDevice device, @NotNull BiFunction<VmaAllocator, Object[], Freeable> storageCreator, @NotNull Object[] parents) {
		this.address = address;
		this.device = device;
		this.storage = storageCreator.apply(this, addIfNotContained(parents, device));
	}
	
	//parents
	private final @NotNull ManagedDevice device;
	
	public ManagedDevice device() {
		return device;
	}
	
	public VkInstance instance() {
		return device.instance();
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
		
		private final long address;
		
		public Storage(@NotNull VmaAllocator o, @NotNull Object[] parents) {
			super(o, parents);
			this.address = o.address;
		}
		
		@Override
		protected @NotNull Barrier handleFree() {
			vmaDestroyAllocator(address);
			return Barrier.DONE_BARRIER;
		}
	}
}
