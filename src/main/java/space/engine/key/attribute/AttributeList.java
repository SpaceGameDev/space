package space.engine.key.attribute;

import org.jetbrains.annotations.NotNull;
import space.engine.event.Event;
import space.engine.event.EventEntry;
import space.engine.event.SequentialEventBuilder;
import space.engine.indexmap.ConcurrentIndexMap;
import space.engine.sync.DelayTask;
import space.engine.sync.lock.SyncLock;
import space.engine.sync.lock.SyncLockImpl;

import java.util.List;
import java.util.function.BooleanSupplier;

import static space.engine.key.attribute.AttributeListCreator.DEFAULT;

/**
 * {@link AttributeList} is threadsafe, however it's modify Object {@link AttributeListModify} is not.
 */
public class AttributeList<TYPE> extends AbstractAttributeList<TYPE> implements SyncLock {
	
	protected final @NotNull AttributeListCreator<TYPE> creator;
	private final @NotNull SyncLock lock = new SyncLockImpl();
	protected final @NotNull Event<ChangeEvent<TYPE>> changeEvent = new SequentialEventBuilder<>();
	
	protected AttributeList(@NotNull AttributeListCreator<TYPE> creator) {
		super(new ConcurrentIndexMap<>(DEFAULT));
		this.creator = creator;
	}
	
	//delegate
	@Override
	public boolean tryLockNow() {
		return lock.tryLockNow();
	}
	
	@Override
	public void tryLockLater(BooleanSupplier callback) {
		lock.tryLockLater(callback);
	}
	
	@Override
	public Runnable unlock() {
		return lock.unlock();
	}
	
	public void addHook(@NotNull EventEntry<ChangeEvent<TYPE>> hook) {
		changeEvent.addHook(hook);
	}
	
	public boolean removeHook(@NotNull EventEntry<ChangeEvent<TYPE>> hook) {
		return changeEvent.removeHook(hook);
	}
	
	//methods
	@Override
	public @NotNull AttributeListCreator<TYPE> creator() {
		return creator;
	}
	
	public @NotNull Event<ChangeEvent<TYPE>> getChangeEvent() {
		return changeEvent;
	}
	
	
	/**
	 * creates a new {@link AttributeListModify AttributeListModify}.
	 * Calls <code>this.{@link AbstractAttributeList#creator()}.{@link AttributeListCreator#createModify()}</code> by default.
	 */
	public @NotNull AttributeListModify<TYPE> createModify() {
		return new AttributeListModify<>(this);
	}
	
	@FunctionalInterface
	public interface ChangeEvent<TYPE> {
		
		void onChange(AttributeListModify<TYPE> modify, List<AttributeKey<?>> changedKeys) throws DelayTask;
	}
	
}
