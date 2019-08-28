package space.engine.window.glfw;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.lwjgl.glfw.GLFWMonitorCallbackI;
import space.engine.barrier.Barrier;
import space.engine.delegate.collection.ObservableCollection;
import space.engine.freeableStorage.Freeable;
import space.engine.freeableStorage.FreeableList;
import space.engine.freeableStorage.FreeableStorageWeak;
import space.engine.window.exception.WindowFrameworkInitializationException;

import java.util.concurrent.ConcurrentHashMap;

import static org.lwjgl.glfw.GLFW.*;

public class GLFWInstance implements Freeable {
	
	public static final Object GLFW_SYNC = new Object();
	
	public static @Nullable Storage instanceRef;
	
	@NotNull GLFWErrorCallbackI glfwErrorCallback = new GLFWErrorCallback();
	@NotNull ObservableCollection<GLFWMonitor> monitors = new ObservableCollection<>(new ConcurrentHashMap<GLFWMonitor, Boolean>().keySet(true));
	@NotNull GLFWMonitorCallbackI glfwMonitorCallback = (monitor, event) -> {
		switch (event) {
			case GLFW_CONNECTED:
				monitors.add(new GLFWMonitor(monitor));
				break;
			case GLFW_DISCONNECTED:
				monitors.remove(new GLFWMonitor(monitor));
				break;
			default:
				throw new IllegalArgumentException();
		}
	};
	
	public static synchronized GLFWInstance getInstance() {
		if (instanceRef != null) {
			GLFWInstance inst = instanceRef.get();
			if (inst != null)
				return inst;
		}
		
		GLFWInstance inst = new GLFWInstance();
		instanceRef = new Storage(inst, new FreeableList[] {Freeable.ROOT_LIST});
		return inst;
	}
	
	//object
	private GLFWInstance() {
		if (!glfwInit())
			throw new WindowFrameworkInitializationException("glfwInit() returned false!");
		glfwSetErrorCallback(glfwErrorCallback);
		glfwSetMonitorCallback(glfwMonitorCallback);
	}
	
	//free
	@Override
	public @NotNull Barrier free() {
		return Barrier.DONE_BARRIER;
	}
	
	@Override
	public boolean isFreed() {
		return false;
	}
	
	@Override
	public @NotNull FreeableList getSubList() {
		if (instanceRef != null)
			return instanceRef.getSubList();
		throw new RuntimeException("getInstance() has to be called before getSubList()!");
	}
	
	protected static class Storage extends FreeableStorageWeak<GLFWInstance> {
		
		public Storage(GLFWInstance referent, Object[] parents) {
			super(referent, parents);
		}
		
		@Override
		protected @NotNull Barrier handleFree() {
			synchronized (GLFWInstance.class) {
				if (this == instanceRef) {
					instanceRef = null;
					glfwTerminate();
				}
			}
			return Barrier.DONE_BARRIER;
		}
	}
}
