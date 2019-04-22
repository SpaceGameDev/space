package space.game.firstTriangle.surface;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;
import space.engine.buffer.AllocatorStack.Frame;
import space.engine.buffer.array.ArrayBufferInt;
import space.engine.buffer.array.ArrayBufferPointer;
import space.engine.buffer.pointer.PointerBufferInt;
import space.engine.buffer.pointer.PointerBufferPointer;
import space.engine.freeableStorage.Freeable;
import space.engine.freeableStorage.Freeable.FreeableWrapper;
import space.engine.freeableStorage.FreeableStorage;
import space.engine.sync.barrier.Barrier;
import space.game.firstTriangle.VkDevice;
import space.game.firstTriangle.VkImage;
import space.game.firstTriangle.VkImageView;
import space.game.firstTriangle.VkQueueFamilyProperties;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;
import static space.engine.buffer.Allocator.*;
import static space.engine.freeableStorage.Freeable.addIfNotContained;
import static space.engine.lwjgl.LwjglStructAllocator.mallocStruct;
import static space.game.firstTriangle.VkException.assertVk;

public class VkSwapChain implements FreeableWrapper {
	
	public static Builder builder(VkDevice device, VkSurfaceSwapChainDetails swapChainDetails) {
		return new Builder(device, swapChainDetails);
	}
	
	public static class Builder {
		
		public Builder(VkDevice device, VkSurfaceSwapChainDetails swapChainDetails) {
			this.device = device;
			this.swapChainDetails = swapChainDetails;
			this.imageCount = swapChainDetails.capabilities().minImageCount() + 1;
			this.transform = swapChainDetails.capabilities().currentTransform();
		}
		
		//device
		private final VkDevice device;
		
		public VkDevice getDevice() {
			return device;
		}
		
		//swapChainDetails
		private final VkSurfaceSwapChainDetails swapChainDetails;
		
		public VkSurfaceSwapChainDetails getSwapChainDetails() {
			return swapChainDetails;
		}
		
		//imageCount
		private int imageCount;
		
		private Builder setImageCount(int imageCount) {
			if (swapChainDetails.capabilities().minImageCount() > imageCount || imageCount > swapChainDetails.capabilities().maxImageCount())
				throw new IllegalArgumentException();
			this.imageCount = imageCount;
			return this;
		}
		
		public int getImageCount() {
			return imageCount;
		}
		
		//imageFormat
		private int imageFormat = -1;
		private int imageColorSpace = -1;
		
		public Builder setImageFormat(int imageFormat, int imageColorSpace) {
			if (!swapChainDetails.isSurfaceFormatSupported(imageFormat, imageColorSpace))
				throw new IllegalArgumentException("format not supported by swapchain!");
			this.imageFormat = imageFormat;
			this.imageColorSpace = imageColorSpace;
			return this;
		}
		
		public int getImageFormat() {
			return imageFormat;
		}
		
		public int getImageColorSpace() {
			return imageColorSpace;
		}
		
		//swapExtend
		private int swapExtendWidth = -1;
		private int swapExtendHeight = -1;
		
		public Builder setSwapExtend(int width, int height) {
			this.swapExtendWidth = width;
			this.swapExtendHeight = height;
			return this;
		}
		
		public Builder setSwapExtendAutomatic() {
			return setSwapExtend(-1, -1);
		}
		
		public int getSwapExtendWidth() {
			return swapExtendWidth;
		}
		
		public int getSwapExtendHeight() {
			return swapExtendHeight;
		}
		
		//imageArrayLayers
		private int imageArrayLayers = 1;
		
		public Builder setImageArrayLayers(int imageArrayLayers) {
			if (imageArrayLayers > swapChainDetails.capabilities().maxImageArrayLayers())
				throw new IllegalArgumentException("imageArrayLayers above capabilities!");
			this.imageArrayLayers = imageArrayLayers;
			return this;
		}
		
		public int getImageArrayLayers() {
			return imageArrayLayers;
		}
		
