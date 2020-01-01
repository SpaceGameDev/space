package space.engine.window.glfw;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI;
import org.lwjgl.glfw.GLFWWindowCloseCallbackI;
import org.lwjgl.glfw.GLFWWindowFocusCallbackI;
import org.lwjgl.glfw.GLFWWindowIconifyCallbackI;
import org.lwjgl.glfw.GLFWWindowPosCallbackI;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;
import space.engine.barrier.Barrier;
import space.engine.barrier.DelayTask;
import space.engine.barrier.future.Future;
import space.engine.baseobject.exceptions.FreedException;
import space.engine.event.Event;
import space.engine.event.EventEntry;
import space.engine.event.SequentialEventBuilder;
import space.engine.event.typehandler.TypeHandlerParallel;
import space.engine.freeableStorage.Freeable;
import space.engine.freeableStorage.Freeable.FreeableWrapper;
import space.engine.freeableStorage.FreeableStorage;
import space.engine.key.attribute.AbstractAttributeList;
import space.engine.key.attribute.AttributeList;
import space.engine.key.attribute.AttributeListModify;
import space.engine.simpleQueue.pool.Executor;
import space.engine.simpleQueue.pool.SimpleThreadPool;
import space.engine.window.Monitor;
import space.engine.window.Monitor.VideoMode;
import space.engine.window.Window;
import space.engine.window.Window.WindowFocusCallback.WindowFocus;
import space.engine.window.WindowThread;
import space.engine.window.extensions.VideoModeDesktopExtension;
import space.engine.window.extensions.VideoModeExtension;
import space.engine.window.extensions.VideoModeFullscreenExtension;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static space.engine.barrier.Barrier.nowRun;
import static space.engine.window.extensions.BorderlessExtension.BORDERLESS;
import static space.engine.window.extensions.MouseInputMode.MOUSE_MODE;
import static space.engine.window.extensions.ResizeableExtension.*;
import static space.engine.window.extensions.VideoModeDesktopExtension.*;
import static space.engine.window.extensions.VideoModeFullscreenExtension.FULLSCREEN_VIDEO_MODE;
import static space.engine.window.glfw.GLFWUtil.toGLFWBoolean;

public class GLFWWindow implements Window, FreeableWrapper {
	
	private static final AtomicInteger WINDOW_THREAD_COUNTER = new AtomicInteger();
	
	/**
	 * keep this reference to prevent freeing of WindowFramework
	 */
	public final @NotNull GLFWContext context;
	public final @NotNull AttributeList<Window> format;
	private final Storage storage;
	
	public static @NotNull Future<GLFWWindow> create(@NotNull GLFWContext context, @NotNull AttributeList<Window> format, Object[] parents) {
		GLFWWindow window = new GLFWWindow(context, format, parents);
		Barrier initTask = nowRun(window.storage, () -> window.initializeNativeWindow(format));
		format.addHook(new EventEntry<>((modify, changes) -> {
			throw new DelayTask(nowRun(window.storage, () -> window.initializeNativeWindow(modify)));
		}));
		return initTask.toFuture(() -> window);
	}
	
	private GLFWWindow(@NotNull GLFWContext context, @NotNull AttributeList<Window> format, Object[] parents) {
		this.context = context;
		this.format = format;
		this.storage = new Storage(this, parents);
	}
	
	//storage
	@Override
	public @NotNull Freeable getStorage() {
		return storage;
	}
	
	public static class Storage extends FreeableStorage implements Executor {
		
		protected volatile long windowPointer;
		protected SimpleThreadPool exec = new SimpleThreadPool(1, r -> new Thread(r, "GLFWWindowThread-" + WINDOW_THREAD_COUNTER.getAndIncrement()));
		
		public Storage(Object referent, Object[] parents) {
			super(referent, parents);
		}
		
		@Override
		protected @NotNull Barrier handleFree() {
			return nowRun(exec, () -> {
				this.deleteNativeWindow();
				throw new DelayTask(exec.stop());
			});
		}
		
