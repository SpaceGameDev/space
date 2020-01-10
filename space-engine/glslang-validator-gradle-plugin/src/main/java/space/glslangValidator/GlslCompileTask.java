package space.glslangValidator;

import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.internal.ExecException;

import java.io.ByteArrayOutputStream;
import java.io.File;

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
	
	@TaskAction
	protected void compile() {
		GlslConfigurationExtension ext = getProject().getExtensions().getByType(GlslConfigurationExtension.class);
		
		getSource().visit(src -> {
			if (src.isDirectory()) {
				File target = src.getRelativePath().getFile(destinationDir.get());
				//noinspection ResultOfMethodCallIgnored
				target.mkdir();
			} else {
				File target = src.getRelativePath().getParent().append(true, src.getName() + ".spv").getFile(destinationDir.get());
				
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				try {
					getProject().exec(spec -> {
						switch (ext.getCompiler()) {
							case "glslangValidator":
								spec.commandLine(ext.getGlslangValidatorPath(), "-V", src.getFile().getAbsolutePath(), "-o", target.getAbsolutePath());
								break;
							case "glslc":
								spec.commandLine(ext.getGlslcPath(), "-c", src.getFile().getAbsolutePath(), "-o", target.getAbsolutePath());
								break;
							default:
								throw new IllegalArgumentException("Compiler " + ext.getCompiler() + " not supported!");
						}
						spec.setStandardOutput(outputStream);
					}).rethrowFailure();
				} catch (ExecException e) {
					throw new ExecException(e.getMessage() + "\n" + outputStream.toString(), e);
				}
			}
		});
	}
}
