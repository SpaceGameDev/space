package space.engine.logger.prefix;

import space.engine.logger.LogMessage;

import java.util.function.Consumer;

@FunctionalInterface
public interface Prefix extends Consumer<LogMessage> {
	
}
