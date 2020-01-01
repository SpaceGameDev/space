package space.engine.recourcePool;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class RecourcePoolTest {
	
	private final BlockResourcePool<Integer> pool;
	
	public RecourcePoolTest() {
		pool = new BlockResourcePool<>(4) {
			
			private AtomicInteger counter = new AtomicInteger();
			
			@Override
			protected Integer[] allocateNewBlock(int count) {
				return IntStream.range(0, count).map(i -> counter.getAndIncrement()).boxed().toArray(Integer[]::new);
			}
		};
	}
	
	@Test
	public void testAllocate() {
		assertEquals((Integer) 3, pool.allocate());
		assertEquals((Integer) 2, pool.allocate());
		assertEquals((Integer) 1, pool.allocate());
	}
	
	@Test
	public void testAllocateAndRelease() {
		assertEquals((Integer) 3, pool.allocate());
		assertEquals((Integer) 2, pool.allocate());
		assertEquals((Integer) 1, pool.allocate());
		pool.release(new Integer[] {1, 2, 3});
		assertEquals((Integer) 3, pool.allocate());
		assertEquals((Integer) 2, pool.allocate());
		assertEquals((Integer) 1, pool.allocate());
	}
	
	@Test
	public void testAllocateAndReleaseInternal() {
		assertInternalState(0, 0);
		assertEquals((Integer) 3, pool.allocate());
		assertInternalState(0, 3);
		assertEquals((Integer) 2, pool.allocate());
		assertInternalState(0, 2);
		assertEquals((Integer) 1, pool.allocate());
		assertInternalState(0, 1);
		assertEquals((Integer) 0, pool.allocate());
		assertInternalState(0, 0);
		pool.release(new Integer[] {3, 2, 1, 0});
		assertInternalState(0, 4);
		assertEquals((Integer) 0, pool.allocate());
		assertInternalState(0, 3);
		assertEquals((Integer) 1, pool.allocate());
		assertInternalState(0, 2);
		assertEquals((Integer) 2, pool.allocate());
		assertInternalState(0, 1);
		assertEquals((Integer) 3, pool.allocate());
		assertInternalState(0, 0);
	}
	
	@Test
	public void testReleaseSpillInternal() {
		for (int i : new int[] {3, 2, 1, 0})
			assertEquals((Integer) i, pool.allocate());
		assertInternalState(0, 0);
		for (int i : new int[] {7, 6, 5, 4})
			assertEquals((Integer) i, pool.allocate());
		assertInternalState(0, 0);
		for (int i : new int[] {11, 10, 9, 8})
			assertEquals((Integer) i, pool.allocate());
		assertInternalState(0, 0);
		
		pool.release(new Integer[] {1, 2, 3, 4});
		assertInternalState(0, 4);
		pool.release(new Integer[] {5, 6, 7, 8});
		assertInternalState(0, 8);
		
		//release spills
		pool.release(8);
		assertInternalState(1, 5);
		pool.release(new Integer[] {9, 10, 11});
		assertInternalState(1, 8);
	}
	
	private void assertInternalState(int global, int local) {
		assertEquals(global, pool.global.size());
		assertEquals(local, pool.local.get().size());
	}
}
