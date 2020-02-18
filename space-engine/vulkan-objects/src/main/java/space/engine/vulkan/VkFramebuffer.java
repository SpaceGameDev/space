package space.engine.vulkan;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
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

public class VkFramebuffer implements CleanerWrapper {
	
	//alloc
	public static @NotNull VkFramebuffer alloc(VkFramebufferCreateInfo info, @NotNull VkDevice device, @NotNull Object[] parents) {
		try (AllocatorFrame frame = Allocator.frame()) {
			PointerBufferPointer ptr = PointerBufferPointer.malloc(frame);
			assertVk(nvkCreateFramebuffer(device, info.address(), 0, ptr.address()));
			return create(ptr.getPointer(), device, parents);
		}
	}
	
	//create
	public static @NotNull VkFramebuffer create(long address, @NotNull VkDevice device, @NotNull Object[] parents) {
		return new VkFramebuffer(address, device, Storage::new, parents);
	}
	
	public static @NotNull VkFramebuffer wrap(long address, @NotNull VkDevice device, @NotNull Object[] parents) {
		return new VkFramebuffer(address, device, Freeable::createDummy, parents);
	}
	
	//const
	public VkFramebuffer(long address, @NotNull VkDevice device, @NotNull BiFunction<VkFramebuffer, Object[], Freeable> storageCreator, @NotNull Object[] parents) {
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
		
		private final @NotNull VkDevice device;
		private final long address;
		
		public Storage(@NotNull VkFramebuffer o, @NotNull Object[] parents) {
			super(o, parents);
			this.device = o.device;
			this.address = o.address;
		}
		
		@Override
		protected @NotNull Barrier handleFree() {
			vkDestroyFramebuffer(device, address, null);
			return Barrier.DONE_BARRIER;
		}
	}
}
