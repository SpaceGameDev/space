package space.game.firstTriangle;

import org.lwjgl.vulkan.EXTDebugUtils;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkLayerProperties;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;
import space.engine.buffer.AllocatorStack.Frame;
import space.engine.buffer.StringConverter;
import space.engine.buffer.array.ArrayBufferPointer;
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
import space.game.firstTriangle.VkDevice.QueueRequestHandler;
import space.game.firstTriangle.surface.VkSurface;
import space.game.firstTriangle.surface.VkSurfaceGLFW;
import space.game.firstTriangle.surface.VkSurfaceSwapChainDetails;
import space.game.firstTriangle.surface.VkSwapChain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;
import static space.engine.buffer.Allocator.allocatorStack;
import static space.engine.lwjgl.LwjglStructAllocator.mallocStruct;
import static space.engine.lwjgl.PointerBufferWrapper.wrapPointer;
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
				
				List<VkExtensionProperties> instanceExtensions = new ArrayList<>();
				List<VkLayerProperties> instanceLayers = new ArrayList<>();
				
				if (USE_VK_LAYER_LUNARG_standard_validation) {
					instanceExtensions.add(Objects.requireNonNull(VkExtensions.extensionNameMap().get(EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME)));
					instanceLayers.add(Objects.requireNonNull(VkLayers.layerNameMap().get("VK_LAYER_LUNARG_standard_validation")));
				}
				instanceExtensions.addAll(VkSurfaceGLFW.getRequiredInstanceExtensions());
				
				VkInstance instance = VkInstance.alloc(
						mallocStruct(frame, VkInstanceCreateInfo::create, VkInstanceCreateInfo.SIZEOF).set(
								VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO,
								0,
								0,
								mallocStruct(frame, VkApplicationInfo::create, VkApplicationInfo.SIZEOF).set(
										VK_STRUCTURE_TYPE_APPLICATION_INFO,
										0,
										StringConverter.stringToUTF8(frame, "space-first-triangle", true).nioBuffer(),
										1,
										StringConverter.stringToUTF8(frame, "space-engine", true).nioBuffer(),
										1,
										VK_API_VERSION_1_0
								),
								wrapPointer(ArrayBufferPointer.alloc(frame, instanceLayers.stream().map(VkLayerProperties::layerName).toArray(java.nio.Buffer[]::new))),
								wrapPointer(ArrayBufferPointer.alloc(frame, instanceExtensions.stream().map(VkExtensionProperties::extensionName).toArray(java.nio.Buffer[]::new)))
						),
						baseLogger.subLogger("vulkan"),
						true,
						new Object[] {frame}
				);
				logger.log(LogLevel.INFO, "created VkInstance: " + instance);
				
				logger.log(LogLevel.INFO, new String2D(
						Stream.concat(
								Stream.of("Physical Devices: "),
								instance.physicalDevices().stream()
										.map(VkPhysicalDevice::properties)
										.map(property -> property.deviceNameString() + " (id: 0x" + property.deviceID() + ") type: " + property.deviceType())
						).toArray(String[]::new)
				));
				
				VkPhysicalDevice physicalDevice = instance.physicalDevices()
														  .stream()
														  .filter(device -> device.properties().deviceType() == VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU)
														  .findFirst()
														  .orElseGet(() -> instance.physicalDevices()
																				   .stream()
																				   .findFirst()
																				   .orElseThrow(() -> new RuntimeException("No GPU found!"))
														  );
				logger.log(LogLevel.INFO, "Selecting: " + physicalDevice.properties().deviceNameString());
				
				VkSurface<GLFWWindow> surface = VkSurfaceGLFW.createSurfaceFromGlfwWindow(instance, window, new Object[] {frame});
				
				VkQueueFamilyProperties queueGraphicsFamily = physicalDevice.queueProperties()
																			.stream()
																			.filter(queueFamily -> (queueFamily.queueFlags() & VK_QUEUE_GRAPHICS_BIT) != 0)
																			.filter(queueFamily -> {
																				try (Frame frame1 = allocatorStack().frame()) {
																					PointerBufferInt success = PointerBufferInt.malloc(frame1);
																					assertVk(KHRSurface.nvkGetPhysicalDeviceSurfaceSupportKHR(physicalDevice,
																																			  queueFamily.index(),
																																			  surface.address(),
																																			  success.address()));
																					return success.getInt() == VK_TRUE;
																				}
																			})
																			.findFirst()
																			.orElseThrow(() -> new RuntimeException("No graphics queue!"));
				
				//device
				List<VkExtensionProperties> deviceExtensions = new ArrayList<>();
				deviceExtensions.add(physicalDevice.extensionNameMap().get(VK_KHR_SWAPCHAIN_EXTENSION_NAME));
				QueueRequestHandler queueRequestHandler = new QueueRequestHandler();
				Supplier<VkQueue> queueGraphicsSupplier = queueRequestHandler.addRequest(queueGraphicsFamily, 1f);
				
				VkDevice device = VkDevice.alloc(
						mallocStruct(frame, VkDeviceCreateInfo::create, VkDeviceCreateInfo.SIZEOF).set(
								VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO,
								0,
								0,
								queueRequestHandler.generateDeviceQueueRequestCreateInfoBuffer(frame),
								null,
								wrapPointer(ArrayBufferPointer.alloc(frame, deviceExtensions.stream().map(VkExtensionProperties::extensionName).toArray(java.nio.Buffer[]::new))),
								null
						),
						physicalDevice,
						new Object[] {frame}
				);
				queueRequestHandler.fillQueueRequestsWithQueues(device);
				VkQueue queueGraphics = requireNonNull(queueGraphicsSupplier.get());
				
				VkSurfaceSwapChainDetails swapChainDetails = VkSurfaceSwapChainDetails.wrap(physicalDevice, surface, new Object[] {frame});
				int[] bestSurfaceFormat = swapChainDetails.getBestSurfaceFormat(new int[][] {{VK_FORMAT_R8G8B8A8_UNORM, VK_COLOR_SPACE_SRGB_NONLINEAR_KHR}});
				VkSwapChain swapChain = VkSwapChain.alloc(
						mallocStruct(frame, VkSwapchainCreateInfoKHR::create, VkSwapchainCreateInfoKHR.SIZEOF).set(
								VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR,
								0,
								0,
								surface.address(),
								swapChainDetails.capabilities().minImageCount() + 1,
								bestSurfaceFormat[0],
								bestSurfaceFormat[1],
								mallocStruct(frame, VkExtent2D::create, VkExtent2D.SIZEOF).set(
										windowAtt.get(WIDTH), windowAtt.get(HEIGHT)
								),
								1,
								VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT,
								VK_SHARING_MODE_EXCLUSIVE,
								null,
								swapChainDetails.capabilities().currentTransform(),
								VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR,
								swapChainDetails.getBestPresentMode(new int[] {VK_PRESENT_MODE_MAILBOX_KHR, VK_PRESENT_MODE_IMMEDIATE_KHR, VK_PRESENT_MODE_FIFO_KHR}),
								true,
								0
						),
						device,
						swapChainDetails,
						new Object[] {frame}
				);
				VkImageView[] swapChainImageViews = swapChain.imageViews();
				
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
