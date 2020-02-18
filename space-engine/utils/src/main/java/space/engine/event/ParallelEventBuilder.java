package space.engine.event;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.engine.barrier.Barrier;
import space.engine.event.typehandler.TypeHandler;
import space.engine.event.typehandler.TypeHandlerParallel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static space.engine.barrier.Barrier.when;

/**
 * This implementation of {@link Event} will submit it's hooks individually as tasks and use Barriers to ensure correct ordering.
 */
public class ParallelEventBuilder<FUNCTION> extends AbstractEventBuilder<FUNCTION> {
	
	private volatile @Nullable List<Node> build;
	
	@Override
	public @NotNull Barrier submit(@NotNull TypeHandler<FUNCTION> typeHandler) {
		if (!(typeHandler instanceof TypeHandlerParallel))
			throw new IllegalArgumentException("TypeHandler " + typeHandler + " does not allow multithreading!");
		
		List<Node> nodes = getBuild();
		Map<Node, Barrier> runMap = new HashMap<>();
		for (Node node : nodes) {
			runMap.put(node, when(node.prev.stream().map(runMap::get).toArray(Barrier[]::new))
					.thenRun(() -> typeHandler.accept(node.entry.function))
			);
		}
		return when(runMap.values());
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
		return computeDependencyOrderedList(computeNodeMap(true));
	}
	
	@Override
	public void clearCache() {
		build = null;
	}
}
