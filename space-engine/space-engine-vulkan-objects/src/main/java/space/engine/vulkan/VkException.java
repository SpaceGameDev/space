package space.engine.vulkan;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.vulkan.VK10.*;

public class VkException extends RuntimeException {
	
	public static final Map<Integer, String> ERROR_MAP = new HashMap<>();
	private static final ThreadLocal<List<RuntimeException>> ERROR_ACCUMULATOR = ThreadLocal.withInitial(ArrayList::new);
	
	static {
		ERROR_MAP.put(VK_SUCCESS, "VK_SUCCESS");
		ERROR_MAP.put(VK_NOT_READY, "VK_NOT_READY");
		ERROR_MAP.put(VK_TIMEOUT, "VK_TIMEOUT");
		ERROR_MAP.put(VK_EVENT_SET, "VK_EVENT_SET");
		ERROR_MAP.put(VK_EVENT_RESET, "VK_EVENT_RESET");
		ERROR_MAP.put(VK_INCOMPLETE, "VK_INCOMPLETE");
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
	
	public static void assertVk() {
		assertVk("Vk error: no return value", false);
	}
	
	public static int assertVk(int error) {
		assertVk("Vk error: " + ERROR_MAP.get(error) + " (" + error + ")", error < 0);
		return error;
	}
	
	public static void assertVk(String error, boolean alwaysThrow) {
		List<RuntimeException> errors = ERROR_ACCUMULATOR.get();
		if (errors.size() > 0) {
			VkException vkException = new VkException(error);
			errors.forEach(vkException::addSuppressed);
			errors.clear();
			throw vkException;
		}
		
		if (alwaysThrow)
			throw new VkException(error);
	}
	
	public static void addError(RuntimeException e) {
		ERROR_ACCUMULATOR.get().add(e);
	}
	
	public VkException(int error) {
		super("Vk error: " + ERROR_MAP.get(error) + " (" + error + ")");
	}
	
	public VkException(@NotNull String message) {
		super(message);
	}
}
