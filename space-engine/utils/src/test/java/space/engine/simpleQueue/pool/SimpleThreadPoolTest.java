package space.engine.simpleQueue.pool;

import org.junit.Test;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class SimpleThreadPoolTest {
	
	@Test(timeout = 250L)
	public void testLifecycle() {
		SimpleThreadPool pool = new SimpleThreadPool(4);
		AtomicInteger counter = new AtomicInteger();
		pool.executeAll(IntStream.range(0, 32).mapToObj(i -> counter::incrementAndGet));
		pool.stop().awaitUninterrupted();
		assertEquals(32, counter.get());
	}
	
	@Test(expected = RejectedExecutionException.class)
	public void testRejectedExecution() {
		SimpleThreadPool pool = new SimpleThreadPool(1);
		pool.stop();
		pool.execute(() -> {
			throw new RuntimeException("Should not be executed!");
		});
	}
}
