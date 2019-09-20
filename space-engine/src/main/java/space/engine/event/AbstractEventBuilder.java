package space.engine.event;

import org.jetbrains.annotations.NotNull;
import space.engine.baseobject.Cache;
import space.engine.baseobject.ToString;
import space.engine.delegate.set.ModificationAwareSet;
import space.engine.string.toStringHelper.ToStringHelper;
import space.engine.string.toStringHelper.ToStringHelper.ToStringHelperObjectsInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public abstract class AbstractEventBuilder<FUNCTION> implements Event<FUNCTION>, Cache {
	
	protected Set<EventEntry<? extends FUNCTION>> list = new ModificationAwareSet<>(new HashSet<>(), this::clearCache);
	
	//hooks
	@Override
	public <T extends FUNCTION> EventEntry<T> addHook(@NotNull EventEntry<T> hook) {
		list.add(hook);
		return hook;
	}
	
	@Override
	public synchronized boolean removeHook(@NotNull EventEntry<FUNCTION> task) {
		return list.remove(task);
	}
	
	@Override
	public abstract void clearCache();
	
	/**
	 * @param optimizeNextPriority whether to resort the {@link List} of next {@link Node}s, in order to put Nodes with more Dependencies further at the top, and with less towards the end
	 */
	protected Map<EventEntry<?>, Node> computeNodeMap(boolean optimizeNextPriority) {
		Map<EventEntry<?>, Node> allNodes = new HashMap<>();
		list.forEach(entry -> allNodes.put(entry, new Node(entry)));
		
		//adding Dependencies
		allNodes.values().forEach(node -> {
			EventEntry<? extends FUNCTION> entry = node.entry;
			for (EventEntry<?> require : entry.requires) {
				Node other = allNodes.get(require);
				if (other != null)
					other.addDependencyAfter(node);
			}
			for (EventEntry<?> requiredBy : entry.requiredBy) {
				Node other = allNodes.get(requiredBy);
				if (other != null)
					node.addDependencyAfter(other);
			}
		});
		
		//optimizeNextPriority
		if (optimizeNextPriority)
			allNodes.values().forEach(node -> node.next.sort((o1, o2) -> o2.next.size() - o1.next.size()));
		
		return allNodes;
	}
	
	protected List<Node> computeDependencyOrderedList() {
		return computeDependencyOrderedList(computeNodeMap(true));
	}
	
	protected List<Node> computeDependencyOrderedList(Map<EventEntry<?>, Node> nodeMap) {
		Map<Node, AtomicInteger> runMap = nodeMap.values().stream().collect(Collectors.toMap(node -> node, node -> new AtomicInteger(node.prev.size()), (a, b) -> b));
		
		List<Node> ret = new ArrayList<>(nodeMap.size());
		while (runMap.size() != 0) {
			boolean foundAny = false;
			Set<Entry<Node, AtomicInteger>> entrySet = runMap.entrySet();
			//noinspection unchecked
			for (Entry<Node, AtomicInteger> entry : entrySet.toArray(new Entry[0])) {
				if (entry.getValue().get() == 0) {
					entrySet.remove(entry);
					foundAny = true;
					
					Node node = entry.getKey();
					ret.add(node);
					node.next.forEach(nextNode -> runMap.get(nextNode).decrementAndGet());
				}
			}
			
			if (!foundAny)
				throw new RuntimeException("Couldn't resolve dependencies! Maybe there was a dependency circle?");
		}
		
		return ret;
	}
	
	/**
	 * one {@link Node} exists for each FUNCTION
	 */
	protected class Node implements ToString {
		
		public final EventEntry<? extends FUNCTION> entry;
		public final List<Node> next = new ArrayList<>(1);
		public final List<Node> prev = new ArrayList<>(1);
		
		public Node(EventEntry<? extends FUNCTION> entry) {
			this.entry = entry;
		}
		
		protected void addDependencyAfter(Node node) {
			this.next.add(node);
			node.prev.add(this);
		}
		
		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof AbstractEventBuilder<?>.Node))
				return false;
			AbstractEventBuilder<?>.Node node = (AbstractEventBuilder<?>.Node) o;
			return Objects.equals(entry, node.entry);
		}
		
		@Override
		public int hashCode() {
			return entry.hashCode();
		}
		
		@Override
		@NotNull
		public <TSHTYPE> TSHTYPE toTSH(@NotNull ToStringHelper<TSHTYPE> api) {
			ToStringHelperObjectsInstance<TSHTYPE> tsh = api.createObjectInstance(this);
			tsh.add("entry", this.entry);
			tsh.add("next.size()", this.next.size());
			tsh.add("prev.size()", this.prev.size());
			return tsh.build();
		}
		
		@Override
		public String toString() {
			return toString0();
		}
	}
}