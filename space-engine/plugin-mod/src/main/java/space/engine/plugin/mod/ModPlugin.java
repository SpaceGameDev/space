package space.engine.plugin.mod;

import org.gradle.api.NonNullApi;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import space.engine.plugin.modengine.ModEnginePlugin;

@NonNullApi
public class ModPlugin implements Plugin<Project> {
	
	@Override
	public void apply(Project project) {
		project.getPlugins().apply(ModEnginePlugin.class);
	}
}
