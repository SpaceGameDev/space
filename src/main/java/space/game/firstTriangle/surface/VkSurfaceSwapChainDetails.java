package space.game.firstTriangle.surface;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;
import space.engine.buffer.AllocatorStack.Frame;
import space.engine.buffer.array.ArrayBufferInt;
import space.engine.buffer.pointer.PointerBufferInt;
import space.engine.freeableStorage.Freeable;
import space.engine.freeableStorage.Freeable.FreeableWrapper;
import space.game.firstTriangle.VkInstance;
import space.game.firstTriangle.VkPhysicalDevice;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.VK10.*;
import static space.engine.buffer.Allocator.*;
import static space.engine.freeableStorage.Freeable.addIfNotContained;
import static space.engine.lwjgl.LwjglStructAllocator.*;
import static space.game.firstTriangle.VkException.assertVk;

public class VkSurfaceSwapChainDetails implements FreeableWrapper {
	
	public static @NotNull VkSurfaceSwapChainDetails wrap(@NotNull VkPhysicalDevice physicalDevice, @NotNull VkSurface<?> vkSurface, @NotNull Object[] parents) {
		return new VkSurfaceSwapChainDetails(physicalDevice, vkSurface, Freeable::createDummy, parents);
	}
	
	public VkSurfaceSwapChainDetails(@NotNull VkPhysicalDevice physicalDevice, @NotNull VkSurface<?> vkSurface, @NotNull BiFunction<VkSurfaceSwapChainDetails, Object[], Freeable> storageCreator, @NotNull Object[] parents) {
		if (physicalDevice.instance() != vkSurface.instance())
			throw new IllegalArgumentException("physicalDevice and surface are required to have the same VkInstance");
		
		//parents
		this.physicalDevice = physicalDevice;
		this.surface = vkSurface;
		
		//storage
		this.storage = storageCreator.apply(this, addIfNotContained(parents, physicalDevice, vkSurface));
		
		try (Frame frame = allocatorStack().frame()) {
			//capabilities
			long surface = vkSurface.address();
			VkSurfaceCapabilitiesKHR capabilities = mallocStruct(allocatorHeap(), VkSurfaceCapabilitiesKHR::create, VkSurfaceCapabilitiesKHR.SIZEOF, new Object[] {storage});
			assertVk(vkGetPhysicalDeviceSurfaceCapabilitiesKHR(physicalDevice, surface, capabilities));
			this.capabilities = capabilities;
			
			//formats
			PointerBufferInt count = PointerBufferInt.malloc(frame);
			VkSurfaceFormatKHR.Buffer formatBuffer;
			while (true) {
				assertVk(nvkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice, surface, count.address(), 0));
				formatBuffer = mallocBuffer(allocatorHeap(), VkSurfaceFormatKHR::create, VkSurfaceFormatKHR.SIZEOF, count.getInt(), new Object[] {storage});
				if (assertVk(nvkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice, surface, count.address(), 0)) == VK_SUCCESS)
					break;
				Freeable.freeObject(formatBuffer);
			}
			this.formatBuffer = formatBuffer;
			this.formats = formatBuffer.stream().collect(Collectors.toList());
			this.formatUndefined = formats.stream().anyMatch(format -> format.format() == VK_FORMAT_UNDEFINED);
			
			//presentModes
			ArrayBufferInt surfaceModeBuffer;
			while (true) {
				assertVk(nvkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice, surface, count.address(), 0));
				surfaceModeBuffer = ArrayBufferInt.malloc(allocatorHeap(), count.getInt(), new Object[] {frame});
				if (assertVk(nvkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice, surface, count.address(), surfaceModeBuffer.address())) == VK_SUCCESS)
					break;
				Freeable.freeObject(surfaceModeBuffer);
			}
			this.presentModes = surfaceModeBuffer.stream().boxed().collect(Collectors.toList());
		}
	}
	
	//parents
	private final @NotNull VkPhysicalDevice physicalDevice;
	private final @NotNull VkSurface<?> surface;
	
	public @NotNull VkInstance instance() {
		return physicalDevice.instance();
	}
	
	public VkPhysicalDevice physicalDevice() {
		return physicalDevice;
	}
	
	public VkSurface<?> surface() {
		return surface;
	}
	
	//storage
	private final @NotNull Freeable storage;
	
	@Override
	public @NotNull Freeable getStorage() {
		return storage;
	}
	
	//capabilities
	private final @NotNull VkSurfaceCapabilitiesKHR capabilities;
	
	public @NotNull VkSurfaceCapabilitiesKHR capabilities() {
		return capabilities;
	}
	
	//formats
	@SuppressWarnings({"FieldCanBeLocal", "unused"})
	private final @NotNull VkSurfaceFormatKHR.Buffer formatBuffer;
	private final @NotNull Collection<VkSurfaceFormatKHR> formats;
	private final boolean formatUndefined;
	
	public @NotNull Collection<VkSurfaceFormatKHR> formats() {
		return formats;
	}
	
	public boolean isFormatUndefined() {
		return formatUndefined;
	}
	
	public boolean isSurfaceFormatSupported(int format, int colorSpace) {
		if (formatUndefined)
			return true;
		
		for (VkSurfaceFormatKHR o : formats)
			if (o.format() == format && o.colorSpace() == colorSpace)
				return true;
		return false;
	}
	
	public int[] getBestSurfaceFormat(int[][] formatsToChooseFrom) {
		if (formatUndefined && formatsToChooseFrom.length > 0)
			return formatsToChooseFrom[0];
		
		for (int[] format : formatsToChooseFrom)
			if (isSurfaceFormatSupported(format[0], format[1]))
				return format;
		throw new RuntimeException("No best SurfaceFormat found!");
	}
	
	public VkSurfaceFormatKHR getBestSurfaceFormat(VkSurfaceFormatKHR[] formatsToChooseFrom) {
		if (formatUndefined && formatsToChooseFrom.length > 0)
			return formatsToChooseFrom[0];
		
		for (VkSurfaceFormatKHR format : formatsToChooseFrom)
			if (isSurfaceFormatSupported(format.format(), format.colorSpace()))
				return format;
		throw new RuntimeException("No best SurfaceFormat found!");
	}
	
	//presentModes
	private final @NotNull Collection<Integer> presentModes;
	
	public @NotNull Collection<Integer> presentModes() {
		return presentModes;
	}
	
	public boolean isPresentModeSupported(int presentMode) {
		return presentModes.contains(presentMode);
	}
	
	public int getBestPresentMode(int[] presentModesToChooseFrom) {
		for (int presentMode : presentModesToChooseFrom)
			if (isPresentModeSupported(presentMode))
				return presentMode;
		throw new RuntimeException("No best PresentMode found!");
	}
}
