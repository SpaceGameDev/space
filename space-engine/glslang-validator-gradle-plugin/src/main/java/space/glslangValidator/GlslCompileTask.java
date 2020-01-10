package space.glslangValidator;

import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.internal.ExecException;
import org.gradle.process.internal.ExecHandle;
import org.gradle.process.internal.ExecHandleBuilder;
import org.gradle.process.internal.ExecHandleFactory;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GlslCompileTask extends SourceTask {
	
	@OutputDirectory
	private final Property<File> destinationDir = getProject().getObjects().property(File.class);
	
	/**
	 * Returns the directory to generate the {@code .class} files into.
	 *
	 * @return The destination directory.
	 */
	@OutputDirectory
	public File getDestinationDir() {
		return destinationDir.getOrNull();
	}
	
	/**
	 * Sets the directory to generate the {@code .class} files into.
	 *
	 * @param destinationDir The destination directory. Must not be null.
	 */
	public void setDestinationDir(File destinationDir) {
		this.destinationDir.set(destinationDir);
	}
	
	/**
	 * Sets the directory to generate the {@code .class} files into.
	 *
	 * @param destinationDir The destination directory. Must not be null.
	 */
	public void setDestinationDir(Provider<File> destinationDir) {
		this.destinationDir.set(destinationDir);
	}
	
	@Inject
	protected ExecHandleFactory getExecHandleFactory() {
		throw new UnsupportedOperationException();
	}
	
	@TaskAction
	protected void compile() {
		GlslConfigurationExtension ext = getProject().getExtensions().getByType(GlslConfigurationExtension.class);
		
		List<Process> result = new ArrayList<>();
		getSource().visit(src -> {
			if (src.isDirectory()) {
				File target = src.getRelativePath().getFile(destinationDir.get());
				//noinspection ResultOfMethodCallIgnored
				target.mkdir();
			} else {
				File target = src.getRelativePath().getParent().append(true, src.getName() + ".spv").getFile(destinationDir.get());
				
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				ExecHandleBuilder builder = getExecHandleFactory().newExec();
				switch (ext.getCompiler()) {
					case "glslangValidator":
						builder.commandLine(ext.getGlslangValidatorPath(), "-V", src.getFile().getAbsolutePath(), "-o", target.getAbsolutePath());
						break;
					case "glslc":
						builder.commandLine(ext.getGlslcPath(), "-c", src.getFile().getAbsolutePath(), "-o", target.getAbsolutePath());
						break;
					default:
						throw new IllegalArgumentException("Compiler " + ext.getCompiler() + " not supported!");
				}
				builder.setStandardOutput(outputStream);
				
				result.add(new Process(builder.build().start(), outputStream));
			}
		});
		
		for (Process p : result) {
			try {
				p.exec.waitForFinish().rethrowFailure().assertNormalExitValue();
			} catch (ExecException e) {
				throw new ExecException(e.getMessage() + "\n" + p.stdout.toString(), e);
			}
		}
	}
	
	private static class Process {
		
		final ExecHandle exec;
		final ByteArrayOutputStream stdout;
		
		public Process(ExecHandle exec, ByteArrayOutputStream stdout) {
			this.exec = exec;
			this.stdout = stdout;
		}
	}
}
