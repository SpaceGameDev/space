package space.engine.sync.test;

import space.engine.Side;
import space.engine.freeableStorage.Freeable;
import space.engine.freeableStorage.stack.FreeableStack.Frame;
import space.engine.sync.DelayTask;
import space.engine.sync.TaskCreator;
import space.engine.sync.Tasks;
import space.engine.sync.barrier.Barrier;
import space.engine.sync.future.Future;
import space.engine.sync.timer.BarrierTimer;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class NestedTaskTest {
	
	public static void main(String[] args) throws InterruptedException {
		try (Frame frame = Freeable.frame()) {
			BarrierTimer TIMER = BarrierTimer.createUnmodifiable(-System.nanoTime(), 1, new Object[] {frame});
			AtomicInteger COUNTER = new AtomicInteger();
			
			TaskCreator<? extends Future<float[]>> future = Tasks.future(() -> {
				int i = COUNTER.getAndIncrement();
				if (i % 2 == 0)
					throw new DelayTask(TIMER.create(i * 500000000L).toFuture(() -> new float[] {i}));
				
				return new float[] {i};
			});
			
			Barrier.awaitAll(
					IntStream.range(0, 10)
							 .mapToObj(i -> future.submit())
							 .peek(o -> o.addHook(() -> System.out.println(Arrays.toString(o.assertGet()))))
							 .collect(Collectors.toList())
			).await();
		} finally {
			Side.exit().awaitUninterrupted();
		}
	}
}
