package space.engine.logger.prefix;

import space.engine.logger.LogMessage;

public class ThreadPrefix extends AbstractPrefix {
	
	public ThreadPrefix() {
	}
	
	public ThreadPrefix(char start, char end) {
		super(start, end);
	}
	
	@Override
	public void accept(LogMessage logMessage) {
		logMessage.prefix.append(startChar).append(logMessage.thread.getName()).append(endChar);
	}
}
