package space.engine.vulkan;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.pointer.PointerBufferInt;
import space.engine.freeable.Freeable;
import space.engine.freeable.Freeable.CleanerWrapper;
import space.engine.vulkan.exception.UnsupportedConfigurationException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.lwjgl.vulkan.VK10.*;
import static space.engine.lwjgl.LwjglStructAllocator.*;
import static space.engine.vulkan.VkException.assertVk;

public class VkPhysicalDevice extends org.lwjgl.vulkan.VkPhysicalDevice implements CleanerWrapper {
	
	public static @NotNull VkPhysicalDevice wrap(long handle, @NotNull VkInstance instance, @NotNull Object[] parents) {
		return new VkPhysicalDevice(handle, instance, Freeable::createDummy, parents);
	}
	
	public VkPhysicalDevice(long handle, @NotNull VkInstance instance, @NotNull BiFunction<VkPhysicalDevice, Object[], Freeable> storageCreator, @NotNull Object[] parents) {
		super(handle, instance);
		this.vkInstance = instance;
		this.storage = storageCreator.apply(this, parents);
		
		try (AllocatorFrame frame = Allocator.frame()) {
			//properties
			VkPhysicalDeviceProperties properties = mallocStruct(Allocator.heap(), VkPhysicalDeviceProperties::create, VkPhysicalDeviceProperties.SIZEOF, new Object[] {storage});
			nvkGetPhysicalDeviceProperties(this, properties.address());
			this.properties = properties;
			
			//queueProperties
			PointerBufferInt count = PointerBufferInt.malloc(frame);
			nvkGetPhysicalDeviceQueueFamilyProperties(this, count.address(), 0);
			org.lwjgl.vulkan.VkQueueFamilyProperties.Buffer queuePropertiesBuffer = mallocBuffer(Allocator.heap(),
																								 org.lwjgl.vulkan.VkQueueFamilyProperties::create,
																								 org.lwjgl.vulkan.VkQueueFamilyProperties.SIZEOF,
																								 count.getInt(),
																								 new Object[] {storage});
			nvkGetPhysicalDeviceQueueFamilyProperties(this, count.address(), queuePropertiesBuffer.address());
			this.queuePropertiesBuffer = queuePropertiesBuffer;
			this.queueProperties = IntStream.range(0, count.getInt()).mapToObj(i -> new VkQueueFamilyProperties(i, queuePropertiesBuffer.get(i))).collect(Collectors.toUnmodifiableList());
			
			//extensions
			VkExtensionProperties.Buffer extensionsBuffer;
			while (true) {
				assertVk(nvkEnumerateDeviceExtensionProperties(this, 0, count.address(), 0));
				extensionsBuffer = mallocBuffer(Allocator.heap(), VkExtensionProperties::create, VkExtensionProperties.SIZEOF, count.getInt(), new Object[] {storage});
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
	
	public @NotNull String identification() {
		return properties.deviceNameString() + " (" + properties.deviceID() + ")";
	}
	
	public String generateInfoString() {
		return identification() + " type: " + deviceTypeToString(properties.deviceType()) + "\n" +
				"    vendor ID: " + properties.vendorID() + "api: v" + properties.apiVersion() + " Driver: v" + properties.driverVersion();
	}
	
	public static String deviceTypeToString(int type) {
		switch (type) {
			case VK_PHYSICAL_DEVICE_TYPE_OTHER:
				return "other";
			case VK_PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU:
				return "integrated GPU";
			case VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU:
				return "discrete GPU";
			case VK_PHYSICAL_DEVICE_TYPE_VIRTUAL_GPU:
				return "virtual GPU";
			case VK_PHYSICAL_DEVICE_TYPE_CPU:
				return "CPU";
		}
		return "unknown";
	}
	
	//queueProperties
	@SuppressWarnings({"FieldCanBeLocal", "unused"})
	private final @NotNull org.lwjgl.vulkan.VkQueueFamilyProperties.Buffer queuePropertiesBuffer;
	private final @NotNull Collection<VkQueueFamilyProperties> queueProperties;
	
	public @NotNull Collection<VkQueueFamilyProperties> queueProperties() {
		return queueProperties;
	}
	
	public Collection<VkQueueFamilyProperties> findQueueFamily(int queueFlags) {
		return findQueueFamily(queueFlags, 0);
	}
	
	public Collection<VkQueueFamilyProperties> findQueueFamily(int queueFlags, int withoutFlags) {
		return queueProperties.stream()
							  .filter(family -> (family.queueFlags() & queueFlags) == queueFlags && (family.queueFlags() & withoutFlags) == 0)
							  .collect(Collectors.toUnmodifiableList());
	}
	
	public Optional<VkQueueFamilyProperties> findQueueFamilySingle(int queueFlags) {
		return findQueueFamilySingle(queueFlags, 0);
	}
	
	public Optional<VkQueueFamilyProperties> findQueueFamilySingle(int queueFlags, int withoutFlags) {
		return queueProperties.stream()
							  .filter(family -> (family.queueFlags() & queueFlags) == queueFlags && (family.queueFlags() & withoutFlags) == 0)
							  .findFirst();
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
	
	public @NotNull Collection<VkExtensionProperties> makeExtensionList(Collection<String> requiredExtensions, Collection<String> optionalExtensions) throws UnsupportedConfigurationException {
		List<VkExtensionProperties> required = requiredExtensions.stream()
																 .map(extensions::get)
																 .collect(Collectors.toUnmodifiableList());
		if (required.stream().anyMatch(Objects::isNull))
			throw new UnsupportedConfigurationException("Required Extensions [" + requiredExtensions.stream().filter(str -> !extensions.containsKey(str)).collect(Collectors.joining(", ")) + "] unavailable!");
		
		return Stream.concat(
				required.stream(),
				optionalExtensions.stream()
								  .map(extensions::get)
								  .filter(Objects::nonNull)
		).collect(Collectors.toUnmodifiableList());
	}
}
