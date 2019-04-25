package space.game.firstTriangle;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.vulkan.VkLayerProperties;
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

public class VkLayers {
	
	@SuppressWarnings("FieldCanBeLocal")
	private static final @NotNull VkLayerProperties.Buffer buffer;
	private static final @NotNull Map<String, VkLayerProperties> map;
	
	static {
		buffer = findLayers();
		map = buffer.stream().collect(Collectors.toUnmodifiableMap(VkLayerProperties::layerNameString, o -> o));
	}
	
	private static @NotNull VkLayerProperties.Buffer findLayers() {
		while (true) {
			try (Frame frame = Allocator.allocatorStack().frame()) {
				PointerBufferInt count = PointerBufferInt.malloc(frame);
				assertVk(nvkEnumerateInstanceLayerProperties(count.address(), 0));
				VkLayerProperties.Buffer layers = mallocBuffer(allocatorHeap(), VkLayerProperties::create, VkLayerProperties.SIZEOF, count.getInt(), new Object[] {ROOT_LIST});
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
}
