package space.engine.logger.printer;

import space.engine.logger.LogMessage;
import space.engine.logger.Logger;
import space.engine.string.CharSequence2D;

import java.util.function.BiConsumer;

public class PipePrinter implements BiConsumer<LogMessage, CharSequence2D> {
	
	public Logger in;
	
	public PipePrinter(Logger in) {
		this.in = in;
	}
	
	@Override
	public void accept(LogMessage logMessage, CharSequence2D charSequence2D) {
		in.logDirect(logMessage);
	}
}
