package space.engine.window.glfw;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengles.GLES;
import space.engine.barrier.future.Future;
import space.engine.baseobject.exceptions.FreedException;
import space.engine.delegate.collection.ObservableCollection;
import space.engine.event.Event;
import space.engine.event.SequentialEventBuilder;
import space.engine.event.typehandler.TypeHandlerParallel;
import space.engine.freeable.Freeable;
import space.engine.key.attribute.AbstractAttributeList;
import space.engine.key.attribute.AttributeList;
import space.engine.window.InputDevice;
import space.engine.window.InputDevice.KeyInputDevice.CharacterInputEvent;
import space.engine.window.InputDevice.KeyInputDevice.KeyInputEvent;
import space.engine.window.InputDevice.Keyboard;
import space.engine.window.InputDevice.Mouse;
import space.engine.window.InputDevice.Mouse.ScrollEvent;
import space.engine.window.InputDevice.PointerInputDevice.MouseMovementEvent;
import space.engine.window.Window;
import space.engine.window.WindowContext;
import space.engine.window.WindowThread;
import space.engine.window.exception.WindowUnsupportedApiTypeException;
import space.engine.window.glfw.GLFWWindow.Storage;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.lwjgl.glfw.GLFW.*;
import static space.engine.barrier.Barrier.nowRun;
import static space.engine.freeable.Freeable.*;
import static space.engine.window.glfw.GLFWUtil.*;

public class GLFWContext implements WindowContext, CleanerWrapper {
	
	public final @NotNull GLFWWindowFramework framework;
	private final @NotNull Storage storage;
	@Nullable Object apiType;
	
	public static Future<GLFWContext> create(@NotNull GLFWWindowFramework framework, @NotNull AttributeList<WindowContext> format, Object[] parents) {
		GLFWContext context = new GLFWContext(framework, parents);
		return nowRun(context.storage, () -> context.initNativeWindow(format)).toFuture(() -> context);
	}
	
	private GLFWContext(@NotNull GLFWWindowFramework framework, Object[] parents) {
		this.framework = framework;
		this.storage = new Storage(this, parents);
	}
	
	//storage
	@Override
	public @NotNull Freeable getStorage() {
		return storage;
	}
	
	//basic
	@Override
	public void execute(@NotNull Runnable command) {
		storage.execute(command);
	}
	
	/**
	 * return value always valid pointer
	 */
	public long getWindowPointer() throws FreedException {
		return storage.getWindowPointer();
	}
	
	//window creation
	@Override
	public @NotNull Future<GLFWWindow> createWindow(@NotNull AttributeList<Window> format, Object[] parents) {
		return GLFWWindow.create(this, format, addIfNotContained(parents, this));
	}
	
	//input
	private final @NotNull Event<KeyInputEvent> eventKeyInputKeyboard = new SequentialEventBuilder<>();
	private final @NotNull Event<CharacterInputEvent> eventCharacterInputKeyboard = new SequentialEventBuilder<>();
	private final @NotNull Event<KeyInputEvent> eventKeyInputMouse = new SequentialEventBuilder<>();
	private final @NotNull Event<MouseMovementEvent> eventMouseMovement = new SequentialEventBuilder<>();
	private final @NotNull Event<ScrollEvent> eventMouseScroll = new SequentialEventBuilder<>();
	private final @NotNull Set<Integer> pressedKeysKeyboard = new ConcurrentHashMap<Integer, Boolean>().keySet(Boolean.TRUE);
	private final @NotNull Set<Integer> pressedKeysMouse = new ConcurrentHashMap<Integer, Boolean>().keySet(Boolean.TRUE);
	private @Nullable double[] mousePosition = null;
	
	private final @NotNull ObservableCollection<InputDevice> inputDevices = new ObservableCollection<>(List.of(new Keyboard() {
		@Override
		public String getName() {
			return "Keyboard";
		}
		
		@Override
		public Event<KeyInputEvent> getKeyInputEvent() {
			return eventKeyInputKeyboard;
		}
		
		@Override
		public Event<CharacterInputEvent> getCharacterInputEvent() {
			return eventCharacterInputKeyboard;
		}
		
		@Override
		public boolean isKeyDown(int key) {
			return pressedKeysKeyboard.contains(key);
		}
		
		@Override
		public String getKeyName(int key) {
			return glfwGetKeyName(key, 0);
		}
	}, new Mouse() {
		@Override
		public String getName() {
			return "Mouse";
		}
		
		@Override
		public boolean isKeyDown(int key) {
			return pressedKeysMouse.contains(key);
		}
		
		@Override
		public @NotNull String getKeyName(int key) {
			return "Mouse button " + key;
		}
		
		@Override
		public Event<KeyInputEvent> getKeyInputEvent() {
			return eventKeyInputMouse;
		}
		
		@Override
		public Event<CharacterInputEvent> getCharacterInputEvent() {
			return Event.voidEvent();
		}
		
		@Override
		public @Nullable double[] getCursorPosition() {
			return mousePosition;
		}
		
		@Override
		public Event<@WindowThread MouseMovementEvent> getMouseMovementEvent() {
			return eventMouseMovement;
		}
		
		@Override
		public @NotNull Event<ScrollEvent> getScrollEvent() {
			return eventMouseScroll;
		}
	}));
	
