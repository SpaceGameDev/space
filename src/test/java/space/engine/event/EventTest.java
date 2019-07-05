package space.engine.event;

import org.junit.Assert;
import org.junit.Test;
import space.engine.SingleThreadPoolTest;
import space.engine.event.typehandler.TypeHandlerParallel;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;

public class EventTest extends SingleThreadPoolTest {
	
	final int eventInput = 42;
	AtomicInteger callCounter = new AtomicInteger();
	
	EventEntry<Consumer<Integer>> unused = new EventEntry<>(createAcceptFunction(-42));
	
	EventEntry<Consumer<Integer>> accept0 = new EventEntry<>(createAcceptFunction(0));
	EventEntry<Consumer<Integer>> accept1 = new EventEntry<>(createAcceptFunction(1), accept0);
	EventEntry<Consumer<Integer>> accept234_1 = new EventEntry<>(createAcceptFunction(2, 4), accept1);
	EventEntry<Consumer<Integer>> accept234_2 = new EventEntry<>(createAcceptFunction(2, 4), accept1);
	EventEntry<Consumer<Integer>> accept234_3 = new EventEntry<>(createAcceptFunction(2, 4), accept1);
	EventEntry<Consumer<Integer>> accept56_1 = new EventEntry<>(createAcceptFunction(5, 6), accept234_1, accept234_2, accept234_3);
	EventEntry<Consumer<Integer>> accept56_2 = new EventEntry<>(createAcceptFunction(5, 6), accept234_1, accept234_2, accept234_3);
	EventEntry<Consumer<Integer>> accept7 = new EventEntry<>(createAcceptFunction(7), accept56_2, accept56_1, unused);
	EventEntry<Consumer<Integer>> accept9 = new EventEntry<>(createAcceptFunction(9));
	EventEntry<Consumer<Integer>> accept8 = new EventEntry<>(createAcceptFunction(8), new EventEntry[] {accept9}, new EventEntry[] {accept7, unused});
	
	@SuppressWarnings("unchecked")
	EventEntry<Consumer<Integer>>[] acceptAll = new EventEntry[] {
			accept0, accept1, accept234_1, accept234_2, accept234_3, accept56_1, accept56_2, accept7, accept8, accept9
	};
	
	private Consumer<Integer> createAcceptFunction(int callId) {
		return integer -> {
			assertEquals(integer.intValue(), eventInput);
			assertEquals(callCounter.getAndIncrement(), callId);
		};
	}
	
	@SuppressWarnings("SameParameterValue")
	private Consumer<Integer> createAcceptFunction(int callIdFrom, int callIdTo) {
		return integer -> {
			assertEquals(integer.intValue(), eventInput);
			Assert.assertThat(callCounter.getAndIncrement(), allOf(greaterThanOrEqualTo(callIdFrom), lessThanOrEqualTo(callIdTo)));
		};
	}
	
	public void testEvent(Event<Consumer<Integer>> eventImpl) throws InterruptedException {
		Arrays.stream(acceptAll).forEach(eventImpl::addHook);
		eventImpl.submit((TypeHandlerParallel<Consumer<Integer>>) func -> func.accept(eventInput)).await();
		assertEquals(callCounter.get(), acceptAll.length);
	}
	
	@Test
	public void testEventBuilderSinglethread() throws InterruptedException {
		testEvent(new SequentialEventBuilder<>());
	}
	
	@Test
	public void testEventBuilderMultithreaded() throws InterruptedException {
		testEvent(new ParallelEventBuilder<>());
	}
}
