package space.game.asteroidsDemo;

import org.lwjgl.vulkan.EXTDebugUtils;
import space.engine.logger.BaseLogger;
import space.engine.logger.Logger;
import space.engine.observable.GeneratingObservableReference;
import space.engine.observable.MutableObservableReference;
import space.engine.observable.ObservableReference;
import space.engine.vulkan.VkInstanceExtensions;
import space.engine.vulkan.VkInstanceValidationLayers;
import space.engine.vulkan.managed.instance.ManagedInstance;
import space.engine.vulkan.surface.glfw.VkSurfaceGLFW;
import space.engine.window.WindowFramework;
import space.engine.window.glfw.GLFWWindowFramework;

import java.util.ArrayList;
import java.util.List;

public class Game {
	
	public static BaseLogger baseLogger = BaseLogger.defaultPrinter(BaseLogger.defaultHandler(new BaseLogger()));
	
	public boolean VK_LAYER_LUNARG_standard_validation = true;
	public boolean VK_LAYER_RENDERDOC_Capture = true;
	private Logger logger = baseLogger.subLogger("asteroidsDemo");
	
	//windowFramework
	public final ObservableReference<WindowFramework> windowFramework = ObservableReference.fromSupplier(() -> {
		GLFWWindowFramework windowFramework = new GLFWWindowFramework();
		VkSurfaceGLFW.assertSupported(windowFramework);
		return windowFramework;
	});
	
	//extension / layer selection
	public final MutableObservableReference<List<String>> instanceExtensions = new MutableObservableReference<>();
	
	//instance
	public final ObservableReference<ManagedInstance> vkInstance = GeneratingObservableReference.create(windowFramework, (windowFramework1, previous) -> {
		List<String> instanceExtensions = new ArrayList<>();
		List<String> instanceLayers = new ArrayList<>();
		if (VK_LAYER_LUNARG_standard_validation) {
			instanceExtensions.add(EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME);
			instanceLayers.add("VK_LAYER_LUNARG_standard_validation");
		}
		if (VK_LAYER_RENDERDOC_Capture) {
			instanceLayers.add("VK_LAYER_RENDERDOC_Capture");
		}
		instanceExtensions.addAll(VkSurfaceGLFW.getRequiredInstanceExtensions((GLFWWindowFramework) windowFramework1));
		
		return ManagedInstance.alloc(
				"asteroidsDemo",
				1,
				baseLogger.subLogger("Vulkan"),
				VkInstanceValidationLayers.makeLayerList(instanceLayers, List.of()),
				VkInstanceExtensions.makeExtensionList(instanceExtensions, List.of()),
				new Object[] {side}
		);
	});
}
