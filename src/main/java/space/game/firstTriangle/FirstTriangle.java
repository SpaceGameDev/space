package space.game.firstTriangle;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.vulkan.EXTDebugUtils;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkWriteDescriptorSet;
import space.engine.Side;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.Buffer;
import space.engine.buffer.array.ArrayBufferFloat;
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
import space.engine.sync.DelayTask;
import space.engine.sync.barrier.Barrier;
import space.engine.sync.barrier.BarrierImpl;
import space.engine.sync.future.Future;
import space.engine.vector.AxisAndAnglef;
import space.engine.vector.Matrix4f;
import space.engine.vector.ProjectionMatrix;
import space.engine.vector.Quaternionf;
import space.engine.vector.Vector3f;
import space.engine.vulkan.VkBuffer;
import space.engine.vulkan.VkCommandBuffer;
import space.engine.vulkan.VkCommandPool;
import space.engine.vulkan.VkInstanceExtensions;
import space.engine.vulkan.VkInstanceValidationLayers;
import space.engine.vulkan.VkMappedBuffer;
import space.engine.vulkan.VkPhysicalDevice;
import space.engine.vulkan.managed.descriptorSet.ManagedDescriptorSetPool;
import space.engine.vulkan.managed.device.ManagedDevice;
import space.engine.vulkan.managed.device.ManagedDeviceSingleQueue;
import space.engine.vulkan.managed.instance.ManagedInstance;
import space.engine.vulkan.managed.renderPass.ManagedFrameBuffer;
import space.engine.vulkan.managed.renderPass.ManagedRenderPass;
import space.engine.vulkan.managed.renderPass.ManagedRenderPass.Callback;
import space.engine.vulkan.managed.surface.ManagedSwapchain;
import space.engine.vulkan.surface.VkSurface;
import space.engine.vulkan.surface.glfw.VkSurfaceGLFW;
import space.engine.vulkan.util.FpsRenderer;
import space.engine.vulkan.vma.VmaBuffer;
import space.engine.vulkan.vma.VmaMappedBuffer;
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
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.lwjgl.util.vma.Vma.VMA_MEMORY_USAGE_CPU_TO_GPU;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK10.*;
import static space.engine.Empties.EMPTY_OBJECT_ARRAY;
import static space.engine.buffer.Allocator.heap;
import static space.engine.lwjgl.LwjglStructAllocator.*;
import static space.engine.primitive.Primitives.FP32;
import static space.engine.sync.Tasks.future;
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
	
	public void run() {
		try (Frame side = Freeable.frame()) {
			
			//log extensions / layers
			logger.log(LogLevel.INFO, "Extensions: " + VkInstanceExtensions.generateInfoString());
			logger.log(LogLevel.INFO, "Layers: " + VkInstanceValidationLayers.generateInfoString());
			
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
			ManagedInstance instance = ManagedInstance.alloc(
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
			ManagedDevice device = ManagedDeviceSingleQueue.alloc(
					physicalDevice,
					physicalDevice.makeExtensionList(deviceExtensionsRequired, deviceExtensionsOptional),
					null,
					new Object[] {side}
			);
			
			//windowContext
			AttributeList<WindowContext> windowContextAtt;
			{
				AttributeListModify<WindowContext> windowContextModify = WindowContext.CREATOR.createModify();
				windowContextModify.put(API_TYPE, null);
				windowContextAtt = windowContextModify.createNewAttributeList();
			}
			GLFWContext windowContext = windowFramework.createContext(windowContextAtt, new Object[] {side}).awaitGetUninterrupted();
			
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
			GLFWWindow window = windowContext.createWindow(windowAtt, new Object[] {side}).awaitGetUninterrupted();
			
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
			ManagedSwapchain<GLFWWindow> swapchain = ManagedSwapchain.alloc(
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
			ObservableReference<Integer> modelId = new ObservableReference<>(0);
			ObservableReference<float[]> vertexData = ObservableReference.generatingReference(() -> MODELS[modelId.assertGet()], modelId);
			ObservableReference<VmaBuffer> vertexBuffer = ObservableReference.generatingReference(() -> {
				float[] data = vertexData.assertGet();
				VmaBuffer vkBuffer = VmaBuffer.alloc(
						0,
						FP32.multiply(data.length),
						VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
						0,
						0,
						device,
						new Object[] {side}
				);
				ArrayBufferFloat dataBuffer = ArrayBufferFloat.alloc(heap(), data, EMPTY_OBJECT_ARRAY);
				Barrier barrierDataUploaded = vkBuffer.uploadData(dataBuffer);
				barrierDataUploaded.addHook(dataBuffer::free);
				throw new DelayTask(barrierDataUploaded.toFuture(() -> vkBuffer));
			}, vertexData);
			
			//uniform buffer
			VkMappedBuffer uniformBuffer = VmaMappedBuffer.alloc(
					0,
					FP32.multiply(3 * 16 + 3),
					VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
					0,
					VMA_MEMORY_USAGE_CPU_TO_GPU,
					device,
					new Object[] {side}
			);
			Buffer uniformBufferMapped = uniformBuffer.mapMemory(new Object[] {side});
			
			//descriptor set
			ManagedDescriptorSetPool firstTrianglePipelineRenderDescriptorSet;
			try (AllocatorFrame frame = Allocator.frame()) {
				firstTrianglePipelineRenderDescriptorSet = new ManagedDescriptorSetPool(device, firstTrianglePipelineRender.descriptorSetLayout(), 1, new Object[] {side});
				
				vkUpdateDescriptorSets(device, allocBuffer(frame, VkWriteDescriptorSet::create, VkWriteDescriptorSet.SIZEOF, IntStream
											   .range(0, framesInFlight)
											   .mapToObj(i -> (Consumer<VkWriteDescriptorSet>) writeDescriptorSet -> writeDescriptorSet.set(
													   VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET,
													   0,
													   firstTrianglePipelineRenderDescriptorSet.sets()[0].address(),
													   0,
													   0,
													   VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
													   null,
													   allocBuffer(frame, VkDescriptorBufferInfo::create, VkDescriptorBufferInfo.SIZEOF, vkDescriptorBufferInfo -> vkDescriptorBufferInfo.set(
															   uniformBuffer.address(),
															   0,
															   uniformBuffer.sizeOf()
													   )),
													   null
											   ))
											   .collect(Collectors.toList())),
									   null);
			}
			
			//commandPool
			VkCommandPool commandPool;
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
				
				VkCommandBuffer commandBuffer = commandPool.allocCommandBuffer(VK_COMMAND_BUFFER_LEVEL_SECONDARY, new Object[] {side});
				commandBuffer.begin(VK_COMMAND_BUFFER_USAGE_SIMULTANEOUS_USE_BIT | VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT, firstTriangleRenderPass.renderPass().subpasses()[0].inheritanceInfo());
				
				firstTrianglePipelineRender.bindPipeline(commandBuffer, firstTrianglePipelineRenderDescriptorSet.sets()[0]);
				vkCmdBindVertexBuffers(commandBuffer, 0, new long[] {vkBuffer.address()}, new long[] {0});
				vkCmdDraw(commandBuffer, (int) (vkBuffer.sizeOf() / FP32.multiply(9)), 1, 0, 0);
				
				commandBuffer.end();
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
			
			//inputs
			BarrierImpl isRunning = new BarrierImpl();
			window.getWindowCloseEvent().addHook(window1 -> isRunning.triggerNow());
			List<Keyboard> keyboards = windowContext.getInputDevices().stream().filter(dev -> dev instanceof Keyboard).map(Keyboard.class::cast).collect(Collectors.toUnmodifiableList());
			List<Mouse> mouses = windowContext.getInputDevices().stream().filter(dev -> dev instanceof Mouse).map(Mouse.class::cast).collect(Collectors.toUnmodifiableList());
			keyboards.forEach(
					keyboard -> keyboard.getKeyInputEvent().addHook((key, pressed) -> {
						if (pressed) {
							boolean next = key == Keycode.KEY_DOWN;
							boolean prev = key == Keycode.KEY_UP;
							if (next || prev) {
								modelId.set(() -> {
									int current = modelId.assertGet();
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
			mouses.forEach(mouse -> mouse.getMouseMovementEvent().addHook((absolute, relative) -> {
				Objects.requireNonNull(relative);
				Quaternionf rotation = new Quaternionf();
				if (relative[0] != 0)
					rotation.multiply(new AxisAndAnglef(0, -1, 0, (float) relative[0] * speedMouse));
				if (relative[1] != 0)
					rotation.multiply(new AxisAndAnglef(1, 0, 0, (float) relative[1] * speedMouse));
				camera.rotateRelative(rotation);
			}));
			
			FpsRenderer<FirstTriangleInfos> fpsRenderer = null;
			try {
				fpsRenderer = new FpsRenderer<>(swapchain, frameBuffer, (imageIndex, timeMillis) -> {
					
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
					
					try (AllocatorFrame frame = Allocator.frame()) {
						Matrix4f matrixModel = camera.toMatrix4Inverse(new Matrix4f());
						float[] translation = new float[3 * 16 + 3];
						matrixPerspective.write(translation, 0);
						matrixModel.write(translation, 16);
						new Matrix4f(matrixModel).inversePure().write(translation, 32);
						camera.position.write(translation, 48);
						ArrayBufferFloat translationMatrix = ArrayBufferFloat.alloc(frame, translation);
						Buffer.copyMemory(translationMatrix, 0, uniformBufferMapped, 0, translationMatrix.sizeOf());
					}
					
					FirstTriangleInfos infos = new FirstTriangleInfos(imageIndex);
					return window.pollEventsTask().toFuture(() -> infos);
				}, 60, EMPTY_OBJECT_ARRAY);
				isRunning.awaitUninterrupted();
			} finally {
				if (fpsRenderer != null)
					fpsRenderer.free().awaitUninterrupted();
			}
			
			logger.log(LogLevel.INFO, "Exit!");
		} finally {
			Side.exit();
		}
	}
}
