package space.engine.logger.prefix;

import space.engine.logger.LogMessage;

public class LogLevelPrefix extends AbstractPrefix {
	
	public LogLevelPrefix() {
	}
	
	public LogLevelPrefix(char start, char end) {
		super(start, end);
	}
	
	@Override
	public void accept(LogMessage logMessage) {
		logMessage.prefix.append(startChar).append(logMessage.level).append(endChar);
	}
}