	@Override
	public @NotNull ObservableCollection<? extends InputDevice> getInputDevices() {
		return inputDevices;
	}
	
	public void triggerKeyInputEventKeyboard(int key, boolean pressed) {
		if (pressed) {
			pressedKeysKeyboard.add(key);
		} else {
			pressedKeysKeyboard.remove(key);
		}
		
		eventKeyInputKeyboard.submit((TypeHandlerParallel<KeyInputEvent>) callback -> callback.onKeyInput(key, pressed));
	}
	
	public void triggerCharacterInputEventKeyboard(String input) {
		eventCharacterInputKeyboard.submit((TypeHandlerParallel<CharacterInputEvent>) callback -> callback.onKeyInput(input));
	}
	
	public void triggerKeyInputEventMouse(int key, boolean pressed) {
		if (pressed) {
			pressedKeysMouse.add(key);
		} else {
			pressedKeysMouse.remove(key);
		}
		
		eventKeyInputMouse.submit((TypeHandlerParallel<KeyInputEvent>) callback -> callback.onKeyInput(key, pressed));
	}
	
	public void triggerMouseMovement(@NotNull double[] absolute) {
		double[] relative = new double[2];
		if (mousePosition != null) {
			relative[0] = absolute[0] - mousePosition[0];
			relative[1] = absolute[1] - mousePosition[1];
		}
		mousePosition = absolute;
		eventMouseMovement.submit((TypeHandlerParallel<MouseMovementEvent>) callback -> callback.onMouseMovement(absolute, relative));
	}
	
	public void triggerScroll(@NotNull double[] relative) {
		eventMouseScroll.submit(((TypeHandlerParallel<ScrollEvent>) callback -> callback.onScroll(relative)));
	}
	
	//windowThread init
	@WindowThread
	protected void initNativeWindow(AbstractAttributeList<WindowContext> newFormat) {
		long windowPointer;
		synchronized (GLFWInstance.GLFW_SYNC) {
			//additional window settings
			glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
			glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
			glfwWindowHint(GLFW_DOUBLEBUFFER, GLFW_FALSE);
			
			//gl api settings
			apiType = newFormat.get(API_TYPE);
			if (apiType == null) {
				glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
			} else if (apiType instanceof OpenGLApiType) {
				OpenGLApiType apiTypeGL = (OpenGLApiType) apiType;
				glfwWindowHint(GLFW_CLIENT_API, covertGLApiTypeToGLFWApi(apiTypeGL));
				switch (apiTypeGL) {
					case GL:
						glfwWindowHint(GLFW_OPENGL_PROFILE, GLFWUtil.covertGLProfileToGLFWProfile(newFormat.get(GL_PROFILE)));
						glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, toGLFWBoolean(newFormat.get(GL_FORWARD_COMPATIBLE)));
					case GL_ES:
						glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, newFormat.get(GL_VERSION_MAJOR));
						glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, newFormat.get(GL_VERSION_MINOR));
						break;
					default:
						throw new WindowUnsupportedApiTypeException(apiType);
				}
			} else {
				throw new WindowUnsupportedApiTypeException(apiType);
			}
			
			//create
			windowPointer = glfwCreateWindow(1, 1, "hidden_context_window", 0L, 0L);
		}
		
		storage.windowPointer = windowPointer;
		initCreateCapabilities(windowPointer);
	}
	
	@WindowThread
	public void initSetGLFWClientApiWindowHint() {
		if (apiType == null) {
			glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
		} else if (apiType instanceof OpenGLApiType) {
			glfwWindowHint(GLFW_CLIENT_API, covertGLApiTypeToGLFWApi((OpenGLApiType) apiType));
		} else {
			throw new WindowUnsupportedApiTypeException(apiType);
		}
	}
	
	@WindowThread
	public void initCreateCapabilities(long windowPointer) {
		// noinspection StatementWithEmptyBody
		if (apiType == null) {
			//no context via window api
		} else if (apiType instanceof OpenGLApiType) {
			glfwMakeContextCurrent(windowPointer);
			switch ((OpenGLApiType) apiType) {
				case GL:
					GL.createCapabilities();
					break;
				case GL_ES:
					GLES.createCapabilities();
					break;
				default:
					throw new WindowUnsupportedApiTypeException(apiType);
			}
		} else {
			throw new WindowUnsupportedApiTypeException(apiType);
		}
	}
	
	@WindowThread
	public long initGetContextSharePointer() {
		if (apiType == null) {
			return 0;
		} else if (apiType instanceof OpenGLApiType) {
			return getWindowPointer();
		} else {
			throw new WindowUnsupportedApiTypeException(apiType);
		}
	}
}
