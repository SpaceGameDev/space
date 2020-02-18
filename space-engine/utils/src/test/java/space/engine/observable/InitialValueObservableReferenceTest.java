package space.engine.observable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import space.engine.barrier.BarrierImpl;
import space.engine.barrier.DelayTask;
import space.engine.barrier.future.FutureNotFinishedException;
import space.engine.observable.ObservableReference.Generator;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class InitialValueObservableReferenceTest {
	
	@Parameters
	@SuppressWarnings("rawtypes")
	public static List<Class<? extends ObservableReference>> getParams() {
		return List.of(StaticObservableReference.class, MutableObservableReference.class);
	}
	
	public final Class<? extends ObservableReference<?>> param;
	
	public InitialValueObservableReferenceTest(Class<? extends ObservableReference<?>> param) {
		this.param = param;
	}
	
	@Test
	public void testInitialValue() throws Exception {
		//noinspection unchecked
		ObservableReference<Integer> reference = (ObservableReference<Integer>) param.getConstructor(Object.class).newInstance(42);
		assertTrue(reference.future().isDone());
		assertEquals((Integer) 42, reference.future().assertGet());
		assertEquals((Integer) 42, reference.assertGet());
	}
	
	@Test
	public void testInitialGenerator() throws Exception {
		BarrierImpl start = new BarrierImpl();
		//noinspection unchecked
		ObservableReference<Integer> reference = (ObservableReference<Integer>) param.getConstructor(Generator.class).newInstance(((Generator<Integer>) (previous) -> {
			throw new DelayTask(start.toFuture(() -> 42));
		}));
		
		assertFalse(reference.future().isDone());
		try {
			reference.assertGet();
			throw new RuntimeException("No FutureNotFinishedException");
		} catch (FutureNotFinishedException ignored) {
		
		}
		
		start.triggerNow();
		assertEquals((Integer) 42, reference.future().awaitGet());
		assertEquals((Integer) 42, reference.future().assertGet());
		assertEquals((Integer) 42, reference.assertGet());
	}
}