		@WindowThread
		private void deleteNativeWindow() {
			if (windowPointer != 0) {
				glfwDestroyWindow(windowPointer);
				windowPointer = 0;
			}
		}
		
		public long getWindowPointer() throws FreedException {
			throwIfFreed();
			return windowPointer;
		}
		
		@Override
		public void execute(@NotNull Runnable command) {
			throwIfFreed();
			exec.execute(command);
		}
	}
	
	//basic
	@Override
	public void execute(@NotNull Runnable command) {
		storage.execute(command);
	}
	
	/**
	 * if thread == {@link WindowThread}: always a valid pointer
	 * if thread != {@link WindowThread}: return value may be valid or 0
	 */
	public long getWindowPointer() throws FreedException {
		return storage.getWindowPointer();
	}
	
	//windowThread init and delete
	@WindowThread
	protected void initializeNativeWindow(AbstractAttributeList<Window> newFormat) {
		long oldWindowPointer = getWindowPointer();
		boolean existingWindow = oldWindowPointer != 0;
		if (existingWindow && newFormat instanceof AttributeListModify) {
			AttributeListModify<Window> modify = (AttributeListModify<Window>) newFormat;
			boolean requiresReopen = Stream.of(VIDEO_MODE, ALPHA_BITS, DEPTH_BITS, STENCIL_BITS, DOUBLE_BUFFER, HAS_TRANSPARENCY, RESIZEABLE, BORDERLESS)
										   .anyMatch(modify::hasChanged);
			if (!requiresReopen) {
				
				//change current window
				if (modify.hasChanged(TITLE))
					glfwSetWindowTitle(oldWindowPointer, modify.get(TITLE));
				if (modify.hasChanged(VISIBLE))
					if (modify.get(VISIBLE))
						glfwShowWindow(oldWindowPointer);
					else
						glfwHideWindow(oldWindowPointer);
				if (modify.get(VIDEO_MODE) == VideoModeDesktopExtension.class) {
					if (modify.hasChanged(WIDTH) || modify.hasChanged(HEIGHT))
						glfwSetWindowSize(oldWindowPointer, modify.get(WIDTH), modify.get(HEIGHT));
					if (modify.hasChanged(POS_X) || modify.hasChanged(POS_Y)) {
						Integer posx = modify.get(POS_X);
						Integer posy = modify.get(POS_Y);
						if (posx != null && posy != null)
							glfwSetWindowPos(oldWindowPointer, posx, posy);
					}
				} else if (modify.get(VIDEO_MODE) == VideoModeFullscreenExtension.class) {
					if (modify.hasChanged(FULLSCREEN_VIDEO_MODE)) {
						VideoMode videoModeFullscreen = getVideoModeFullscreen(newFormat);
						GLFWMonitor monitor = getVideoModeFullscreenMonitor(videoModeFullscreen);
						glfwSetWindowMonitor(oldWindowPointer, monitor.pointer, 0, 0, videoModeFullscreen.width(), videoModeFullscreen.height(), videoModeFullscreen.refreshRate());
					}
				}
				if (modify.hasChanged(MINX) || modify.hasChanged(MINY) || modify.hasChanged(MAXX) || modify.hasChanged(MAXY))
					glfwSetWindowSizeLimits(oldWindowPointer, modify.get(MINX), modify.get(MINY), modify.get(MAXX), modify.get(MAXY));
				if (modify.hasChanged(MOUSE_MODE)) {
					int cursorValue;
					switch (modify.get(MOUSE_MODE)) {
						case CURSOR_NORMAL:
							cursorValue = GLFW_CURSOR_NORMAL;
							break;
						case CURSOR_HIDDEN:
							cursorValue = GLFW_CURSOR_HIDDEN;
							break;
						case CURSOR_DISABLED:
							cursorValue = GLFW_CURSOR_DISABLED;
							break;
						default:
							throw new IllegalArgumentException();
					}
					glfwSetInputMode(oldWindowPointer, GLFW_CURSOR, cursorValue);
				}
				return;
			}
		}
		
		//requires new window
		//store old data
		int[] prevPosition = null;
		if (existingWindow) {
			int[] posx = new int[1];
			int[] posy = new int[1];
			glfwGetWindowPos(storage.windowPointer, posx, posy);
			prevPosition = new int[] {posx[0], posy[0]};
		}
		
		//delete old
		deleteNativeWindow();
		
		//create new
		long windowPointer;
		synchronized (GLFWInstance.GLFW_SYNC) {
			glfwDefaultWindowHints();
			
			glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
			glfwWindowHint(GLFW_DOUBLEBUFFER, toGLFWBoolean(newFormat.get(DOUBLE_BUFFER)));
			glfwWindowHint(GLFW_DEPTH_BITS, newFormat.get(DEPTH_BITS));
			glfwWindowHint(GLFW_STENCIL_BITS, newFormat.get(STENCIL_BITS));
			context.initSetGLFWClientApiWindowHint();
			
			@NotNull Class<? extends VideoModeExtension> videoMode = newFormat.get(VIDEO_MODE);
			if (videoMode == VideoModeDesktopExtension.class) {
				
				//fbo
				boolean hasTransparency = newFormat.get(HAS_TRANSPARENCY);
				glfwWindowHint(GLFW_ALPHA_BITS, hasTransparency ? GLFW_DONT_CARE : newFormat.get(ALPHA_BITS));
				glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, toGLFWBoolean(hasTransparency));
				
				//extension BorderlessExtension
				glfwWindowHint(GLFW_DECORATED, toGLFWBoolean(!newFormat.get(BORDERLESS)));
				//extension ResizeableExtension
				boolean resizable = newFormat.get(RESIZEABLE);
				glfwWindowHint(GLFW_RESIZABLE, resizable ? GLFW_TRUE : GLFW_FALSE);
				
				//createWindow
				windowPointer = glfwCreateWindow(newFormat.get(WIDTH), newFormat.get(HEIGHT), newFormat.get(TITLE), 0, context.initGetContextSharePointer());
				if (prevPosition != null)
					glfwSetWindowPos(windowPointer, prevPosition[0], prevPosition[1]);
				
				//extension ResizeableExtension
				if (resizable)
					glfwSetWindowSizeLimits(windowPointer, newFormat.get(MINX), newFormat.get(MINY), newFormat.get(MAXX), newFormat.get(MAXY));
			} else if (videoMode == VideoModeFullscreenExtension.class) {
				VideoMode videoModeFullscreen = getVideoModeFullscreen(newFormat);
				GLFWMonitor monitor = getVideoModeFullscreenMonitor(videoModeFullscreen);
				
				//fbo
				glfwWindowHint(GLFW_RED_BITS, videoModeFullscreen.bitsR());
				glfwWindowHint(GLFW_GREEN_BITS, videoModeFullscreen.bitsG());
				glfwWindowHint(GLFW_BLUE_BITS, videoModeFullscreen.bitsB());
				glfwWindowHint(GLFW_ALPHA_BITS, newFormat.get(ALPHA_BITS));
				glfwWindowHint(GLFW_REFRESH_RATE, videoModeFullscreen.refreshRate());
				
				//createWindow
				windowPointer = glfwCreateWindow(videoModeFullscreen.width(), videoModeFullscreen.height(), newFormat.get(TITLE), monitor.pointer, context.getWindowPointer());
			} else {
				throw new IllegalStateException("format[VIDEO_MODE] was unsupported type: " + videoMode.getName());
			}
			
			int cursorValue;
			switch (newFormat.get(MOUSE_MODE)) {
				case CURSOR_NORMAL:
					cursorValue = GLFW_CURSOR_NORMAL;
					break;
				case CURSOR_HIDDEN:
					cursorValue = GLFW_CURSOR_HIDDEN;
					break;
				case CURSOR_DISABLED:
					cursorValue = GLFW_CURSOR_DISABLED;
					break;
				default:
					throw new IllegalArgumentException();
			}
			glfwSetInputMode(windowPointer, GLFW_CURSOR, cursorValue);
		}
		
