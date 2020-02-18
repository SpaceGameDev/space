package space.engine.logger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.engine.string.CharSequence2D;
import space.engine.string.String2D;

public interface Logger {
	
	//subLogger
	@NotNull String name();
	
	@NotNull Logger subLogger(String name);
	
	@Nullable Logger parentLogger();
	
	//log
	default void log(@NotNull LogLevel level, @NotNull String str) {
		log(level, new String2D(str));
	}
	
	default void log(@NotNull LogLevel level, @NotNull CharSequence2D str) {
		logDirect(new LogMessage(Thread.currentThread(), this, level, str));
	}
	
	void logDirect(@NotNull LogMessage msg);
}
