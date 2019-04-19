package space.game.firstTriangle;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.vulkan.VK10.*;

public class VkException extends RuntimeException {
	
	public static final Map<Integer, String> ERROR_MAP = new HashMap<>();
	
	static {
		ERROR_MAP.put(VK_ERROR_OUT_OF_HOST_MEMORY, "VK_ERROR_OUT_OF_HOST_MEMORY");
		ERROR_MAP.put(VK_ERROR_OUT_OF_DEVICE_MEMORY, "VK_ERROR_OUT_OF_DEVICE_MEMORY");
		ERROR_MAP.put(VK_ERROR_INITIALIZATION_FAILED, "VK_ERROR_INITIALIZATION_FAILED");
		ERROR_MAP.put(VK_ERROR_DEVICE_LOST, "VK_ERROR_DEVICE_LOST");
		ERROR_MAP.put(VK_ERROR_MEMORY_MAP_FAILED, "VK_ERROR_MEMORY_MAP_FAILED");
		ERROR_MAP.put(VK_ERROR_LAYER_NOT_PRESENT, "VK_ERROR_LAYER_NOT_PRESENT");
		ERROR_MAP.put(VK_ERROR_EXTENSION_NOT_PRESENT, "VK_ERROR_EXTENSION_NOT_PRESENT");
		ERROR_MAP.put(VK_ERROR_FEATURE_NOT_PRESENT, "VK_ERROR_FEATURE_NOT_PRESENT");
		ERROR_MAP.put(VK_ERROR_INCOMPATIBLE_DRIVER, "VK_ERROR_INCOMPATIBLE_DRIVER");
		ERROR_MAP.put(VK_ERROR_TOO_MANY_OBJECTS, "VK_ERROR_TOO_MANY_OBJECTS");
		ERROR_MAP.put(VK_ERROR_FORMAT_NOT_SUPPORTED, "VK_ERROR_FORMAT_NOT_SUPPORTED");
		ERROR_MAP.put(VK_ERROR_FRAGMENTED_POOL, "VK_ERROR_FRAGMENTED_POOL");
	}
	
	public static int assertVk(int error) {
		if (error < 0)
			throw new VkException(error);
		return error;
	}
	
	public VkException(int error) {
		super("Vk error: " + ERROR_MAP.get(error) + " (" + error + ")");
	}
	
	public VkException(String message) {
		super(message);
	}
}
