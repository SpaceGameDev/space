package space.game.firstTriangle;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.util.vma.VmaAllocatorCreateInfo;
import org.lwjgl.util.vma.VmaVulkanFunctions;
import org.lwjgl.vulkan.EXTDebugUtils;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
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
import org.lwjgl.vulkan.VkSubpassDependency;
import org.lwjgl.vulkan.VkSubpassDescription;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import org.lwjgl.vulkan.VkViewport;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.StringConverter;
import space.engine.buffer.array.ArrayBufferFloat;
import space.engine.buffer.array.ArrayBufferInt;
import space.engine.buffer.array.ArrayBufferLong;
import space.engine.buffer.array.ArrayBufferPointer;
import space.engine.buffer.pointer.PointerBufferInt;
import space.engine.freeableStorage.Freeable;
import space.engine.freeableStorage.FreeableStorageCleaner;
import space.engine.freeableStorage.stack.FreeableStack.Frame;
import space.engine.key.attribute.AttributeList;
import space.engine.key.attribute.AttributeListModify;
import space.engine.logger.BaseLogger;
import space.engine.logger.LogLevel;
import space.engine.logger.Logger;
import space.engine.string.String2D;
import space.engine.vulkan.VkCommandBuffer;
import space.engine.vulkan.VkCommandPool;
import space.engine.vulkan.VkFence;
import space.engine.vulkan.VkFramebuffer;
import space.engine.vulkan.VkGraphicsPipeline;
import space.engine.vulkan.VkImageView;
import space.engine.vulkan.VkInstance;
import space.engine.vulkan.VkInstanceExtensions;
import space.engine.vulkan.VkInstanceValidationLayers;
import space.engine.vulkan.VkPhysicalDevice;
import space.engine.vulkan.VkPipelineLayout;
import space.engine.vulkan.VkQueue;
import space.engine.vulkan.VkRenderPass;
import space.engine.vulkan.VkSemaphore;
import space.engine.vulkan.VkShaderModule;
import space.engine.vulkan.exception.UnsupportedConfigurationException;
import space.engine.vulkan.managed.device.ManagedDevice;
import space.engine.vulkan.managed.device.ManagedDeviceQuadQueues;
import space.engine.vulkan.managed.instance.ManagedInstance;
import space.engine.vulkan.surface.VkSurface;
import space.engine.vulkan.surface.VkSwapchain;
import space.engine.vulkan.surface.glfw.VkSurfaceGLFW;
import space.engine.vulkan.vma.VkBuffer;
import space.engine.vulkan.vma.VmaAllocator;
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
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import static org.lwjgl.util.vma.Vma.VMA_MEMORY_USAGE_CPU_TO_GPU;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;
import static space.engine.lwjgl.LwjglStructAllocator.*;
import static space.engine.lwjgl.PointerBufferWrapper.wrapPointer;
import static space.engine.primitive.Primitives.FP32;
import static space.engine.vulkan.VkInstance.DEFAULT_BEST_PHYSICAL_DEVICE_TYPES;
import static space.engine.vulkan.managed.device.ManagedDevice.QUEUE_TYPE_GRAPHICS;
import static space.engine.window.Window.*;
import static space.engine.window.WindowContext.API_TYPE;
import static space.engine.window.extensions.VideoModeExtension.*;

public class FirstTriangle {
	
	public static boolean VK_LAYER_LUNARG_standard_validation = true;
	public static boolean VK_LAYER_RENDERDOC_Capture = true;
	public static final int MAX_FRAMES_IN_FLIGHT = 2;
	public static BaseLogger baseLogger = BaseLogger.defaultPrinter(BaseLogger.defaultHandler(new BaseLogger()));
	private static Logger logger = baseLogger.subLogger("firstTriangle");
	
