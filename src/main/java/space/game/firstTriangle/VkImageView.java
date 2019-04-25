package space.game.firstTriangle;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.vulkan.VkComponentMapping;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import space.engine.buffer.AllocatorStack.Frame;
import space.engine.buffer.pointer.PointerBufferPointer;
import space.engine.freeableStorage.Freeable;
import space.engine.freeableStorage.Freeable.FreeableWrapper;
import space.engine.freeableStorage.FreeableStorage;
import space.engine.sync.barrier.Barrier;

import java.util.function.BiFunction;

import static org.lwjgl.vulkan.VK10.*;
import static space.engine.buffer.Allocator.*;
import static space.engine.lwjgl.LwjglStructAllocator.mallocStruct;
import static space.game.firstTriangle.VkException.assertVk;

public class VkImageView implements FreeableWrapper {
	
	public static final VkComponentMapping SWIZZLE_MASK_IDENTITY = mallocStruct(allocatorHeap(), VkComponentMapping::create, VkComponentMapping.SIZEOF, new Object[] {ROOT_LIST}).set(
			VK_COMPONENT_SWIZZLE_IDENTITY,
			VK_COMPONENT_SWIZZLE_IDENTITY,
			VK_COMPONENT_SWIZZLE_IDENTITY,
			VK_COMPONENT_SWIZZLE_IDENTITY
	);
	
	//alloc
	public static @NotNull VkImageView alloc(@NotNull VkImageViewCreateInfo info, @NotNull VkImage image, @NotNull Object[] parents) {
		try (Frame frame = allocatorStack().frame()) {
			PointerBufferPointer imageViewPtr = PointerBufferPointer.malloc(frame);
			assertVk(nvkCreateImageView(image.device(), info.address(), 0, imageViewPtr.address()));
			return create(imageViewPtr.getPointer(), image, parents);
		}
	}
	
	//create
	public static @NotNull VkImageView create(long imageView, @NotNull VkImage image, @NotNull Object[] parents) {
		return new VkImageView(imageView, image, Storage::new, parents);
	}
	
	public static @NotNull VkImageView wrap(long imageView, @NotNull VkImage image, @NotNull Object[] parents) {
		return new VkImageView(imageView, image, Freeable::createDummy, parents);
	}
	
	//const
	public VkImageView(long address, @NotNull VkImage image, @NotNull BiFunction<VkImageView, Object[], Freeable> storageCreator, @NotNull Object[] parents) {
		this.image = image;
		this.address = address;
		this.storage = storageCreator.apply(this, Freeable.addIfNotContained(parents, image));
	}
	
	//parents
	private final @NotNull VkImage image;
	
	public @NotNull VkImage image() {
		return image;
	}
	
	public @NotNull VkDevice device() {
		return image.device();
	}
	
	public @NotNull VkInstance instance() {
		return image.instance();
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
		
		private final @NotNull VkImage image;
		private final long address;
		
		public Storage(@NotNull VkImageView imageView, @NotNull Object[] parents) {
			super(imageView, parents);
			this.image = imageView.image();
			this.address = imageView.address();
		}
		
		@Override
		protected @NotNull Barrier handleFree() {
			nvkDestroyImageView(image.device(), address, 0);
			return Barrier.ALWAYS_TRIGGERED_BARRIER;
		}
	}
}
