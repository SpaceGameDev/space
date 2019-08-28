package space.engine.window;

import org.jetbrains.annotations.NotNull;
import space.engine.barrier.Barrier;
import space.engine.event.Event;
import space.engine.freeableStorage.Freeable;
import space.engine.key.attribute.AttributeKey;
import space.engine.key.attribute.AttributeListCreator;
import space.engine.simpleQueue.pool.Executor;
import space.engine.window.extensions.VideoModeExtension;

/**
 * A {@link Window} is a {@link Window} on the Desktop.
 * Use {@link Window#free()} to free and close the {@link Window}.
 * <b>Opened {@link Window Windows} cannot be altered. {@link Window#free()} the opened one and create a new {@link Window} with your {@link WindowContext}</b>
 */
public interface Window extends Freeable, Executor {
	
	AttributeListCreator<Window> CREATOR = new AttributeListCreator<>();
	
	AttributeKey<@NotNull Class<? extends VideoModeExtension>> VIDEO_MODE = CREATOR.createKeyNormal();
	/**
	 * ignored when using {@link space.engine.window.extensions.VideoModeDesktopExtension} with {@link space.engine.window.extensions.VideoModeDesktopExtension#HAS_TRANSPARENCY}
	 */
	AttributeKey<@NotNull Integer> ALPHA_BITS = CREATOR.createKeyWithDefault(0);
	AttributeKey<@NotNull Integer> DEPTH_BITS = CREATOR.createKeyWithDefault(0);
	AttributeKey<@NotNull Integer> STENCIL_BITS = CREATOR.createKeyWithDefault(0);
	
	//attributes
	AttributeKey<@NotNull String> TITLE = CREATOR.createKeyWithDefault("Untitled Window");
	AttributeKey<@NotNull Boolean> VISIBLE = CREATOR.createKeyWithDefault(Boolean.TRUE);
	AttributeKey<@NotNull Boolean> DOUBLE_BUFFER = CREATOR.createKeyWithDefault(Boolean.TRUE);
	
	//context implementation specific methods
	
	/**
	 * Swap the Framebuffer of a FBO from OpenGL.
	 *
	 * @param opengl_texture_id the FBO id from OpenGL
	 * @return a {@link Barrier} done when swapping is finished
	 */
	@NotNull Barrier openGL_SwapBuffer(int opengl_texture_id);
	
	/**
	 * Swap the Framebuffer of a FBO from OpenGL ES.
	 *
	 * @param opengl_es_texture_id the FBO id from OpenGL ES
	 * @return a {@link Barrier} done when swapping is finished
	 */
	@NotNull Barrier openGL_ES_SwapBuffer(int opengl_es_texture_id);
	
	//events
	@NotNull Event<WindowCloseCallback> getWindowCloseEvent();
	
	@FunctionalInterface
	interface WindowCloseCallback {
		
		void windowRequestClose(Window window);
	}
	
	@NotNull Event<WindowMoveAndResizeCallback> getWindowResizeEvent();
	
	@FunctionalInterface
	interface WindowMoveAndResizeCallback {
		
		void windowResize(Window window, int posx, int posy, int width, int height);
	}
	
	@NotNull Event<WindowFramebufferSizeCallback> getWindowFramebufferSizeEvent();
	
	@FunctionalInterface
	interface WindowFramebufferSizeCallback {
		
		void windowFramebufferSize(Window window, int width, int height);
	}
	
	@NotNull Event<WindowFocusCallback> getWindowFocusEvent();
	
	@FunctionalInterface
	interface WindowFocusCallback {
		
		void windowFocusChange(Window window, WindowFocus focus);
		
		enum WindowFocus {
			
			/**
			 * Window is focused by the user.
			 * <b>Has</b> to be supported.
			 */
			FOCUSED,
			/**
			 * Window is in background and may be visible.
			 * <b>Has</b> to be supported.
			 */
			BACKGROUND,
			/**
			 * Window is in background and not visible.
			 * May <b>not</b> be supported.
			 */
			HIDDEN
		}
	}
}
