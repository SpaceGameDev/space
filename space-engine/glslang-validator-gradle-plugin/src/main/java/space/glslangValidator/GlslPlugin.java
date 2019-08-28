package space.glslangValidator;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;

import javax.inject.Inject;
import java.io.File;
import java.util.Map;

public class GlslPlugin implements Plugin<Project> {
	
	@Inject
	public GlslPlugin() {
	}
	
	@Override
	public void apply(Project project) {
		//java plugin + convention
		project.getPluginManager().apply(JavaPlugin.class);
		JavaPluginConvention javaPluginConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
		javaPluginConvention.getSourceSets().all(sourceSet -> {
			
			//SourceDirectorySet
			final GlslSourcesSet glslSourcesSet = new GlslSourcesSet(sourceSet, project);
			glslSourcesSet.getGlsl().setOutputDir(project.provider(() -> new File(project.getBuildDir(), "spirv/" + sourceSet.getName())));
			new DslObject(sourceSet).getConvention().getPlugins().put("glsl", glslSourcesSet);
			
			//compile task
			GlslCompileTask compileTask = project.getTasks().create(sourceSet.getCompileTaskName("glsl"), GlslCompileTask.class, glslCompileTask ->
					glslCompileTask.setSourcesSet(glslSourcesSet.getGlsl())
			);
			
			//output directory
			project.afterEvaluate(project1 -> sourceSet.getOutput().dir(Map.of("builtBy", compileTask.getName()), glslSourcesSet.getGlsl().getOutputDir()));
		});
	}
}
