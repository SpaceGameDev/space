package space.engine.simpleQueue;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class SimpleQueueTest {
	
	@Parameters
	public static Collection<Supplier<? extends SimpleQueue<Integer>>> parameters() {
		return List.of(() -> new ArraySimpleQueue<>(16), LinkedSimpleQueue::new, ConcurrentLinkedSimpleQueue::new);
	}
	
	private final SimpleQueue<Integer> queue;
	
	public SimpleQueueTest(Supplier<? extends SimpleQueue<Integer>> supplier) {
		this.queue = supplier.get();
	}
	
	@Test
	public void testNormalUse() {
		queue.add(0);
		queue.add(1);
		queue.add(2);
		queue.add(3);
		
		int[] slots = new int[4];
		for (int i = 0; i < 3; i++)
			slots[Objects.requireNonNull(queue.remove())] = 1;
		assertEquals(3, Arrays.stream(slots).sum());
	}
	
	@Test
	public void testQueueDryup() {
		assertNull(queue.remove());
		
		queue.add(0);
		Assert.assertEquals((Integer) 0, queue.remove());
		assertNull(queue.remove());
	}
	
	@Test
	public void testAddAll() {
		Runnable remove4Numbers = () -> {
			int[] slots = new int[4];
			for (int i = 0; i < 4; i++)
				slots[Objects.requireNonNull(queue.remove())] = 1;
			assertEquals(4, Arrays.stream(slots).sum());
		};
		
		//collection
		queue.addCollection(List.of(0, 1, 2, 3));
		remove4Numbers.run();
		
		//array
		queue.addArray(new Integer[] {0, 1, 2, 3});
		remove4Numbers.run();
	}
}
