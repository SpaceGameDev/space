package space.jandexIndexer;

import org.gradle.api.NonNullApi;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;

@NonNullApi
public class JandexIndexerPlugin implements Plugin<Project> {
	
	@Override
	public void apply(Project project) {
		project.getPlugins().withType(JavaPlugin.class, javaPlugin -> {
			JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
			
			javaConvention.getSourceSets().forEach(sourceSet -> {
				Provider<Directory> jandexOutputDir = project.getLayout().getBuildDirectory().dir("jandex/" + sourceSet.getName());
				TaskProvider<JandexIndexerTask> jandexTask = project.getTasks().register(sourceSet.getTaskName("generate", "Jandex"), JandexIndexerTask.class, t -> {
					t.setSource(sourceSet.getOutput().getClassesDirs());
					t.setIndexOutputFile(jandexOutputDir.map(directory -> directory.file("META-INF/jandex.idx")));
					t.dependsOn(sourceSet.getClassesTaskName());
				});
				sourceSet.getOutput().dir(jandexOutputDir);
				project.getTasks().named(sourceSet.getClassesTaskName()).configure(task -> task.finalizedBy(jandexTask));
			});
		});
	}
}
