package space.engine.window;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.engine.barrier.future.Future;
import space.engine.delegate.collection.ObservableCollection;
import space.engine.freeableStorage.Freeable;
import space.engine.key.attribute.AttributeKey;
import space.engine.key.attribute.AttributeList;
import space.engine.key.attribute.AttributeListCreator;
import space.engine.simpleQueue.pool.Executor;

/**
 * The {@link WindowContext} is the Conext you do all your drawing with.
 *
 * <b>If you are using OpenGL or OpenGL ES:</b><br>
 * The Thread you create the Context with is the Thread you render everything with.
 * If you have a finished FrameBuffer, call {\@link Window#openGL_SwapFramebuffer(int)} or {\@link Window#openGL_ES_SwapFramebuffer(int)} respectively.
 */
@SuppressWarnings("unused")
public interface WindowContext extends Freeable, Executor {
	
	Future<? extends Window> createWindow(@NotNull AttributeList<Window> format, Object[] parents);
	
	//attributes
	AttributeListCreator<WindowContext> CREATOR = new AttributeListCreator<>();
	
	//api
	AttributeKey<@Nullable Object> API_TYPE = CREATOR.createKeyNormal();
	
	//OpenGL / OpenGL ES
	AttributeKey<@NotNull GLProfile> GL_PROFILE = CREATOR.createKeyWithDefault(GLProfile.PROFILE_ANY);
	AttributeKey<@NotNull Integer> GL_VERSION_MAJOR = CREATOR.createKeyWithDefault(3);
	AttributeKey<@NotNull Integer> GL_VERSION_MINOR = CREATOR.createKeyWithDefault(2);
	AttributeKey<@NotNull Boolean> GL_FORWARD_COMPATIBLE = CREATOR.createKeyWithDefault(true);
	
	//input
	@NotNull ObservableCollection<? extends InputDevice> getInputDevices();
	
	//enums
	enum OpenGLApiType {
		
		GL,
		GL_ES
		
	}
	
	enum GLProfile {
		
		PROFILE_ANY,
		PROFILE_CORE,
		PROFILE_COMPAT
		
	}
}
