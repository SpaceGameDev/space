package space.game.firstTriangle;

import org.lwjgl.vulkan.EXTDebugUtils;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkClearColorValue;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkLayerProperties;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkSubpassDescription;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;
import org.lwjgl.vulkan.VkViewport;
import space.engine.buffer.AllocatorStack.Frame;
import space.engine.buffer.StringConverter;
import space.engine.buffer.array.ArrayBufferFloat;
import space.engine.buffer.array.ArrayBufferInt;
import space.engine.buffer.array.ArrayBufferLong;
import space.engine.buffer.array.ArrayBufferPointer;
import space.engine.buffer.pointer.PointerBufferInt;
import space.engine.freeableStorage.FreeableStorageCleaner;
import space.engine.key.attribute.AttributeList;
import space.engine.key.attribute.AttributeListModify;
import space.engine.logger.BaseLogger;
import space.engine.logger.LogLevel;
import space.engine.logger.Logger;
import space.engine.string.String2D;
import space.engine.vulkan.VkCommandBuffer;
import space.engine.vulkan.VkCommandPool;
import space.engine.vulkan.VkDevice;
import space.engine.vulkan.VkDevice.QueueRequestHandler;
import space.engine.vulkan.VkExtensions;
import space.engine.vulkan.VkFramebuffer;
import space.engine.vulkan.VkGraphicsPipeline;
import space.engine.vulkan.VkImageView;
import space.engine.vulkan.VkInstance;
import space.engine.vulkan.VkLayers;
import space.engine.vulkan.VkPhysicalDevice;
import space.engine.vulkan.VkPipelineLayout;
import space.engine.vulkan.VkQueue;
import space.engine.vulkan.VkQueueFamilyProperties;
import space.engine.vulkan.VkRenderPass;
import space.engine.vulkan.VkSemaphore;
import space.engine.vulkan.VkShaderModule;
import space.engine.vulkan.surface.VkSurface;
import space.engine.vulkan.surface.VkSurfaceDetails;
import space.engine.vulkan.surface.VkSwapchain;
import space.engine.vulkan.surface.glfw.VkSurfaceGLFW;
import space.engine.window.Window;
import space.engine.window.WindowContext;
import space.engine.window.extensions.VideoModeDesktopExtension;
import space.engine.window.glfw.GLFWContext;
import space.engine.window.glfw.GLFWWindow;
import space.engine.window.glfw.GLFWWindowFramework;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;
import static space.engine.buffer.Allocator.*;
import static space.engine.lwjgl.LwjglStructAllocator.*;
import static space.engine.lwjgl.PointerBufferWrapper.wrapPointer;
import static space.engine.vulkan.VkException.assertVk;
import static space.engine.window.Window.*;
import static space.engine.window.WindowContext.API_TYPE;
import static space.engine.window.extensions.VideoModeExtension.*;

public class FirstTriangle {
	
	public static boolean VK_LAYER_LUNARG_standard_validation = false;
	public static boolean VK_LAYER_RENDERDOC_Capture = false;
	public static BaseLogger baseLogger = BaseLogger.defaultPrinter(BaseLogger.defaultHandler(new BaseLogger()));
	private static Logger logger = baseLogger.subLogger("firstTriangle");
	
