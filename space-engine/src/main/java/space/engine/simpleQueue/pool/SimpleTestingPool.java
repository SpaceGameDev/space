package space.engine.simpleQueue.pool;

import org.jetbrains.annotations.NotNull;
import space.engine.simpleQueue.ConcurrentLinkedSimpleQueue;

public class SimpleTestingPool implements Executor {
	
	private final @NotNull ConcurrentLinkedSimpleQueue<Runnable> queue = new ConcurrentLinkedSimpleQueue<>();
	
	@Override
	public void execute(@NotNull Runnable command) {
		queue.add(command);
	}
	
	public void handle() {
		while (true) {
			Runnable run = queue.remove();
			if (run != null) {
				try {
					run.run();
				} catch (Poison e) {
					return;
				}
			} else {
				try {
					Thread.sleep(10);
				} catch (InterruptedException ignored) {
				
				}
			}
		}
	}
	
	public void poison() {
		execute(new Poison());
	}
	
	private static class Poison extends RuntimeException implements Runnable {
		
		public Poison() {
			super(null, null, false, false);
		}
		
		@Override
		public void run() {
			throw this;
		}
	}
}
