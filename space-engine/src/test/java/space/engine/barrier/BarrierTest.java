package space.engine.barrier;

import org.junit.Test;
import space.engine.barrier.future.Future;

import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class BarrierTest {
	
	@Test
	public void testBarrierImpl() {
		BarrierImpl barrier = new BarrierImpl();
		assertFalse(barrier.isDone());
		barrier.triggerNow();
		assertTrue(barrier.isDone());
	}
	
	@Test
	public void testBarrierImplHooks() {
		BarrierImpl barrier = new BarrierImpl();
		
		boolean[] called = new boolean[1];
		barrier.addHook(() -> called[0] = true);
		
		assertFalse(barrier.isDone());
		assertFalse(called[0]);
		barrier.triggerNow();
		assertTrue(barrier.isDone());
		assertTrue(called[0]);
	}
	
	@Test
	public void testBarrierWhen() {
		BarrierImpl[] barriers = IntStream
				.range(0, 5)
				.mapToObj(i -> new BarrierImpl())
				.toArray(BarrierImpl[]::new);
		Barrier all = Barrier.when(barriers);
		
		for (BarrierImpl barrier : barriers) {
			assertFalse(all.isDone());
			barrier.triggerNow();
		}
		assertTrue(all.isDone());
	}
	
	@Test
	public void testBarrierInner() {
		BarrierImpl outer = new BarrierImpl();
		BarrierImpl inner = new BarrierImpl();
		Future<Barrier> future = outer.toFuture(() -> inner);
		Barrier all = Barrier.inner(future);
		
		assertFalse(outer.isDone());
		assertFalse(inner.isDone());
		assertFalse(all.isDone());
		
		outer.triggerNow();
		assertTrue(outer.isDone());
		assertFalse(inner.isDone());
		assertFalse(all.isDone());
		
		inner.triggerNow();
		assertTrue(outer.isDone());
		assertTrue(inner.isDone());
		assertTrue(all.isDone());
	}
	
	@Test
	public void testBarrierInnerReversedOrder() {
		BarrierImpl outer = new BarrierImpl();
		BarrierImpl inner = new BarrierImpl();
		Future<Barrier> future = outer.toFuture(() -> inner);
		Barrier all = Barrier.inner(future);
		
		assertFalse(outer.isDone());
		assertFalse(inner.isDone());
		assertFalse(all.isDone());
		
		inner.triggerNow();
		assertFalse(outer.isDone());
		assertTrue(inner.isDone());
		assertFalse(all.isDone());
		
		outer.triggerNow();
		assertTrue(outer.isDone());
		assertTrue(inner.isDone());
		assertTrue(all.isDone());
	}
}
