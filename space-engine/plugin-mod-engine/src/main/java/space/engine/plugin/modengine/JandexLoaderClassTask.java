package space.engine.plugin.modengine;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.NonNullApi;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@NonNullApi
public class JandexLoaderClassTask extends DefaultTask {
	
	//required
	@OutputDirectory
	private DirectoryProperty outputDirectory = getProject().getObjects().directoryProperty();
	@Input
	private Property<String> packagePath = getProject().getObjects().property(String.class);
	@Input
	private Property<String> className = getProject().getObjects().property(String.class).convention("Index");
	
	//computed
	@OutputDirectory
	@Optional
	private DirectoryProperty packageDirectory = getProject().getObjects().directoryProperty().convention(outputDirectory.dir(packagePath.map(s -> s.replace('.', '/'))));
	@OutputFile
	@Optional
	private RegularFileProperty javaFile = getProject().getObjects().fileProperty().convention(packageDirectory.file(className.map(s -> s + ".java")));
	@OutputFile
	@Optional
	private RegularFileProperty jandexFile = getProject().getObjects().fileProperty().convention(packageDirectory.file(className.map(s -> s + ".idx")));
	
	@TaskAction
	protected void index() {
		getProject().delete(outputDirectory.getAsFileTree());
		String className = this.className.get();
		String variableIndex = "index";
		String variableIndexName = "fileName";
		
		String classSrc =
				"package " + packagePath.get() + ";\n" +
						"\n" +
						"import org.jboss.jandex.Index;\n" +
						"import space.engine.jandex.JandexUtils;\n" +
						"\n" +
						"public class " + className + " {\n" +
						"\n" +
						"\tpublic static final String " + variableIndexName + " = \"" + jandexFile.get().getAsFile().getName() + "\";\n" +
						"\tpublic static final Index " + variableIndex + " = JandexUtils.readIndexAssertExists(" + className + ".class, " + variableIndexName + ");" +
						"\n" +
						"}\n";
		try {
			Files.write(javaFile.get().getAsFile().toPath(), classSrc.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new GradleException("IO Error writing " + javaFile.get().getAsFile().getAbsolutePath() + ": " + e.getMessage(), e);
		}
	}
	
	//getter required
	public DirectoryProperty getOutputDirectory() {
		return outputDirectory;
	}
	
	public Property<String> getPackagePath() {
		return packagePath;
	}
	
	public Property<String> getClassName() {
		return className;
	}
	
	//getter optional
	public DirectoryProperty getPackageDirectory() {
		return packageDirectory;
	}
	
	public RegularFileProperty getJavaFile() {
		return javaFile;
	}
}
