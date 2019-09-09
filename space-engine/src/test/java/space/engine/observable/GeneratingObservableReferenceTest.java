package space.engine.observable;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GeneratingObservableReferenceTest {
	
	@Test
	public void testGeneratingReference() {
		MutableObservableReference<Integer> a = new MutableObservableReference<>(1);
		MutableObservableReference<Integer> b = new MutableObservableReference<>(2);
		ObservableReference<Integer> res = GeneratingObservableReference.create(a, b, (o, o2, previous) -> o + o2);
		
		a.set(2).awaitUninterrupted();
		flush(res);
		assertEquals((Integer) 4, res.assertGet());
		a.set(3).awaitUninterrupted();
		flush(res);
		assertEquals((Integer) 5, res.assertGet());
		b.set(3).awaitUninterrupted();
		flush(res);
		assertEquals((Integer) 6, res.assertGet());
	}
	
	//specifically tests that the chain is not async because async overhead
	@Test
	public void testGeneratingReferenceChainNoAsync() throws InterruptedException {
		MutableObservableReference<Integer> start = new MutableObservableReference<>(0);
		final int CHAIN_SIZE = 10;
		
		ObservableReference<Integer> g = start;
		for (int i = 0; i < CHAIN_SIZE; i++)
			g = GeneratingObservableReference.create(g, (a, previous) -> a + 1);
		
		Thread[] callee = new Thread[1];
		g.addHook(integer -> callee[0] = Thread.currentThread()).barrier.awaitUninterrupted();
		
		for (int i = 0; i < 10; i++) {
			start.set(i);
			assertEquals(Thread.currentThread(), callee[0]);
			assertEquals(((Integer) (i + CHAIN_SIZE)), g.getFuture().awaitGet());
		}
	}
	
	@SuppressWarnings("deprecation")
	private void flush(ObservableReference<Integer> res) {
		res.getLatestBarrier().awaitUninterrupted();
	}
}
