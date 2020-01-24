package space.engine.observable;

import org.junit.Test;
import space.engine.barrier.Barrier;
import space.engine.barrier.BarrierImpl;
import space.engine.barrier.DelayTask;
import space.engine.event.EventEntry;

import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class MutableObservableReferenceTest {
	
	MutableObservableReference<Integer> reference = new MutableObservableReference<>();
	
	@Test
	public void testBasics() {
		assertFalse(reference.future().isDone());
		
		reference.set(1).awaitUninterrupted();
		assertEquals((Integer) 1, reference.assertGet());
		
		reference.set(2).awaitUninterrupted();
		assertEquals((Integer) 2, reference.assertGet());
	}
	
	@Test
	public void testEventCallback() {
		Integer[] lastCallback = new Integer[1];
		reference.addHook(i -> lastCallback[0] = i);
		
		assertFalse(reference.future().isDone());
		
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
				throw new DelayTask(callbackWait[0]);
		});
		
		assertFalse(reference.future().isDone());
		
		callbackWait[0] = new BarrierImpl();
		Barrier setTo1 = reference.set(1);
		assertFalse(setTo1.isDone());
		callbackWait[0].triggerNow();
		setTo1.awaitUninterrupted();
		assertEquals((Integer) 1, reference.assertGet());
		
		callbackWait[0] = new BarrierImpl();
		Barrier setTo2 = reference.set(2);
		assertFalse(setTo2.isDone());
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
			throw new DelayTask(callbackWait[i]);
		})).awaitUninterrupted();
		
		Barrier setTo0 = reference.setMayCancel(0);
		callbackNotify[0].awaitUninterrupted();
		Barrier setTo1 = reference.setMayCancel(1);
		Barrier setTo2 = reference.setMayCancel(2);
		
		assertFalse(setTo0.isDone());
		assertFalse(setTo1.isDone());
		assertFalse(setTo2.isDone());
		assertTrue(callbackNotify[0].isDone());
		assertFalse(callbackNotify[1].isDone());
		assertFalse(callbackNotify[2].isDone());
		
		//these indexes are weird but correct!
		callbackWait[0].triggerNow();
		setTo1.awaitUninterrupted();
		callbackNotify[2].awaitUninterrupted();
		
		assertTrue(setTo0.isDone());
		assertTrue(setTo1.isDone());
		assertFalse(setTo2.isDone());
		assertTrue(callbackNotify[0].isDone());
		assertFalse(callbackNotify[1].isDone()); //cancelled -> not called -> false
		assertTrue(callbackNotify[2].isDone());
		
		callbackWait[2].triggerNow();
		setTo2.awaitUninterrupted();
		
		assertTrue(setTo0.isDone());
		assertTrue(setTo1.isDone());
		assertTrue(setTo2.isDone());
		assertTrue(callbackNotify[0].isDone());
		assertFalse(callbackNotify[1].isDone()); //cancelled -> not called -> false
		assertTrue(callbackNotify[2].isDone());
	}
}