		//imageUsageBit
		private int imageUsageBit = -1;
		
		public Builder setImageUsageBit(int imageUsageBit) {
			this.imageUsageBit = imageUsageBit;
			return this;
		}
		
		public int getImageUsageBit() {
			return imageUsageBit;
		}
		
		//sharingMode
		private int imageSharingMode = -1;
		private @Nullable int[] imageSharingQueueIndices;
		
		public Builder setImageSharingMode(int imageSharingMode, @Nullable int[] imageSharingQueueIndices) {
			this.imageSharingMode = imageSharingMode;
			this.imageSharingQueueIndices = imageSharingQueueIndices;
			return this;
		}
		
		public Builder setImageSharingModeExclusive() {
			return setImageSharingMode(VK_SHARING_MODE_EXCLUSIVE, null);
		}
		
		public Builder setImageSharingModeConcurrent(int[] imageSharingQueueIndices) {
			return setImageSharingMode(VK_SHARING_MODE_CONCURRENT, imageSharingQueueIndices);
		}
		
		public Builder setImageSharingModeConcurrent(VkQueueFamilyProperties[] imageSharingQueueIndices) {
			return setImageSharingMode(VK_SHARING_MODE_CONCURRENT, Arrays.stream(imageSharingQueueIndices).mapToInt(VkQueueFamilyProperties::index).toArray());
		}
		
		public int getImageSharingMode() {
			return imageSharingMode;
		}
		
		public @Nullable int[] getImageSharingQueueIndices() {
			return imageSharingQueueIndices;
		}
		
		//transform
		private int transform;
		
		public Builder setTransform(int transform) {
			this.transform = transform;
			return this;
		}
		
		public int getTransform() {
			return transform;
		}
		
		//alpha
		private boolean enableAlphaChannel = false;
		
		public Builder setEnableAlphaChannel(boolean enableAlphaChannel) {
			this.enableAlphaChannel = enableAlphaChannel;
			return this;
		}
		
		public boolean isAlphaChannelEnabled() {
			return enableAlphaChannel;
		}
		
		//presentMode
		private int presentMode = VK_PRESENT_MODE_FIFO_KHR;
		
		public Builder setPresentMode(int presentMode) {
			this.presentMode = presentMode;
			return this;
		}
		
		public Builder setBestPresentMode(int[] presentModesToChooseFrom) {
			return setPresentMode(swapChainDetails.getBestPresentMode(presentModesToChooseFrom));
		}
		
		public int getPresentMode() {
			return presentMode;
		}
		
		//clipped
		private boolean clipped = true;
		
		public Builder setClipped(boolean clipped) {
			this.clipped = clipped;
			return this;
		}
		
		public boolean isClipped() {
			return clipped;
		}
		
		//oldSwapChain
		private long oldSwapChain = 0;
		
		public Builder setOldSwapChain(long oldSwapChain) {
			this.oldSwapChain = oldSwapChain;
			return this;
		}
		
		public long getOldSwapChain() {
			return oldSwapChain;
		}
		
		//build
		public void validate() {
			if (imageFormat == -1 || imageColorSpace == -1)
				throw new IllegalStateException("imageFormat and imageColorSpace not set!");
			if (swapExtendWidth == -1 || swapExtendHeight == -1)
				throw new IllegalStateException("swapExtendWidth and swapExtendHeight not set!");
			if (imageUsageBit == -1)
				throw new IllegalStateException("imageUsageBit not set!");
		}
		
