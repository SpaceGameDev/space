package space.glslangValidator;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.plugins.JavaBasePlugin;
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
		project.getPlugins().withType(JavaBasePlugin.class, appliedPlugin -> {
			JavaPluginConvention javaPluginConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
			javaPluginConvention.getSourceSets().all(sourceSet -> {
				
				//SourceDirectorySet
				final GlslSourcesSet glslSourcesSet = new GlslSourcesSet(sourceSet, project);
				glslSourcesSet.getGlsl().setOutputDir(project.provider(() -> new File(project.getBuildDir(), "spirv/" + sourceSet.getName())));
				new DslObject(sourceSet).getConvention().getPlugins().put("glsl", glslSourcesSet);
				
				//compile task
				GlslCompileTask compileTask = project.getTasks().create(sourceSet.getCompileTaskName("glsl"), GlslCompileTask.class, task -> {
					task.source(glslSourcesSet.getGlsl());
					task.setDestinationDir(glslSourcesSet.getGlsl().getOutputDir());
				});
				
				//output directory
				sourceSet.getOutput().dir(Map.of("builtBy", compileTask.getName()), glslSourcesSet.getGlsl().getOutputDir());
			});
		});
	}
}
