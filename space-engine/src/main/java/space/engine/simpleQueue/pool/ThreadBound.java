package space.engine.simpleQueue.pool;

import org.jetbrains.annotations.NotNull;
import space.engine.barrier.Barrier;
import space.engine.barrier.functions.Starter;
import space.engine.event.EventEntry;
import space.engine.event.SequentialEventBuilder;

import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import static space.engine.barrier.Barrier.when;

public class ThreadBound {
	
	private static final ConcurrentHashMap<@NotNull Thread, @NotNull Entry> map = new ConcurrentHashMap<>();
	
	public static class Entry {
		
		private final @NotNull Executor executor;
		private final @NotNull SequentialEventBuilder<Starter<?>> onShutdown = new SequentialEventBuilder<>();
		
		public Entry(@NotNull Executor executor) {
			this.executor = executor;
		}
		
		public Barrier free() {
			Builder<Barrier> b = Stream.builder();
			onShutdown.runImmediatelyThrowIfWait(starter -> b.add(starter.startInlineException()));
			return when(b.build());
		}
	}
	
	//methods
	public static Entry addQueue(Thread thread, Executor exec) {
		Entry entry = new Entry(exec);
		if (map.putIfAbsent(thread, entry) != null)
			throw new IllegalStateException("SimpleQueue already set for Thread " + thread);
		return entry;
	}
	
	public static Executor getQueue(Thread thread) {
		@NotNull Entry exec = map.get(thread);
		if (exec == null)
			throw new IllegalStateException("No queue present for Thread " + thread);
		return exec.executor;
	}
	
	public static void submit(Thread thread, Runnable run) {
		getQueue(thread).execute(run);
	}
	
	public static void addShutdownHook(Thread thread, Starter<?> entry) {
		addShutdownHook(thread, new EventEntry<>(entry));
	}
	
	public static void addShutdownHook(Thread thread, EventEntry<Starter<?>> entry) {
		@NotNull Entry exec = map.get(thread);
		if (exec == null)
			throw new IllegalStateException("No queue present for Thread " + thread);
		exec.onShutdown.addHook(entry);
	}
}
