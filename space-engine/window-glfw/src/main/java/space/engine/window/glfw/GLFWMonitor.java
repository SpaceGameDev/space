package space.engine.window.glfw;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWVidMode.Buffer;
import space.engine.window.Monitor;

import java.util.Objects;

import static org.lwjgl.glfw.GLFW.*;

public class GLFWMonitor implements Monitor {
	
	public long pointer;
	public String name;
	public int physicalWidth;
	public int physicalHeight;
	public int posX;
	public int posY;
	public VideoMode currentVideoMode;
	public VideoMode[] availableVideoModes;
	
	public GLFWMonitor(long pointer) {
		this.pointer = pointer;
		int[] x = new int[1];
		int[] y = new int[1];
		
		//name
		name = Objects.requireNonNull(glfwGetMonitorName(pointer));
		
		//physicalSize
		glfwGetMonitorPhysicalSize(pointer, x, y);
		physicalWidth = x[0];
		physicalHeight = y[0];
		
		//pos
		glfwGetMonitorPos(pointer, x, y);
		posX = x[0];
		posY = y[0];
		
		//videoMode
		currentVideoMode = new GLFWVideoModeMonitor(Objects.requireNonNull(glfwGetVideoMode(pointer)));
		Buffer modes = Objects.requireNonNull(glfwGetVideoModes(pointer));
		VideoMode[] array = new VideoMode[modes.capacity()];
		for (int i = 0; i < array.length; i++)
			array[i] = new GLFWVideoModeMonitor(modes.get(i));
		availableVideoModes = array;
	}
	
	@NotNull
	@Override
	public String name() {
		return name;
	}
	
	@Override
	public int physicalWidth() {
		return physicalWidth;
	}
	
	@Override
	public int physicalHeight() {
		return physicalHeight;
	}
	
	@Override
	public int posX() {
		return posX;
	}
	
	@Override
	public int posY() {
		return posY;
	}
	
	@NotNull
	@Override
	public VideoMode getDefaultVideoMode() {
		return currentVideoMode;
	}
	
	@NotNull
	@Override
	public VideoMode[] getAvailableVideoModes() {
		return availableVideoModes;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof GLFWMonitor))
			return false;
		GLFWMonitor that = (GLFWMonitor) o;
		return pointer == that.pointer;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(pointer);
	}
	
	public class GLFWVideoModeMonitor implements VideoMode {
		
		public GLFWVidMode mode;
		
		public GLFWVideoModeMonitor(GLFWVidMode mode) {
			this.mode = mode;
		}
		
		@Override
		@NotNull
		public GLFWMonitor monitor() {
			return GLFWMonitor.this;
		}
		
		@Override
		public int width() {
			return mode.width();
		}
		
		@Override
		public int height() {
			return mode.height();
		}
		
		@Override
		public int bitsR() {
			return mode.redBits();
		}
		
		@Override
		public int bitsG() {
			return mode.greenBits();
		}
		
		@Override
		public int bitsB() {
			return mode.blueBits();
		}
		
		@Override
		public int refreshRate() {
			return mode.refreshRate();
		}
	}
}
