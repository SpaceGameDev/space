package space.engine.event;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.engine.barrier.Barrier;
import space.engine.barrier.DelayTask;
import space.engine.barrier.functions.RunnableWithDelay;
import space.engine.event.typehandler.TypeHandler;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static space.engine.barrier.Barrier.nowRun;

/**
 * This implementation of {@link Event} will call it's hooks sequentially in a single thread
 */
public class SequentialEventBuilder<FUNCTION> extends AbstractEventBuilder<FUNCTION> {
	
	private volatile @Nullable List<FUNCTION> build;
	
	@Override
	public @NotNull Barrier submit(@NotNull TypeHandler<FUNCTION> typeHandler) {
		Iterator<FUNCTION> iterator = getBuild().iterator();
		return nowRun(new RunnableWithDelay() {
			@Override
			public void run() throws DelayTask {
				try {
					while (iterator.hasNext())
						typeHandler.accept(iterator.next());
				} catch (DelayTask e) {
					throw new DelayTask(e.barrier.thenRun(this));
				}
			}
		});
	}
	
	/**
	 * use {@link #runImmediatelyThrowIfWait(TypeHandler)} or {@link #runImmediatelyIfPossible(TypeHandler)}.{@link Barrier#addHook(Runnable) addHook(Runnable)} instead
	 */
	@Deprecated
	public void runImmediately(@NotNull TypeHandler<FUNCTION> typeHandler) {
		for (FUNCTION function : getBuild()) {
			try {
				typeHandler.accept(function);
			} catch (DelayTask e) {
				//waiting is generally a bad idea, but we cannot do anything else in this case
				//SequentialEventBuilders used with runImmediately should either way never enter this state
				e.barrier.awaitUninterrupted();
			}
		}
	}
	
	/**
	 * throws an {@link UnsupportedOperationException} if any Task wants to wait / throws {@link DelayTask}
	 */
	public void runImmediatelyThrowIfWait(@NotNull TypeHandler<FUNCTION> typeHandler) {
		for (FUNCTION function : getBuild()) {
			try {
				typeHandler.accept(function);
			} catch (DelayTask e) {
				throw new UnsupportedOperationException("Waiting with runImmediatelyThrowIfWait() is not allowed! Barrier: " + e.barrier);
			}
		}
	}
	
	public Barrier runImmediatelyIfPossible(@NotNull TypeHandler<FUNCTION> typeHandler) {
		Iterator<FUNCTION> iterator = getBuild().iterator();
		return nowRun(Runnable::run, new RunnableWithDelay() {
			@Override
			public void run() throws DelayTask {
				try {
					while (iterator.hasNext())
						typeHandler.accept(iterator.next());
				} catch (DelayTask e) {
					throw new DelayTask(e.barrier.thenRun(this));
				}
			}
		});
	}
	
	//build
	public List<FUNCTION> getBuild() {
		//non-synchronized access
		List<FUNCTION> build = this.build;
		if (build != null)
			return build;
		
		synchronized (this) {
			//synchronized access to prevent generating list multiple times
			build = this.build;
			if (build != null)
				return build;
			
			//actual build
			build = computeBuild();
			this.build = build;
			return build;
		}
	}
	
	@NotNull
	private List<FUNCTION> computeBuild() {
		return computeDependencyOrderedList().stream().map(node -> node.entry.function).collect(Collectors.toList());
	}
	
	@Override
	public void clearCache() {
		build = null;
	}
}