		storage.windowPointer = windowPointer;
		context.initCreateCapabilities(windowPointer);
		
		//events
		glfwSetWindowCloseCallback(windowPointer, eventGLFWWindowCloseCallback);
		glfwSetWindowPosCallback(windowPointer, eventGLFWWindowPosCallback);
		glfwSetWindowSizeCallback(windowPointer, eventGLFWWindowSizeCallback);
		glfwSetFramebufferSizeCallback(windowPointer, eventGLFWFramebufferSizeCallback);
		glfwSetWindowFocusCallback(windowPointer, eventGLFWWindowFocusCallback);
		glfwSetWindowIconifyCallback(windowPointer, eventGLFWWindowIconifyCallback);
		glfwSetKeyCallback(windowPointer, (window, key, scancode, action, mods) -> {
			switch (action) {
				case GLFW_PRESS:
					context.triggerKeyInputEventKeyboard(key, true);
					break;
				case GLFW_RELEASE:
					context.triggerKeyInputEventKeyboard(key, false);
					break;
				case GLFW_REPEAT:
					break;
				default:
					throw new IllegalArgumentException();
			}
		});
		glfwSetCharCallback(windowPointer, (window, codepoint) -> context.triggerCharacterInputEventKeyboard(new String(Character.toChars(codepoint))));
		glfwSetCursorPosCallback(windowPointer, (window, xpos, ypos) -> context.triggerMouseMovement(new double[] {xpos, ypos}));
		glfwSetScrollCallback(windowPointer, (window, xoffset, yoffset) -> context.triggerScroll(new double[] {xoffset, yoffset}));
		
