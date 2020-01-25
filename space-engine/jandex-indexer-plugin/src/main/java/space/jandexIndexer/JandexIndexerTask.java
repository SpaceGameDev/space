package space.jandexIndexer;

import org.gradle.api.GradleException;
import org.gradle.api.NonNullApi;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.file.FileVisitor;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexWriter;
import org.jboss.jandex.Indexer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

@NonNullApi
public class JandexIndexerTask extends SourceTask {
	
	@OutputFile
	private RegularFileProperty indexOutputFile = getProject().getObjects().fileProperty();
	
	@TaskAction
	protected void index() {
		Indexer indexer = new Indexer();
		getSource().visit(new FileVisitor() {
			@Override
			public void visitDir(FileVisitDetails dirDetails) {
			
			}
			
			@Override
			public void visitFile(FileVisitDetails fileDetails) {
				try (FileInputStream in = new FileInputStream(fileDetails.getFile())) {
					indexer.index(in);
				} catch (IOException e) {
					throw new GradleException("IO Error", e);
				}
			}
		});
		
		Index index = indexer.complete();
		try (FileOutputStream out = new FileOutputStream(getIndexOutputFile().getAsFile())) {
			new IndexWriter(out).write(index);
		} catch (IOException e) {
			throw new GradleException("IO Error", e);
		}
	}
	
	public RegularFile getIndexOutputFile() {
		return indexOutputFile.get();
	}
	
	public void setIndexOutputFile(File indexOutputFile) {
		this.indexOutputFile.set(indexOutputFile);
	}
	
	public void setIndexOutputFile(Provider<RegularFile> destinationDir) {
		this.indexOutputFile.set(destinationDir);
	}
}
