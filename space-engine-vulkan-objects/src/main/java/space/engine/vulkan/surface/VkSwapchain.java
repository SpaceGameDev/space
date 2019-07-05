package space.engine.vulkan.surface;

import org.jetbrains.annotations.NotNull;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.array.ArrayBufferPointer;
import space.engine.buffer.pointer.PointerBufferInt;
import space.engine.freeableStorage.Freeable;
import space.engine.freeableStorage.Freeable.FreeableWrapper;
import space.engine.freeableStorage.FreeableStorage;
import space.engine.sync.barrier.Barrier;
import space.engine.vulkan.VkDevice;
import space.engine.vulkan.VkImage;
import space.engine.vulkan.VkImageView;
import space.engine.window.Window;

import java.util.Arrays;
import java.util.function.BiFunction;

import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;
import static space.engine.freeableStorage.Freeable.addIfNotContained;
import static space.engine.vulkan.VkException.assertVk;

public class VkSwapchain<WINDOW extends Window> implements FreeableWrapper {
	
	//create
	public static <WINDOW extends Window> VkSwapchain<WINDOW> create(long address, @NotNull VkDevice device, @NotNull VkSurface<WINDOW> surface, int imageFormat, int width, int height, int layers, @NotNull Object[] parents) {
		return new VkSwapchain<>(address, device, surface, imageFormat, width, height, layers, Storage::new, parents);
	}
	
	public static <WINDOW extends Window> VkSwapchain<WINDOW> wrap(long address, @NotNull VkDevice device, @NotNull VkSurface<WINDOW> surface, int imageFormat, int width, int height, int layers, @NotNull Object[] parents) {
		return new VkSwapchain<>(address, device, surface, imageFormat, width, height, layers, Freeable::createDummy, parents);
	}
	
	//const
	public VkSwapchain(long address, @NotNull VkDevice device, @NotNull VkSurface<WINDOW> surface, int imageFormat, int width, int height, int layers, @NotNull BiFunction<VkSwapchain, Object[], Freeable> storageCreator, @NotNull Object[] parents) {
		this.device = device;
		this.surface = surface;
		this.address = address;
		this.imageFormat = imageFormat;
		this.width = width;
		this.height = height;
		this.layers = layers;
		this.storage = storageCreator.apply(this, addIfNotContained(parents, device));
		
		//images
		try (AllocatorFrame frame = Allocator.frame()) {
			PointerBufferInt count = PointerBufferInt.malloc(frame);
			ArrayBufferPointer imagesBuffer;
			while (true) {
				assertVk(nvkGetSwapchainImagesKHR(device, address, count.address(), 0));
				imagesBuffer = ArrayBufferPointer.malloc(Allocator.heap(), count.getInt(), new Object[] {frame});
				if (assertVk(nvkGetSwapchainImagesKHR(device, address, count.address(), imagesBuffer.address())) == VK_SUCCESS)
					break;
				Freeable.freeObject(imagesBuffer);
			}
			
			this.images = imagesBuffer.stream().mapToObj(ptr -> VkImage.wrap(ptr, VK_IMAGE_TYPE_2D, imageFormat, width, height, 1, 1, layers, 1, VK_IMAGE_TILING_LINEAR, device, new Object[] {this})).toArray(VkImage[]::new);
			this.imageViews = Arrays.stream(images).map(image -> VkImageView.alloc(
					image,
					0,
					VK_IMAGE_VIEW_TYPE_2D,
					VK_IMAGE_ASPECT_COLOR_BIT,
					0, 1,
					0, 1,
					new Object[] {this}
			)).toArray(VkImageView[]::new);
		}
	}
	
	//parents
	private final @NotNull VkDevice device;
	private final @NotNull VkSurface<WINDOW> surface;
	
	public @NotNull VkDevice device() {
		return device;
	}
	
	public @NotNull VkSurface<WINDOW> surface() {
		return surface;
	}
	
	//storage
	private final @NotNull Freeable storage;
	
	@Override
	public @NotNull Freeable getStorage() {
		return storage;
	}
	
	//address
	private final long address;
	
	public long address() {
		return address;
	}
	
	public static class Storage extends FreeableStorage {
		
		private final @NotNull VkDevice device;
		private final long address;
		
		public Storage(@NotNull VkSwapchain swapChain, @NotNull Object[] parents) {
			super(swapChain, parents);
			this.device = swapChain.device();
			this.address = swapChain.address();
		}
		
		@Override
		protected @NotNull Barrier handleFree() {
			nvkDestroySwapchainKHR(device, address, 0);
			return Barrier.ALWAYS_TRIGGERED_BARRIER;
		}
	}
	
	//parameters
	private final int imageFormat, width, height, layers;
	
	public int imageFormat() {
		return imageFormat;
	}
	
	public int width() {
		return width;
	}
	
	public int height() {
		return height;
	}
	
	public int layers() {
		return layers;
	}
	
	//images
	private final @NotNull VkImage[] images;
	private final @NotNull VkImageView[] imageViews;
	
	public @NotNull VkImage[] images() {
		return images;
	}
	
	public @NotNull VkImageView[] imageViews() {
		return imageViews;
	}
}
