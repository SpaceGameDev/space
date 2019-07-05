package space.engine.observable;

import org.junit.Test;
import space.engine.event.EventEntry;
import space.engine.sync.barrier.Barrier;
import space.engine.sync.barrier.BarrierImpl;

import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class ObservableReferenceTest {
	
	ObservableReference<Integer> reference = new ObservableReference<>();
	
	@Test
	public void testBasics() {
		assertNull(reference.assertGet());
		
		reference.set(1).awaitUninterrupted();
		assertEquals((Integer) 1, reference.assertGet());
		
		reference.set(2).awaitUninterrupted();
		assertEquals((Integer) 2, reference.assertGet());
	}
	
	@Test
	public void testEventCallback() {
		Integer[] lastCallback = new Integer[1];
		reference.addHook(i -> lastCallback[0] = i);
		
		assertNull(reference.assertGet());
		
		reference.set(1).awaitUninterrupted();
		assertEquals((Integer) 1, lastCallback[0]);
		lastCallback[0] = null;
		
		reference.set(2).awaitUninterrupted();
		assertEquals((Integer) 2, lastCallback[0]);
		lastCallback[0] = null;
	}
	
	@Test
	public void testOrderingGuarantee() {
		BarrierImpl[] callbackWait = new BarrierImpl[1];
		reference.addHook(i -> {
			if (callbackWait[0] != null)
				callbackWait[0].awaitUninterrupted();
		});
		
		assertNull(reference.assertGet());
		
		callbackWait[0] = new BarrierImpl();
		Barrier setTo1 = reference.set(1);
		assertFalse(setTo1.isFinished());
		callbackWait[0].triggerNow();
		setTo1.awaitUninterrupted();
		assertEquals((Integer) 1, reference.assertGet());
		
		callbackWait[0] = new BarrierImpl();
		Barrier setTo2 = reference.set(2);
		assertFalse(setTo2.isFinished());
		callbackWait[0].triggerNow();
		setTo2.awaitUninterrupted();
		assertEquals((Integer) 2, reference.assertGet());
	}
	
	@Test
	public void testCanceling() {
		reference.set(3).awaitUninterrupted();
		
		BarrierImpl[] callbackNotify = IntStream.range(0, 4).mapToObj(i -> new BarrierImpl()).toArray(BarrierImpl[]::new);
		BarrierImpl[] callbackWait = IntStream.range(0, 4).mapToObj(i -> new BarrierImpl()).toArray(BarrierImpl[]::new);
		callbackWait[3].triggerNow();
		reference.addHook(new EventEntry<>(i -> {
			callbackNotify[i].triggerNow();
			callbackWait[i].awaitUninterrupted();
		})).awaitUninterrupted();
		
		Barrier setTo0 = reference.set(0);
		callbackNotify[0].awaitUninterrupted();
		Barrier setTo1 = reference.set(1);
		Barrier setTo2 = reference.set(2);
		
		assertFalse(setTo0.isFinished());
		assertFalse(setTo1.isFinished());
		assertFalse(setTo2.isFinished());
		assertTrue(callbackNotify[0].isFinished());
		assertFalse(callbackNotify[1].isFinished());
		assertFalse(callbackNotify[2].isFinished());
		
		//these indexes are weird but correct!
		callbackWait[0].triggerNow();
		setTo1.awaitUninterrupted();
		callbackNotify[2].awaitUninterrupted();
		
		assertTrue(setTo0.isFinished());
		assertTrue(setTo1.isFinished());
		assertFalse(setTo2.isFinished());
		assertTrue(callbackNotify[0].isFinished());
		assertFalse(callbackNotify[1].isFinished()); //cancelled -> not called -> false
		assertTrue(callbackNotify[2].isFinished());
		
		callbackWait[2].triggerNow();
		setTo2.awaitUninterrupted();
		
		assertTrue(setTo0.isFinished());
		assertTrue(setTo1.isFinished());
		assertTrue(setTo2.isFinished());
		assertTrue(callbackNotify[0].isFinished());
		assertFalse(callbackNotify[1].isFinished()); //cancelled -> not called -> false
		assertTrue(callbackNotify[2].isFinished());
	}
	
	@Test
	@SuppressWarnings("deprecation")
	public void testGeneratingReference() {
		ObservableReference<Integer> a = new ObservableReference<>(1);
		ObservableReference<Integer> b = new ObservableReference<>(2);
		ObservableReference<Integer> res = ObservableReference.generatingReference(() -> a.assertGet() + b.assertGet(), a, b);
		
		a.set(2).awaitUninterrupted();
		res.getLatestBarrier().awaitUninterrupted();
		assertEquals((Integer) 4, res.assertGet());
		a.set(3).awaitUninterrupted();
		res.getLatestBarrier().awaitUninterrupted();
		assertEquals((Integer) 5, res.assertGet());
		b.set(3).awaitUninterrupted();
		res.getLatestBarrier().awaitUninterrupted();
		assertEquals((Integer) 6, res.assertGet());
	}
}
