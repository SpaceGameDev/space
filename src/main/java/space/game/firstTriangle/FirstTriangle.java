package space.game.firstTriangle;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.util.vma.VmaAllocatorCreateInfo;
import org.lwjgl.util.vma.VmaVulkanFunctions;
import org.lwjgl.vulkan.EXTDebugUtils;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;
import space.engine.Side;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.Buffer;
import space.engine.buffer.array.ArrayBufferFloat;
import space.engine.buffer.array.ArrayBufferLong;
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
import space.engine.vulkan.VkInstance;
import space.engine.vulkan.VkInstanceExtensions;
import space.engine.vulkan.VkInstanceValidationLayers;
import space.engine.vulkan.VkPhysicalDevice;
import space.engine.vulkan.VkSemaphore;
import space.engine.vulkan.managed.device.ManagedDevice;
import space.engine.vulkan.managed.device.ManagedDeviceSingleQueue;
import space.engine.vulkan.managed.instance.ManagedInstance;
import space.engine.vulkan.managed.renderPass.ManagedFrameBuffer;
import space.engine.vulkan.managed.renderPass.ManagedRenderPass;
import space.engine.vulkan.managed.renderPass.ManagedRenderPass.Callback;
import space.engine.vulkan.managed.surface.ManagedSwapchain;
import space.engine.vulkan.surface.VkSurface;
import space.engine.vulkan.surface.glfw.VkSurfaceGLFW;
import space.engine.vulkan.vma.VkBuffer;
import space.engine.vulkan.vma.VmaAllocator;
import space.engine.window.InputDevice.Keyboard;
import space.engine.window.InputDevice.Mouse;
import space.engine.window.Keycode;
import space.engine.window.Window;
import space.engine.window.WindowContext;
import space.engine.window.extensions.MouseInputMode.Modes;
import space.engine.window.extensions.VideoModeDesktopExtension;
import space.engine.window.glfw.GLFWContext;
import space.engine.window.glfw.GLFWWindow;
import space.engine.window.glfw.GLFWWindowFramework;
import space.game.firstTriangle.model.ModelBunny;
import space.game.firstTriangle.model.ModelCube;
import space.game.firstTriangle.model.ModelDragon;
import space.game.firstTriangle.model.ModelHappyBuddha;
import space.game.firstTriangle.renderPass.FirstTriangleInfos;
import space.game.firstTriangle.renderPass.FirstTrianglePipelineRender;
import space.game.firstTriangle.renderPass.FirstTriangleRenderPass;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.lwjgl.util.vma.Vma.VMA_MEMORY_USAGE_CPU_TO_GPU;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK10.*;
import static space.engine.lwjgl.LwjglStructAllocator.*;
import static space.engine.primitive.Primitives.FP32;
import static space.engine.sync.Tasks.future;
import static space.engine.sync.barrier.Barrier.*;
import static space.engine.vector.AxisAndAnglef.toRadians;
import static space.engine.vulkan.managed.device.ManagedDevice.*;
import static space.engine.window.Keycode.*;
import static space.engine.window.Window.*;
import static space.engine.window.WindowContext.API_TYPE;
import static space.engine.window.extensions.MouseInputMode.MOUSE_MODE;
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
					ModelBunny.bunny(new Matrix4f().modelScale(new Vector3f(20, -20, 20)).modelOffset(new Vector3f(0, -1 / 20f, 0))),
					ModelDragon.dragon(new Matrix4f().modelScale(new Vector3f(20, -20, 20)).modelOffset(new Vector3f(0, -1 / 20f, 0))),
					ModelHappyBuddha.happyBuddha(new Matrix4f().modelScale(new Vector3f(20, -20, 20)).modelOffset(new Vector3f(0, -1 / 20f, 0)))
			};
		} catch (IOException e) {
			throw new ExceptionInInitializerError(e);
		}
	}
	
	public boolean VK_LAYER_LUNARG_standard_validation = true;
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
	private ObservableReference<VkBuffer> vertexBuffer;
	private VkBuffer[] uniformBuffer;
	private Buffer[] uniformBufferMapped;
	private VkDescriptorPool descriptorPool;
	private VkDescriptorSet[] descriptorSets;
	private VkCommandPool commandPool;
	
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
			
			VkPhysicalDevice physicalDevice = Objects.requireNonNull(instance.getBestPhysicalDevice(
					new int[][] {
							{VK_PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU},
							{VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU},
							{}
					},
					deviceExtensionsRequired, deviceExtensionsOptional));
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
				windowModify.put(MOUSE_MODE, Modes.CURSOR_DISABLED);
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
			int framesInFlight = 1;
			
			FirstTriangleRenderPass firstTriangleRenderPass = new FirstTriangleRenderPass(device, swapExtend, swapchain.imageFormat()[0], new Object[] {side});
			FirstTrianglePipelineRender firstTrianglePipelineRender = new FirstTrianglePipelineRender(firstTriangleRenderPass, new Object[] {side});
			ManagedFrameBuffer<FirstTriangleInfos> frameBuffer = new ManagedFrameBuffer<>(
					firstTriangleRenderPass.renderPass(),
					device.getQueue(QUEUE_TYPE_GRAPHICS, QUEUE_FLAG_REALTIME_BIT),
					new Object[] {
							swapchain.imageViews()
					},
					swapExtend.extent().width(),
					swapExtend.extent().height(),
					1,
					new Object[] {side}
			);
			
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
								FP32.multiply(3 * 16 + 3),
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
						ArrayBufferLong.alloc(frame, IntStream.range(0, framesInFlight).mapToLong(i -> firstTrianglePipelineRender.descriptorSetLayout().address()).toArray()).nioBuffer()
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
			ObservableReference<VkCommandBuffer> commandBuffers = ObservableReference.generatingReference(() -> {
				VkBuffer vkBuffer = vertexBuffer.assertGet();
				
				VkCommandBuffer commandBuffer = commandPool.allocCmdBuffer(VK_COMMAND_BUFFER_LEVEL_SECONDARY, new Object[] {side});
				commandBuffer.beginCommandBuffer(VK_COMMAND_BUFFER_USAGE_SIMULTANEOUS_USE_BIT | VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT, firstTriangleRenderPass.renderPass().subpasses()[0].inheritanceInfo());
				
				firstTrianglePipelineRender.bindPipeline(commandBuffer, descriptorSets[0]);
				vkCmdBindVertexBuffers(commandBuffer, 0, new long[] {vkBuffer.address()}, new long[] {0});
				vkCmdDraw(commandBuffer, (int) (vkBuffer.sizeOf() / FP32.multiply(9)), 1, 0, 0);
				
				commandBuffer.endCommandBuffer();
				return commandBuffer;
			}, vertexBuffer);
			
			firstTriangleRenderPass.renderPass().callbacks().addHook(new Callback<>() {
				private Future<VkCommandBuffer[]> future;
				
				@Override
				public void beginRenderpass(@NotNull ManagedFrameBuffer<FirstTriangleInfos> render, FirstTriangleInfos infos) {
					future = future(() -> {
						VkCommandBuffer vkCommandBuffer = commandBuffers.getFuture().assertGet();
						return new VkCommandBuffer[] {vkCommandBuffer, vkCommandBuffer};
					}).submit(commandBuffers.getFuture());
				}
				
				@Override
				public @Nullable Future<VkCommandBuffer[]> subpassCmdBuffers(@NotNull ManagedFrameBuffer<FirstTriangleInfos> render, FirstTriangleInfos infos, @NotNull ManagedRenderPass.Subpass subpass) {
					if (subpass.id() == 0)
						return future;
					return null;
				}
				
				@Override
				public void endRenderpass(@NotNull ManagedFrameBuffer<FirstTriangleInfos> render, FirstTriangleInfos infos, @NotNull Barrier barrier) {
				
				}
			});
			
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
			List<Keyboard> keyboards = windowContext.getInputDevices().stream().filter(dev -> dev instanceof Keyboard).map(Keyboard.class::cast).collect(Collectors.toUnmodifiableList());
			List<Mouse> mouses = windowContext.getInputDevices().stream().filter(dev -> dev instanceof Mouse).map(Mouse.class::cast).collect(Collectors.toUnmodifiableList());
			keyboards.forEach(
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
			
			Matrix4f matrixPerspective = ProjectionMatrix.projection(new Matrix4f(), 90, 1, 0.1f, 1000f);
			Entity camera = new Entity();
			camera.translateAbsolute(new Vector3f(0, 0, 5));
			
			float speedMouse = 0.008f;
			float speedMovement = 0.05f;
			mouses.forEach(mouse -> {
				mouse.getMouseMovementEvent().addHook((absolute, relative) -> {
					Objects.requireNonNull(relative);
					Quaternionf rotation = new Quaternionf();
					if (relative[0] != 0)
						rotation.multiply(new AxisAndAnglef(0, -1, 0, (float) relative[0] * speedMouse));
					if (relative[1] != 0)
						rotation.multiply(new AxisAndAnglef(1, 0, 0, (float) relative[1] * speedMouse));
					camera.rotateRelative(rotation);
				});
			});
			
			for (int frameId = 0; isRunning[0]; frameId = (frameId + 1) % framesInFlight) {
				keyboards.forEach(keyboard -> {
					Vector3f translation = new Vector3f();
					Quaternionf rotation = new Quaternionf();
					if (keyboard.isKeyDown(KEY_A))
						translation.add(new Vector3f(-speedMovement, 0, 0));
					else if (keyboard.isKeyDown(KEY_D))
						translation.add(new Vector3f(speedMovement, 0, 0));
					else if (keyboard.isKeyDown(KEY_R) || keyboard.isKeyDown(KEY_SPACE))
						translation.add(new Vector3f(0, -speedMovement, 0));
					else if (keyboard.isKeyDown(KEY_F) || keyboard.isKeyDown(KEY_LEFT_SHIFT))
						translation.add(new Vector3f(0, speedMovement, 0));
					else if (keyboard.isKeyDown(KEY_W))
						translation.add(new Vector3f(0, 0, -speedMovement));
					else if (keyboard.isKeyDown(KEY_S))
						translation.add(new Vector3f(0, 0, speedMovement));
					else if (keyboard.isKeyDown(KEY_Q))
						rotation.multiply(new AxisAndAnglef(0, 0, 1, toRadians(-2)));
					else if (keyboard.isKeyDown(KEY_E))
						rotation.multiply(new AxisAndAnglef(0, 0, 1, toRadians(2)));
					camera.rotateRelative(rotation);
					camera.translateRelative(translation);
				});
				
				barrierFrameDone[frameId].awaitUninterrupted();
				
				try (AllocatorFrame frame = Allocator.frame()) {
					int imageIndex = swapchain.acquire(Long.MAX_VALUE, semaphoreImageAvailable[frameId], null);
					
					Matrix4f matrixModel = camera.toMatrix4Inverse(new Matrix4f());
					float[] translation = new float[3 * 16 + 3];
					matrixPerspective.write(translation, 0);
					matrixModel.write(translation, 16);
					new Matrix4f(matrixModel).inversePure().write(translation, 32);
					camera.position.write(translation, 48);
					ArrayBufferFloat translationMatrix = ArrayBufferFloat.alloc(frame, translation);
					Buffer.copyMemory(translationMatrix, 0, uniformBufferMapped[frameId], 0, translationMatrix.sizeOf());
					
					Future<Barrier> frameDone = frameBuffer.render(
							new FirstTriangleInfos(imageIndex),
							new VkSemaphore[] {semaphoreImageAvailable[frameId]},
							new int[] {VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT},
							new VkSemaphore[] {semaphoreRenderFinished[frameId]}
					);
					Barrier presentDone = swapchain.present(new VkSemaphore[] {semaphoreRenderFinished[frameId]}, imageIndex).submit(frameDone);
					
					Barrier pollEventsBarrier = window.pollEventsTask();
					barrierFrameDone[frameId] = awaitAll(innerBarrier(frameDone), presentDone, pollEventsBarrier);
				}
				
				try {
					Thread.sleep(1000L / 60);
				} catch (InterruptedException ignored) {
				
				}
			}
			awaitAll(barrierFrameDone).awaitUninterrupted();
			
			logger.log(LogLevel.INFO, "Exit!");
		} finally {
			Side.exit();
		}
	}
}
