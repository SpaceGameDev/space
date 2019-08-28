package space.engine.barrier.functions;

/**
 * A Callable which can also throw any {@link Throwable Throwables}, not just {@link Exception Exceptions}.
 */
@FunctionalInterface
public interface Callable<V> {
	
	V call() throws Throwable;
}