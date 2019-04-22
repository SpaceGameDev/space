package space.game.firstTriangle.surface;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;
import space.engine.buffer.AllocatorStack.Frame;
import space.engine.buffer.array.ArrayBufferInt;
import space.engine.buffer.pointer.PointerBufferInt;
import space.engine.freeableStorage.Freeable;
import space.engine.freeableStorage.Freeable.FreeableWrapper;

import java.util.Collection;
import java.util.stream.Collectors;

import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.VK10.*;
import static space.engine.buffer.Allocator.*;
import static space.engine.freeableStorage.Freeable.addIfNotContained;
import static space.engine.lwjgl.LwjglStructAllocator.*;
import static space.game.firstTriangle.VkException.assertVk;

public class VkSurfaceSwapChainDetails implements FreeableWrapper {
	
	public VkSurfaceSwapChainDetails(VkPhysicalDevice physicalDevice, VkSurface<?> vkSurface, Object[] parents) {
		if (physicalDevice.getInstance() != vkSurface.getInstance())
			throw new IllegalArgumentException("physicalDevice and surface are required to have the same VkInstance");
		
		//parents
		this.instance = physicalDevice.getInstance();
		this.physicalDevice = physicalDevice;
		this.surface = vkSurface;
		
		//storage
		this.storage = Freeable.createDummy(this, addIfNotContained(parents, physicalDevice, vkSurface));
		
		try (Frame frame = allocatorStack().frame()) {
			//capabilities
			long surface = vkSurface.getSurface();
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
	private final VkInstance instance;
	private final VkPhysicalDevice physicalDevice;
	private final VkSurface<?> surface;
	
	public VkInstance getInstance() {
		return instance;
	}
	
	public VkPhysicalDevice getPhysicalDevice() {
		return physicalDevice;
	}
	
	public VkSurface<?> getSurface() {
		return surface;
	}
	
	//storage
	private final Freeable storage;
	
	@Override
	public @NotNull Freeable getStorage() {
		return storage;
	}
	
	//capabilities
	private final VkSurfaceCapabilitiesKHR capabilities;
	
	public VkSurfaceCapabilitiesKHR capabilities() {
		return capabilities;
	}
	
	//formats
	@SuppressWarnings({"FieldCanBeLocal", "unused"})
	private final VkSurfaceFormatKHR.Buffer formatBuffer;
	private final Collection<VkSurfaceFormatKHR> formats;
	private final boolean formatUndefined;
	
	public Collection<VkSurfaceFormatKHR> formats() {
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
	
	//presentModes
	private final Collection<Integer> presentModes;
	
	public Collection<Integer> presentModes() {
		return presentModes;
	}
	
	public boolean isPresentModeSupported(int presentMode) {
		return presentModes.contains(presentMode);
	}
	
	public int getBestPresentMode(int[] presentModesToChooseFrom) {
		for (int presentMode : presentModesToChooseFrom)
			if (isPresentModeSupported(presentMode))
				return presentMode;
		throw new RuntimeException("No best present mode found!");
	}
}
