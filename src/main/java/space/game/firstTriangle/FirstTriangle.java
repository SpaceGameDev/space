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
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkExtent2D;
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
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import org.lwjgl.vulkan.VkViewport;
import org.lwjgl.vulkan.VkWriteDescriptorSet;
import space.engine.Side;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.Buffer;
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
import space.engine.observable.NoUpdate;
import space.engine.observable.ObservableReference;
import space.engine.sync.barrier.Barrier;
import space.engine.sync.future.Future;
import space.engine.vector.AxisAndAnglef;
import space.engine.vector.Matrix4f;
import space.engine.vector.ProjectionMatrix;
import space.engine.vector.Quaternionf;
import space.engine.vector.Vector3f;
import space.engine.vulkan.VkCommandBuffer;
import space.engine.vulkan.VkCommandPool;
import space.engine.vulkan.VkDescriptorPool;
import space.engine.vulkan.VkDescriptorSet;
import space.engine.vulkan.VkDescriptorSetLayout;
import space.engine.vulkan.VkFramebuffer;
import space.engine.vulkan.VkGraphicsPipeline;
import space.engine.vulkan.VkInstance;
import space.engine.vulkan.VkInstanceExtensions;
import space.engine.vulkan.VkInstanceValidationLayers;
import space.engine.vulkan.VkPhysicalDevice;
import space.engine.vulkan.VkPipelineLayout;
import space.engine.vulkan.VkRenderPass;
import space.engine.vulkan.VkSemaphore;
import space.engine.vulkan.VkShaderModule;
import space.engine.vulkan.managed.device.ManagedDevice;
import space.engine.vulkan.managed.device.ManagedDeviceSingleQueue;
import space.engine.vulkan.managed.instance.ManagedInstance;
import space.engine.vulkan.managed.surface.ManagedSwapchain;
import space.engine.vulkan.surface.VkSurface;
import space.engine.vulkan.surface.glfw.VkSurfaceGLFW;
import space.engine.vulkan.vma.VkBuffer;
import space.engine.vulkan.vma.VmaAllocator;
import space.engine.window.InputDevice.Keyboard;
import space.engine.window.Keycode;
import space.engine.window.Window;
import space.engine.window.WindowContext;
import space.engine.window.extensions.VideoModeDesktopExtension;
import space.engine.window.glfw.GLFWContext;
import space.engine.window.glfw.GLFWWindow;
import space.engine.window.glfw.GLFWWindowFramework;
import space.game.firstTriangle.model.ModelBunny;
import space.game.firstTriangle.model.ModelCube;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.PI;
import static org.lwjgl.util.vma.Vma.VMA_MEMORY_USAGE_CPU_TO_GPU;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;
import static space.engine.lwjgl.LwjglStructAllocator.*;
import static space.engine.lwjgl.PointerBufferWrapper.wrapPointer;
import static space.engine.primitive.Primitives.FP32;
import static space.engine.sync.barrier.Barrier.ALWAYS_TRIGGERED_BARRIER;
import static space.engine.vector.AxisAndAnglef.toRadians;
import static space.engine.vulkan.VkInstance.DEFAULT_BEST_PHYSICAL_DEVICE_TYPES;
import static space.engine.vulkan.managed.device.ManagedDevice.*;
import static space.engine.window.Window.*;
import static space.engine.window.WindowContext.API_TYPE;
import static space.engine.window.extensions.VideoModeExtension.*;

@SuppressWarnings("FieldCanBeLocal")
public class FirstTriangle implements Runnable {
	
	public static void main(String[] args) {
		FreeableStorageCleaner.setCleanupLogger(baseLogger);
		new FirstTriangle().run();
	}
	
	public static BaseLogger baseLogger = BaseLogger.defaultPrinter(BaseLogger.defaultHandler(new BaseLogger()));
	
	public static final ObservableReference<Integer> MODEL_ID = new ObservableReference<>(0);
	public static final float[][] MODELS;
	
	static {
		try {
			MODELS = new float[][] {
					ModelCube.CUBE,
					{
							0, -1, 0, 0, 0, 1, 1.0f, 1.0f, 1.0f,
							1, 1, 0, 0, 0, 1, 0.0f, 1.0f, 0.0f,
							-1, 1, 0, 0, 0, 1, 0.0f, 0.0f, 1.0f,
							0, -1, 0, 0, 0, -1, 1.0f, 1.0f, 1.0f,
							-1, 1, 0, 0, 0, -1, 0.0f, 0.0f, 1.0f,
							1, 1, 0, 0, 0, -1, 0.0f, 1.0f, 0.0f,
					},
					ModelBunny.bunny(0, new Matrix4f().modelScale(new Vector3f(20, -20, 20)).modelOffset(new Vector3f(0, 1, 0)))
			};
		} catch (IOException e) {
			throw new ExceptionInInitializerError(e);
		}
	}
	
