package space.game.asteroidsDemo;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.vulkan.EXTDebugUtils;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkRect2D;
import space.engine.Side;
import space.engine.barrier.BarrierImpl;
import space.engine.barrier.DelayTask;
import space.engine.barrier.functions.RunnableWithDelay;
import space.engine.barrier.future.Future;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.array.ArrayBufferFloat;
import space.engine.freeable.CleanerThread;
import space.engine.freeable.Freeable;
import space.engine.freeable.stack.FreeableStack.Frame;
import space.engine.key.attribute.AttributeList;
import space.engine.key.attribute.AttributeListModify;
import space.engine.logger.BaseLogger;
import space.engine.logger.LogLevel;
import space.engine.logger.Logger;
import space.engine.observable.MutableObservableReference;
import space.engine.vector.AxisAngle;
import space.engine.vector.Matrix4;
import space.engine.vector.ProjectionMatrix;
import space.engine.vector.Quaternion;
import space.engine.vector.Vector3;
import space.engine.vulkan.VkInstance;
import space.engine.vulkan.VkInstanceExtensions;
import space.engine.vulkan.VkInstanceValidationLayers;
import space.engine.vulkan.VkPhysicalDevice;
import space.engine.vulkan.managed.device.ManagedDevice;
import space.engine.vulkan.managed.device.ManagedDeviceSingleQueue;
import space.engine.vulkan.managed.instance.ManagedInstance;
import space.engine.vulkan.managed.renderPass.ManagedFrameBuffer;
import space.engine.vulkan.managed.surface.ManagedSwapchain;
import space.engine.vulkan.surface.VkSurface;
import space.engine.vulkan.surface.glfw.VkSurfaceGLFW;
import space.engine.vulkan.util.FpsRenderer;
import space.engine.vulkan.vma.VmaBuffer;
import space.engine.vulkan.vma.VmaMappedBuffer;
import space.engine.window.InputDevice.Keyboard;
import space.engine.window.InputDevice.Mouse;
import space.engine.window.Window;
import space.engine.window.WindowContext;
import space.engine.window.extensions.MouseInputMode.Modes;
import space.engine.window.extensions.VideoModeDesktopExtension;
import space.engine.window.glfw.GLFWContext;
import space.engine.window.glfw.GLFWWindow;
import space.engine.window.glfw.GLFWWindowFramework;
import space.game.asteroidsDemo.asteroid.AsteroidPipeline;
import space.game.asteroidsDemo.asteroid.AsteroidPlacer;
import space.game.asteroidsDemo.asteroid.AsteroidRenderer;
import space.game.asteroidsDemo.asteroid.AsteroidRenderer.AsteroidModel;
import space.game.asteroidsDemo.entity.Camera;
import space.game.asteroidsDemo.gasgiant.Gasgiant;
import space.game.asteroidsDemo.gasgiant.GasgiantPipeline;
import space.game.asteroidsDemo.gasgiant.GasgiantRenderer;
import space.game.asteroidsDemo.model.ModelAsteroids;
import space.game.asteroidsDemo.model.ModelAsteroids.Result;
import space.game.asteroidsDemo.renderPass.AsteroidDemoInfos;
import space.game.asteroidsDemo.renderPass.AsteroidDemoRenderPass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.lwjgl.util.vma.Vma.*;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK10.*;
import static space.engine.Empties.EMPTY_OBJECT_ARRAY;
import static space.engine.barrier.Barrier.*;
import static space.engine.buffer.Allocator.heap;
import static space.engine.lwjgl.LwjglStructAllocator.mallocStruct;
import static space.engine.primitive.Primitives.FP32;
import static space.engine.vector.AxisAngle.toRadians;
import static space.engine.vulkan.managed.device.ManagedDevice.*;
import static space.engine.window.Keycode.*;
import static space.engine.window.Window.*;
import static space.engine.window.WindowContext.API_TYPE;
import static space.engine.window.extensions.MouseInputMode.MOUSE_MODE;
import static space.engine.window.extensions.VideoModeExtension.*;

@SuppressWarnings("FieldCanBeLocal")
public class AsteroidsDemo implements RunnableWithDelay {
	
	public static void main(String[] args) {
		CleanerThread.setCleanupLogger(baseLogger);
		nowRun(new AsteroidsDemo()).awaitUninterrupted();
	}
	
