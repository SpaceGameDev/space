package space.engine.barrier.entity;

import org.junit.Test;
import space.engine.barrier.Barrier;
import space.engine.barrier.future.Future;

import static org.junit.Assert.assertEquals;
import static space.engine.barrier.Barrier.*;

public class EntityExampleConcept {
	
	@Test(timeout = 250L)
	public void countingEntityConcept() {
		class CountingEntity extends AbstractEntity {
			
			int count;
		}
		
		EntityRef<CountingEntity> entity1 = EntityRef.createRef(new CountingEntity());
		EntityRef<CountingEntity> entity2 = EntityRef.createRef(new CountingEntity());
		
		Barrier setup = when(
				nowLockEntity(entity1, ea -> ea.runOn(e -> e.count = 50)),
				nowLockEntity(entity2, ea -> ea.runOn(e -> e.count = 50))
		);
		
		Barrier transaction1 = when(
				setup.thenLockEntity(entity1, entity2, (ea1, ea2) -> when(
						ea1.runOn(e -> e.count--),
						ea2.runOn(e -> e.count++)
				))
		);
		
		Future<Integer> transaction2 = when(
				transaction1.thenLockEntity(entity1, entity2, (ea1, ea2) -> {
					//ERROR: can't cast this!
					Future<Integer> entity1Count = ea1.startOn(e -> nowFuture(() -> e.count), Future.delegate());
					
					Barrier transaction = entity1Count.thenStart(() -> when(
							ea1.runOn(e -> e.count -= entity1Count.assertGet()),
							ea2.runOn(e -> e.count += entity1Count.assertGet())
					));
					
					return transaction.toFuture(entity1Count);
				}, Future.delegate())
		);
		
		Future<String> printTotal = when(transaction2.thenStart(() -> {
			Future<Integer> entity1Total = nowLockEntityAndFutureOn(entity1, e -> e.count);
			Future<Integer> entity2Total = nowLockEntityAndFutureOn(entity2, e -> e.count);
			return when(entity1Total, entity2Total).thenFuture(() -> entity1Total.assertGet() + " - " + entity2Total.assertGet());
		}, Future.delegate()));
		
		transaction2.awaitGetUninterrupted();
		assertEquals(49, (int) transaction2.assertGet());
		
		printTotal.awaitUninterrupted();
		assertEquals("0 - 100", printTotal.assertGet());
	}
}
