package space.engine.event;

import org.jetbrains.annotations.NotNull;
import space.engine.event.typehandler.TypeHandler;
import space.engine.event.typehandler.TypeHandlerParallel;
import space.engine.sync.Tasks;
import space.engine.sync.barrier.Barrier;
import space.engine.sync.lock.SyncLock;
import space.engine.sync.taskImpl.MultiTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This implementation of {@link Event} will submit it's hooks individually as tasks and use Barriers to ensure correct ordering.
 */
public class ParallelEventBuilder<FUNCTION> extends AbstractEventBuilder<FUNCTION> {
	
	private volatile List<Node> build;
	
	@Override
	public @NotNull Barrier submit(@NotNull TypeHandler<FUNCTION> typeHandler, @NotNull SyncLock[] locks, @NotNull Barrier... barriers) {
		if (!(typeHandler instanceof TypeHandlerParallel))
			throw new IllegalArgumentException("TypeHandler " + typeHandler + " does not allow multithreading!");
		
		List<Node> nodes = getBuild();
		return new MultiTask(locks, barriers) {
			@Override
			protected Barrier setup(Barrier start) {
				Map<Node, Barrier> runMap = new HashMap<>();
				for (Node node : nodes) {
					Barrier[] prevBarriers = node.prev.stream().map(runMap::get).toArray(Barrier[]::new);
					Barrier[] barrier = new Barrier[prevBarriers.length + 1];
					barrier[0] = start;
					System.arraycopy(prevBarriers, 0, barrier, 1, prevBarriers.length);
					runMap.put(node, Tasks.runnable(() -> typeHandler.accept(node.entry.function)).submit(barrier));
				}
				return Barrier.awaitAll(runMap.values());
			}
		};
	}
	
	//build
	public List<Node> getBuild() {
		//non-synchronized access
		List<Node> build = this.build;
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
	
	private List<Node> computeBuild() {
		return computeDependencyOrderedList();
	}
	
	@Override
	public void clearCache() {
		super.clearCache();
		build = null;
	}
}
