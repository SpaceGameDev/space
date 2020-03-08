package space.engine.plugin.modengine;

import org.gradle.api.NonNullApi;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.Directory;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;

@NonNullApi
public class ModEnginePlugin implements Plugin<Project> {
	
	public static final String TASK_PREPARE = "prepare";
	
	@Override
	public void apply(Project project) {
		project.getPlugins().apply(JavaLibraryPlugin.class);
		initPrepareTask(project);
		initJandexIndexing(project);
	}
	
	public static TaskProvider<Task> initPrepareTask(Project project) {
		return project.getTasks().register(TASK_PREPARE);
	}
	
	public static void initJandexIndexing(Project project) {
		JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);
		javaConvention.getSourceSets().forEach(sourceSet -> {
			//constants
			Provider<String> packagePath = project.provider(() -> ClassnameUtils.toValidJavaPackageIdentifier(project.getGroup() + ".jandex"));
			Provider<String> className = project.provider(() -> ClassnameUtils.toValidJavaClassIdentifier(project.getName() + "-Index"));
			
			//dependencies required
			project.getDependencies().add("implementation", "space.engine:jandex");
			
			//generate loader class
			Provider<Directory> outputDirLoader = project.getLayout().getBuildDirectory().dir("jandex/loader/" + sourceSet.getName());
			TaskProvider<JandexLoaderClassTask> taskGenerateLoader = project.getTasks().register(sourceSet.getTaskName("generate", "JandexLoaderClass"), JandexLoaderClassTask.class, t -> {
				t.getOutputDirectory().value(outputDirLoader);
				t.getPackagePath().value(packagePath);
				t.getClassName().value(className);
			});
			sourceSet.getJava().srcDir(outputDirLoader);
			project.getTasks().named(sourceSet.getCompileJavaTaskName()).configure(t -> t.dependsOn(taskGenerateLoader));
			project.getTasks().named(TASK_PREPARE).configure(t -> t.dependsOn(taskGenerateLoader));
			
			//generate index
			Provider<Directory> outputDirIndex = project.getLayout().getBuildDirectory().dir("jandex/index/" + sourceSet.getName());
			TaskProvider<JandexIndexTask> taskGenerateIndex = project.getTasks().register(sourceSet.getTaskName("generate", "JandexIndex"), JandexIndexTask.class, t -> {
				t.getOutputDirectory().value(outputDirIndex);
				t.getPackagePath().value(packagePath);
				t.getClassName().value(className);
				
				t.setSource(sourceSet.getOutput().getClassesDirs());
				t.dependsOn(sourceSet.getClassesTaskName());
			});
			sourceSet.getOutput().dir(outputDirIndex);
			project.getTasks().named(sourceSet.getClassesTaskName()).configure(task -> task.finalizedBy(taskGenerateIndex));
		});
	}
}
