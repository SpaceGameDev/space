package space.engine.sync;

import org.junit.Test;
import space.engine.SingleThreadPoolTest;
import space.engine.sync.barrier.Barrier;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.*;
import static space.engine.sync.Tasks.*;

public class TasksTest extends SingleThreadPoolTest {
	
	private static final int threads = 20;
	
	@Test
	public void testRunnable() throws InterruptedException {
		AtomicBoolean b = new AtomicBoolean(false);
		runnable(() -> b.set(true)).submit().await();
		assertTrue(b.get());
	}
	
	@Test
	public void testRunnableDelayed() throws InterruptedException {
		AtomicBoolean b = new AtomicBoolean(false);
		runnable(() -> {
			throw new DelayTask(runnable(() -> b.set(true)).submit());
		}).submit().await();
		assertTrue(b.get());
	}
	
	@Test
	public void testMultipleTasksFromOneCreator() throws InterruptedException {
		AtomicInteger counter = new AtomicInteger();
		TaskCreator<? extends Barrier> taskCreator = runnable(counter::incrementAndGet);
		Barrier.awaitAll(IntStream.range(0, threads).mapToObj(i2 -> taskCreator.submit()).toArray(Barrier[]::new)).await();
		assertEquals(counter.get(), threads);
	}
	
	@Test
	public void testFuture() throws InterruptedException {
		assertEquals(future(() -> "string").submit().awaitGet(), "string");
	}
	
	@Test(expected = IOException.class)
	public void testFutureWithException() throws InterruptedException, IOException {
		futureWithException(IOException.class, () -> {
			throw new IOException("inside task");
		}).submit().awaitGet();
	}
	
	@Test(expected = IOException.class)
	public void testFutureWithXException() throws InterruptedException, IOException, ClassNotFoundException, NoSuchMethodException {
		Tasks.<Object, NoSuchMethodException, IOException, ClassNotFoundException>futureWith3Exception(NoSuchMethodException.class, IOException.class, ClassNotFoundException.class, () -> {
			throw new IOException("inside task");
		}).submit().awaitGet();
	}
	
	@Test(expected = IOException.class)
	public void testFutureWithExceptionDelayed() throws InterruptedException, IOException {
		futureWithException(IOException.class, () -> {
			throw new DelayTask(futureWithException(IOException.class, () -> {
				throw new IOException("inside task");
			}).submit());
		}).submit().awaitGet();
	}
	
	@Test(expected = IOException.class)
	public void testFutureWithXExceptionDelayed() throws InterruptedException, IOException, ClassNotFoundException {
		futureWith3Exception(RuntimeException.class, IOException.class, ClassNotFoundException.class, () -> {
			throw new DelayTask(futureWithException(IOException.class, () -> {
				throw new IOException("inside task");
			}).submit());
		}).submit().awaitGet();
	}
	
	@Test
	public void testSequential() throws InterruptedException {
		AtomicInteger counter = new AtomicInteger();
		sequential(IntStream.range(0, threads).mapToObj(i -> runnable(() -> assertEquals(counter.getAndIncrement(), i))).collect(Collectors.toList())).submit().await();
		assertEquals(counter.get(), threads);
	}
	
	@Test
	public void testParallel() throws InterruptedException {
		AtomicInteger counter = new AtomicInteger();
		sequential(IntStream.range(0, threads).mapToObj(i -> runnable(counter::getAndIncrement)).collect(Collectors.toList())).submit().await();
		assertEquals(counter.get(), threads);
	}
	
	@Test
	public void testMultiCustom() {
		TaskCreator<? extends Barrier> taskCreator = multiCustom(barrier -> {
			AtomicInteger counter = new AtomicInteger();
			Barrier first = taskCheckCounter(counter, 0).submit(barrier);
			TaskCreator<?> middleCreator = taskCheckCounter(counter, 1, 2, 3);
			Barrier[] middle = {middleCreator.submit(first), middleCreator.submit(first), middleCreator.submit(first)};
			return taskCheckCounter(counter, 4).submit(middle);
		});
		for (int i = 0; i < 3; i++)
			taskCreator.submit();
	}
	
	public TaskCreator<?> taskCheckCounter(AtomicInteger counter, int... valid) {
		return runnable(() -> {
			int i = counter.getAndIncrement();
			assertThat(Arrays.stream(valid).boxed().collect(Collectors.toList()), hasItem(i));
		});
	}
}
