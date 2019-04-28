package space.game.firstTriangle;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import space.engine.buffer.AllocatorStack.Frame;
import space.engine.buffer.pointer.PointerBufferInt;
import space.engine.freeableStorage.Freeable;
import space.engine.freeableStorage.Freeable.FreeableWrapper;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.lwjgl.vulkan.VK10.*;
import static space.engine.buffer.Allocator.*;
import static space.engine.lwjgl.LwjglStructAllocator.*;
import static space.game.firstTriangle.VkException.assertVk;

public class VkPhysicalDevice extends org.lwjgl.vulkan.VkPhysicalDevice implements FreeableWrapper {
	
	public static @NotNull VkPhysicalDevice wrap(long handle, @NotNull VkInstance instance, @NotNull Object[] parents) {
		return new VkPhysicalDevice(handle, instance, Freeable::createDummy, parents);
	}
	
	public VkPhysicalDevice(long handle, @NotNull VkInstance instance, @NotNull BiFunction<VkPhysicalDevice, Object[], Freeable> storageCreator, @NotNull Object[] parents) {
		super(handle, instance);
		this.vkInstance = instance;
		this.storage = storageCreator.apply(this, parents);
		
		try (Frame frame = allocatorStack().frame()) {
			//properties
			VkPhysicalDeviceProperties properties = mallocStruct(allocatorHeap(), VkPhysicalDeviceProperties::create, VkPhysicalDeviceProperties.SIZEOF, new Object[] {storage});
			nvkGetPhysicalDeviceProperties(this, properties.address());
			this.properties = properties;
			
			//queueProperties
			PointerBufferInt count = PointerBufferInt.malloc(frame);
			nvkGetPhysicalDeviceQueueFamilyProperties(this, count.address(), 0);
			org.lwjgl.vulkan.VkQueueFamilyProperties.Buffer queuePropertiesBuffer = mallocBuffer(allocatorHeap(), org.lwjgl.vulkan.VkQueueFamilyProperties::create, org.lwjgl.vulkan.VkQueueFamilyProperties.SIZEOF,
																								 count.getInt(), new Object[] {storage});
			nvkGetPhysicalDeviceQueueFamilyProperties(this, count.address(), queuePropertiesBuffer.address());
			this.queuePropertiesBuffer = queuePropertiesBuffer;
			this.queueProperties = IntStream.range(0, count.getInt()).mapToObj(i -> new VkQueueFamilyProperties(i, queuePropertiesBuffer.get(i))).collect(Collectors.toUnmodifiableList());
			
			//extensions
			VkExtensionProperties.Buffer extensionsBuffer;
			while (true) {
				assertVk(nvkEnumerateDeviceExtensionProperties(this, 0, count.address(), 0));
				extensionsBuffer = mallocBuffer(allocatorHeap(), VkExtensionProperties::create, VkExtensionProperties.SIZEOF, count.getInt(), new Object[] {storage});
				if (assertVk(nvkEnumerateDeviceExtensionProperties(this, 0, count.address(), extensionsBuffer.address())) == VK_SUCCESS)
					break;
				Freeable.freeObject(extensionsBuffer);
			}
			this.extensionsBuffer = extensionsBuffer;
			this.extensions = extensionsBuffer.stream().collect(Collectors.toUnmodifiableMap(VkExtensionProperties::extensionNameString, o -> o, (o, o2) -> o));
		}
	}
	
	//parents
	private final @NotNull VkInstance vkInstance;
	
	public @NotNull VkInstance instance() {
		return vkInstance;
	}
	
	@Override
	@Deprecated
	public @NotNull VkInstance getInstance() {
		return vkInstance;
	}
	
	//storage
	private final Freeable storage;
	
	@Override
	public @NotNull Freeable getStorage() {
		return storage;
	}
	
	//properties
	private final @NotNull VkPhysicalDeviceProperties properties;
	
	public @NotNull VkPhysicalDeviceProperties properties() {
		return properties;
	}
	
	//queueProperties
	@SuppressWarnings({"FieldCanBeLocal", "unused"})
	private final @NotNull org.lwjgl.vulkan.VkQueueFamilyProperties.Buffer queuePropertiesBuffer;
	private final @NotNull Collection<VkQueueFamilyProperties> queueProperties;
	
	public @NotNull Collection<VkQueueFamilyProperties> queueProperties() {
		return queueProperties;
	}
	
	//extensions
	@SuppressWarnings({"FieldCanBeLocal", "unused"})
	private final @NotNull org.lwjgl.vulkan.VkExtensionProperties.Buffer extensionsBuffer;
	private final @NotNull Map<String, VkExtensionProperties> extensions;
	
	public @NotNull Map<String, VkExtensionProperties> extensionNameMap() {
		return extensions;
	}
	
	public @NotNull Collection<VkExtensionProperties> extensions() {
		return extensions.values();
	}
}
