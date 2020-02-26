package space.engine.plugin.pack;

import org.gradle.api.NonNullApi;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ApplicationPlugin;
import org.gradle.api.plugins.JavaLibraryPlugin;

@NonNullApi
public class PackPlugin implements Plugin<Project> {
	
	@Override
	public void apply(Project project) {
		project.getPlugins().apply(JavaLibraryPlugin.class);
		project.getPlugins().apply(ApplicationPlugin.class);
	}
}
