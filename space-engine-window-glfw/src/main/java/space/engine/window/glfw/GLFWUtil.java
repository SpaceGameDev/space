package space.engine.window.glfw;

import space.engine.window.WindowContext.GLProfile;
import space.engine.window.WindowContext.OpenGLApiType;

import static org.lwjgl.glfw.GLFW.*;

public class GLFWUtil {
	
	//boolean
	public static int toGLFWBoolean(boolean b) {
		return b ? GLFW_TRUE : GLFW_FALSE;
	}
	
	public static boolean fromGLFWBoolean(int b) {
		return b == GLFW_TRUE;
	}
	
	//opengl types
	public static int covertGLApiTypeToGLFWApi(OpenGLApiType type) {
		switch (type) {
			case GL:
				return GLFW_OPENGL_API;
			case GL_ES:
				return GLFW_OPENGL_ES_API;
		}
		return GLFW_NO_API;
	}
	
	public static int covertGLProfileToGLFWProfile(GLProfile type) {
		switch (type) {
			case PROFILE_ANY:
				return GLFW_OPENGL_ANY_PROFILE;
			case PROFILE_CORE:
				return GLFW_OPENGL_CORE_PROFILE;
			case PROFILE_COMPAT:
				return GLFW_OPENGL_COMPAT_PROFILE;
		}
		throw new IllegalArgumentException("Invalid type: " + type);
	}
	
}
