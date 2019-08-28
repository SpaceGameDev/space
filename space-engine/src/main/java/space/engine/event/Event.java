package space.engine.event;

import org.jetbrains.annotations.NotNull;
import space.engine.barrier.Barrier;
import space.engine.event.typehandler.TypeHandler;

import static space.engine.barrier.Barrier.DONE_BARRIER;

/**
 * The {@link Event} Object is used to {@link Event#addHook(EventEntry)} and {@link Event#removeHook(EventEntry)} Hooks,
 * which will be called when the {@link Event} is triggered. The order in which they will be triggered is determined by
 * the Dependencies of the  supplied {@link EventEntry}.
 */
public interface Event<FUNCTION> {
	
	/**
	 * adds the hook to this event
	 *
	 * @param hook the hook to add
	 */
	void addHook(@NotNull EventEntry<? extends FUNCTION> hook);
	
	/**
	 * creates a {@link EventEntry} and adds it as a hook.
	 */
	default EventEntry<FUNCTION> addHook(FUNCTION function) {
		EventEntry<FUNCTION> entry = new EventEntry<>(function);
		addHook(entry);
		return entry;
	}
	
	/**
	 * creates a {@link EventEntry} and adds it as a hook.
	 */
	default EventEntry<FUNCTION> addHook(FUNCTION function, @NotNull EventEntry<?>... requires) {
		EventEntry<FUNCTION> entry = new EventEntry<>(function, requires);
		addHook(entry);
		return entry;
	}
	
	/**
	 * creates a {@link EventEntry} and adds it as a hook.
	 */
	default EventEntry<FUNCTION> addHook(FUNCTION function, @NotNull EventEntry<?>[] requiredBy, @NotNull EventEntry<?>... requires) {
		EventEntry<FUNCTION> entry = new EventEntry<>(function, requiredBy, requires);
		addHook(entry);
		return entry;
	}
	
	/**
	 * Removes the specified hook.<br>
	 * It is expected that the Method will be called very rarely and shouldn't be optimized via hashing or other techniques.
	 *
	 * @param hook the hook to be removed
	 * @return true if successful
	 */
	boolean removeHook(@NotNull EventEntry<FUNCTION> hook);
	
	/**
	 * Runs this Event with the specified {@link TypeHandler}.
	 *
	 * @return a Barrier triggered when the execution finished.
	 */
	@NotNull Barrier submit(@NotNull TypeHandler<FUNCTION> typeHandler);
	
	static <FUNCTION> Event<FUNCTION> voidEvent() {
		return new Event<>() {
			@Override
			public void addHook(@NotNull EventEntry<? extends FUNCTION> hook) {
			
			}
			
			@Override
			public boolean removeHook(@NotNull EventEntry<FUNCTION> hook) {
				return false;
			}
			
			@Override
			public @NotNull Barrier submit(@NotNull TypeHandler<FUNCTION> typeHandler) {
				return DONE_BARRIER;
			}
		};
	}
}