	public static void main(String[] args) throws InterruptedException, IOException {
		FreeableStorageCleaner.setCleanupLogger(baseLogger);
		try (Frame side = allocatorStack().frame()) {
			
			//extensions
			logger.log(LogLevel.INFO, new String2D(
					Stream.concat(
							Stream.of("Extensions: "),
							VkExtensions.extensions().stream()
										.map(ex -> ex.extensionNameString() + " v" + ex.specVersion())
					).toArray(String[]::new)
			));
			
			//layers
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
			
			//windowFramework
			GLFWWindowFramework windowFramework = new GLFWWindowFramework();
			VkSurfaceGLFW.assertSupported(windowFramework);
			
			//extension / layer selection
			List<VkExtensionProperties> instanceExtensions = new ArrayList<>();
			List<VkLayerProperties> instanceLayers = new ArrayList<>();
			
			if (VK_LAYER_LUNARG_standard_validation) {
				instanceExtensions.add(Objects.requireNonNull(VkExtensions.extensionNameMap().get(EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME)));
				instanceLayers.add(Objects.requireNonNull(VkLayers.layerNameMap().get("VK_LAYER_LUNARG_standard_validation")));
			}
			if (VK_LAYER_RENDERDOC_Capture) {
				instanceLayers.add(Objects.requireNonNull(VkLayers.layerNameMap().get("VK_LAYER_RENDERDOC_Capture")));
			}
			instanceExtensions.addAll(VkSurfaceGLFW.getRequiredInstanceExtensions(windowFramework));
			
			//instance
			VkInstance instance;
			try (Frame frame = allocatorStack().frame()) {
				instance = VkInstance.alloc(
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
						VK_LAYER_LUNARG_standard_validation,
						new Object[] {side}
				);
			}
			
			logger.log(LogLevel.INFO, "created VkInstance: " + instance);
			
			//physical device
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
			
			//windowContext
			AttributeList<WindowContext> windowContextAtt;
			{
				AttributeListModify<WindowContext> windowContextModify = WindowContext.CREATOR.createModify();
				windowContextModify.put(API_TYPE, null);
				windowContextAtt = windowContextModify.createNewAttributeList();
			}
			GLFWContext windowContext = windowFramework.createContext(windowContextAtt, new Object[] {side}).awaitGet();
			
			//window
			AttributeList<Window> windowAtt;
			{
				AttributeListModify<Window> windowModify = Window.CREATOR.createModify();
				windowModify.put(VIDEO_MODE, VideoModeDesktopExtension.class);
				windowModify.put(TITLE, "Vulkan Window");
				windowAtt = windowModify.createNewAttributeList();
			}
			GLFWWindow window = windowContext.createWindow(windowAtt, new Object[] {side}).awaitGet();
			
			//surface
			VkSurface<GLFWWindow> surface = VkSurfaceGLFW.createSurfaceFromGlfwWindow(instance, window, new Object[] {side});
			
			//queueFamily
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
			
			VkDevice device;
			try (Frame frame = allocatorStack().frame()) {
				device = VkDevice.alloc(
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
						new Object[] {side}
				);
			}
			queueRequestHandler.fillQueueRequestsWithQueues(device);
			VkQueue queueGraphics = requireNonNull(queueGraphicsSupplier.get());
			
			//swapExtend
			VkRect2D swapExtend;
			try (Frame frame = allocatorStack().frame()) {
				swapExtend = mallocStruct(allocatorHeap(), VkRect2D::create, VkRect2D.SIZEOF, new Object[] {side}).set(
						mallocStruct(frame, VkOffset2D::create, VkOffset2D.SIZEOF).set(
								0, 0
						),
						mallocStruct(frame, VkExtent2D::create, VkExtent2D.SIZEOF).set(
								windowAtt.get(WIDTH), windowAtt.get(HEIGHT)
						)
				);
			}
			
			//swapchain
			VkSurfaceDetails swapChainDetails = VkSurfaceDetails.wrap(physicalDevice, surface, new Object[] {side});
			int[] bestSurfaceFormat = swapChainDetails.getBestSurfaceFormat(new int[][] {{VK_FORMAT_R8G8B8A8_UNORM, VK_COLOR_SPACE_SRGB_NONLINEAR_KHR}});
			VkSwapchain swapChain;
			try (Frame frame = allocatorStack().frame()) {
				swapChain = VkSwapchain.alloc(
						mallocStruct(frame, VkSwapchainCreateInfoKHR::create, VkSwapchainCreateInfoKHR.SIZEOF).set(
								VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR,
								0,
								0,
								surface.address(),
								swapChainDetails.capabilities().minImageCount() + 1,
								bestSurfaceFormat[0],
								bestSurfaceFormat[1],
								swapExtend.extent(),
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
						new Object[] {side}
				);
			}
			VkImageView[] swapChainImageViews = swapChain.imageViews();
			
			//renderPass
			VkRenderPass renderPass;
			try (Frame frame = allocatorStack().frame()) {
				renderPass = VkRenderPass.alloc(mallocStruct(frame, VkRenderPassCreateInfo::create, VkRenderPassCreateInfo.SIZEOF).set(
						VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO,
						0,
						0,
						allocBuffer(frame, VkAttachmentDescription::create, VkAttachmentDescription.SIZEOF, new VkAttachmentDescription[] {
								mallocStruct(frame, VkAttachmentDescription::create, VkAttachmentDescription.SIZEOF).set(
										0,
										bestSurfaceFormat[0],
										VK_SAMPLE_COUNT_1_BIT,
										VK_ATTACHMENT_LOAD_OP_CLEAR,
										VK_ATTACHMENT_STORE_OP_STORE,
										VK_ATTACHMENT_LOAD_OP_DONT_CARE,
										VK_ATTACHMENT_STORE_OP_DONT_CARE,
										VK_IMAGE_LAYOUT_UNDEFINED,
										VK_IMAGE_LAYOUT_PRESENT_SRC_KHR
								)
						}),
						allocBuffer(frame, VkSubpassDescription::create, VkSubpassDescription.SIZEOF, new VkSubpassDescription[] {
								mallocStruct(frame, VkSubpassDescription::create, VkSubpassDescription.SIZEOF).set(
										0,
										VK_PIPELINE_BIND_POINT_GRAPHICS,
										null,
										1,
										allocBuffer(frame, VkAttachmentReference::create, VkAttachmentReference.SIZEOF, new VkAttachmentReference[] {
												mallocStruct(frame, VkAttachmentReference::create, VkAttachmentReference.SIZEOF).set(
														0,
														VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL
												)
										}),
										null,
										null,
										null
								)
						}),
						null
				), device, new Object[] {side});
			}
			
			//pipeline layout
			VkPipelineLayout pipelineLayout;
			try (Frame frame = allocatorStack().frame()) {
				pipelineLayout = VkPipelineLayout.alloc(mallocStruct(frame, VkPipelineLayoutCreateInfo::create, VkPipelineLayoutCreateInfo.SIZEOF).set(
						VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO,
						0,
						0,
						null,
						null
				), device, new Object[] {side});
			}
			
			//shader module
			VkShaderModule shaderModuleVert = VkShaderModule.alloc(device,
																   Objects.requireNonNull(FirstTriangle.class.getResourceAsStream("firstTriangle.vert.spv")).readAllBytes(),
																   new Object[] {side});
			VkShaderModule shaderModuleFrag = VkShaderModule.alloc(device,
																   Objects.requireNonNull(FirstTriangle.class.getResourceAsStream("firstTriangle.frag.spv")).readAllBytes(),
																   new Object[] {side});
			
			//pipeline
			VkGraphicsPipeline pipeline;
			try (Frame frame = allocatorStack().frame()) {
				pipeline = VkGraphicsPipeline.alloc(mallocStruct(frame, VkGraphicsPipelineCreateInfo::create, VkGraphicsPipelineCreateInfo.SIZEOF).set(
						VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO,
						0,
						0,
						allocBuffer(frame, VkPipelineShaderStageCreateInfo::create, VkPipelineShaderStageCreateInfo.SIZEOF, new VkPipelineShaderStageCreateInfo[] {
								mallocStruct(frame, VkPipelineShaderStageCreateInfo::create, VkPipelineShaderStageCreateInfo.SIZEOF).set(
										VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO,
										0,
										0,
										VK_SHADER_STAGE_VERTEX_BIT,
										shaderModuleVert.address(),
										StringConverter.stringToUTF8(frame, "main", true).nioBuffer(),
										null
								),
								mallocStruct(frame, VkPipelineShaderStageCreateInfo::create, VkPipelineShaderStageCreateInfo.SIZEOF).set(
										VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO,
										0,
										0,
										VK_SHADER_STAGE_FRAGMENT_BIT,
										shaderModuleFrag.address(),
										StringConverter.stringToUTF8(frame, "main", true).nioBuffer(),
										null
								)
						}),
						mallocStruct(frame, VkPipelineVertexInputStateCreateInfo::create, VkPipelineVertexInputStateCreateInfo.SIZEOF).set(
								VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO,
								0,
								0,
								null,
								null
						),
						mallocStruct(frame, VkPipelineInputAssemblyStateCreateInfo::create, VkPipelineInputAssemblyStateCreateInfo.SIZEOF).set(
								VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO,
								0,
								0,
								VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST,
								false
						
						),
						null,
						mallocStruct(frame, VkPipelineViewportStateCreateInfo::create, VkPipelineViewportStateCreateInfo.SIZEOF).set(
								VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO,
								0,
								0,
								1,
								allocBuffer(frame, VkViewport::create, VkViewport.SIZEOF, new VkViewport[] {
										mallocStruct(frame, VkViewport::create, VkViewport.SIZEOF).set(
												swapExtend.offset().x(), swapExtend.offset().y(),
												swapExtend.extent().width(), swapExtend.extent().height(),
												0, 1
										)
								}),
								1,
								allocBuffer(frame, VkRect2D::create, VkRect2D.SIZEOF, new VkRect2D[] {
										swapExtend
								})
						),
						mallocStruct(frame, VkPipelineRasterizationStateCreateInfo::create, VkPipelineRasterizationStateCreateInfo.SIZEOF).set(
								VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO,
								0,
								0,
								false,
								false,
								VK_POLYGON_MODE_FILL,
								VK_CULL_MODE_BACK_BIT,
								VK_FRONT_FACE_CLOCKWISE,
								false,
								0,
								0,
								0,
								1
						),
						mallocStruct(frame, VkPipelineMultisampleStateCreateInfo::create, VkPipelineMultisampleStateCreateInfo.SIZEOF).set(
								VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO,
								0,
								0,
								1,
								false,
								1.0f,
								null,
								false,
								false
						),
						null,
						mallocStruct(frame, VkPipelineColorBlendStateCreateInfo::create, VkPipelineColorBlendStateCreateInfo.SIZEOF).set(
								VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO,
								0,
								0,
								false,
								VK_LOGIC_OP_COPY,
								allocBuffer(frame, VkPipelineColorBlendAttachmentState::create, VkPipelineColorBlendAttachmentState.SIZEOF, new VkPipelineColorBlendAttachmentState[] {
										mallocStruct(frame, VkPipelineColorBlendAttachmentState::create, VkPipelineColorBlendAttachmentState.SIZEOF).set(
												false,
												VK_BLEND_FACTOR_ONE,
												VK_BLEND_FACTOR_ZERO,
												VK_BLEND_OP_ADD,
												VK_BLEND_FACTOR_ONE,
												VK_BLEND_FACTOR_ZERO,
												VK_BLEND_OP_ADD,
												VK_COLOR_COMPONENT_R_BIT | VK_COLOR_COMPONENT_G_BIT | VK_COLOR_COMPONENT_B_BIT | VK_COLOR_COMPONENT_A_BIT
										)
								}),
								ArrayBufferFloat.alloc(frame, new float[] {0, 0, 0, 0}).nioBuffer()
						),
						null,
						pipelineLayout.address(),
						renderPass.address(),
						0,
						0,
						-1
				), device, new Object[] {side});
			}
			
			//framebuffer
			VkFramebuffer[] framebuffers = Arrays
					.stream(swapChainImageViews)
					.map(swapChainImageView -> {
						try (Frame frame = allocatorStack().frame()) {
							return VkFramebuffer.alloc(mallocStruct(frame, VkFramebufferCreateInfo::create, VkFramebufferCreateInfo.SIZEOF).set(
									VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO,
									0,
									0,
									renderPass.address(),
									ArrayBufferLong.alloc(frame, new long[] {swapChainImageView.address()}).nioBuffer(),
									swapExtend.extent().width(),
									swapExtend.extent().height(),
									1
							), device, new Object[] {side});
						}
					})
					.toArray(VkFramebuffer[]::new);
			
			VkCommandPool commandPool;
			try (Frame frame = allocatorStack().frame()) {
				commandPool = VkCommandPool.alloc(mallocStruct(frame, VkCommandPoolCreateInfo::create, VkCommandPoolCreateInfo.SIZEOF).set(
						VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO,
						0,
						0,
						queueGraphicsFamily.index()
				), device, new Object[] {side});
			}
			
			VkCommandBuffer[] commandBuffers = commandPool.allocCmdBuffers(VK_COMMAND_BUFFER_LEVEL_PRIMARY, framebuffers.length, new Object[] {side});
			for (int i = 0; i < commandBuffers.length; i++) {
				try (Frame frame = allocatorStack().frame()) {
					VkCommandBuffer commandBuffer = commandBuffers[i];
					commandBuffer.beginCommandBuffer(mallocStruct(frame, VkCommandBufferBeginInfo::create, VkCommandBufferBeginInfo.SIZEOF).set(
							VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO,
							0,
							VK_COMMAND_BUFFER_USAGE_SIMULTANEOUS_USE_BIT,
							null
					));
					
					vkCmdBeginRenderPass(commandBuffer, mallocStruct(frame, VkRenderPassBeginInfo::create, VkRenderPassBeginInfo.SIZEOF).set(
							VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO,
							0,
							renderPass.address(),
							framebuffers[i].address(),
							swapExtend,
							allocBuffer(frame, VkClearValue::create, VkClearValue.SIZEOF, new VkClearValue[] {
									mallocStruct(frame, VkClearValue::create, VkClearValue.SIZEOF).color(
											mallocStruct(frame, VkClearColorValue::create, VkClearColorValue.SIZEOF)
													.float32(0, 0.5f)
													.float32(1, 0.0f)
													.float32(2, 0.0f)
													.float32(3, 1.0f)
									)
							})
					), VK_SUBPASS_CONTENTS_INLINE);
					
					vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline.address());
					vkCmdDraw(commandBuffer, 3, 1, 0, 0);
					vkCmdEndRenderPass(commandBuffer);
					
					commandBuffer.endCommandBuffer();
				}
			}
			
			VkSemaphore semaphoreImageAvailable, semaphoreRenderFinished;
			try (Frame frame = allocatorStack().frame()) {
				semaphoreImageAvailable = VkSemaphore.alloc(mallocStruct(frame, VkSemaphoreCreateInfo::create, VkSemaphoreCreateInfo.SIZEOF).set(
						VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO,
						0,
						0
				), device, new Object[] {side});
				semaphoreRenderFinished = VkSemaphore.alloc(mallocStruct(frame, VkSemaphoreCreateInfo::create, VkSemaphoreCreateInfo.SIZEOF).set(
						VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO,
						0,
						0
				), device, new Object[] {side});
			}
			
			//main loop
			boolean[] isRunning = {true};
			window.getWindowCloseEvent().addHook(window1 -> isRunning[0] = false);
			
			while (isRunning[0]) {
				try (Frame frame = allocatorStack().frame()) {
					PointerBufferInt imageIndexPtr = PointerBufferInt.malloc(frame);
					nvkAcquireNextImageKHR(device, swapChain.address(), Long.MAX_VALUE, semaphoreImageAvailable.address(), 0, imageIndexPtr.address());
					int imageIndex = imageIndexPtr.getInt();
					
					queueGraphics.submit(mallocStruct(frame, VkSubmitInfo::create, VkSubmitInfo.SIZEOF).set(
							VK_STRUCTURE_TYPE_SUBMIT_INFO,
							0,
							1,
							ArrayBufferLong.alloc(frame, new long[] {semaphoreImageAvailable.address()}).nioBuffer(),
							ArrayBufferInt.alloc(frame, new int[] {VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT}).nioBuffer(),
							wrapPointer(ArrayBufferPointer.alloc(frame, new long[] {commandBuffers[imageIndex].address()})),
							ArrayBufferLong.alloc(frame, new long[] {semaphoreRenderFinished.address()}).nioBuffer()
					));
					
					swapChain.present(mallocStruct(frame, VkPresentInfoKHR::create, VkPresentInfoKHR.SIZEOF).set(
							VK_STRUCTURE_TYPE_PRESENT_INFO_KHR,
							0,
							ArrayBufferLong.alloc(frame, new long[] {semaphoreRenderFinished.address()}).nioBuffer(),
							1,
							ArrayBufferLong.alloc(frame, new long[] {swapChain.address()}).nioBuffer(),
							ArrayBufferInt.alloc(frame, new int[] {imageIndex}).nioBuffer(),
							null
					), queueGraphics);
				}
				
				vkDeviceWaitIdle(device);
				
				window.pollEventsTask().awaitUninterrupted();
//				Thread.sleep(1000L / 100);
			}
			
			logger.log(LogLevel.INFO, "Exit!");
		}
	}
}
