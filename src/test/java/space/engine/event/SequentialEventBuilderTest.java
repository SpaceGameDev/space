package space.engine.event;

import org.junit.Test;
import space.engine.sync.DelayTask;
import space.engine.sync.Tasks.ConsumerWithDelay;
import space.engine.sync.barrier.Barrier;
import space.engine.sync.barrier.BarrierImpl;

import java.util.function.Consumer;

import static org.junit.Assert.*;
import static space.engine.sync.barrier.Barrier.ALWAYS_TRIGGERED_BARRIER;

public class SequentialEventBuilderTest {
	
	@Test
	public void testRunImmediatelyIfPossibleNormal() {
		SequentialEventBuilder<Consumer<Integer>> event = new SequentialEventBuilder<>();
		
		Integer[] callback = new Integer[1];
		Thread[] callbackTh = new Thread[1];
		event.addHook(i -> {
			callback[0] = i;
			callbackTh[0] = Thread.currentThread();
		});
		
		assertNull(callback[0]);
		assertNull(callbackTh[0]);
		
		event.runImmediatelyIfPossible(consumer -> consumer.accept(5)).awaitUninterrupted();
		assertEquals((Integer) 5, callback[0]);
		assertEquals(Thread.currentThread(), callbackTh[0]);
	}
	
	@Test
	public void testRunImmediatelyIfPossibleWithWait() {
		SequentialEventBuilder<ConsumerWithDelay<Integer>> event = new SequentialEventBuilder<>();
		
		Integer[] callback = new Integer[2];
		Thread[] callbackTh = new Thread[2];
		BarrierImpl callback1Wait = new BarrierImpl();
		BarrierImpl callback1Notify = new BarrierImpl();
		EventEntry<ConsumerWithDelay<Integer>> eventEntry1 = new EventEntry<>(i -> {
			callback[0] = i;
			callbackTh[0] = Thread.currentThread();
			callback1Notify.triggerNow();
			throw new DelayTask(callback1Wait);
		});
		event.addHook(eventEntry1);
		EventEntry<ConsumerWithDelay<Integer>> eventEntry2 = new EventEntry<>(i -> {
			callback[1] = i;
			callbackTh[1] = Thread.currentThread();
		}, eventEntry1);
		event.addHook(eventEntry2);
		
		assertNull(callback[0]);
		assertNull(callbackTh[0]);
		assertNull(callback[1]);
		assertNull(callbackTh[1]);
		
		Barrier eventDone = event.runImmediatelyIfPossible(consumer -> consumer.accept(5));
		callback1Notify.awaitUninterrupted();
		
		assertEquals((Integer) 5, callback[0]);
		assertEquals(Thread.currentThread(), callbackTh[0]);
		assertNull(callback[1]);
		assertNull(callbackTh[1]);
		
		callback1Wait.triggerNow();
		eventDone.awaitUninterrupted();
		
		assertEquals((Integer) 5, callback[0]);
		assertEquals(Thread.currentThread(), callbackTh[0]);
		assertEquals((Integer) 5, callback[1]);
		assertNotNull(callbackTh[1]);
		assertNotEquals(Thread.currentThread(), callbackTh[1]);
	}
	
	@Test
	public void testRunImmediatelyThrowIfWaitNormal() {
		SequentialEventBuilder<Consumer<Integer>> event = new SequentialEventBuilder<>();
		
		Integer[] callback = new Integer[1];
		Thread[] callbackTh = new Thread[1];
		event.addHook(i -> {
			callback[0] = i;
			callbackTh[0] = Thread.currentThread();
		});
		
		event.runImmediatelyThrowIfWait(consumer -> consumer.accept(5));
		
		assertEquals((Integer) 5, callback[0]);
		assertEquals(Thread.currentThread(), callbackTh[0]);
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void testRunImmediatelyThrowIfWaitWithWaitingCallback() {
		SequentialEventBuilder<ConsumerWithDelay<Integer>> event = new SequentialEventBuilder<>();
		
		event.addHook(i -> {
			throw new DelayTask(ALWAYS_TRIGGERED_BARRIER);
		});
		
		event.runImmediatelyThrowIfWait(consumer -> consumer.accept(5));
	}
}
