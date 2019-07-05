package space.engine.event.typehandler;

import org.jetbrains.annotations.NotNull;
import space.engine.sync.DelayTask;
import space.engine.sync.Tasks.ConsumerWithDelay;

/**
 * Sequential only.
 */
public class TypeConsumerDelay<T> implements TypeHandler<ConsumerWithDelay<T>> {
	
	public T obj;
	
	public TypeConsumerDelay(T obj) {
		this.obj = obj;
	}
	
	@Override
	public void accept(@NotNull ConsumerWithDelay<T> c) throws DelayTask {
		c.accept(obj);
	}
}
