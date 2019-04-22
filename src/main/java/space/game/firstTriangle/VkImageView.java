package space.game.firstTriangle;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.vulkan.VkComponentMapping;
import org.lwjgl.vulkan.VkImageSubresourceRange;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import space.engine.buffer.AllocatorStack.Frame;
import space.engine.buffer.pointer.PointerBufferPointer;
import space.engine.freeableStorage.Freeable;
import space.engine.freeableStorage.Freeable.FreeableWrapper;
import space.engine.freeableStorage.FreeableStorage;
import space.engine.sync.barrier.Barrier;

import static org.lwjgl.vulkan.VK10.*;
import static space.engine.buffer.Allocator.allocatorStack;
import static space.engine.freeableStorage.Freeable.addIfNotContained;
import static space.engine.lwjgl.LwjglStructAllocator.mallocStruct;

public class VkImageView implements FreeableWrapper {
	
	public static VkImageView create(VkImage image, int viewType, int format, int swizzleR, int swizzleG, int swizzleB, int swizzleA, int aspectMask, int mipLevelBase, int mipLevelCount, int arrayLayerBase, int arrayLayerCount, Object[] parents) {
		try (Frame frame = allocatorStack().frame()) {
			VkImageViewCreateInfo info = mallocStruct(frame, VkImageViewCreateInfo::create, VkImageViewCreateInfo.SIZEOF).set(
					VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO,
					0,
					0,
					image.getImage(),
					viewType,
					format,
					mallocStruct(frame, VkComponentMapping::create, VkComponentMapping.SIZEOF).set(
							swizzleR,
							swizzleG,
							swizzleB,
							swizzleA
					),
					mallocStruct(frame, VkImageSubresourceRange::create, VkImageSubresourceRange.SIZEOF).set(
							aspectMask,
							mipLevelBase,
							mipLevelCount,
							arrayLayerBase,
							arrayLayerCount
					)
			);
			
			PointerBufferPointer imageViewPtr = PointerBufferPointer.malloc(frame);
			nvkCreateImageView(image.getDevice(), info.address(), 0, imageViewPtr.address());
			return new VkImageView(image, imageViewPtr.getPointer(), parents);
		}
	}
	
	public VkImageView(VkImage image, long imageView, Object[] parents) {
		this.storage = new Storage(this, image, imageView, addIfNotContained(parents, image));
		this.image = image;
	}
	
	//image
	private final VkImage image;
	
	public VkImage getImage() {
		return image;
	}
	
	public VkDevice getDevice() {
		return image.getDevice();
	}
	
	//storage
	private final Storage storage;
	
	@Override
	public @NotNull Freeable getStorage() {
		return storage;
	}
	
	public static class Storage extends FreeableStorage {
		
		private final VkImage image;
		private final long imageView;
		
		public Storage(@Nullable Object referent, VkImage image, long imageView, @NotNull Object[] parents) {
			super(referent, parents);
			this.image = image;
			this.imageView = imageView;
		}
		
		@Override
		protected @NotNull Barrier handleFree() {
			nvkDestroyImageView(image.getDevice(), imageView, 0);
			return Barrier.ALWAYS_TRIGGERED_BARRIER;
		}
	}
}
