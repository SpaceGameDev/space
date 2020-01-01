package space.engine.vulkan;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.vulkan.VkLayerProperties;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.pointer.PointerBufferInt;
import space.engine.freeable.Freeable;
import space.engine.vulkan.exception.UnsupportedConfigurationException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.lwjgl.vulkan.VK10.*;
import static space.engine.freeable.Freeable.ROOT_LIST;
import static space.engine.lwjgl.LwjglStructAllocator.mallocBuffer;
import static space.engine.vulkan.VkException.assertVk;

public class VkInstanceValidationLayers {
	
	@SuppressWarnings("FieldCanBeLocal")
	private static final @NotNull VkLayerProperties.Buffer buffer;
	private static final @NotNull Map<String, VkLayerProperties> map;
	
	static {
		buffer = findLayers();
		map = buffer.stream().collect(Collectors.toUnmodifiableMap(VkLayerProperties::layerNameString, o -> o));
	}
	
	private static @NotNull VkLayerProperties.Buffer findLayers() {
		while (true) {
			try (AllocatorFrame frame = Allocator.frame()) {
				PointerBufferInt count = PointerBufferInt.malloc(frame);
				assertVk(nvkEnumerateInstanceLayerProperties(count.address(), 0));
				VkLayerProperties.Buffer layers = mallocBuffer(Allocator.heap(), VkLayerProperties::create, VkLayerProperties.SIZEOF, count.getInt(), new Object[] {ROOT_LIST});
				if (assertVk(nvkEnumerateInstanceLayerProperties(count.address(), layers.address())) == VK_SUCCESS)
					return layers;
				Freeable.freeObject(layers);
			}
		}
	}
	
	public static @NotNull Map<String, VkLayerProperties> layerNameMap() {
		return map;
	}
	
	public static @NotNull Collection<VkLayerProperties> layers() {
		return map.values();
	}
	
	public static @NotNull Collection<VkLayerProperties> makeLayerList(Collection<String> requiredLayers, Collection<String> optionalLayers) throws UnsupportedConfigurationException {
		VkLayerProperties[] required = requiredLayers.stream()
													 .map(map::get)
													 .toArray(VkLayerProperties[]::new);
		if (Arrays.stream(required).anyMatch(Objects::isNull))
			throw new UnsupportedConfigurationException("Required Extensions [" + requiredLayers.stream().filter(str -> !map.containsKey(str)).collect(Collectors.joining(", ")) + "] unavailable!");
		
		return Stream.concat(
				Arrays.stream(required),
				optionalLayers.stream()
							  .map(map::get)
							  .filter(Objects::nonNull)
		).collect(Collectors.toUnmodifiableList());
	}
	
	public static @NotNull String generateInfoString() {
		return VkInstanceValidationLayers.layers()
										 .stream()
										 .flatMap(layer -> Stream.of(
												 layer.layerNameString() + " v" + layer.specVersion() + "-" + layer.implementationVersion(),
												 "    " + layer.descriptionString()
										 ))
										 .collect(Collectors.joining("\n"));
	}
}
