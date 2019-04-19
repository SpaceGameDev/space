package space.game.firstTriangle;

import org.lwjgl.system.NativeType;
import org.lwjgl.vulkan.VkExtent3D;

public class VkQueueFamilyProperties {
	
	private final int index;
	private final org.lwjgl.vulkan.VkQueueFamilyProperties delegate;
	
	public VkQueueFamilyProperties(int index, org.lwjgl.vulkan.VkQueueFamilyProperties delegate) {
		this.index = index;
		this.delegate = delegate;
	}
	
	public int index() {
		return index;
	}
	
	@NativeType("VkQueueFlags")
	public int queueFlags() {
		return delegate.queueFlags();
	}
	
	@NativeType("uint32_t")
	public int queueCount() {
		return delegate.queueCount();
	}
	
	@NativeType("uint32_t")
	public int timestampValidBits() {
		return delegate.timestampValidBits();
	}
	
	public VkExtent3D minImageTransferGranularity() {
		return delegate.minImageTransferGranularity();
	}
	
	public org.lwjgl.vulkan.VkQueueFamilyProperties getLwjglQueue() {
		return delegate;
	}
}
