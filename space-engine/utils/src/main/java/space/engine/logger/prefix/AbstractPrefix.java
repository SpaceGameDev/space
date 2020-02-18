package space.engine.logger.prefix;

public abstract class AbstractPrefix implements Prefix {
	
	public char startChar;
	public char endChar;
	
	public AbstractPrefix() {
		this('[', ']');
	}
	
	public AbstractPrefix(char startChar, char endChar) {
		this.startChar = startChar;
		this.endChar = endChar;
	}
}
