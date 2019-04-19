package space.game.firstTriangle;

import org.lwjgl.vulkan.EXTDebugUtils;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkLayerProperties;
import org.lwjgl.vulkan.VkQueue;
import space.engine.buffer.AllocatorStack.Frame;
import space.engine.logger.BaseLogger;
import space.engine.logger.LogLevel;
import space.engine.logger.Logger;
import space.engine.string.String2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.lwjgl.vulkan.VK10.*;
import static space.engine.buffer.Allocator.allocatorStack;

public class FirstTriangle {
	
	public static boolean USE_VK_LAYER_LUNARG_standard_validation = true;
	public static BaseLogger baseLogger = BaseLogger.defaultPrinter(BaseLogger.defaultHandler(new BaseLogger()));
	
	public static void main(String[] args) {
		Logger logger = baseLogger.subLogger("firstTriangle");
		try (Frame frame = allocatorStack().frame()) {
			logger.log(LogLevel.INFO, new String2D(
					Stream.concat(
							Stream.of("Extensions: "),
							VkExtensions.extensions().stream()
										.map(ex -> ex.extensionNameString() + " v" + ex.specVersion())
					).toArray(String[]::new)
			));
			
			logger.log(LogLevel.INFO, new String2D(
					Stream.concat(
							Stream.of("Layers: "),
							VkLayers.layers().stream()
									.flatMap(layer -> Stream.of(
											layer.layerNameString() + " v" + layer.specVersion(),
											"    " + layer.descriptionString()
									))
					).toArray(String[]::new)
			));
			
			List<VkLayerProperties> selectedLayers = new ArrayList<>();
			List<VkExtensionProperties> selectedExt = new ArrayList<>();
			if (USE_VK_LAYER_LUNARG_standard_validation) {
				selectedExt.add(Objects.requireNonNull(VkExtensions.extensionNameMap().get(EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME)));
				selectedLayers.add(Objects.requireNonNull(VkLayers.layerNameMap().get("VK_LAYER_LUNARG_standard_validation")));
			}
			
			VkInstance vkInstance = VkInstance.builder()
											  .setGame("space-first-triangle", 1)
											  .setVkVersion(VK_API_VERSION_1_0)
											  .setLogger(baseLogger.subLogger("vulkan"))
											  .addLayer(selectedLayers)
											  .addExtension(selectedExt)
											  .build(new Object[] {frame});
			logger.log(LogLevel.INFO, "created VkInstance: " + vkInstance);
			
			logger.log(LogLevel.INFO, new String2D(
					Stream.concat(
							Stream.of("Physical Devices: "),
							vkInstance.getPhysicalDevices().stream()
									  .map(VkPhysicalDevice::properties)
									  .map(property -> property.deviceNameString() + " (id: 0x" + property.deviceID() + ") type: " + property.deviceType())
					).toArray(String[]::new)
			));
			
			VkPhysicalDevice vkPhysicalDevice = vkInstance.getPhysicalDevices()
														  .stream()
														  .filter(device -> device.properties().deviceType() == VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU)
														  .findFirst()
														  .orElseGet(() -> vkInstance.getPhysicalDevices()
																					 .stream()
																					 .findFirst()
																					 .orElseThrow(() -> new RuntimeException("No GPU found!"))
														  );
			logger.log(LogLevel.INFO, "Selecting: " + vkPhysicalDevice.properties().deviceNameString());
			
			VkQueueFamilyProperties vkQueueFamilyGraphics = vkPhysicalDevice.getQueueProperties()
																			.stream()
																			.filter(queueFamily -> (queueFamily.queueFlags() & VK_QUEUE_GRAPHICS_BIT) != 0)
																			.findFirst()
																			.orElseThrow(() -> new RuntimeException("No graphics queue!"));
			
			VkDevice.Builder vkDeviceBuilder = VkDevice.builder(vkPhysicalDevice);
			Supplier<VkQueue> vkQueueGraphicsSupplier = vkDeviceBuilder.addQueueRequest(vkQueueFamilyGraphics, 0.5f);
			VkDevice vkDevice = vkDeviceBuilder.build(new Object[] {frame});
			VkQueue vkQueueGraphics = requireNonNull(vkQueueGraphicsSupplier.get());
			
			logger.log(LogLevel.INFO, "Exit!");
		}
	}
}
