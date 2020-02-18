package space.engine.logger.printer;

import space.engine.logger.LogMessage;
import space.engine.string.CharSequence2D;

import java.io.PrintStream;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SimpleStringPrinter implements BiConsumer<LogMessage, CharSequence2D> {
	
	public final Consumer<String> consumer;
	
	public SimpleStringPrinter(Consumer<String> consumer) {
		this.consumer = consumer;
	}
	
	public SimpleStringPrinter(PrintStream consumer) {
		this.consumer = consumer::println;
	}
	
	@Override
	public void accept(LogMessage logMessage, CharSequence2D msg) {
		consumer.accept(msg.toString0());
	}
}
