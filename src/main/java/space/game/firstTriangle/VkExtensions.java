package space.game.firstTriangle;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.vulkan.VkExtensionProperties;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.Frame;
import space.engine.buffer.pointer.PointerBufferInt;
import space.engine.freeableStorage.Freeable;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static org.lwjgl.vulkan.VK10.*;
import static space.engine.buffer.Allocator.allocatorHeap;
import static space.engine.freeableStorage.Freeable.ROOT_LIST;
import static space.engine.lwjgl.LwjglStructAllocator.mallocBuffer;
import static space.game.firstTriangle.VkException.assertVk;

public class VkExtensions {
	
	@SuppressWarnings("FieldCanBeLocal")
	private static final VkExtensionProperties.Buffer buffer;
	private static final Map<String, VkExtensionProperties> map;
	
	static {
		buffer = findExtensions();
		map = buffer.stream().collect(Collectors.toUnmodifiableMap(VkExtensionProperties::extensionNameString, o -> o));
	}
	
	private static VkExtensionProperties.Buffer findExtensions() {
		while (true) {
			try (Frame frame = Allocator.allocatorStack().frame()) {
				PointerBufferInt count = PointerBufferInt.malloc(frame);
				assertVk(nvkEnumerateInstanceExtensionProperties(0, count.address(), 0));
				VkExtensionProperties.Buffer extensions = mallocBuffer(allocatorHeap(), VkExtensionProperties::create, VkExtensionProperties.SIZEOF, count.getInt(), new Object[] {ROOT_LIST});
				if (assertVk(nvkEnumerateInstanceExtensionProperties(0, count.address(), extensions.address())) == VK_SUCCESS)
					return extensions;
				Freeable.freeObject(extensions);
			}
		}
	}
	
	public static @NotNull Map<String, VkExtensionProperties> extensionNameMap() {
		return map;
	}
	
	public static @NotNull Collection<VkExtensionProperties> extensions() {
		return map.values();
	}
}
