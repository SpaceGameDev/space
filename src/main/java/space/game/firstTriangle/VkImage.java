package space.game.firstTriangle;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkImageCreateInfo;
import space.engine.buffer.AllocatorStack.Frame;
import space.engine.buffer.pointer.PointerBufferPointer;
import space.engine.freeableStorage.Freeable;
import space.engine.freeableStorage.Freeable.FreeableWrapper;
import space.engine.freeableStorage.FreeableStorage;
import space.engine.sync.barrier.Barrier;

import java.util.function.BiFunction;

import static org.lwjgl.vulkan.VK10.nvkDestroyImage;
import static space.engine.buffer.Allocator.allocatorStack;
import static space.engine.freeableStorage.Freeable.addIfNotContained;
import static space.game.firstTriangle.VkException.assertVk;

public class VkImage implements FreeableWrapper {
	
	//alloc
	public static @NotNull VkImage alloc(@NotNull VkImageCreateInfo info, @NotNull VkDevice device, @NotNull Object[] parents) {
		try (Frame frame = allocatorStack().frame()) {
			PointerBufferPointer vkImagePtr = PointerBufferPointer.malloc(frame);
			assertVk(VK10.nvkCreateImage(device, info.address(), 0, vkImagePtr.address()));
			return new VkImage(vkImagePtr.getPointer(), device, Storage::new, parents);
		}
	}
	
	//create
	public static @NotNull VkImage create(long address, @NotNull VkDevice device, @NotNull Object[] parents) {
		return new VkImage(address, device, Storage::new, parents);
	}
	
	public static @NotNull VkImage wrap(long address, @NotNull VkDevice device, @NotNull Object[] parents) {
		return new VkImage(address, device, Freeable::createDummy, parents);
	}
	
	//const
	public VkImage(long address, @NotNull VkDevice device, @NotNull BiFunction<VkImage, Object[], Freeable> storageCreator, @NotNull Object[] parents) {
		this.device = device;
		this.address = address;
		this.storage = storageCreator.apply(this, addIfNotContained(parents, device));
	}
	
	//parents
	private final @NotNull VkDevice device;
	
	public @NotNull VkDevice device() {
		return device;
	}
	
	public @NotNull VkInstance instance() {
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
		
		public Storage(@NotNull VkImage image, @NotNull Object[] parents) {
			super(image, parents);
			this.device = image.device();
			this.address = image.address();
		}
		
		@Override
		protected @NotNull Barrier handleFree() {
			nvkDestroyImage(device, address, 0);
			return Barrier.ALWAYS_TRIGGERED_BARRIER;
		}
	}
}
