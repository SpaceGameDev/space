package space.engine.event.typehandler;

/**
 * A {@link TypeHandlerParallel} handles the Execution of FUNCTIONs AND allows parallel execution.
 *
 * @param <FUNCTION> the Function type
 * @see TypeHandler if this TypeHandler requires serial execution.
 */
public interface TypeHandlerParallel<FUNCTION> extends TypeHandler<FUNCTION> {

}
