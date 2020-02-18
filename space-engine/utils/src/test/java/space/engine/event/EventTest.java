package space.engine.event;

import org.junit.Assert;
import org.junit.Test;
import space.engine.barrier.Barrier;
import space.engine.barrier.DelayTask;
import space.engine.barrier.functions.ConsumerWithDelay;
import space.engine.event.typehandler.TypeHandlerParallel;
import space.engine.simpleQueue.pool.SimpleTestingPool;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static space.engine.barrier.Barrier.nowRun;

public class EventTest {
	
	final SimpleTestingPool debugPool = new SimpleTestingPool();
	final int eventInput = 42;
	AtomicInteger callCounter = new AtomicInteger();
	
	EventEntry<ConsumerWithDelay<Integer>> unused = new EventEntry<>(createAcceptFunction(-42));
	EventEntry<ConsumerWithDelay<Integer>> accept0 = new EventEntry<>(createAcceptFunction(0));
	EventEntry<ConsumerWithDelay<Integer>> accept1 = new EventEntry<>(createAcceptFunction(1), accept0);
	EventEntry<ConsumerWithDelay<Integer>> accept234_1 = new EventEntry<>(createAcceptFunction(2, 4), accept1);
	EventEntry<ConsumerWithDelay<Integer>> accept234_2 = new EventEntry<>(createAcceptFunction(2, 4), accept1);
	EventEntry<ConsumerWithDelay<Integer>> accept234_3 = new EventEntry<>(createAcceptFunction(2, 4), accept1);
	EventEntry<ConsumerWithDelay<Integer>> accept56_1 = new EventEntry<>(createAcceptFunction(5, 6), accept234_1, accept234_2, accept234_3);
	EventEntry<ConsumerWithDelay<Integer>> accept56_2 = new EventEntry<>(createAcceptFunction(5, 6), accept234_1, accept234_2, accept234_3);
	EventEntry<ConsumerWithDelay<Integer>> accept7 = new EventEntry<>(createAcceptFunction(7), accept56_2, accept56_1, unused);
	EventEntry<ConsumerWithDelay<Integer>> accept9 = new EventEntry<>(createAcceptFunction(9));
	EventEntry<ConsumerWithDelay<Integer>> accept8 = new EventEntry<>(createAcceptFunction(8), new EventEntry[] {accept9}, new EventEntry[] {accept7, unused});
	List<EventEntry<ConsumerWithDelay<Integer>>> acceptAll = List.of(accept0, accept1, accept234_1, accept234_2, accept234_3, accept56_1, accept56_2, accept7, accept8, accept9);
	
	private ConsumerWithDelay<Integer> createAcceptFunction(int callId) {
		return integer -> {
			throw new DelayTask(nowRun(debugPool, () -> {
				assertEquals(integer.intValue(), eventInput);
				assertEquals(callCounter.getAndIncrement(), callId);
			}));
		};
	}
	
	@SuppressWarnings("SameParameterValue")
	private ConsumerWithDelay<Integer> createAcceptFunction(int callIdFrom, int callIdTo) {
		return integer -> {
			throw new DelayTask(nowRun(debugPool, () -> {
				assertEquals(integer.intValue(), eventInput);
				Assert.assertThat(callCounter.getAndIncrement(), allOf(greaterThanOrEqualTo(callIdFrom), lessThanOrEqualTo(callIdTo)));
			}));
		};
	}
	
	public void testEvent(Event<ConsumerWithDelay<Integer>> eventImpl) throws InterruptedException {
		acceptAll.forEach(eventImpl::addHook);
		Barrier submit = eventImpl.submit((TypeHandlerParallel<ConsumerWithDelay<Integer>>) func -> func.accept(eventInput));
		submit.addHook(debugPool::poison);
		debugPool.handle();
		submit.await();
		assertEquals(callCounter.get(), acceptAll.size());
	}
	
	@Test(timeout = 1000L)
	public void testEventBuilderSinglethread() throws InterruptedException {
		testEvent(new SequentialEventBuilder<>());
	}
	
	@Test(timeout = 250L)
	public void testEventBuilderMultithreaded() throws InterruptedException {
		testEvent(new ParallelEventBuilder<>());
	}
	
	@Test(expected = AssertionError.class)
	public void testEventBuilderAssertionError() throws InterruptedException {
		acceptAll = Stream.concat(
				acceptAll.stream(),
				Stream.of(new EventEntry<>((ConsumerWithDelay<Integer>) integer -> {
					throw new DelayTask(nowRun(debugPool, () -> {
						assertEquals(integer.intValue(), eventInput + 1);
					}));
				}))
		).collect(Collectors.toUnmodifiableList());
		testEvent(new SequentialEventBuilder<>());
	}
}
