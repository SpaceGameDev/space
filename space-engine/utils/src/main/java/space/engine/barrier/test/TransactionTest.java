package space.engine.barrier.test;

import org.jetbrains.annotations.NotNull;
import space.engine.Side;
import space.engine.barrier.Barrier;
import space.engine.barrier.BarrierImpl;
import space.engine.barrier.lock.SyncLock;
import space.engine.barrier.lock.SyncLockImpl;
import space.engine.simpleQueue.pool.Executor;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static space.engine.Side.pool;
import static space.engine.barrier.Barrier.*;

/**
 * To make this work: add the following lines to the top of
 * SyncLock.acquireLocks(SyncLock[] locks, int exceptLock, Runnable callback):
 */
//		if(locks.length == 0) {
//			callback.run();
//			return true;
//		}
//
//	+	if(TransactionTest.COUNTER != null)
//	+		TransactionTest.COUNTER.incrementAndGet();
//
//		int i;
//		boolean success = true;
public class TransactionTest {
	
	public static int[] TRANSACTION_COUNT = new int[] {2, 4, 6, 8, 10, 15, 20, 50, 100, 500, 1000, 5000, 10000};
	public static boolean FANCY_PRINTOUT = false;
	
	public static AtomicInteger COUNTER;
	
	public static class Entity implements Executor {
		
		public SyncLock lock = new SyncLockImpl();
		public int count = 0;
		
		@Override
		public void execute(@NotNull Runnable command) {
			pool().execute(command);
		}
	}
	
	public static void main(String[] args) throws InterruptedException {
		try {
			System.out.print(""); //initialization
			
			//run
			for (int i : TRANSACTION_COUNT) {
				run(i);
			}
		} finally {
			Side.exit().awaitUninterrupted();
		}
	}
	
	public static void run(int transactionCount) throws InterruptedException {
		COUNTER = new AtomicInteger();
		Entity entity1 = new Entity();
		Entity entity2 = new Entity();
		
		BarrierImpl start = new BarrierImpl();
		if (FANCY_PRINTOUT)
			System.out.println(transactionCount + " Transactions: submitting");
		Barrier task = start.thenStart(() -> when(
				IntStream.range(0, transactionCount)
						 .mapToObj(i -> i % 2 == 0 ? transaction(entity1, entity2, 1) : transaction(entity2, entity1, 1))
		));
		if (FANCY_PRINTOUT)
			System.out.println(transactionCount + " Transactions: launching");
		start.triggerNow();
		if (FANCY_PRINTOUT)
			System.out.println(transactionCount + " Transactions: awaiting");
		task.await();
		if (FANCY_PRINTOUT)
			System.out.println(transactionCount + " Transactions: done!");
		
		if (transactionCount % 2 == 0) {
			if (!(entity1.count == 0 && entity2.count == 0))
				throw new RuntimeException("Transaction result invalid: " + entity1.count + " - " + entity2.count + " have to be 0 - 0");
		} else {
			if (!(entity1.count == -1 && entity2.count == 1))
				throw new RuntimeException("Transaction result invalid: " + entity1.count + " - " + entity2.count + " have to be -1 - 1");
		}
		
		int count = COUNTER.get();
		if (FANCY_PRINTOUT)
			System.out.println(transactionCount + " Transactions: " + count + " calls to acquireLocks(). That's " + ((double) count / transactionCount) + " times!");
		else
			System.out.println(count + "\t" + Double.toString((double) count / transactionCount).replace('.', ','));
	}
	
	public static Barrier transaction(Entity from, Entity to, int delta) {
		return nowLock(new SyncLock[] {from.lock, to.lock}, () ->
				when(
						nowRun(from, () -> from.count -= delta),
						nowRun(to, () -> to.count += delta)
				)
		);
	}
}
