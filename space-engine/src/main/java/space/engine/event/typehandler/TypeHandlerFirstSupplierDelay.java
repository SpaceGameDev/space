package space.engine.event.typehandler;

import org.jetbrains.annotations.NotNull;
import space.engine.sync.DelayTask;
import space.engine.sync.Tasks.SupplierWithDelay;

/**
 * The {@link #result()} will be the first {@link SupplierWithDelay} returning something != null.
 * Sequential only.
 */
public class TypeHandlerFirstSupplierDelay<T> implements TypeHandler<SupplierWithDelay<T>> {
	
	private T result;
	
	@Override
	public void accept(@NotNull SupplierWithDelay<T> task) throws DelayTask {
		if (result == null)
			result = task.get();
	}
	
	public T result() {
		return result;
	}
}
