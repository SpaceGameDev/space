package space.engine.plugin.modengine;

import org.gradle.api.GradleException;
import org.gradle.api.NonNullApi;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.file.FileVisitor;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexWriter;
import org.jboss.jandex.Indexer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

@NonNullApi
public class JandexIndexTask extends SourceTask {
	
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
	private RegularFileProperty jandexFile = getProject().getObjects().fileProperty().convention(packageDirectory.file(className.map(s -> s + ".idx")));
	
	@TaskAction
	protected void index() {
		//clear directory
		getProject().delete(outputDirectory.getAsFileTree());
		
		//create index
		Indexer indexer = new Indexer();
		getSource().visit(new FileVisitor() {
			@Override
			public void visitDir(FileVisitDetails dirDetails) {
			
			}
			
			@Override
			public void visitFile(FileVisitDetails fileDetails) {
				if (fileDetails.getName().endsWith(".class")) {
					try (InputStream in = fileDetails.open()) {
						indexer.index(in);
					} catch (IOException e) {
						throw new GradleException("IO Error reading '" + fileDetails.getPath() + "': " + e.getMessage(), e);
					}
				}
			}
		});
		
		//write index
		Index index = indexer.complete();
		try (FileOutputStream out = new FileOutputStream(jandexFile.get().getAsFile())) {
			new IndexWriter(out).write(index);
		} catch (IOException e) {
			throw new GradleException("IO Error writing '" + jandexFile.get().getAsFile().getAbsolutePath() + "': " + e.getMessage(), e);
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
	
	public RegularFileProperty getJandexFile() {
		return jandexFile;
	}
}
