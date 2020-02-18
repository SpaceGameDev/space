package space.engine.window;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Methods with this Annotation should only be called by Threads having a window context.
 * {@link java.util.concurrent.Executor Executors} having a Window Context are the {@link WindowContext} and {@link Window}.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.TYPE_USE})
public @interface WindowThread {

}
