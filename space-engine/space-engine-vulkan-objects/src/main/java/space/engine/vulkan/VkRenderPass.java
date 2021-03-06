package space.engine.vulkan;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import space.engine.barrier.Barrier;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.pointer.PointerBufferPointer;
import space.engine.freeable.Cleaner;
import space.engine.freeable.Freeable;
import space.engine.freeable.Freeable.CleanerWrapper;

import java.util.function.BiFunction;

import static org.lwjgl.vulkan.VK10.*;
import static space.engine.freeable.Freeable.addIfNotContained;
import static space.engine.vulkan.VkException.assertVk;

public class VkRenderPass implements CleanerWrapper {
	
	//alloc
	public static @NotNull VkRenderPass alloc(VkRenderPassCreateInfo info, @NotNull VkDevice device, @NotNull Object[] parents) {
		try (AllocatorFrame frame = Allocator.frame()) {
			PointerBufferPointer ptr = PointerBufferPointer.malloc(frame);
			assertVk(nvkCreateRenderPass(device, info.address(), 0, ptr.address()));
			return create(ptr.getPointer(), device, parents);
		}
	}
	
	//create
	public static @NotNull VkRenderPass create(long address, @NotNull VkDevice device, @NotNull Object[] parents) {
		return new VkRenderPass(address, device, Storage::new, parents);
	}
	
	public static @NotNull VkRenderPass wrap(long address, @NotNull VkDevice device, @NotNull Object[] parents) {
		return new VkRenderPass(address, device, Freeable::createDummy, parents);
	}
	
	//const
	public VkRenderPass(long address, @NotNull VkDevice device, @NotNull BiFunction<VkRenderPass, Object[], Freeable> storageCreator, @NotNull Object[] parents) {
		this.address = address;
		this.device = device;
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
		private final @NotNull VkDevice device;
		
		public Storage(@NotNull VkRenderPass o, @NotNull Object[] parents) {
			super(o, parents);
			this.address = o.address();
			this.device = o.device();
		}
		
		@Override
		protected @NotNull Barrier handleFree() {
			vkDestroyRenderPass(device, address, null);
			return Barrier.DONE_BARRIER;
		}
	}
}
