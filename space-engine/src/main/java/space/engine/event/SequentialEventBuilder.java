package space.engine.event;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.engine.event.typehandler.TypeHandler;
import space.engine.sync.DelayTask;
import space.engine.sync.TaskCreator;
import space.engine.sync.Tasks;
import space.engine.sync.barrier.Barrier;
import space.engine.sync.lock.SyncLock;
import space.engine.sync.taskImpl.RunnableTask;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static space.engine.Side.pool;

/**
 * This implementation of {@link Event} will call it's hooks sequentially in a single thread
 */
public class SequentialEventBuilder<FUNCTION> extends AbstractEventBuilder<FUNCTION> {
	
	private volatile @Nullable List<FUNCTION> build;
	
	@Override
	public @NotNull Barrier submit(@NotNull TypeHandler<FUNCTION> typeHandler, @NotNull SyncLock[] locks, @NotNull Barrier... barriers) {
		Iterator<FUNCTION> iterator = getBuild().iterator();
		//noinspection unchecked
		TaskCreator<? extends Barrier>[] runnableWithDelay = new TaskCreator[1];
		runnableWithDelay[0] = Tasks.runnable(() -> {
			try {
				while (iterator.hasNext()) {
					typeHandler.accept(iterator.next());
				}
			} catch (DelayTask e) {
				throw new DelayTask(runnableWithDelay[0].submit(e.barrier));
			}
		});
		return runnableWithDelay[0].submit(locks, barriers);
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
		return runImmediatelyIfPossible(typeHandler, Barrier.EMPTY_BARRIER_ARRAY);
	}
	
	public Barrier runImmediatelyIfPossible(@NotNull TypeHandler<FUNCTION> typeHandler, Barrier... barriers) {
		if (barriers.length == 0 || Arrays.stream(barriers).allMatch(Barrier::isFinished)) {
			//all barriers already finished
			
			Iterator<FUNCTION> iterator = getBuild().iterator();
			boolean[] firstRun = new boolean[] {true};
			//noinspection unchecked
			TaskCreator<? extends RunnableTask>[] runnableWithDelay = new TaskCreator[1];
			runnableWithDelay[0] = (locks, barriers1) -> new RunnableTask(locks, barriers1) {
				
				@Override
				protected void submit1(Runnable toRun) {
					if (firstRun[0]) {
						firstRun[0] = false;
						toRun.run();
					} else {
						pool().execute(toRun);
					}
				}
				
				@Override
				protected void execute() throws DelayTask {
					try {
						while (iterator.hasNext()) {
							typeHandler.accept(iterator.next());
						}
					} catch (DelayTask e) {
						throw new DelayTask(runnableWithDelay[0].submit(e.barrier));
					}
				}
			};
			return runnableWithDelay[0].submit();
		}
		
		//if not go the normal route
		return submit(typeHandler, barriers);
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
		super.clearCache();
		build = null;
	}
}
