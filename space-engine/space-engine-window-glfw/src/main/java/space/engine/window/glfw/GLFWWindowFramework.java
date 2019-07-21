package space.engine.window.glfw;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import space.engine.barrier.future.Future;
import space.engine.delegate.collection.ObservableCollection;
import space.engine.freeableStorage.Freeable;
import space.engine.freeableStorage.Freeable.FreeableWrapper;
import space.engine.key.attribute.AttributeList;
import space.engine.window.Monitor;
import space.engine.window.WindowContext;
import space.engine.window.WindowFramework;
import space.engine.window.extensions.BorderlessExtension;
import space.engine.window.extensions.ResizeableExtension;
import space.engine.window.extensions.VideoModeDesktopExtension;
import space.engine.window.extensions.VideoModeFullscreenExtension;
import space.engine.window.extensions.WindowExtension;

import java.util.Collection;
import java.util.List;

import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static space.engine.freeableStorage.Freeable.addIfNotContained;

public class GLFWWindowFramework implements WindowFramework, FreeableWrapper {
	
	/**
	 * finalization of the object will {@link GLFW#glfwTerminate()}
	 */
	@SuppressWarnings("unused")
	private final GLFWInstance glfwInstance = GLFWInstance.getInstance();
	private final Freeable storage = Freeable.createDummy(new Object[] {glfwInstance});
	
	//window
	@NotNull
	@Override
	public Future<? extends GLFWContext> createContext(@NotNull AttributeList<WindowContext> format, Object[] parents) {
		return GLFWContext.create(this, format, addIfNotContained(parents, this));
	}
	
	@Override
	public Collection<Class<? extends WindowExtension>> getSupportedWindowExtensions() {
		return List.of(BorderlessExtension.class, ResizeableExtension.class, VideoModeDesktopExtension.class, VideoModeFullscreenExtension.class);
	}
	
	//monitor
	@NotNull
	@Override
	public ObservableCollection<? extends Monitor> getAllMonitors() {
		return glfwInstance.monitors;
	}
	
	@NotNull
	@Override
	public Future<? extends Monitor> getPrimaryMonitor() {
		return Future.finished(getPrimaryMonitorDirect());
	}
	
	@NotNull
	public GLFWMonitor getPrimaryMonitorDirect() {
		return new GLFWMonitor(glfwGetPrimaryMonitor());
	}
	
	//free
	@Override
	public @NotNull Freeable getStorage() {
		return storage;
	}
}