	public boolean VK_LAYER_LUNARG_standard_validation = false;
	public boolean VK_LAYER_RENDERDOC_Capture = true;
	private Logger logger = baseLogger.subLogger("firstTriangle");
	private ObservableReference<float[]> vertexData = ObservableReference.generatingReference(() -> MODELS[MODEL_ID.assertGet()], MODEL_ID);
	
	private GLFWWindowFramework windowFramework;
	private VkInstance instance;
	private ManagedDevice device;
	private VmaAllocator vmaAllocator;
	private GLFWContext windowContext;
	private GLFWWindow window;
	private VkSurface<GLFWWindow> surface;
	private ManagedSwapchain<?> swapchain;
	private int framesInFlight;
	private VkRenderPass renderPass;
	private VkDescriptorSetLayout descriptorSetLayout;
	private VkPipelineLayout pipelineLayout;
	private VkGraphicsPipeline pipeline;
	private VkFramebuffer[] framebuffers;
	private ObservableReference<VkBuffer> vertexBuffer;
	private VkBuffer[] uniformBuffer;
	private Buffer[] uniformBufferMapped;
	private VkDescriptorPool descriptorPool;
	private VkDescriptorSet[] descriptorSets;
	private VkCommandPool commandPool;
	private ObservableReference<VkCommandBuffer[]> commandBuffers;
	
	private VkSemaphore[] semaphoreImageAvailable, semaphoreRenderFinished;
	private Barrier[] barrierFrameDone;
	
