package space.glslangValidator;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.internal.ExecException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class GlslCompileTask extends DefaultTask {
	
	private SourceDirectorySet sourcesSet;
	
	public SourceDirectorySet getSourcesSet() {
		return sourcesSet;
	}
	
	public void setSourcesSet(SourceDirectorySet sourcesSet) {
		this.sourcesSet = sourcesSet;
	}
	
	@InputFiles
	@SkipWhenEmpty
	@PathSensitive(PathSensitivity.ABSOLUTE)
	public FileTree getSource() {
		return getProject().files(sourcesSet).getAsFileTree();
	}
	
	@OutputDirectory
	public File getDestinationDir() {
		return sourcesSet.getOutputDir();
	}
	
	@TaskAction
	protected void compile() {
		for (File srcDir : sourcesSet.getSrcDirs()) {
			for (File src : getProject().fileTree(srcDir).matching(sourcesSet.getFilter()).getFiles()) {
				File target = new File(sourcesSet.getOutputDir(), src.toString().substring(srcDir.toString().length()) + ".spv");
				//noinspection ResultOfMethodCallIgnored
				target.getParentFile().mkdirs();
				
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				try {
					getProject().exec(execSpec -> {
						try {
							execSpec.commandLine("glslc", "-c", src.getCanonicalPath(), "-o", target.getCanonicalPath());
							execSpec.setStandardOutput(outputStream);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}).rethrowFailure();
				} catch (ExecException e) {
					throw new ExecException(e.getMessage() + "\n" + outputStream.toString(), e);
				}
			}
		}
	}
}
