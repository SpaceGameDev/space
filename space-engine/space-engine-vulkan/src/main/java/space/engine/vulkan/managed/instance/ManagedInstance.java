package space.engine.vulkan.managed.instance;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.vulkan.EXTDebugUtils;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkLayerProperties;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.StringConverter;
import space.engine.buffer.array.ArrayBufferPointer;
import space.engine.buffer.pointer.PointerBufferPointer;
import space.engine.logger.Logger;
import space.engine.vulkan.VkInstance;

import java.nio.Buffer;
import java.util.Collection;

import static org.lwjgl.vulkan.VK10.*;
import static space.engine.lwjgl.LwjglStructAllocator.mallocStruct;
import static space.engine.lwjgl.PointerBufferWrapper.wrapPointer;

public class ManagedInstance extends VkInstance {
	
	public static final String ENGINE_NAME = "space-engine";
	public static final int ENGINE_VERSION = 1;
	
	public static ManagedInstance alloc(@NotNull String applicationName, int applicationVersion, @NotNull Logger logger, Collection<VkLayerProperties> validationLayers, Collection<VkExtensionProperties> extensions, Object[] parents) {
		try (AllocatorFrame frame = Allocator.frame()) {
			boolean initDebugCallback = extensions.stream().anyMatch(ex -> EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME.equals(ex.extensionNameString()));
			VkInstanceCreateInfo info = mallocStruct(frame, VkInstanceCreateInfo::create, VkInstanceCreateInfo.SIZEOF).set(
					VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO,
					0,
					0,
					mallocStruct(frame, VkApplicationInfo::create, VkApplicationInfo.SIZEOF).set(
							VK_STRUCTURE_TYPE_APPLICATION_INFO,
							0,
							StringConverter.stringToUTF8(frame, applicationName, true).nioBuffer(),
							applicationVersion,
							StringConverter.stringToUTF8(frame, ENGINE_NAME, true).nioBuffer(),
							ENGINE_VERSION,
							VK_API_VERSION_1_0
					),
					wrapPointer(ArrayBufferPointer.alloc(frame, validationLayers.stream().map(VkLayerProperties::layerName).toArray(Buffer[]::new))),
					wrapPointer(ArrayBufferPointer.alloc(frame, extensions.stream().map(VkExtensionProperties::extensionName).toArray(Buffer[]::new)))
			);
			
			PointerBufferPointer ptr = PointerBufferPointer.malloc(frame);
			nvkCreateInstance(info.address(), 0, ptr.address());
			return new ManagedInstance(ptr.getPointer(), info, logger, initDebugCallback, parents);
		}
	}
	
	public ManagedInstance(long handle, @NotNull VkInstanceCreateInfo ci, @NotNull Logger logger, boolean initDebugCallback, @NotNull Object[] parents) {
		super(handle, ci, logger, initDebugCallback, VkInstance.Storage::new, parents);
	}
}
