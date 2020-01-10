package space.glslangValidator;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;

public class GlslConfigurationExtension {
	
	public static final String EXTENSION_NAME = "glsl";
	
	private final Project project;
	private final Property<String> compiler;
	private final Property<String> glslangValidatorPath;
	private final Property<String> glslcPath;
	
	public GlslConfigurationExtension(Project project) {
		this.project = project;
		this.compiler = project.getObjects().property(String.class).convention("glslangValidator");
		this.glslangValidatorPath = project.getObjects().property(String.class).convention("glslangValidator");
		this.glslcPath = project.getObjects().property(String.class).convention("glslc");
	}
	
	public Project project() {
		return project;
	}
	
	public String getCompiler() {
		return compiler.get();
	}
	
	public void setCompiler(String compiler) {
		this.compiler.set(compiler);
	}
	
	public String getGlslangValidatorPath() {
		return glslangValidatorPath.get();
	}
	
	public void setGlslangValidatorPath(String path) {
		this.glslangValidatorPath.set(path);
	}
	
	public String getGlslcPath() {
		return glslcPath.get();
	}
	
	public void setGlslcPath(String path) {
		this.glslcPath.set(path);
	}
}
