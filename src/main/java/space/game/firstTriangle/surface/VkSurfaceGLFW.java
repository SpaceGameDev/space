package space.game.firstTriangle.surface;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkExtensionProperties;
import space.engine.buffer.AllocatorStack.Frame;
import space.engine.buffer.StringConverter;
import space.engine.buffer.pointer.PointerBufferPointer;
import space.engine.window.glfw.GLFWWindow;
import space.engine.window.glfw.GLFWWindowFramework;
import space.game.firstTriangle.VkExtensions;
import space.game.firstTriangle.VkInstance;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.lwjgl.glfw.GLFWVulkan.*;
import static space.engine.buffer.Allocator.allocatorStack;
import static space.engine.lwjgl.PointerBufferWrapper.streamPointerBuffer;
import static space.game.firstTriangle.VkException.assertVk;

public class VkSurfaceGLFW {
	
	//supported
	
	/**
	 * the parameter #windowFramework is unused by this method and just ensures GLFW is initialized before this is called
	 */
	public static boolean supported(@SuppressWarnings("unused") GLFWWindowFramework windowFramework) {
		return glfwVulkanSupported();
	}
	
	/**
	 * the parameter #windowFramework is unused by this method and just ensures GLFW is initialized before this is called
	 */
	public static void assertSupported(GLFWWindowFramework windowFramework) {
		if (!supported(windowFramework))
			throw new RuntimeException("GLFW Vulkan not supported!");
	}
	
	//extensions
	private static final @Nullable Collection<VkExtensionProperties> instanceExtensions;
	
	static {
		PointerBuffer pointerBuffer = glfwGetRequiredInstanceExtensions();
		if (pointerBuffer == null) {
			instanceExtensions = null;
		} else {
			List<VkExtensionProperties> extensions = streamPointerBuffer(pointerBuffer)
					.mapToObj(StringConverter::UTF8ToString)
					.map(name -> VkExtensions.extensionNameMap().get(name))
					.collect(Collectors.toList());
			
			if (extensions.stream().anyMatch(Objects::isNull)) {
				instanceExtensions = null;
			} else {
				instanceExtensions = extensions;
			}
		}
	}
	
	public static @NotNull Collection<VkExtensionProperties> getRequiredInstanceExtensions() {
		if (instanceExtensions == null)
			throw new RuntimeException("Required extensions unavailable!");
		return instanceExtensions;
	}
	
	//surface creation
	public static VkSurface<GLFWWindow> createSurfaceFromGlfwWindow(VkInstance instance, GLFWWindow window, Object[] parents) {
		try (Frame frame = allocatorStack().frame()) {
			PointerBufferPointer surfacePtr = PointerBufferPointer.malloc(frame);
			assertVk(nglfwCreateWindowSurface(instance.address(), window.getWindowPointer(), 0, surfacePtr.address()));
			return new VkSurface<>(instance, window, surfacePtr.getPointer(), parents);
		}
	}
}
