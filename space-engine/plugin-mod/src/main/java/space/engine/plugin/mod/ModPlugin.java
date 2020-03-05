package space.engine.plugin.mod;

import org.gradle.api.NonNullApi;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaLibraryPlugin;
import space.engine.plugin.modengine.ModEnginePlugin;

@NonNullApi
public class ModPlugin implements Plugin<Project> {
	
	@Override
	public void apply(Project project) {
		project.getPlugins().apply(JavaLibraryPlugin.class);
		ModEnginePlugin.initPrepareTask(project);
		ModEnginePlugin.initJandexIndexing(project);
	}
}
