package space.game.asteroidsDemo;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.vulkan.EXTDebugUtils;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkRect2D;
import space.engine.Side;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.array.ArrayBufferFloat;
import space.engine.freeableStorage.Freeable;
import space.engine.freeableStorage.FreeableStorageCleaner;
import space.engine.freeableStorage.stack.FreeableStack.Frame;
import space.engine.key.attribute.AttributeList;
import space.engine.key.attribute.AttributeListModify;
import space.engine.logger.BaseLogger;
import space.engine.logger.LogLevel;
import space.engine.logger.Logger;
import space.engine.observable.ObservableReference;
import space.engine.sync.barrier.BarrierImpl;
import space.engine.sync.future.Future;
import space.engine.vector.AxisAndAnglef;
import space.engine.vector.Matrix4f;
import space.engine.vector.ProjectionMatrix;
import space.engine.vector.Quaternionf;
import space.engine.vector.Translation;
import space.engine.vector.Vector3f;
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
import space.game.asteroidsDemo.asteroid.Asteroid;
import space.game.asteroidsDemo.asteroid.AsteroidPipeline;
import space.game.asteroidsDemo.asteroid.AsteroidPlacer;
import space.game.asteroidsDemo.asteroid.AsteroidRenderer;
import space.game.asteroidsDemo.asteroid.AsteroidRenderer.AsteroidModel;
import space.game.asteroidsDemo.entity.Camera;
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
import static space.engine.buffer.Allocator.heap;
import static space.engine.lwjgl.LwjglStructAllocator.mallocStruct;
import static space.engine.primitive.Primitives.FP32;
import static space.engine.sync.barrier.Barrier.awaitAll;
import static space.engine.vector.AxisAndAnglef.toRadians;
import static space.engine.vulkan.managed.device.ManagedDevice.*;
import static space.engine.window.Keycode.*;
import static space.engine.window.Window.*;
import static space.engine.window.WindowContext.API_TYPE;
import static space.engine.window.extensions.MouseInputMode.MOUSE_MODE;
import static space.engine.window.extensions.VideoModeExtension.*;

@SuppressWarnings("FieldCanBeLocal")
public class AsteroidsDemo implements Runnable {
	
	public static void main(String[] args) {
		FreeableStorageCleaner.setCleanupLogger(baseLogger);
		new AsteroidsDemo().run();
	}
	
	public static BaseLogger baseLogger = BaseLogger.defaultPrinter(BaseLogger.defaultHandler(new BaseLogger()));
	
	public boolean VK_LAYER_LUNARG_standard_validation = false;
	public boolean VK_LAYER_RENDERDOC_Capture = true;
	private Logger logger = baseLogger.subLogger("asteroidsDemo");
	
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
			AsteroidPipeline asteroidPipeline = new AsteroidPipeline(asteroidDemoRenderPass, new Object[] {side});
			ManagedFrameBuffer<AsteroidDemoInfos> frameBuffer = asteroidDemoRenderPass.createManagedFrameBuffer(swapchain, device.getQueue(QUEUE_TYPE_GRAPHICS, QUEUE_FLAG_REALTIME_BIT), new Object[] {side});
			
			//renderer
			VmaBuffer[] asteroid_gasgiant = uploadAsteroids(device, new Object[] {side},
															ModelAsteroids.generateAsteroid(3000, new float[] {0f, 0f, 0f, 0.0f}, 0)
			).awaitGetUninterrupted();
			VmaBuffer[] asteroid_r2 = uploadAsteroids(device, new Object[] {side},
													  ModelAsteroids.generateAsteroid(3, new float[] {1f, 0.5f}, 1),
													  ModelAsteroids.generateAsteroid(3, new float[] {1f}, 1),
													  ModelAsteroids.generateAsteroid(3, new float[] {}, 1)
			).awaitGetUninterrupted();
			VmaBuffer[] asteroid_r4 = uploadAsteroids(device, new Object[] {side},
													  ModelAsteroids.generateAsteroid(5, new float[] {1f, 0.5f}, 2),
													  ModelAsteroids.generateAsteroid(5, new float[] {1f}, 2),
													  ModelAsteroids.generateAsteroid(5, new float[] {}, 2)
			).awaitGetUninterrupted();
			VmaBuffer[] asteroid_r6 = uploadAsteroids(device, new Object[] {side},
													  ModelAsteroids.generateAsteroid(8, new float[] {1f, 0.5f}, 3),
													  ModelAsteroids.generateAsteroid(8, new float[] {1f}, 3),
													  ModelAsteroids.generateAsteroid(8, new float[] {}, 3)
			).awaitGetUninterrupted();
			VmaBuffer[] asteroid_r8 = uploadAsteroids(device, new Object[] {side},
													  ModelAsteroids.generateAsteroid(11, new float[] {1f, 0.5f, 0.1f}, 4),
													  ModelAsteroids.generateAsteroid(11, new float[] {1f, 0.5f}, 4),
													  ModelAsteroids.generateAsteroid(11, new float[] {1f}, 4),
													  ModelAsteroids.generateAsteroid(11, new float[] {}, 4)
			).awaitGetUninterrupted();
			VmaBuffer[] asteroid_r10 = uploadAsteroids(device, new Object[] {side},
													   ModelAsteroids.generateAsteroid(15, new float[] {1f, 0.5f, 0.1f}, 5),
													   ModelAsteroids.generateAsteroid(15, new float[] {1f, 0.5f}, 5),
													   ModelAsteroids.generateAsteroid(15, new float[] {1f}, 5),
													   ModelAsteroids.generateAsteroid(15, new float[] {}, 5)
			).awaitGetUninterrupted();
			VmaBuffer[] asteroid_r12 = uploadAsteroids(device, new Object[] {side},
													   ModelAsteroids.generateAsteroid(20, new float[] {1f, 0.5f, 0.1f}, 6),
													   ModelAsteroids.generateAsteroid(20, new float[] {1f, 0.5f}, 6),
													   ModelAsteroids.generateAsteroid(20, new float[] {1f}, 6),
													   ModelAsteroids.generateAsteroid(20, new float[] {}, 6)
			).awaitGetUninterrupted();
			