		public VkSwapChain build(Object[] parents) {
			validate();
			try (Frame frame = allocatorStack().frame()) {
				VkSwapchainCreateInfoKHR info = mallocStruct(frame, VkSwapchainCreateInfoKHR::create, VkSwapchainCreateInfoKHR.SIZEOF).set(
						VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR,
						0,
						0,
						swapChainDetails.getSurface().getSurface(),
						imageCount,
						imageFormat,
						imageColorSpace,
						mallocStruct(frame, VkExtent2D::create, VkExtent2D.SIZEOF).set(
								swapExtendWidth, swapExtendHeight
						),
						1,
						imageUsageBit,
						imageSharingMode,
						imageSharingQueueIndices == null ? null : ArrayBufferInt.alloc(frame, imageSharingQueueIndices).nioBuffer(),
						transform,
						enableAlphaChannel ? VK_COMPOSITE_ALPHA_INHERIT_BIT_KHR : VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR,
						presentMode,
						clipped,
						oldSwapChain
				);
				
				PointerBufferPointer swapChainPtr = PointerBufferPointer.malloc(frame);
				assertVk(nvkCreateSwapchainKHR(device, info.address(), 0, swapChainPtr.address()));
				return new VkSwapChain(device, swapChainDetails, swapChainPtr.getPointer(), imageFormat, parents);
			}
		}
	}
	
	public VkSwapChain(VkDevice device, VkSurfaceSwapChainDetails swapChainDetails, long swapChain, int imageFormat, @NotNull Object[] parents) {
		this.device = device;
		this.swapChainDetails = swapChainDetails;
		this.storage = new Storage(this, device, swapChain, addIfNotContained(parents, device));
		this.swapChain = swapChain;
		
		//images
		try (Frame frame = allocatorStack().frame()) {
			PointerBufferInt count = PointerBufferInt.malloc(frame);
			ArrayBufferPointer imagesBuffer;
			while (true) {
				assertVk(nvkGetSwapchainImagesKHR(device, swapChain, count.address(), 0));
				imagesBuffer = ArrayBufferPointer.malloc(allocatorHeap(), count.getInt(), new Object[] {frame});
				if (assertVk(nvkGetSwapchainImagesKHR(device, swapChain, count.address(), imagesBuffer.address())) == VK_SUCCESS)
					break;
				Freeable.freeObject(imagesBuffer);
			}
			
			this.images = imagesBuffer.stream().mapToObj(ptr -> VkImage.wrap(device, ptr, new Object[] {this})).collect(Collectors.toUnmodifiableList());
			this.imageViews = images.stream().map(image -> VkImageView.create(
					image,
					VK_IMAGE_VIEW_TYPE_2D,
					imageFormat,
					VK_COMPONENT_SWIZZLE_IDENTITY,
					VK_COMPONENT_SWIZZLE_IDENTITY,
					VK_COMPONENT_SWIZZLE_IDENTITY,
					VK_COMPONENT_SWIZZLE_IDENTITY,
					VK_IMAGE_ASPECT_COLOR_BIT,
					0,
					1,
					0,
					1,
					new Object[] {this}
			)).collect(Collectors.toUnmodifiableList());
		}
	}
	
	//device
	private final VkDevice device;
	
	public VkDevice getDevice() {
		return device;
	}
	
	private final VkSurfaceSwapChainDetails swapChainDetails;
	
	public VkSurfaceSwapChainDetails getSwapChainDetails() {
		return swapChainDetails;
	}
	
	//storage
	private final Storage storage;
	
	@Override
	public @NotNull Freeable getStorage() {
		return storage;
	}
	
	public static class Storage extends FreeableStorage {
		
		private final VkDevice device;
		private final long swapChain;
		
		public Storage(@Nullable Object referent, VkDevice device, long swapChain, @NotNull Object[] parents) {
			super(referent, parents);
			this.device = device;
			this.swapChain = swapChain;
		}
		
		@Override
		protected @NotNull Barrier handleFree() {
			nvkDestroySwapchainKHR(device, swapChain, 0);
			return Barrier.ALWAYS_TRIGGERED_BARRIER;
		}
	}
	
	//swapChain
	private final long swapChain;
	
	public long getSwapChain() {
		return swapChain;
	}
	
	//images
	private final List<VkImage> images;
	private final List<VkImageView> imageViews;
	
	public List<VkImage> getImages() {
		return images;
	}
	
	public List<VkImageView> getImageViews() {
		return imageViews;
	}
}
