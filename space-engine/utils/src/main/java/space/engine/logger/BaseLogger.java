package space.engine.logger;

import org.jetbrains.annotations.NotNull;
import space.engine.event.EventEntry;
import space.engine.event.SequentialEventBuilder;
import space.engine.event.typehandler.TypeBiConsumer;
import space.engine.logger.prefix.LogLevelPrefix;
import space.engine.logger.prefix.Prefix;
import space.engine.logger.prefix.SubLoggerPrefix;
import space.engine.logger.prefix.ThreadPrefix;
import space.engine.logger.prefix.TimePrefix;
import space.engine.logger.printer.SeparatedPrinter;
import space.engine.logger.printer.SimpleStringPrinter;
import space.engine.string.CharSequence2D;
import space.engine.string.String2D;
import space.engine.string.StringBuilder2D;

import java.text.SimpleDateFormat;
import java.util.function.BiConsumer;

public class BaseLogger extends AbstractLogger {
	
	public static final EventEntry<Prefix> PREFIX_TIME = new EventEntry<>(new TimePrefix(new SimpleDateFormat("HH:mm:ss")));
	public static final EventEntry<Prefix> PREFIX_THREAD = new EventEntry<>(new ThreadPrefix(), PREFIX_TIME);
	public static final EventEntry<Prefix> PREFIX_LOGLEVEL = new EventEntry<>(new LogLevelPrefix(), PREFIX_THREAD);
	public static final EventEntry<Prefix> PREFIX_SUBLOGGER = new EventEntry<>(new SubLoggerPrefix(), PREFIX_LOGLEVEL);
	
	public static final EventEntry<BiConsumer<LogMessage, CharSequence2D>> PRINTER_SEPARATED = new EventEntry<>(new SeparatedPrinter(new SimpleStringPrinter(System.out),
																																	 new SimpleStringPrinter(System.err)));
	public static final EventEntry<BiConsumer<LogMessage, CharSequence2D>> PRINTER_OUT = new EventEntry<>(new SimpleStringPrinter(System.out));
	
	SequentialEventBuilder<Prefix> handler = new SequentialEventBuilder<>();
	SequentialEventBuilder<BiConsumer<LogMessage, CharSequence2D>> printer = new SequentialEventBuilder<>();
	public String prefixMessageSeparator;
	
	public BaseLogger() {
		this(": ");
	}
	
	public BaseLogger(String prefixMessageSeparator) {
		this.prefixMessageSeparator = prefixMessageSeparator;
	}
	
	//subLogger
	@NotNull
	@Override
	public String name() {
		return "root";
	}
	
	@Override
	public Logger parentLogger() {
		return null;
	}
	
	@NotNull
	@Override
	public Logger subLogger(String name) {
		return new SubLogger(this, name);
	}
	
	//log
	@Override
	public void logDirect0(LogMessage msg) {
		handler.runImmediatelyThrowIfWait(consumer -> consumer.accept(msg));
		String2D str = new StringBuilder2D().append(msg.prefix).append(prefixMessageSeparator).append(msg.msg).toString2D();
		printer.runImmediatelyThrowIfWait(new TypeBiConsumer<>(msg, str));
	}
	
	//utility
	
	/**
	 * adds a {@link java.util.Date} (formatted with HH:mm:ss), {@link Thread}, {@link LogLevel} and {@link SubLogger} prefix to the {@link BaseLogger}
	 */
	public static BaseLogger defaultHandler(BaseLogger logger) {
		logger.handler.addHook(PREFIX_TIME);
		logger.handler.addHook(PREFIX_THREAD);
		logger.handler.addHook(PREFIX_LOGLEVEL);
		logger.handler.addHook(PREFIX_SUBLOGGER);
		return logger;
	}
	
	/**
	 * adds the default System.out and System.err {@link java.io.PrintStream}s as Printer to the BaseLogger
	 */
	public static BaseLogger defaultPrinter(BaseLogger logger) {
		logger.printer.addHook(PRINTER_SEPARATED);
		return logger;
	}
}
