package space.game.firstTriangle;

import org.lwjgl.vulkan.EXTDebugUtils;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VkQueue;
import space.engine.buffer.AllocatorStack.Frame;
import space.engine.buffer.pointer.PointerBufferInt;
import space.engine.freeableStorage.FreeableStorageCleaner;
import space.engine.key.attribute.AttributeList;
import space.engine.key.attribute.AttributeListModify;
import space.engine.logger.BaseLogger;
import space.engine.logger.LogLevel;
import space.engine.logger.Logger;
import space.engine.string.String2D;
import space.engine.window.Window;
import space.engine.window.WindowContext;
import space.engine.window.extensions.VideoModeDesktopExtension;
import space.engine.window.glfw.GLFWContext;
import space.engine.window.glfw.GLFWWindow;
import space.engine.window.glfw.GLFWWindowFramework;
import space.game.firstTriangle.VkInstance.Builder;
import space.game.firstTriangle.surface.VkSurface;
import space.game.firstTriangle.surface.VkSurfaceGLFW;
import space.game.firstTriangle.surface.VkSurfaceSwapChainDetails;
import space.game.firstTriangle.surface.VkSwapChain;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK10.*;
import static space.engine.buffer.Allocator.allocatorStack;
import static space.engine.window.Window.*;
import static space.engine.window.WindowContext.API_TYPE;
import static space.engine.window.extensions.VideoModeExtension.*;
import static space.game.firstTriangle.VkException.assertVk;

public class FirstTriangle {
	
	public static boolean USE_VK_LAYER_LUNARG_standard_validation = true;
	public static BaseLogger baseLogger = BaseLogger.defaultPrinter(BaseLogger.defaultHandler(new BaseLogger()));
	private static Logger logger = baseLogger.subLogger("firstTriangle");
	
	public static void main(String[] args) throws InterruptedException {
		FreeableStorageCleaner.setCleanupLogger(baseLogger);
		
		//glfw
		try (Frame frame = allocatorStack().frame()) {
			//framework
			GLFWWindowFramework windowFramework = new GLFWWindowFramework();
			VkSurfaceGLFW.assertSupported(windowFramework);
			
			//windowContext
			AttributeList<WindowContext> windowContextAtt;
			{
				AttributeListModify<WindowContext> windowContextModify = WindowContext.CREATOR.createModify();
				windowContextModify.put(API_TYPE, null);
				windowContextAtt = windowContextModify.createNewAttributeList();
			}
			GLFWContext windowContext = windowFramework.createContext(windowContextAtt, new Object[] {frame}).awaitGet();
			
			//window
			AttributeList<Window> windowAtt;
			{
				AttributeListModify<Window> windowModify = Window.CREATOR.createModify();
				windowModify.put(VIDEO_MODE, VideoModeDesktopExtension.class);
				windowModify.put(TITLE, "Vulkan Window");
				windowAtt = windowModify.createNewAttributeList();
			}
			GLFWWindow window = windowContext.createWindow(windowAtt, new Object[] {frame}).awaitGet();
			
			//vulkan
			{
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
				
				Builder vkInstanceBuilder = VkInstance.builder()
													  .setGame("space-first-triangle", 1)
													  .setVkVersion(VK_API_VERSION_1_0)
													  .setLogger(baseLogger.subLogger("vulkan"));
				vkInstanceBuilder.addExtension(VkSurfaceGLFW.getRequiredInstanceExtensions());
				if (USE_VK_LAYER_LUNARG_standard_validation) {
					vkInstanceBuilder.addExtension(Objects.requireNonNull(VkExtensions.extensionNameMap().get(EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME)));
					vkInstanceBuilder.addLayer(Objects.requireNonNull(VkLayers.layerNameMap().get("VK_LAYER_LUNARG_standard_validation")));
				}
				VkInstance vkInstance = vkInstanceBuilder.build(new Object[] {frame});
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
															  .filter(device -> device.properties().deviceType() == VK_PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU)
															  .findFirst()
															  .orElseGet(() -> vkInstance.getPhysicalDevices()
																						 .stream()
																						 .findFirst()
																						 .orElseThrow(() -> new RuntimeException("No GPU found!"))
															  );
				logger.log(LogLevel.INFO, "Selecting: " + vkPhysicalDevice.properties().deviceNameString());
				
				VkSurface<GLFWWindow> windowSurface = VkSurfaceGLFW.createSurfaceFromGlfwWindow(vkInstance, window, new Object[] {frame});
				
				VkQueueFamilyProperties vkQueueFamilyGraphics = vkPhysicalDevice.getQueueProperties()
																				.stream()
																				.filter(queueFamily -> (queueFamily.queueFlags() & VK_QUEUE_GRAPHICS_BIT) != 0)
																				.filter(queueFamily -> {
																					try (Frame frame1 = allocatorStack().frame()) {
																						PointerBufferInt success = PointerBufferInt.malloc(frame1);
																						assertVk(KHRSurface.nvkGetPhysicalDeviceSurfaceSupportKHR(vkPhysicalDevice,
																																				  queueFamily.index(),
																																				  windowSurface.getSurface(),
																																				  success.address()));
																						return success.getInt() == VK_TRUE;
																					}
																				})
																				.findFirst()
																				.orElseThrow(() -> new RuntimeException("No graphics queue!"));
				
				VkDevice.Builder vkDeviceBuilder = VkDevice.builder(vkPhysicalDevice);
				vkDeviceBuilder.addExtension(vkPhysicalDevice.extensionNameMap().get(VK_KHR_SWAPCHAIN_EXTENSION_NAME));
				Supplier<VkQueue> vkQueueGraphicsSupplier = vkDeviceBuilder.addQueueRequest(vkQueueFamilyGraphics, 0.5f);
				VkDevice vkDevice = vkDeviceBuilder.build(new Object[] {frame});
				VkQueue vkQueueGraphics = requireNonNull(vkQueueGraphicsSupplier.get());
				
				VkSurfaceSwapChainDetails swapChainDetails = new VkSurfaceSwapChainDetails(vkPhysicalDevice, windowSurface, new Object[] {frame});
				VkSwapChain swapChain = VkSwapChain.builder(vkDevice, swapChainDetails)
												   .setImageFormat(VK_FORMAT_R8G8B8A8_UNORM, VK_COLOR_SPACE_SRGB_NONLINEAR_KHR)
												   .setSwapExtend(windowAtt.get(WIDTH), windowAtt.get(HEIGHT))
												   .setImageUsageBit(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)
												   .setImageSharingModeExclusive()
												   .setBestPresentMode(new int[] {VK_PRESENT_MODE_MAILBOX_KHR, VK_PRESENT_MODE_IMMEDIATE_KHR, VK_PRESENT_MODE_FIFO_KHR})
												   .build(new Object[] {frame});
				
				//main loop
				{
					boolean[] isRunning = {true};
					window.getWindowCloseEvent().addHook(window1 -> isRunning[0] = false);
					
					while (isRunning[0]) {
						
						
						window.pollEventsTask().awaitUninterrupted();
						Thread.sleep(1000L / 60);
					}
				}
				
				logger.log(LogLevel.INFO, "Exit!");
			}
		}
	}
}