			AsteroidRenderer asteroidRenderer = new AsteroidRenderer(
					asteroidDemoRenderPass,
					asteroidPipeline,
					Stream.of(asteroid_gasgiant, asteroid_r2, asteroid_r4, asteroid_r6, asteroid_r8, asteroid_r10, asteroid_r12)
						  .map(models -> {
							  float[] minDistance;
							  switch (models.length) {
								  case 1:
									  minDistance = new float[] {Float.POSITIVE_INFINITY};
									  break;
								  case 3:
									  minDistance = new float[] {1200, 2500, Float.POSITIVE_INFINITY};
									  break;
								  case 4:
									  minDistance = new float[] {400, 1200, 2500, Float.POSITIVE_INFINITY};
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
			//gas giant
			Asteroid gasGiant = new Asteroid(0);
			gasGiant.position[0].set(0, 0, 2000 * AsteroidPlacer.RADIUS_FACTOR);
			gasGiant.rotation[0].multiply(new AxisAndAnglef(0, 1, 0, (float) Math.PI));
			gasGiant.rotation[1].multiply(new AxisAndAnglef(0, 1, 0, (float) Math.PI / 150));
			asteroidRenderer.addAsteroid(gasGiant);
			//asteroids
			AsteroidPlacer.placeAsteroids(asteroidRenderer, new float[] {0, 3, 2, 2, 1, 1, 1});
			
			//uniform buffer
			VmaMappedBuffer uniformBuffer = VmaMappedBuffer.alloc(
					0,
					(4 + 3 + 1 + 1) * 4 * FP32.bytes,
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
			
			Matrix4f matrixPerspective = ProjectionMatrix.projection(new Matrix4f(), 90, (float) swapExtend.extent().width() / swapExtend.extent().height(), 0.1f, 100000f);
			Camera camera = new Camera();
			camera.rotateRelative(new AxisAndAnglef(0, 1, 0, 9f / 8f * (float) Math.PI).toQuaternion(new Quaternionf()));
			
			float speedMouse = 0.008f;
			float speedMovement = 0.05f;
			ObservableReference<@NotNull Float> speedMovementMultiplier = new ObservableReference<>(1f);
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
				mouse.getScrollEvent().addHook(relative -> speedMovementMultiplier.set(() -> {
					float curr = speedMovementMultiplier.assertGet();
					float newV = curr + (float) relative[1];
					return newV < 1 ? 1 : newV;
				}));
			});
			
			FpsRenderer<AsteroidDemoInfos> fpsRenderer = null;
			try {
				fpsRenderer = new FpsRenderer<>(device, swapchain, frameBuffer, (imageIndex, frameEventTime) -> {
					
					keyboards.forEach(keyboard -> {
						Vector3f translation = new Vector3f();
						Quaternionf rotation = new Quaternionf();
						if (keyboard.isKeyDown(KEY_A))
							translation.add(new Vector3f(-speedMovement, 0, 0));
						if (keyboard.isKeyDown(KEY_D))
							translation.add(new Vector3f(speedMovement, 0, 0));
						if (keyboard.isKeyDown(KEY_R) || keyboard.isKeyDown(KEY_SPACE))
							translation.add(new Vector3f(0, -speedMovement, 0));
						if (keyboard.isKeyDown(KEY_F) || keyboard.isKeyDown(KEY_LEFT_SHIFT))
							translation.add(new Vector3f(0, speedMovement, 0));
						if (keyboard.isKeyDown(KEY_W))
							translation.add(new Vector3f(0, 0, -speedMovement));
						if (keyboard.isKeyDown(KEY_S))
							translation.add(new Vector3f(0, 0, speedMovement));
						if (keyboard.isKeyDown(KEY_Q))
							rotation.multiply(new AxisAndAnglef(0, 0, 1, toRadians(-2)));
						if (keyboard.isKeyDown(KEY_E))
							rotation.multiply(new AxisAndAnglef(0, 0, 1, toRadians(2)));
						camera.rotateRelative(rotation);
						float multi = speedMovementMultiplier.assertGet();
						camera.translateRelative(translation.multiply(multi * multi));
					});
					
					AsteroidDemoInfos infos = new AsteroidDemoInfos(imageIndex, matrixPerspective, camera, camera.toTranslation(new Translation()).inverse(), gasGiant, new Vector3f(10, 1, 0).normalize(), frameEventTime / 60f, uniformBuffer);
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
		
		return awaitAll(modelBuffers).toFuture(() -> modelBuffers.stream().map(Future::assertGet).toArray(VmaBuffer[]::new));
	}
}
