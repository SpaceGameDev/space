package space.engine.barrier;

import org.junit.Test;
import space.engine.SingleThreadPoolTest;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;
import static space.engine.barrier.Barrier.*;

public class TasksTest extends SingleThreadPoolTest {
	
	@Test
	public void testRun() {
		BarrierImpl start = new BarrierImpl();
		AtomicBoolean b = new AtomicBoolean(false);
		Barrier task = start.thenRun(() -> b.set(true));
		
		assertFalse(b.get());
		start.triggerNow();
		task.awaitUninterrupted();
		assertTrue(b.get());
	}
	
	@Test
	public void testRunDelayedReturn() {
		BarrierImpl start = new BarrierImpl();
		AtomicBoolean b = new AtomicBoolean(false);
		Barrier task = start.thenRun(() -> nowRun(() -> b.set(true)));
		
		assertFalse(b.get());
		start.triggerNow();
		task.awaitUninterrupted();
		assertTrue(b.get());
	}
	
	@Test
	public void testRunDelayedException() {
		BarrierImpl start = new BarrierImpl();
		AtomicBoolean b = new AtomicBoolean(false);
		Barrier task = start.thenRun(() -> {
			throw new DelayTask(nowRun(() -> b.set(true)));
		});
		
		assertFalse(b.get());
		start.triggerNow();
		task.awaitUninterrupted();
		assertTrue(b.get());
	}
	
	@Test
	public void testFuture() {
		assertEquals(nowFuture(() -> "string").awaitGetUninterrupted(), "string");
	}
	
	@Test(expected = IOException.class)
	public void testFutureWithException() throws IOException {
		nowFutureWithException(IOException.class, () -> {
			throw new IOException("inside task");
		}).awaitGetUninterrupted();
	}
	
	@Test(expected = IOException.class)
	public void testFutureWithXException() throws IOException, ClassNotFoundException, NoSuchMethodException {
		Barrier.<Object, NoSuchMethodException, IOException, ClassNotFoundException>nowFutureWith3Exception(NoSuchMethodException.class, IOException.class, ClassNotFoundException.class, () -> {
			throw new IOException("inside task");
		}).awaitGetUninterrupted();
	}
	
	@Test(expected = IOException.class)
	public void testFutureWithExceptionDelayed() throws IOException {
		nowFutureWithException(IOException.class, () -> {
			throw new DelayTask(nowFutureWithException(IOException.class, () -> {
				throw new IOException("inside task");
			}));
		}).awaitGetUninterrupted();
	}
	
	@Test(expected = IOException.class)
	public void testFutureWithXExceptionDelayed() throws IOException, ClassNotFoundException {
		nowFutureWith3Exception(RuntimeException.class, IOException.class, ClassNotFoundException.class, () -> {
			throw new DelayTask(nowFutureWithException(IOException.class, () -> {
				throw new IOException("inside task");
			}));
		}).awaitGetUninterrupted();
	}
}