	public void run() {
		try (Frame side = Freeable.frame()) {
			
			//log extensions / layers
			logger.log(LogLevel.INFO, "Extensions: " + VkInstanceExtensions.generateInfoString());
			logger.log(LogLevel.INFO, "Layers: " + VkInstanceValidationLayers.generateInfoString());
			
			//windowFramework
			windowFramework = new GLFWWindowFramework();
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
			instance = ManagedInstance.alloc(
					"firstTriangle",
					1,
					baseLogger.subLogger("Vulkan"),
					VkInstanceValidationLayers.makeLayerList(instanceLayers, List.of()),
					VkInstanceExtensions.makeExtensionList(instanceExtensions, List.of()),
					new Object[] {side}
			);
			
			//physical device
			logger.log(LogLevel.INFO, "Physical Devices: " + instance.physicalDevicesGenerateInfoString());
			
			List<String> deviceExtensionsRequired = new ArrayList<>();
			List<String> deviceExtensionsOptional = new ArrayList<>();
			deviceExtensionsRequired.add(VK_KHR_SWAPCHAIN_EXTENSION_NAME);
			
			VkPhysicalDevice physicalDevice = Objects.requireNonNull(instance.getBestPhysicalDevice(DEFAULT_BEST_PHYSICAL_DEVICE_TYPES, deviceExtensionsRequired, deviceExtensionsOptional));
			logger.log(LogLevel.INFO, "Selecting: " + physicalDevice.identification());
			
			//device
			device = ManagedDeviceSingleQueue.alloc(physicalDevice,
													physicalDevice.makeExtensionList(deviceExtensionsRequired, deviceExtensionsOptional),
													null,
													new Object[] {side});
			
			//vmaAllocator
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
			windowContext = windowFramework.createContext(windowContextAtt, new Object[] {side}).awaitGetUninterrupted();
			
			//window
			AttributeList<Window> windowAtt;
			{
				AttributeListModify<Window> windowModify = Window.CREATOR.createModify();
				windowModify.put(VIDEO_MODE, VideoModeDesktopExtension.class);
				windowModify.put(TITLE, "Vulkan Window");
				windowModify.put(WIDTH, 1080);
				windowModify.put(HEIGHT, 1080);
				windowAtt = windowModify.createNewAttributeList();
			}
			window = windowContext.createWindow(windowAtt, new Object[] {side}).awaitGetUninterrupted();
			
			//surface
			surface = VkSurfaceGLFW.createSurfaceFromGlfwWindow(physicalDevice, window, new Object[] {side});
			
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
			swapchain = ManagedSwapchain.alloc(
					device,
					surface,
					null,
					null,
					swapExtend.extent(),
					null,
					VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT,
					null,
					null,
					null,
					null,
					null,
					new Object[] {side}
			);
			framesInFlight = swapchain.imageViews().length;
			
			//renderPass
			try (AllocatorFrame frame = Allocator.frame()) {
				renderPass = VkRenderPass.alloc(mallocStruct(frame, VkRenderPassCreateInfo::create, VkRenderPassCreateInfo.SIZEOF).set(
						VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO,
						0,
						0,
						allocBuffer(frame, VkAttachmentDescription::create, VkAttachmentDescription.SIZEOF, vkAttachmentDescription -> vkAttachmentDescription.set(
								0,
								swapchain.imageFormat()[0],
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
			
			//descriptor set
			try (AllocatorFrame frame = Allocator.frame()) {
				descriptorSetLayout = VkDescriptorSetLayout.alloc(mallocStruct(frame, VkDescriptorSetLayoutCreateInfo::create, VkDescriptorSetLayoutCreateInfo.SIZEOF).set(
						VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO,
						0,
						0,
						allocBuffer(frame, VkDescriptorSetLayoutBinding::create, VkDescriptorSetLayoutBinding.SIZEOF,
									vkDescriptorSetLayoutBinding -> vkDescriptorSetLayoutBinding.set(
											0,
											VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
											1,
											VK_SHADER_STAGE_VERTEX_BIT,
											null
									)
						)
				
				), device, new Object[] {side});
			}
			
			//pipeline layout
			try (AllocatorFrame frame = Allocator.frame()) {
				pipelineLayout = VkPipelineLayout.alloc(mallocStruct(frame, VkPipelineLayoutCreateInfo::create, VkPipelineLayoutCreateInfo.SIZEOF).set(
						VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO,
						0,
						0,
						ArrayBufferLong.alloc(frame, new long[] {descriptorSetLayout.address()}).nioBuffer(),
						null
				), device, new Object[] {side});
			}
			
			//pipeline
			try (AllocatorFrame frame = Allocator.frame()) {
				try {
					VkShaderModule shaderModuleVert = VkShaderModule.alloc(device,
																		   Objects.requireNonNull(FirstTriangle.class.getResourceAsStream("firstTriangle.vert.spv")).readAllBytes(),
																		   new Object[] {frame});
					VkShaderModule shaderModuleFrag = VkShaderModule.alloc(device,
																		   Objects.requireNonNull(FirstTriangle.class.getResourceAsStream("firstTriangle.frag.spv")).readAllBytes(),
																		   new Object[] {frame});
					
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
											.stride(FP32.bytes * 9)
											.inputRate(VK_VERTEX_INPUT_RATE_VERTEX)
									),
									allocBuffer(frame, VkVertexInputAttributeDescription::create, VkVertexInputAttributeDescription.SIZEOF,
												inPosition -> inPosition.set(
														0,
														0,
														VK_FORMAT_R32G32B32_SFLOAT,
														0
												),
												inNormal -> inNormal.set(
														1,
														0,
														VK_FORMAT_R32G32B32_SFLOAT,
														FP32.multiply(3)
												),
												inColor -> inColor.set(
														2,
														0,
														VK_FORMAT_R32G32B32_SFLOAT,
														FP32.multiply(6)
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
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			
			//framebuffer
			framebuffers = Arrays
					.stream(swapchain.imageViews())
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
			
			//vertex buffer
			vertexBuffer = ObservableReference.generatingReference(() -> {
				float[] data = vertexData.assertGet();
				
				try (AllocatorFrame frame = Allocator.frame()) {
					return VkBuffer.alloc(mallocStruct(frame, VkBufferCreateInfo::create, VkBufferCreateInfo.SIZEOF).set(
							VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO,
							0,
							0,
							FP32.multiply(data.length),
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
					), vmaAllocator, ArrayBufferFloat.alloc(Allocator.heap(), data, new Object[] {frame}), new Object[] {side});
				}
			}, vertexData);
			
			//uniform buffer
			try (AllocatorFrame frame = Allocator.frame()) {
				uniformBuffer = IntStream
						.range(0, framesInFlight)
						.mapToObj(i -> VkBuffer.alloc(mallocStruct(frame, VkBufferCreateInfo::create, VkBufferCreateInfo.SIZEOF).set(
								VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO,
								0,
								0,
								FP32.multiply(32),
								VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
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
						), vmaAllocator, new Object[] {side}))
						.toArray(VkBuffer[]::new);
				uniformBufferMapped = Arrays.stream(uniformBuffer)
											.map(ubo -> ubo.mapMemory(new Object[] {side}))
											.toArray(Buffer[]::new);
			}
			
			//descriptor pool
			try (AllocatorFrame frame = Allocator.frame()) {
				descriptorPool = VkDescriptorPool.alloc(mallocStruct(frame, VkDescriptorPoolCreateInfo::create, VkDescriptorPoolCreateInfo.SIZEOF).set(
						VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO,
						0,
						0,
						framesInFlight,
						allocBuffer(frame, VkDescriptorPoolSize::create, VkDescriptorPoolSize.SIZEOF,
									vkDescriptorPoolSize -> vkDescriptorPoolSize.set(
											VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
											framesInFlight
									)
						)
				), device, new Object[] {side});
			}
			
			//descriptor set
			try (AllocatorFrame frame = Allocator.frame()) {
				descriptorSets = descriptorPool.allocateDescriptorSetsWrap(mallocStruct(frame, VkDescriptorSetAllocateInfo::create, VkDescriptorSetAllocateInfo.SIZEOF).set(
						VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO,
						0,
						descriptorPool.address(),
						ArrayBufferLong.alloc(frame, IntStream.range(0, framesInFlight).mapToLong(i -> descriptorSetLayout.address()).toArray()).nioBuffer()
				), new Object[] {side});
				
				vkUpdateDescriptorSets(device, allocBuffer(frame, VkWriteDescriptorSet::create, VkWriteDescriptorSet.SIZEOF, IntStream
											   .range(0, framesInFlight)
											   .mapToObj(i -> (Consumer<VkWriteDescriptorSet>) writeDescriptorSet -> writeDescriptorSet.set(
													   VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET,
													   0,
													   descriptorSets[i].address(),
													   0,
													   0,
													   VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
													   null,
													   allocBuffer(frame, VkDescriptorBufferInfo::create, VkDescriptorBufferInfo.SIZEOF, vkDescriptorBufferInfo -> vkDescriptorBufferInfo.set(
															   uniformBuffer[i].address(),
															   0,
															   uniformBuffer[i].sizeOf()
													   )),
													   null
											   ))
											   .collect(Collectors.toList())),
									   null);
			}
			
			//commandPool
			try (AllocatorFrame frame = Allocator.frame()) {
				commandPool = VkCommandPool.alloc(mallocStruct(frame, VkCommandPoolCreateInfo::create, VkCommandPoolCreateInfo.SIZEOF).set(
						VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO,
						0,
						0,
						device.getQueueFamily(QUEUE_TYPE_GRAPHICS).index()
				), device, new Object[] {side});
			}
			
			//commandBuffer
			commandBuffers = ObservableReference.generatingReference(() -> {
				VkBuffer vkBuffer = vertexBuffer.assertGet();
				
				VkCommandBuffer[] commandBufferArray = commandPool.allocCmdBuffers(VK_COMMAND_BUFFER_LEVEL_PRIMARY, framebuffers.length, new Object[] {side});
				for (int i = 0; i < commandBufferArray.length; i++) {
					try (AllocatorFrame frame = Allocator.frame()) {
						VkCommandBuffer commandBuffer = commandBufferArray[i];
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
													.float32(0, 0.0f)
													.float32(1, 0.0f)
													.float32(2, 0.0f)
													.float32(3, 1.0f)
								)
						), VK_SUBPASS_CONTENTS_INLINE);
						
						vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline.address());
						
						vkCmdBindDescriptorSets(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, pipelineLayout.address(), 0, new long[] {descriptorSets[i].address()}, null);
						vkCmdBindVertexBuffers(commandBuffer, 0, new long[] {vkBuffer.address()}, new long[] {0});
						vkCmdDraw(commandBuffer, (int) (vkBuffer.sizeOf() / FP32.multiply(9)), 1, 0, 0);
						
						vkCmdEndRenderPass(commandBuffer);
						
						commandBuffer.endCommandBuffer();
					}
				}
				return commandBufferArray;
			}, vertexBuffer);
			
			//synchronization
			try (AllocatorFrame frame = Allocator.frame()) {
				VkSemaphoreCreateInfo semaphoreInfo = mallocStruct(frame, VkSemaphoreCreateInfo::create, VkSemaphoreCreateInfo.SIZEOF).set(
						VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO,
						0,
						0
				);
				IntFunction<@NotNull VkSemaphore> semaphoreMap = i -> VkSemaphore.alloc(semaphoreInfo, device, new Object[] {side});
				semaphoreImageAvailable = IntStream.range(0, framesInFlight).mapToObj(semaphoreMap).toArray(VkSemaphore[]::new);
				semaphoreRenderFinished = IntStream.range(0, framesInFlight).mapToObj(semaphoreMap).toArray(VkSemaphore[]::new);
				
				barrierFrameDone = IntStream.range(0, framesInFlight).mapToObj(i -> ALWAYS_TRIGGERED_BARRIER).toArray(Barrier[]::new);
			}
			
			//main loop
			boolean[] isRunning = {true};
			window.getWindowCloseEvent().addHook(window1 -> isRunning[0] = false);
			windowContext.getInputDevices().stream().filter(dev -> dev instanceof Keyboard).map(Keyboard.class::cast).forEach(
					keyboard -> keyboard.getKeyInputEvent().addHook((key, pressed) -> {
						if (pressed) {
							boolean next = key == Keycode.KEY_DOWN;
							boolean prev = key == Keycode.KEY_UP;
							if (next || prev) {
								MODEL_ID.set(() -> {
									int current = MODEL_ID.assertGet();
									if (next && current + 1 < MODELS.length) {
										return current + 1;
									} else if (prev && current > 0) {
										return current - 1;
									}
									throw new NoUpdate();
								});
							}
						}
					})
			);
			
			Matrix4f matrixPerspective = ProjectionMatrix.projection(new Matrix4f(), 90, 1, 0.1f, 10f);
			
			for (int frameId = 0; isRunning[0]; frameId = (frameId + 1) % framesInFlight) {
				barrierFrameDone[frameId].awaitUninterrupted();
				
				try (AllocatorFrame frame = Allocator.frame()) {
					PointerBufferInt imageIndexPtr = PointerBufferInt.malloc(frame);
					nvkAcquireNextImageKHR(device, swapchain.address(), Long.MAX_VALUE, semaphoreImageAvailable[frameId].address(), 0, imageIndexPtr.address());
					int imageIndex = imageIndexPtr.getInt();
					
					Quaternionf rotation = new Quaternionf();
					rotation.multiply(new AxisAndAnglef(1, 0, 0, toRadians(-45)));
					rotation.multiply(new AxisAndAnglef(0, 1, 0, (float) ((System.nanoTime() / 1000_000_000d) * 2 * PI / 10 + PI)));
					Matrix4f matrixModel = rotation.toMatrix4(new Matrix4f());
					matrixModel.modelOffset(new Vector3f(0, 0, -5));
					
					float[] translation = new float[32];
					matrixPerspective.write(translation, 0);
					matrixModel.write(translation, 16);
					ArrayBufferFloat translationMatrix = ArrayBufferFloat.alloc(frame, translation);
					Buffer.copyMemory(translationMatrix, 0, uniformBufferMapped[frameId], 0, translationMatrix.sizeOf());
					
					VkCommandBuffer[] vkCommandBuffers = commandBuffers.getFuture().awaitGetUninterrupted();
					Future<Barrier> frameDone = device.getQueue(QUEUE_TYPE_GRAPHICS, QUEUE_FLAG_REALTIME_BIT).submit(
							mallocStruct(frame, VkSubmitInfo::create, VkSubmitInfo.SIZEOF).set(
									VK_STRUCTURE_TYPE_SUBMIT_INFO,
									0,
									1,
									ArrayBufferLong.alloc(frame, new long[] {semaphoreImageAvailable[frameId].address()}).nioBuffer(),
									ArrayBufferInt.alloc(frame, new int[] {VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT}).nioBuffer(),
									wrapPointer(ArrayBufferPointer.alloc(frame, vkCommandBuffers == null ? new long[] {} : new long[] {vkCommandBuffers[imageIndex].address()})),
									ArrayBufferLong.alloc(frame, new long[] {semaphoreRenderFinished[frameId].address()}).nioBuffer()
							)
					).submit();
					
					swapchain.present(mallocStruct(frame, VkPresentInfoKHR::create, VkPresentInfoKHR.SIZEOF).set(
							VK_STRUCTURE_TYPE_PRESENT_INFO_KHR,
							0,
							ArrayBufferLong.alloc(frame, new long[] {semaphoreRenderFinished[frameId].address()}).nioBuffer(),
							1,
							ArrayBufferLong.alloc(frame, new long[] {swapchain.address()}).nioBuffer(),
							ArrayBufferInt.alloc(frame, new int[] {imageIndex}).nioBuffer(),
							null
					)).submit(frameDone);
					
					barrierFrameDone[frameId] = frameDone.awaitGetUninterrupted();
				}
				Barrier pollEventsBarrier = window.pollEventsTask();
				
				try {
					Thread.sleep(1000L / 60);
				} catch (InterruptedException ignored) {
				
				}
				pollEventsBarrier.awaitUninterrupted();
			}
			Barrier.awaitAll(barrierFrameDone).awaitUninterrupted();
			
			logger.log(LogLevel.INFO, "Exit!");
		} finally {
			Side.exit();
		}
	}
}
