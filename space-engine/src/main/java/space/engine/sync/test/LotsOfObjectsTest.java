package space.engine.sync.test;

import org.jetbrains.annotations.NotNull;
import space.engine.Side;
import space.engine.sync.TaskCreator;
import space.engine.sync.barrier.Barrier;
import space.engine.sync.barrier.BarrierImpl;
import space.engine.sync.test.TransactionTest.Entity;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static space.engine.sync.Tasks.parallel;

public class LotsOfObjectsTest {
	
	public static final int[] OBJECT_COUNT = new int[] {500, 1000, 2000};
	public static boolean FANCY_PRINTOUT = true;
	public static boolean TIMER_PRINTOUT = true;
	
	public static void main(String[] args) throws InterruptedException {
		try {
			System.out.print(""); //initialization
			
			//run
			for (int count : OBJECT_COUNT) {
				long timeNeeded = run(count);
				System.out.println(String.format("%1$3s", count) + ": " + formatTimeMs(timeNeeded)
										   + " " + ((double) (count * (count - 1)) / (timeNeeded / 1E9d)) + "tr/s");
			}
		} finally {
			Side.exit().awaitUninterrupted();
		}
	}
	
	private static class IntVector {
		
		public int x;
		public int y;
		
		public IntVector(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}
	
	private static long run(int objectsCount) throws InterruptedException {
		Entity[] world = IntStream.range(0, objectsCount).mapToObj(v -> new Entity()).toArray(Entity[]::new);
		long time;
		
		if (FANCY_PRINTOUT)
			System.out.println(objectsCount + " Objects: taskCreator");
		time = System.nanoTime();
		TaskCreator<? extends Barrier> taskCreator =
				parallel(IntStream.range(0, world.length)
								  .boxed()
								  .flatMap(x -> IntStream.range(0, world.length)
														 .mapToObj(y -> new IntVector(x, y))
														 .filter(v -> v.x != v.y))
								  .map(v -> TransactionTest.createTransaction(world[v.x], world[v.y]))
								  .collect(Collectors.toList()));
		if (TIMER_PRINTOUT)
			System.out.println(formatTimeMs(System.nanoTime() - time));
		
		BarrierImpl barrier = new BarrierImpl();
		
		if (FANCY_PRINTOUT)
			System.out.println(objectsCount + " Objects: submitting");
		long totalTime = time = System.nanoTime();
		Barrier task = taskCreator.submit(barrier);
		if (TIMER_PRINTOUT)
			System.out.println(formatTimeMs(System.nanoTime() - time));
		
		if (FANCY_PRINTOUT)
			System.out.println(objectsCount + " Objects: launching");
		time = System.nanoTime();
		barrier.triggerNow();
		if (TIMER_PRINTOUT)
			System.out.println(formatTimeMs(System.nanoTime() - time));
		
		if (FANCY_PRINTOUT)
			System.out.println(objectsCount + " Objects: awaiting");
		time = System.nanoTime();
		task.await();
		if (TIMER_PRINTOUT)
			System.out.println(formatTimeMs(System.nanoTime() - time));
		
		if (FANCY_PRINTOUT)
			System.out.println(objectsCount + " Objects: done!");
		
		for (Entity entity : world)
			if (entity.count != 0)
				throw new RuntimeException();
		
		long totalDelta = System.nanoTime() - totalTime;
		if (TIMER_PRINTOUT)
			System.out.println("total execution time: " + formatTimeMs(totalDelta));
		return totalDelta;
	}
	
	@NotNull
	private static String formatTimeMs(long time) {
		return String.format("%1$3s", NANOSECONDS.toSeconds(time)) + "," +
				String.format("%1$3s", NANOSECONDS.toMillis(time) % 1000) + "." +
				String.format("%1$3s", NANOSECONDS.toMicros(time) % 1000) + "." +
				String.format("%1$3s", NANOSECONDS.toNanos(time) % 1000) + "ms";
	}
}
