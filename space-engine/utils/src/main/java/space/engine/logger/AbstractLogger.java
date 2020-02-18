package space.engine.logger;

import org.jetbrains.annotations.NotNull;

public abstract class AbstractLogger implements Logger {
	
	public LogLevel minLevel = LogLevel.INFO;
	
	public void setMinLevel(LogLevel minLevel) {
		this.minLevel = minLevel;
	}
	
	public final void logDirect(@NotNull LogMessage msg) {
		if (minLevel.allowLog(msg.level))
			logDirect0(msg);
	}
	
	public abstract void logDirect0(LogMessage msg);
}
