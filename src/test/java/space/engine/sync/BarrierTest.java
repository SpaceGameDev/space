package space.engine.sync;

import org.junit.Test;
import space.engine.sync.barrier.Barrier;
import space.engine.sync.barrier.BarrierImpl;
import space.engine.sync.future.Future;

import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class BarrierTest {
	
	@Test
	public void testBarrierImpl() {
		BarrierImpl barrier = new BarrierImpl();
		assertFalse(barrier.isFinished());
		barrier.triggerNow();
		assertTrue(barrier.isFinished());
	}
	
	@Test
	public void testBarrierImplHooks() {
		BarrierImpl barrier = new BarrierImpl();
		
		boolean[] called = new boolean[1];
		barrier.addHook(() -> called[0] = true);
		
		assertFalse(barrier.isFinished());
		assertFalse(called[0]);
		barrier.triggerNow();
		assertTrue(barrier.isFinished());
		assertTrue(called[0]);
	}
	
	@Test
	public void testBarrierAwaitAll() {
		BarrierImpl[] barriers = IntStream
				.range(0, 5)
				.mapToObj(i -> new BarrierImpl())
				.toArray(BarrierImpl[]::new);
		Barrier all = Barrier.awaitAll(barriers);
		
		for (BarrierImpl barrier : barriers) {
			assertFalse(all.isFinished());
			barrier.triggerNow();
		}
		assertTrue(all.isFinished());
	}
	
	@Test
	public void testBarrierInnerBarrier() {
		BarrierImpl outer = new BarrierImpl();
		BarrierImpl inner = new BarrierImpl();
		Future<Barrier> future = outer.toFuture(() -> inner);
		Barrier all = Barrier.innerBarrier(future);
		
		assertFalse(outer.isFinished());
		assertFalse(inner.isFinished());
		assertFalse(all.isFinished());
		
		outer.triggerNow();
		assertTrue(outer.isFinished());
		assertFalse(inner.isFinished());
		assertFalse(all.isFinished());
		
		inner.triggerNow();
		assertTrue(outer.isFinished());
		assertTrue(inner.isFinished());
		assertTrue(all.isFinished());
	}
	
	@Test
	public void testBarrierInnerBarrierReversedOrder() {
		BarrierImpl outer = new BarrierImpl();
		BarrierImpl inner = new BarrierImpl();
		Future<Barrier> future = outer.toFuture(() -> inner);
		Barrier all = Barrier.innerBarrier(future);
		
		assertFalse(outer.isFinished());
		assertFalse(inner.isFinished());
		assertFalse(all.isFinished());
		
		inner.triggerNow();
		assertFalse(outer.isFinished());
		assertTrue(inner.isFinished());
		assertFalse(all.isFinished());
		
		outer.triggerNow();
		assertTrue(outer.isFinished());
		assertTrue(inner.isFinished());
		assertTrue(all.isFinished());
	}
}
