package space.engine.vulkan;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.vulkan.VkComponentMapping;
import org.lwjgl.vulkan.VkImageSubresourceRange;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.pointer.PointerBufferPointer;
import space.engine.freeableStorage.Freeable;
import space.engine.freeableStorage.Freeable.FreeableWrapper;
import space.engine.freeableStorage.FreeableStorage;
import space.engine.sync.barrier.Barrier;

import java.util.function.BiFunction;

import static org.lwjgl.vulkan.VK10.*;
import static space.engine.lwjgl.LwjglStructAllocator.mallocStruct;
import static space.engine.vulkan.VkException.assertVk;

public class VkImageView implements FreeableWrapper {
	
	//alloc
	public static @NotNull VkImageView alloc(@NotNull VkImage image, int flags, int imageViewType, int aspectMask, int baseMipLevel, int levelCount, int baseArrayLayer, int layerCount, @NotNull Object[] parents) {
		return alloc(
				image,
				flags,
				imageViewType,
				image.imageFormat(),
				aspectMask,
				baseMipLevel,
				levelCount,
				baseArrayLayer,
				layerCount,
				parents
		);
	}
	
	public static @NotNull VkImageView alloc(@NotNull VkImage image, int flags, int imageViewType, int format, int aspectMask, int baseMipLevel, int levelCount, int baseArrayLayer, int layerCount, @NotNull Object[] parents) {
		return alloc(
				image,
				flags,
				imageViewType,
				format,
				VK_COMPONENT_SWIZZLE_IDENTITY,
				VK_COMPONENT_SWIZZLE_IDENTITY,
				VK_COMPONENT_SWIZZLE_IDENTITY,
				VK_COMPONENT_SWIZZLE_IDENTITY,
				aspectMask,
				baseMipLevel,
				levelCount,
				baseArrayLayer,
				layerCount,
				parents
		);
	}
	
	public static @NotNull VkImageView alloc(@NotNull VkImage image, int flags, int imageViewType, int format, int swizzleR, int swizzleG, int swizzleB, int swizzleA, int aspectMask, int baseMipLevel, int levelCount, int baseArrayLayer, int layerCount, @NotNull Object[] parents) {
		try (AllocatorFrame frame = Allocator.frame()) {
			return alloc(mallocStruct(frame, VkImageViewCreateInfo::create, VkImageViewCreateInfo.SIZEOF).set(
					VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO,
					0,
					flags,
					image.address(),
					imageViewType,
					format,
					mallocStruct(frame, VkComponentMapping::create, VkComponentMapping.SIZEOF).set(
							swizzleR,
							swizzleG,
							swizzleB,
							swizzleA
					),
					mallocStruct(frame, VkImageSubresourceRange::create, VkImageSubresourceRange.SIZEOF).set(
							aspectMask,
							baseMipLevel,
							levelCount,
							baseArrayLayer,
							layerCount
					)
			), image, parents);
		}
	}
	
	public static @NotNull VkImageView alloc(@NotNull VkImageViewCreateInfo info, @NotNull VkImage image, @NotNull Object[] parents) {
		try (AllocatorFrame frame = Allocator.frame()) {
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
