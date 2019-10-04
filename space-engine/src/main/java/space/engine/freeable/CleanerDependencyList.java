package space.engine.freeable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.engine.barrier.Barrier;
import space.engine.baseobject.exceptions.FreedException;

import java.util.ArrayList;
import java.util.Objects;

import static space.engine.barrier.Barrier.DONE_BARRIER;

public class CleanerDependencyList implements Freeable {
	
	private boolean isFreed = false;
	private @Nullable Entry first;
	private @Nullable Barrier freeBarrier;
	
	public synchronized CleanerDependencyList.Entry insert(@NotNull Freeable storage) {
		if (isFreed)
			throw new FreedException(this);
		
		Entry entry = new Entry(storage);
		if (first != null) {
			entry.next = first;
			first.prev = entry;
		}
		first = entry;
		return entry;
	}
	
	@Override
	public synchronized @NotNull Barrier free() {
		if (isFreed)
			return Objects.requireNonNull(freeBarrier);
		isFreed = true;
		
		if (first == null)
			return freeBarrier = DONE_BARRIER;
		
		//free entries
		ArrayList<Barrier> list = new ArrayList<>();
		Entry next = first;
		while (next != null) {
			Barrier free = next.freeable.free();
			if (free != DONE_BARRIER)
				list.add(free);
			next = next.next;
		}
		
		//make entries unreachable
		first = null;
		return freeBarrier = Barrier.when(list);
	}
	
	@Override
	public synchronized boolean isFreed() {
		return isFreed;
	}
	
	@Override
	public @NotNull CleanerDependencyList getSubList() {
		return this;
	}
	
	public class Entry {
		
		public final Freeable freeable;
		private @Nullable Entry prev;
		private @Nullable Entry next;
		
		public Entry(Freeable freeable) {
			this.freeable = freeable;
		}
		
		/**
		 * removes an Entry from the List
		 */
		public void remove() {
			synchronized (CleanerDependencyList.this) {
				if (isFreed || (prev == null && next == null))
					return;
				
				if (prev != null)
					prev.next = next;
				if (next != null)
					next.prev = prev;
				if (first == this)
					first = next;
				
				next = null;
				prev = null;
			}
		}
	}
}