		if (newFormat.get(VISIBLE)) {
			glfwShowWindow(windowPointer);
		}
	}
	
	private VideoMode getVideoModeFullscreen(AbstractAttributeList<Window> newFormat) {
		VideoMode videoModeFullscreen = newFormat.get(FULLSCREEN_VIDEO_MODE);
		if (videoModeFullscreen != null)
			return videoModeFullscreen;
		return context.framework.getPrimaryMonitorDirect().getDefaultVideoMode();
	}
	
	private GLFWMonitor getVideoModeFullscreenMonitor(VideoMode videoModeFullscreen) {
		Monitor monitor = videoModeFullscreen.monitor();
		if (!(monitor instanceof GLFWMonitor))
			throw new IllegalArgumentException("Monitor not from GLFW");
		return (GLFWMonitor) monitor;
	}
	
	@WindowThread
	protected void deleteNativeWindow() {
		storage.deleteNativeWindow();
	}
	
	//swap buffers
	@Override
	public @NotNull Barrier openGL_SwapBuffer(int opengl_tex_id) {
		return nowRun(storage, () -> {
			glEnable(GL_TEXTURE_2D);
			glBindTexture(GL_TEXTURE_2D, opengl_tex_id);
			
			glBegin(GL_QUADS);
			glTexCoord2f(0, 0);
			glVertex2f(-1, -1);
			glTexCoord2f(1, 0);
			glVertex2f(1, -1);
			glTexCoord2f(1, 1);
			glVertex2f(1, 1);
			glTexCoord2f(0, 1);
			glVertex2f(-1, 1);
			glEnd();
			
			glBindTexture(GL_TEXTURE_2D, 0);
			glDisable(GL_TEXTURE_2D);
			
			swapBuffers();
		});
	}
	
	@Override
	public @NotNull Barrier openGL_ES_SwapBuffer(int opengl_es_texture_id) {
		throw new UnsupportedOperationException("Not implemented!");
	}
	
	@WindowThread
	public void swapBuffers() {
		glfwSwapBuffers(storage.getWindowPointer());
		glfwPollEvents();
	}
	
	@WindowThread
	public void pollEvents() {
		glfwPollEvents();
	}
	
	public Barrier pollEventsTask() {
		return nowRun(this, this::pollEvents);
	}
	
	//events
	private final Event<WindowCloseCallback> eventWindowClose = new SequentialEventBuilder<>();
	private final Event<WindowMoveAndResizeCallback> eventWindowMoveAndResize = new SequentialEventBuilder<>();
	private final Event<WindowFramebufferSizeCallback> eventWindowFramebufferSize = new SequentialEventBuilder<>();
	private final Event<WindowFocusCallback> eventWindowFocus = new SequentialEventBuilder<>();
	
	//event extra vars
	private WindowFocus focus;
	
	//event callbacks
	private final GLFWWindowCloseCallbackI eventGLFWWindowCloseCallback = window ->
			eventWindowClose.submit((TypeHandlerParallel<WindowCloseCallback>) callback -> callback.windowRequestClose(GLFWWindow.this));
	private final GLFWWindowPosCallbackI eventGLFWWindowPosCallback = (window, x, y) -> {
		int[] width = new int[1];
		int[] height = new int[1];
		glfwGetWindowSize(getWindowPointer(), width, height);
		eventWindowMoveAndResize.submit((TypeHandlerParallel<WindowMoveAndResizeCallback>) callback -> callback.windowResize(GLFWWindow.this, x, y, width[0], height[0]));
	};
	private final GLFWWindowSizeCallbackI eventGLFWWindowSizeCallback = (window, width, height) -> {
		int[] x = new int[1];
		int[] y = new int[1];
		glfwGetWindowPos(getWindowPointer(), x, y);
		eventWindowMoveAndResize.submit((TypeHandlerParallel<WindowMoveAndResizeCallback>) callback -> callback.windowResize(GLFWWindow.this, x[0], y[0], width, height));
	};
	private final GLFWFramebufferSizeCallbackI eventGLFWFramebufferSizeCallback = (window, width, height) ->
			eventWindowFramebufferSize.submit((TypeHandlerParallel<WindowFramebufferSizeCallback>) callback -> callback.windowFramebufferSize(this, width, height));
	private final GLFWWindowFocusCallbackI eventGLFWWindowFocusCallback = (window, focused) -> {
		if (focused) {
			focus = WindowFocus.FOCUSED;
		} else {
			if (focus == WindowFocus.FOCUSED)
				focus = WindowFocus.BACKGROUND;
		}
		triggerEventWindowFocus();
	};
	private final GLFWWindowIconifyCallbackI eventGLFWWindowIconifyCallback = (window, iconified) -> {
		if (iconified) {
			focus = WindowFocus.HIDDEN;
		} else {
			if (focus == WindowFocus.HIDDEN)
				focus = WindowFocus.BACKGROUND;
		}
		triggerEventWindowFocus();
	};
	
	private void triggerEventWindowFocus() {
		eventWindowFocus.submit((TypeHandlerParallel<WindowFocusCallback>) callback -> callback.windowFocusChange(this, focus));
	}
	
	@Override
	public @NotNull Event<WindowCloseCallback> getWindowCloseEvent() {
		return eventWindowClose;
	}
	
	@Override
	public @NotNull Event<WindowMoveAndResizeCallback> getWindowResizeEvent() {
		return eventWindowMoveAndResize;
	}
	
	@Override
	public @NotNull Event<WindowFramebufferSizeCallback> getWindowFramebufferSizeEvent() {
		return eventWindowFramebufferSize;
	}
	
	@Override
	public @NotNull Event<WindowFocusCallback> getWindowFocusEvent() {
		return eventWindowFocus;
	}
}