	public static void main(String[] args) throws InterruptedException, IOException, UnsupportedConfigurationException {
		FreeableStorageCleaner.setCleanupLogger(baseLogger);
		try (Frame side = Freeable.frame()) {
			
			//log extensions / layers
			logger.log(LogLevel.INFO, new String2D("Extensions: ").concat(VkInstanceExtensions.generateInfoString()));
			logger.log(LogLevel.INFO, new String2D("Layers: ").concat(VkInstanceValidationLayers.generateInfoString()));
			
			//windowFramework
			GLFWWindowFramework windowFramework = new GLFWWindowFramework();
			VkSurfaceGLFW.assertSupported(windowFramework);
			
			//extension / layer selection
			List<String> instanceExtensions = new ArrayList<>();
			List<String> instanceLayers = new ArrayList<>();
			if (VK_LAYER_LUNARG_standard_validation) {
				instanceExtensions.add(EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME);
				instanceLayers.add("VK_LAYER_LUNARG_standard_validation");
			}
			if (VK_LAYER_RENDERDOC_Capture) {
				instanceLayers.add("VK_LAYER_RENDERDOC_Capture");
			}
			instanceExtensions.addAll(VkSurfaceGLFW.getRequiredInstanceExtensions(windowFramework));
			
			//instance
			VkInstance instance = ManagedInstance.alloc(
					"firstTriangle",
					1,
					baseLogger.subLogger("Vulkan"),
					VkInstanceValidationLayers.makeLayerList(instanceLayers, List.of()),
					VkInstanceExtensions.makeExtensionList(instanceExtensions, List.of()),
					new Object[] {side}
			);
			
			//physical device
			logger.log(LogLevel.INFO, new String2D("Physical Devices: ").concat(instance.physicalDevicesGenerateInfoString()));
			
			List<String> deviceExtensionsRequired = new ArrayList<>();
			List<String> deviceExtensionsOptional = new ArrayList<>();
			deviceExtensionsRequired.add(VK_KHR_SWAPCHAIN_EXTENSION_NAME);
			
			VkPhysicalDevice physicalDevice = Objects.requireNonNull(instance.getBestPhysicalDevice(DEFAULT_BEST_PHYSICAL_DEVICE_TYPES, deviceExtensionsRequired, deviceExtensionsOptional));
			logger.log(LogLevel.INFO, "Selecting: " + physicalDevice.identification());
			
			//device
			ManagedDevice device = ManagedDeviceQuadQueues.alloc(physicalDevice,
																 physicalDevice.makeExtensionList(deviceExtensionsRequired, deviceExtensionsOptional),
																 null,
																 false,
																 new Object[] {side});
			VkQueue queueGraphics = device.getQueue(QUEUE_TYPE_GRAPHICS, 0);
			
			//vmaAllocator
			VmaAllocator vmaAllocator;
			try (AllocatorFrame frame = Allocator.frame()) {
				vmaAllocator = VmaAllocator.alloc(mallocStruct(frame, VmaAllocatorCreateInfo::create, VmaAllocatorCreateInfo.SIZEOF).set(
						0,
						device.physicalDevice(),
						device,
						0,
						null,
						null,
						0,
						null,
						mallocStruct(frame, VmaVulkanFunctions::create, VmaVulkanFunctions.SIZEOF).set(
								instance,
								device
						),
						null
				), device, new Object[] {side});
			}
			
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
			VkSurface<GLFWWindow> surface = VkSurfaceGLFW.createSurfaceFromGlfwWindow(physicalDevice, window, new Object[] {side});
			
			//swapExtend
			VkRect2D swapExtend;
			try (AllocatorFrame frame = Allocator.frame()) {
				swapExtend = mallocStruct(Allocator.heap(), VkRect2D::create, VkRect2D.SIZEOF, new Object[] {side}).set(
						mallocStruct(frame, VkOffset2D::create, VkOffset2D.SIZEOF).set(
								0, 0
						),
						mallocStruct(frame, VkExtent2D::create, VkExtent2D.SIZEOF).set(
								windowAtt.get(WIDTH), windowAtt.get(HEIGHT)
						)
				);
			}
			
			//swapchain
			int[] bestSurfaceFormat = surface.getBestSurfaceFormat(new int[][] {{VK_FORMAT_R8G8B8A8_UNORM, VK_COLOR_SPACE_SRGB_NONLINEAR_KHR}});
			VkSwapchain swapChain;
			try (AllocatorFrame frame = Allocator.frame()) {
				swapChain = VkSwapchain.alloc(
						mallocStruct(frame, VkSwapchainCreateInfoKHR::create, VkSwapchainCreateInfoKHR.SIZEOF).set(
								VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR,
								0,
								0,
								surface.address(),
								surface.capabilities().minImageCount() + 1,
								bestSurfaceFormat[0],
								bestSurfaceFormat[1],
								swapExtend.extent(),
								1,
								VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT,
								VK_SHARING_MODE_EXCLUSIVE,
								null,
								surface.capabilities().currentTransform(),
								VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR,
								surface.getBestPresentMode(new int[] {VK_PRESENT_MODE_MAILBOX_KHR, VK_PRESENT_MODE_IMMEDIATE_KHR, VK_PRESENT_MODE_FIFO_KHR}),
								true,
								0
						),
						device,
						surface,
						new Object[] {side}
				);
			}
			VkImageView[] swapChainImageViews = swapChain.imageViews();
			
			//renderPass
			VkRenderPass renderPass;
			try (AllocatorFrame frame = Allocator.frame()) {
				renderPass = VkRenderPass.alloc(mallocStruct(frame, VkRenderPassCreateInfo::create, VkRenderPassCreateInfo.SIZEOF).set(
						VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO,
						0,
						0,
						allocBuffer(frame, VkAttachmentDescription::create, VkAttachmentDescription.SIZEOF, vkAttachmentDescription -> vkAttachmentDescription.set(
								0,
								bestSurfaceFormat[0],
								VK_SAMPLE_COUNT_1_BIT,
								VK_ATTACHMENT_LOAD_OP_CLEAR,
								VK_ATTACHMENT_STORE_OP_STORE,
								VK_ATTACHMENT_LOAD_OP_DONT_CARE,
								VK_ATTACHMENT_STORE_OP_DONT_CARE,
								VK_IMAGE_LAYOUT_UNDEFINED,
								VK_IMAGE_LAYOUT_PRESENT_SRC_KHR
						)),
						allocBuffer(frame, VkSubpassDescription::create, VkSubpassDescription.SIZEOF, vkSubpassDescription -> vkSubpassDescription.set(
								0,
								VK_PIPELINE_BIND_POINT_GRAPHICS,
								null,
								1,
								allocBuffer(frame, VkAttachmentReference::create, VkAttachmentReference.SIZEOF, vkAttachmentReference -> vkAttachmentReference.set(
										0,
										VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL
								)),
								null,
								null,
								null
						)),
						allocBuffer(frame, VkSubpassDependency::create, VkSubpassDependency.SIZEOF, vkSubpassDependency -> vkSubpassDependency.set(
								VK_SUBPASS_EXTERNAL,
								0,
								VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT,
								VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT,
								0,
								VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT,
								0
						))
				), device, new Object[] {side});
			}
			
			//pipeline layout
			VkPipelineLayout pipelineLayout;
			try (AllocatorFrame frame = Allocator.frame()) {
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
			try (AllocatorFrame frame = Allocator.frame()) {
				pipeline = VkGraphicsPipeline.alloc(mallocStruct(frame, VkGraphicsPipelineCreateInfo::create, VkGraphicsPipelineCreateInfo.SIZEOF).set(
						VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO,
						0,
						0,
						allocBuffer(frame, VkPipelineShaderStageCreateInfo::create, VkPipelineShaderStageCreateInfo.SIZEOF,
									vkPipelineShaderStageCreateInfo -> vkPipelineShaderStageCreateInfo.set(
											VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO,
											0,
											0,
											VK_SHADER_STAGE_VERTEX_BIT,
											shaderModuleVert.address(),
											StringConverter.stringToUTF8(frame, "main", true).nioBuffer(),
											null
									),
									vkPipelineShaderStageCreateInfo -> vkPipelineShaderStageCreateInfo.set(
											VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO,
											0,
											0,
											VK_SHADER_STAGE_FRAGMENT_BIT,
											shaderModuleFrag.address(),
											StringConverter.stringToUTF8(frame, "main", true).nioBuffer(),
											null
									)
						),
						mallocStruct(frame, VkPipelineVertexInputStateCreateInfo::create, VkPipelineVertexInputStateCreateInfo.SIZEOF).set(
								VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO,
								0,
								0,
								allocBuffer(frame, VkVertexInputBindingDescription::create, VkVertexInputBindingDescription.SIZEOF, vkVertexInputBindingDescription -> vkVertexInputBindingDescription
										.binding(0)
										.stride(FP32.bytes * 5)
										.inputRate(VK_VERTEX_INPUT_RATE_VERTEX)
								),
								allocBuffer(frame, VkVertexInputAttributeDescription::create, VkVertexInputAttributeDescription.SIZEOF,
											inPosition -> inPosition.set(
													0,
													0,
													VK_FORMAT_R32G32_SFLOAT,
													0
											),
											inColor -> inColor.set(
													1,
													0,
													VK_FORMAT_R32G32B32_SFLOAT,
													FP32.multiply(2)
											)
								)
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
								allocBuffer(frame, VkViewport::create, VkViewport.SIZEOF, vkViewPort -> vkViewPort.set(
										swapExtend.offset().x(),
										swapExtend.offset().y(),
										swapExtend.extent().width(),
										swapExtend.extent().height(),
										0,
										1
								)),
								1,
								wrapBuffer(VkRect2D::create, swapExtend)
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
								allocBuffer(frame, VkPipelineColorBlendAttachmentState::create, VkPipelineColorBlendAttachmentState.SIZEOF,
											vkPipelineColorBlendAttachmentState -> vkPipelineColorBlendAttachmentState.set(
													false,
													VK_BLEND_FACTOR_ONE,
													VK_BLEND_FACTOR_ZERO,
													VK_BLEND_OP_ADD,
													VK_BLEND_FACTOR_ONE,
													VK_BLEND_FACTOR_ZERO,
													VK_BLEND_OP_ADD,
													VK_COLOR_COMPONENT_R_BIT | VK_COLOR_COMPONENT_G_BIT | VK_COLOR_COMPONENT_B_BIT | VK_COLOR_COMPONENT_A_BIT
											)
								),
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
						try (AllocatorFrame frame = Allocator.frame()) {
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
			
			float[] vertexData = {
					0.0f, -0.5f, 1.0f, 1.0f, 1.0f,
					0.5f, 0.5f, 0.0f, 1.0f, 0.0f,
					-0.5f, 0.5f, 0.0f, 0.0f, 1.0f
			};
			
			VkBuffer vertexBuffer;
			try (AllocatorFrame frame = Allocator.frame()) {
				vertexBuffer = VkBuffer.alloc(mallocStruct(frame, VkBufferCreateInfo::create, VkBufferCreateInfo.SIZEOF).set(
						VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO,
						0,
						0,
						FP32.multiply(vertexData.length),
						VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
						VK_SHARING_MODE_EXCLUSIVE,
						null
				), mallocStruct(frame, VmaAllocationCreateInfo::create, VmaAllocationCreateInfo.SIZEOF).set(
						0,
						VMA_MEMORY_USAGE_CPU_TO_GPU,
						0,
						0,
						0,
						0,
						0
				), vmaAllocator, ArrayBufferFloat.alloc(frame, vertexData), new Object[] {side});
			}
			
			//commandPool
			VkCommandPool commandPool;
			try (AllocatorFrame frame = Allocator.frame()) {
				commandPool = VkCommandPool.alloc(mallocStruct(frame, VkCommandPoolCreateInfo::create, VkCommandPoolCreateInfo.SIZEOF).set(
						VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO,
						0,
						0,
						queueGraphics.queueFamily().index()
				), device, new Object[] {side});
			}
			
			//commandBuffer
			VkCommandBuffer[] commandBuffers = commandPool.allocCmdBuffers(VK_COMMAND_BUFFER_LEVEL_PRIMARY, framebuffers.length, new Object[] {side});
			for (int i = 0; i < commandBuffers.length; i++) {
				try (AllocatorFrame frame = Allocator.frame()) {
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
							allocBuffer(frame, VkClearValue::create, VkClearValue.SIZEOF, vkClearValue ->
									vkClearValue.color()
												.float32(0, 0.5f)
												.float32(1, 0.0f)
												.float32(2, 0.0f)
												.float32(3, 1.0f)
							)
					), VK_SUBPASS_CONTENTS_INLINE);
					
					vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline.address());
					
					vkCmdBindVertexBuffers(commandBuffer, 0, new long[] {vertexBuffer.address()}, new long[] {0});
					vkCmdDraw(commandBuffer, 3, 1, 0, 0);
					
					vkCmdEndRenderPass(commandBuffer);
					
					commandBuffer.endCommandBuffer();
				}
			}
			
			//synchronization
			VkSemaphore[] semaphoreImageAvailable, semaphoreRenderFinished;
			VkFence[] fenceFrameDone;
			try (AllocatorFrame frame = Allocator.frame()) {
				VkSemaphoreCreateInfo semaphoreInfo = mallocStruct(frame, VkSemaphoreCreateInfo::create, VkSemaphoreCreateInfo.SIZEOF).set(
						VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO,
						0,
						0
				);
				IntFunction<@NotNull VkSemaphore> semaphoreMap = i -> VkSemaphore.alloc(semaphoreInfo, device, new Object[] {side});
				semaphoreImageAvailable = IntStream.range(0, MAX_FRAMES_IN_FLIGHT).mapToObj(semaphoreMap).toArray(VkSemaphore[]::new);
				semaphoreRenderFinished = IntStream.range(0, MAX_FRAMES_IN_FLIGHT).mapToObj(semaphoreMap).toArray(VkSemaphore[]::new);
				
				VkFenceCreateInfo fenceInfo = mallocStruct(frame, VkFenceCreateInfo::create, VkFenceCreateInfo.SIZEOF).set(
						VK_STRUCTURE_TYPE_FENCE_CREATE_INFO,
						0,
						VK_FENCE_CREATE_SIGNALED_BIT
				);
				fenceFrameDone = IntStream.range(0, MAX_FRAMES_IN_FLIGHT).mapToObj(i -> VkFence.alloc(fenceInfo, device, new Object[] {side})).toArray(VkFence[]::new);
			}
			
			//main loop
			boolean[] isRunning = {true};
			window.getWindowCloseEvent().addHook(window1 -> isRunning[0] = false);
			
			for (int i = 0; isRunning[0]; i = (i + 1) % MAX_FRAMES_IN_FLIGHT) {
				vkWaitForFences(device, fenceFrameDone[i].address(), true, Long.MAX_VALUE);
				vkResetFences(device, fenceFrameDone[i].address());
				
				try (AllocatorFrame frame = Allocator.frame()) {
					PointerBufferInt imageIndexPtr = PointerBufferInt.malloc(frame);
					nvkAcquireNextImageKHR(device, swapChain.address(), Long.MAX_VALUE, semaphoreImageAvailable[i].address(), 0, imageIndexPtr.address());
					int imageIndex = imageIndexPtr.getInt();
					
					vkQueueSubmit(queueGraphics, mallocStruct(frame, VkSubmitInfo::create, VkSubmitInfo.SIZEOF).set(
							VK_STRUCTURE_TYPE_SUBMIT_INFO,
							0,
							1,
							ArrayBufferLong.alloc(frame, new long[] {semaphoreImageAvailable[i].address()}).nioBuffer(),
							ArrayBufferInt.alloc(frame, new int[] {VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT}).nioBuffer(),
							wrapPointer(ArrayBufferPointer.alloc(frame, new long[] {commandBuffers[imageIndex].address()})),
							ArrayBufferLong.alloc(frame, new long[] {semaphoreRenderFinished[i].address()}).nioBuffer()
					), fenceFrameDone[i].address());
					
					swapChain.present(mallocStruct(frame, VkPresentInfoKHR::create, VkPresentInfoKHR.SIZEOF).set(
							VK_STRUCTURE_TYPE_PRESENT_INFO_KHR,
							0,
							ArrayBufferLong.alloc(frame, new long[] {semaphoreRenderFinished[i].address()}).nioBuffer(),
							1,
							ArrayBufferLong.alloc(frame, new long[] {swapChain.address()}).nioBuffer(),
							ArrayBufferInt.alloc(frame, new int[] {imageIndex}).nioBuffer(),
							null
					), queueGraphics);
				}
				
				window.pollEventsTask().awaitUninterrupted();
//				Thread.sleep(1000L / 100);
			}
			vkWaitForFences(device, Arrays.stream(fenceFrameDone).mapToLong(VkFence::address).toArray(), true, Long.MAX_VALUE);
			
			logger.log(LogLevel.INFO, "Exit!");
		}
	}
}