	public static BaseLogger baseLogger = BaseLogger.defaultPrinter(BaseLogger.defaultHandler(new BaseLogger()));
	
	public boolean VK_LAYER_LUNARG_standard_validation = true;
	public boolean VK_LAYER_RENDERDOC_Capture = false;
	private Logger logger = baseLogger.subLogger("asteroidsDemo");
	public final boolean ASTEROIDS_FLAT = true;
	
	public void run() throws DelayTask {
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
					"asteroidsDemo",
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
					VkInstance.DEFAULT_BEST_PHYSICAL_DEVICE_TYPES,
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
				windowModify.put(WIDTH, 1920);
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
			
			//renderPass and pipeline
			AsteroidDemoRenderPass asteroidDemoRenderPass = new AsteroidDemoRenderPass(device, swapExtend, swapchain.imageFormat(), new Object[] {side});
			
			//asteroids
			AsteroidPipeline asteroidPipeline = new AsteroidPipeline(asteroidDemoRenderPass, new Object[] {side});
			ManagedFrameBuffer<AsteroidDemoInfos> frameBuffer = asteroidDemoRenderPass.createManagedFrameBuffer(swapchain, device.getQueue(QUEUE_TYPE_GRAPHICS, QUEUE_FLAG_REALTIME_BIT), new Object[] {side});
			
			float[][] config = new float[][] {
					new float[] {0.5f},
					new float[] {0.5f, 0.3f},
					new float[] {0.5f, 0.3f, 0.2f},
					new float[] {0.5f, 0.3f, 0.2f, 0.1f},
			};
			
			VmaBuffer[] asteroid_r2 = uploadAsteroids(device, new Object[] {side},
													  ModelAsteroids.generateAsteroid(2, config[2], ASTEROIDS_FLAT, 1),
													  ModelAsteroids.generateAsteroid(2, config[1], ASTEROIDS_FLAT, 1),
													  ModelAsteroids.generateAsteroid(2, config[0], ASTEROIDS_FLAT, 1)
			).awaitGetUninterrupted();
			VmaBuffer[] asteroid_r4 = uploadAsteroids(device, new Object[] {side},
													  ModelAsteroids.generateAsteroid(4, config[2], ASTEROIDS_FLAT, 2),
													  ModelAsteroids.generateAsteroid(4, config[1], ASTEROIDS_FLAT, 2),
													  ModelAsteroids.generateAsteroid(4, config[0], ASTEROIDS_FLAT, 2)
			).awaitGetUninterrupted();
			VmaBuffer[] asteroid_r6 = uploadAsteroids(device, new Object[] {side},
													  ModelAsteroids.generateAsteroid(6, config[2], ASTEROIDS_FLAT, 3),
													  ModelAsteroids.generateAsteroid(6, config[1], ASTEROIDS_FLAT, 3),
													  ModelAsteroids.generateAsteroid(6, config[0], ASTEROIDS_FLAT, 3)
			).awaitGetUninterrupted();
			VmaBuffer[] asteroid_r8 = uploadAsteroids(device, new Object[] {side},
													  ModelAsteroids.generateAsteroid(8, config[3], ASTEROIDS_FLAT, 4),
													  ModelAsteroids.generateAsteroid(8, config[2], ASTEROIDS_FLAT, 4),
													  ModelAsteroids.generateAsteroid(8, config[1], ASTEROIDS_FLAT, 4),
													  ModelAsteroids.generateAsteroid(8, config[0], ASTEROIDS_FLAT, 4)
			).awaitGetUninterrupted();
			VmaBuffer[] asteroid_r10 = uploadAsteroids(device, new Object[] {side},
													   ModelAsteroids.generateAsteroid(10, config[3], ASTEROIDS_FLAT, 5),
													   ModelAsteroids.generateAsteroid(10, config[2], ASTEROIDS_FLAT, 5),
													   ModelAsteroids.generateAsteroid(10, config[1], ASTEROIDS_FLAT, 5),
													   ModelAsteroids.generateAsteroid(10, config[0], ASTEROIDS_FLAT, 5)
			).awaitGetUninterrupted();
			VmaBuffer[] asteroid_r12 = uploadAsteroids(device, new Object[] {side},
													   ModelAsteroids.generateAsteroid(12, config[3], ASTEROIDS_FLAT, 6),
													   ModelAsteroids.generateAsteroid(12, config[2], ASTEROIDS_FLAT, 6),
													   ModelAsteroids.generateAsteroid(12, config[1], ASTEROIDS_FLAT, 6),
													   ModelAsteroids.generateAsteroid(12, config[0], ASTEROIDS_FLAT, 6)
			).awaitGetUninterrupted();
			
			AsteroidRenderer asteroidRenderer = new AsteroidRenderer(
					asteroidDemoRenderPass,
					asteroidPipeline,
					Stream.of(asteroid_r2, asteroid_r4, asteroid_r6, asteroid_r8, asteroid_r10, asteroid_r12)
						  .map(models -> {
							  float[] minDistance;
							  switch (models.length) {
								  case 1:
									  minDistance = new float[] {Float.POSITIVE_INFINITY};
									  break;
								  case 2:
									  minDistance = new float[] {3500, Float.POSITIVE_INFINITY};
									  break;
								  case 3:
									  minDistance = new float[] {2000, 3500, Float.POSITIVE_INFINITY};
									  break;
								  case 4:
									  minDistance = new float[] {1000, 2000, 3500, Float.POSITIVE_INFINITY};
									  break;
								  default:
									  throw new RuntimeException();
							  }
							  return new AsteroidModel(models, minDistance);
						  })
						  .toArray(AsteroidModel[]::new),
					new Object[] {side}
			);
			asteroidDemoRenderPass.callbacks().addHook(asteroidRenderer);
			AsteroidPlacer.placeAsteroids(asteroidRenderer, new float[] {3, 2, 2, 1, 1, 1}, 1);
			
			//gasgiant
			Gasgiant gasGiant = new Gasgiant(0);
			gasGiant.position[0] = new Vector3(0, 0, 2000 * AsteroidPlacer.RADIUS_FACTOR);
			gasGiant.rotation[0] = gasGiant.rotation[0].multiply(new AxisAngle(0, 1, 0, (float) Math.PI));
			gasGiant.rotation[1] = gasGiant.rotation[1].multiply(new AxisAngle(0, 1, 0, (float) Math.PI / 150));
			
			VmaBuffer gasgiant_model = uploadAsteroids(device, new Object[] {side},
													   ModelAsteroids.generateAsteroid(3000, new float[] {0f, 0f, 0f, 0f, 0f, 0f}, false, 0)
			).awaitGetUninterrupted()[0];
			GasgiantPipeline gasgiantPipeline = new GasgiantPipeline(asteroidDemoRenderPass, new Object[] {side});
			GasgiantRenderer gasgiantRenderer = new GasgiantRenderer(asteroidDemoRenderPass, gasgiantPipeline, gasgiant_model, gasGiant, new Object[] {side});
			asteroidDemoRenderPass.callbacks().addHook(gasgiantRenderer);
			
			//uniform buffer
			VmaMappedBuffer uniformBuffer = VmaMappedBuffer.alloc(
					0,
					AsteroidDemoInfos.UNIFORM_GLOBAL_SIZEOF,
					VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
					VMA_ALLOCATION_CREATE_MAPPED_BIT,
					VMA_MEMORY_USAGE_CPU_TO_GPU,
					device,
					new Object[] {side}
			);
			
			//inputs
			BarrierImpl isRunning = new BarrierImpl();
			window.getWindowCloseEvent().addHook(window1 -> isRunning.triggerNow());
			List<Keyboard> keyboards = windowContext.getInputDevices().stream().filter(dev -> dev instanceof Keyboard).map(Keyboard.class::cast).collect(Collectors.toUnmodifiableList());
			List<Mouse> mouses = windowContext.getInputDevices().stream().filter(dev -> dev instanceof Mouse).map(Mouse.class::cast).collect(Collectors.toUnmodifiableList());
			
			Matrix4 matrixPerspective = ProjectionMatrix.projection(90, (float) swapExtend.extent().width() / swapExtend.extent().height(), 0.1f, 100000f);
			Camera camera = new Camera();
			
			float speedMouse = 0.008f;
			float speedMovement = 0.05f;
			MutableObservableReference<@NotNull Float> speedMovementMultiplier = new MutableObservableReference<>(1f);
			mouses.forEach(mouse -> {
				mouse.getMouseMovementEvent().addHook((absolute, relative) -> {
					Objects.requireNonNull(relative);
					Quaternion rotation = Quaternion.identity();
					if (relative[0] != 0)
						rotation = rotation.multiply(new AxisAngle(0, 1, 0, (float) relative[0] * speedMouse));
					if (relative[1] != 0)
						rotation = rotation.multiply(new AxisAngle(-1, 0, 0, (float) relative[1] * speedMouse));
					camera.rotateRelative(rotation);
				});
				mouse.getScrollEvent().addHook(relative -> speedMovementMultiplier.set(curr -> {
					float newV = curr + (float) relative[1];
					return newV < 1 ? 1 : newV;
				}));
			});
			
			FpsRenderer<AsteroidDemoInfos> fpsRenderer = null;
			try {
				fpsRenderer = new FpsRenderer<>(device, swapchain, frameBuffer, (imageIndex, frameEventTime) -> {
					
					keyboards.forEach(keyboard -> {
						Vector3 translation = Vector3.zero();
						Quaternion rotation = Quaternion.identity();
						if (keyboard.isKeyDown(KEY_A))
							translation = translation.add(new Vector3(-speedMovement, 0, 0));
						if (keyboard.isKeyDown(KEY_D))
							translation = translation.add(new Vector3(speedMovement, 0, 0));
						if (keyboard.isKeyDown(KEY_R) || keyboard.isKeyDown(KEY_SPACE))
							translation = translation.add(new Vector3(0, -speedMovement, 0));
						if (keyboard.isKeyDown(KEY_F) || keyboard.isKeyDown(KEY_LEFT_SHIFT))
							translation = translation.add(new Vector3(0, speedMovement, 0));
						if (keyboard.isKeyDown(KEY_W))
							translation = translation.add(new Vector3(0, 0, -speedMovement));
						if (keyboard.isKeyDown(KEY_S))
							translation = translation.add(new Vector3(0, 0, speedMovement));
						if (keyboard.isKeyDown(KEY_Q))
							rotation = rotation.multiply(new AxisAngle(0, 0, 1, toRadians(3)));
						if (keyboard.isKeyDown(KEY_E))
							rotation = rotation.multiply(new AxisAngle(0, 0, -1, toRadians(3)));
						camera.rotateRelative(rotation);
						float multi = speedMovementMultiplier.assertGet();
						camera.translateRelative(translation.multiply(multi * multi));
					});
					
					AsteroidDemoInfos infos = new AsteroidDemoInfos(imageIndex, matrixPerspective, camera, gasGiant, new Vector3(4.5f, -1, 0).normalize(), frameEventTime / 60f, uniformBuffer);
					return window.pollEventsTask().toFuture(() -> infos);
				}, 60, EMPTY_OBJECT_ARRAY);
				isRunning.awaitUninterrupted();
			} finally {
				if (fpsRenderer != null)
					fpsRenderer.free().awaitUninterrupted();
			}
			
			logger.log(LogLevel.INFO, "Exit!");
			throw new DelayTask(Side.exit());
		}
	}
	
	private static Future<VmaBuffer[]> uploadAsteroids(ManagedDevice device, Object[] parents, ModelAsteroids.Result... models) {
		return uploadModel(device, parents, Arrays.stream(models).map(Result::unpackIndexBuffer).toArray(float[][]::new));
	}
	
	private static Future<VmaBuffer[]> uploadModel(ManagedDevice device, Object[] parents, float[]... models) {
		List<Future<VmaBuffer>> modelBuffers = Arrays
				.stream(models)
				.map(data -> {
					try (AllocatorFrame frame = Allocator.frame()) {
						VmaBuffer vmaBuffer = VmaBuffer.alloc(0, data.length * FP32.bytes, VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT, 0, VMA_MEMORY_USAGE_GPU_ONLY, device, parents);
						ArrayBufferFloat dataBuffer = ArrayBufferFloat.alloc(heap(), data, new Object[] {frame});
						return vmaBuffer.uploadData(dataBuffer).toFuture(() -> vmaBuffer);
					}
				})
				.collect(Collectors.toUnmodifiableList());
		
		return when(modelBuffers).toFuture(() -> modelBuffers.stream().map(Future::assertGet).toArray(VmaBuffer[]::new));
	}
}
