package space.game.firstTriangle;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.vulkan.VkComponentMapping;
import org.lwjgl.vulkan.VkImageSubresourceRange;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import space.engine.buffer.AllocatorStack.Frame;
import space.engine.buffer.pointer.PointerBufferPointer;
import space.engine.freeableStorage.Freeable;
import space.engine.freeableStorage.Freeable.FreeableWrapper;
import space.engine.freeableStorage.FreeableStorage;
import space.engine.sync.barrier.Barrier;

import java.util.function.BiFunction;

import static org.lwjgl.vulkan.VK10.*;
import static space.engine.buffer.Allocator.allocatorStack;
import static space.engine.lwjgl.LwjglStructAllocator.mallocStruct;
import static space.game.firstTriangle.VkException.assertVk;

public class VkImageView implements FreeableWrapper {
	
	//alloc
	public static VkImageView alloc(VkImage image, int viewType, int format, int swizzleR, int swizzleG, int swizzleB, int swizzleA, int aspectMask, int mipLevelBase, int mipLevelCount, int arrayLayerBase, int arrayLayerCount, Object[] parents) {
		try (Frame frame = allocatorStack().frame()) {
			VkImageViewCreateInfo info = mallocStruct(frame, VkImageViewCreateInfo::create, VkImageViewCreateInfo.SIZEOF).set(
					VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO,
					0,
					0,
					image.address(),
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
			assertVk(nvkCreateImageView(image.device(), info.address(), 0, imageViewPtr.address()));
			return create(image, imageViewPtr.getPointer(), parents);
		}
	}
	
	//create
	public static VkImageView create(VkImage image, long imageView, Object[] parents) {
		return new VkImageView(image, imageView, Storage::new, parents);
	}
	
	public static VkImageView wrap(VkImage image, long imageView, Object[] parents) {
		return new VkImageView(image, imageView, Freeable::createDummy, parents);
	}
	
	//const
	public VkImageView(VkImage image, long address, BiFunction<VkImageView, Object[], Freeable> storageCreator, Object[] parents) {
		this.image = image;
		this.address = address;
		this.storage = storageCreator.apply(this, Freeable.addIfNotContained(parents, image));
	}
	
	//parents
	private final VkImage image;
	
	public VkImage image() {
		return image;
	}
	
	public VkDevice device() {
		return image.device();
	}
	
	public VkInstance instance() {
		return image.instance();
	}
	
	//address
	private final long address;
	
	public long address() {
		return address;
	}
	
	//storage
	private final Freeable storage;
	
	@Override
	public @NotNull Freeable getStorage() {
		return storage;
	}
	
	public static class Storage extends FreeableStorage {
		
		private final VkImage image;
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
