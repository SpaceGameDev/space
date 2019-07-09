package space.engine.sync.test;

import org.jetbrains.annotations.NotNull;
import space.engine.Side;
import space.engine.sync.TaskCreator;
import space.engine.sync.barrier.Barrier;
import space.engine.sync.barrier.BarrierImpl;
import space.engine.sync.test.TransactionTest.Entity;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static space.engine.sync.Tasks.parallel;

public class LotsOfObjectsTest {
	
	public static final int[] OBJECT_COUNT = new int[] {500, 1000, 2000, 3000, 4000};
	public static boolean FANCY_PRINTOUT = false;
	public static boolean TIMER_PRINTOUT = false;
	
	public static void main(String[] args) throws InterruptedException {
		try {
			System.out.print(""); //initialization
			
			//run
			for (int count : OBJECT_COUNT) {
				Result result = run(count);
				System.out.println(String.format("%1$3s", count) + ": " + formatTimeMs(result.totalTime)
										   + " " + ((double) result.transactions) / (result.totalTime / 1E9d) + "tr/s");
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
	
	private static Result run(int objectsCount) throws InterruptedException {
		Entity[] world = IntStream.range(0, objectsCount).mapToObj(v -> new Entity()).toArray(Entity[]::new);
		long time;
		
		if (FANCY_PRINTOUT)
			System.out.println(objectsCount + " Objects: taskCreator");
		time = System.nanoTime();
		AtomicInteger transactionCount = new AtomicInteger();
		TaskCreator<? extends Barrier> taskCreator;
		{
			List<? extends TaskCreator<? extends Barrier>> transactions = IntStream
					.range(0, world.length)
					.boxed()
					.flatMap(x -> IntStream.range(0, world.length)
										   .mapToObj(y -> new IntVector(x, y))
										   .filter(v -> v.x != v.y))
					.map(v -> TransactionTest.createTransaction(world[v.x], world[v.y]))
					.collect(Collectors.toList());
			transactionCount.set(transactions.size());
			taskCreator = parallel(transactions);
		}
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
		long totalDelta = System.nanoTime() - totalTime;
		
		for (Entity entity : world)
			if (entity.count != 0)
				throw new RuntimeException();
		
		if (TIMER_PRINTOUT)
			System.out.println("total execution time: " + formatTimeMs(totalDelta));
		return new Result(totalDelta, transactionCount.get());
	}
	
	public static class Result {
		
		public final long totalTime;
		public final long transactions;
		
		public Result(long totalTime, long transactions) {
			this.totalTime = totalTime;
			this.transactions = transactions;
		}
	}
	
	@NotNull
	private static String formatTimeMs(long time) {
		return String.format("%1$3s", NANOSECONDS.toSeconds(time)) + "," +
				String.format("%1$3s", NANOSECONDS.toMillis(time) % 1000) + "." +
				String.format("%1$3s", NANOSECONDS.toMicros(time) % 1000) + "." +
				String.format("%1$3s", NANOSECONDS.toNanos(time) % 1000) + "ms";
	}
}
