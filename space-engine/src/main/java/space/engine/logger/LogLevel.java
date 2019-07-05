package space.engine.logger;

/**
 * An enum containing all LogLevels
 * level < 0 are fine information
 * level = 0 are {@link LogLevel} INFO
 * level > 0 are any kind of warning or error
 */
@SuppressWarnings("unused")
public enum LogLevel {
	
	DIE(3),
	ERROR(2),
	WARNING(1),
	INFO(0),
	FINE(-1),
	FINER(-2),
	FINEST(-3);
	
	public final int level;
	
	LogLevel(int level) {
		this.level = level;
	}
	
	/**
	 * @return true if other is above or equal to the minimum {@link LogLevel} (this)
	 */
	public boolean allowLog(LogLevel other) {
		return other.level >= level;
	}
}
