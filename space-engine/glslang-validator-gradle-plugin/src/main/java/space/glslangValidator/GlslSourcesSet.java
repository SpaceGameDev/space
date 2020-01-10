package space.glslangValidator;

import groovy.lang.Closure;
import org.gradle.api.Project;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.tasks.DefaultSourceSet;
import org.gradle.api.tasks.SourceSet;

import static org.gradle.util.ConfigureUtil.configure;

public class GlslSourcesSet {
	
	private final SourceDirectorySet glsl;
	
	public GlslSourcesSet(SourceSet sourceSet, Project project) {
		glsl = project.getObjects().sourceDirectorySet("glsl", ((DefaultSourceSet) sourceSet).getDisplayName() + " glsl source");
		glsl.srcDir("src/" + sourceSet.getName() + "/glsl");
		glsl.getFilter()
			.include("**/*.vert", "**/*.tesc", "**/*.tese", "**/*.geom", "**/*.frag", "**/*.comp", "**/*.mesh", "**/*.task")
			.include("**/*.rgen", "**/*.rint", "**/*.rahit", "**/*.rchit", "**/*.rmiss", "**/*.rcall");
	}
	
	public SourceDirectorySet getGlsl() {
		return glsl;
	}
	
	public GlslSourcesSet glsl(Closure<?> closure) {
		configure(closure, getGlsl());
		return this;
	}
}
