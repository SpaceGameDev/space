package space.engine.vulkan;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.vulkan.VkExtensionProperties;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.pointer.PointerBufferInt;
import space.engine.freeableStorage.Freeable;
import space.engine.vulkan.exception.UnsupportedConfigurationException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.lwjgl.vulkan.VK10.*;
import static space.engine.freeableStorage.Freeable.ROOT_LIST;
import static space.engine.lwjgl.LwjglStructAllocator.mallocBuffer;
import static space.engine.vulkan.VkException.assertVk;

public class VkInstanceExtensions {
	
	@SuppressWarnings("FieldCanBeLocal")
	private static final @NotNull VkExtensionProperties.Buffer buffer;
	private static final @NotNull Map<String, VkExtensionProperties> map;
	
	static {
		buffer = findExtensions();
		map = buffer.stream().collect(Collectors.toUnmodifiableMap(VkExtensionProperties::extensionNameString, o -> o));
	}
	
	private static @NotNull VkExtensionProperties.Buffer findExtensions() {
		while (true) {
			try (AllocatorFrame frame = Allocator.frame()) {
				PointerBufferInt count = PointerBufferInt.malloc(frame);
				assertVk(nvkEnumerateInstanceExtensionProperties(0, count.address(), 0));
				VkExtensionProperties.Buffer extensions = mallocBuffer(Allocator.heap(), VkExtensionProperties::create, VkExtensionProperties.SIZEOF, count.getInt(), new Object[] {ROOT_LIST});
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
	
	public static @NotNull Collection<VkExtensionProperties> makeExtensionList(Collection<String> requiredExtensions, Collection<String> optionalExtensions) throws UnsupportedConfigurationException {
		VkExtensionProperties[] required = requiredExtensions.stream()
															 .map(map::get)
															 .toArray(VkExtensionProperties[]::new);
		if (Arrays.stream(required).anyMatch(Objects::isNull))
			throw new UnsupportedConfigurationException("Required Extensions [" + requiredExtensions.stream().filter(str -> !map.containsKey(str)).collect(Collectors.joining(", ")) + "] unavailable!");
		
		return Stream.concat(
				Arrays.stream(required),
				optionalExtensions.stream()
								  .map(map::get)
								  .filter(Objects::nonNull)
		).collect(Collectors.toUnmodifiableList());
	}
	
	public static @NotNull String generateInfoString() {
		return VkInstanceExtensions.extensions()
								   .stream()
								   .map(ex -> ex.extensionNameString() + " v" + ex.specVersion())
								   .collect(Collectors.joining("\n"));
	}
}
