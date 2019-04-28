package space.game.firstTriangle;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import space.engine.buffer.AllocatorStack.Frame;
import space.engine.buffer.array.ArrayBufferPointer;
import space.engine.buffer.pointer.PointerBufferPointer;
import space.engine.freeableStorage.Freeable;
import space.engine.freeableStorage.Freeable.FreeableWrapper;
import space.engine.freeableStorage.FreeableStorage;
import space.engine.sync.barrier.Barrier;

import java.util.function.BiFunction;
import java.util.stream.IntStream;

import static org.lwjgl.vulkan.VK10.*;
import static space.engine.buffer.Allocator.allocatorStack;
import static space.engine.freeableStorage.Freeable.addIfNotContained;
import static space.engine.lwjgl.LwjglStructAllocator.mallocStruct;
import static space.game.firstTriangle.VkException.assertVk;

public class VkCommandPool implements FreeableWrapper {
	
	//alloc
	public static @NotNull VkCommandPool alloc(VkCommandPoolCreateInfo info, @NotNull VkDevice device, @NotNull Object[] parents) {
		try (Frame frame = allocatorStack().frame()) {
			PointerBufferPointer ptr = PointerBufferPointer.malloc(frame);
			assertVk(nvkCreateCommandPool(device, info.address(), 0, ptr.address()));
			return create(ptr.getPointer(), device, parents);
		}
	}
	
	//create
	public static @NotNull VkCommandPool create(long address, @NotNull VkDevice device, @NotNull Object[] parents) {
		return new VkCommandPool(address, device, Storage::new, parents);
	}
	
	public static @NotNull VkCommandPool wrap(long address, @NotNull VkDevice device, @NotNull Object[] parents) {
		return new VkCommandPool(address, device, Freeable::createDummy, parents);
	}
	
	//const
	public VkCommandPool(long address, @NotNull VkDevice device, @NotNull BiFunction<VkCommandPool, Object[], Freeable> storageCreator, @NotNull Object[] parents) {
		this.address = address;
		this.device = device;
		this.storage = storageCreator.apply(this, addIfNotContained(parents, device));
	}
	
	//parents
	//TODO: add parents
	private final @NotNull VkDevice device;
	
	public VkDevice device() {
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
	
	public static class Storage extends FreeableStorage {
		
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
			return Barrier.ALWAYS_TRIGGERED_BARRIER;
		}
	}
	
	//allocCmdBuffer
	public VkCommandBuffer allocCmdBuffer(int level, Object[] parents) {
		try (Frame frame = allocatorStack().frame()) {
			VkCommandBufferAllocateInfo info = mallocStruct(frame, VkCommandBufferAllocateInfo::create, VkCommandBufferAllocateInfo.SIZEOF).set(
					VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO,
					0,
					this.address,
					level,
					1
			);
			
			PointerBufferPointer ptr = PointerBufferPointer.malloc(frame);
			nvkAllocateCommandBuffers(device, info.address(), ptr.address());
			return VkCommandBuffer.wrap(ptr.getPointer(), device, this, parents);
		}
	}
	
	public VkCommandBuffer[] allocCmdBuffers(int level, int count, Object[] parents) {
		try (Frame frame = allocatorStack().frame()) {
			VkCommandBufferAllocateInfo info = mallocStruct(frame, VkCommandBufferAllocateInfo::create, VkCommandBufferAllocateInfo.SIZEOF).set(
					VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO,
					0,
					this.address,
					level,
					count
			);
			
			ArrayBufferPointer ptr = ArrayBufferPointer.malloc(frame, count);
			nvkAllocateCommandBuffers(device, info.address(), ptr.address());
			return IntStream.range(0, count)
							.mapToObj(i -> VkCommandBuffer.wrap(ptr.getPointer(i), device, this, parents))
							.toArray(VkCommandBuffer[]::new);
		}
	}
}
