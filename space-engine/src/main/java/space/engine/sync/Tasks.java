package space.engine.sync;

import space.engine.baseobject.CanceledCheck;
import space.engine.sync.barrier.Barrier;
import space.engine.sync.barrier.BarrierImpl;
import space.engine.sync.barrier.CancelableBarrier;
import space.engine.sync.future.Future;
import space.engine.sync.future.FutureWithException;
import space.engine.sync.future.FutureWithException.FutureWith2Exception;
import space.engine.sync.future.FutureWithException.FutureWith3Exception;
import space.engine.sync.future.FutureWithException.FutureWith4Exception;
import space.engine.sync.future.FutureWithException.FutureWith5Exception;
import space.engine.sync.lock.SyncLock;
import space.engine.sync.taskImpl.FutureTask;
import space.engine.sync.taskImpl.FutureTaskWithException;
import space.engine.sync.taskImpl.FutureTaskWithException.FutureTaskWith2Exception;
import space.engine.sync.taskImpl.FutureTaskWithException.FutureTaskWith3Exception;
import space.engine.sync.taskImpl.FutureTaskWithException.FutureTaskWith4Exception;
import space.engine.sync.taskImpl.FutureTaskWithException.FutureTaskWith5Exception;
import space.engine.sync.taskImpl.MultiTask;
import space.engine.sync.taskImpl.RunnableCancelableTask;
import space.engine.sync.taskImpl.RunnableTask;

import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.function.Function;

import static space.engine.ArrayUtils.mergeIfNeeded;
import static space.engine.Side.pool;
import static space.engine.sync.barrier.Barrier.EMPTY_BARRIER_ARRAY;
import static space.engine.sync.lock.SyncLock.EMPTY_SYNCLOCK_ARRAY;

public class Tasks {
	
	private Tasks() {
	}
	
	//Runnable
	@FunctionalInterface
	public interface RunnableWithDelay {
		
		void run() throws DelayTask;
	}
	
	public static TaskCreator<? extends Barrier> runnable(RunnableWithDelay run) {
		return runnable(pool(), EMPTY_SYNCLOCK_ARRAY, EMPTY_BARRIER_ARRAY, run);
	}
	
	public static TaskCreator<? extends Barrier> runnable(Barrier[] staticBarriers, RunnableWithDelay run) {
		return runnable(pool(), EMPTY_SYNCLOCK_ARRAY, staticBarriers, run);
	}
	
	public static TaskCreator<? extends Barrier> runnable(SyncLock[] staticLocks, Barrier[] staticBarriers, RunnableWithDelay run) {
		return runnable(pool(), staticLocks, staticBarriers, run);
	}
	
	public static TaskCreator<? extends Barrier> runnable(Executor exec, Barrier[] staticBarriers, RunnableWithDelay run) {
		return runnable(exec, EMPTY_SYNCLOCK_ARRAY, staticBarriers, run);
	}
	
	public static TaskCreator<? extends Barrier> runnable(Executor exec, RunnableWithDelay run) {
		return runnable(exec, EMPTY_SYNCLOCK_ARRAY, EMPTY_BARRIER_ARRAY, run);
	}
	
	public static TaskCreator<? extends Barrier> runnable(Executor exec, SyncLock[] staticLocks, Barrier[] staticBarriers, RunnableWithDelay run) {
		return (locks, barriers) -> new RunnableTask(mergeIfNeeded(SyncLock[]::new, staticLocks, locks), mergeIfNeeded(Barrier[]::new, staticBarriers, barriers)) {
			@Override
			protected synchronized void submit1(Runnable toRun) {
				exec.execute(toRun);
			}
			
			@Override
			protected void execute() throws DelayTask {
				run.run();
			}
		};
	}
	
	//Future
	@FunctionalInterface
	public interface SupplierWithDelay<T> {
		
		T get() throws DelayTask;
	}
	
	public static <R> TaskCreator<? extends Future<R>> future(SupplierWithDelay<R> run) {
		return future(pool(), EMPTY_SYNCLOCK_ARRAY, EMPTY_BARRIER_ARRAY, run);
	}
	
	public static <R> TaskCreator<? extends Future<R>> future(Barrier[] staticBarriers, SupplierWithDelay<R> run) {
		return future(pool(), EMPTY_SYNCLOCK_ARRAY, staticBarriers, run);
	}
	
	public static <R> TaskCreator<? extends Future<R>> future(SyncLock[] staticLocks, Barrier[] staticBarriers, SupplierWithDelay<R> run) {
		return future(pool(), staticLocks, staticBarriers, run);
	}
	
	public static <R> TaskCreator<? extends Future<R>> future(Executor exec, SupplierWithDelay<R> run) {
		return future(exec, EMPTY_SYNCLOCK_ARRAY, EMPTY_BARRIER_ARRAY, run);
	}
	
	public static <R> TaskCreator<? extends Future<R>> future(Executor exec, Barrier[] staticBarriers, SupplierWithDelay<R> run) {
		return future(exec, EMPTY_SYNCLOCK_ARRAY, staticBarriers, run);
	}
	
	public static <R> TaskCreator<? extends Future<R>> future(Executor exec, SyncLock[] staticLocks, Barrier[] staticBarriers, SupplierWithDelay<R> run) {
		return (locks, barriers) -> new FutureTask<>(mergeIfNeeded(SyncLock[]::new, staticLocks, locks), mergeIfNeeded(Barrier[]::new, staticBarriers, barriers)) {
			@Override
			protected synchronized void submit1(Runnable toRun) {
				exec.execute(toRun);
			}
			
			@Override
			protected R execute0() throws DelayTask {
				return run.get();
			}
		};
	}
	
	//FutureWithException
	@FunctionalInterface
	public interface SupplierWithDelayAndException<T, EX extends Throwable> {
		
		T get() throws DelayTask, EX;
	}
	
	public static <R, EX extends Throwable> TaskCreator<? extends FutureWithException<R, EX>> futureWithException(Class<EX> exceptionClass, SupplierWithDelayAndException<R, EX> run) {
		return futureWithException(exceptionClass, pool(), EMPTY_SYNCLOCK_ARRAY, EMPTY_BARRIER_ARRAY, run);
	}
	
	public static <R, EX extends Throwable> TaskCreator<? extends FutureWithException<R, EX>> futureWithException(Class<EX> exceptionClass, Barrier[] staticBarriers, SupplierWithDelayAndException<R, EX> run) {
		return futureWithException(exceptionClass, pool(), EMPTY_SYNCLOCK_ARRAY, staticBarriers, run);
	}
	
	public static <R, EX extends Throwable> TaskCreator<? extends FutureWithException<R, EX>> futureWithException(Class<EX> exceptionClass, SyncLock[] staticLocks, Barrier[] staticBarriers, SupplierWithDelayAndException<R, EX> run) {
		return futureWithException(exceptionClass, pool(), staticLocks, staticBarriers, run);
	}
	
	public static <R, EX extends Throwable> TaskCreator<? extends FutureWithException<R, EX>> futureWithException(Class<EX> exceptionClass, Executor exec, SupplierWithDelayAndException<R, EX> run) {
		return futureWithException(exceptionClass, exec, EMPTY_SYNCLOCK_ARRAY, EMPTY_BARRIER_ARRAY, run);
	}
	
	public static <R, EX extends Throwable> TaskCreator<? extends FutureWithException<R, EX>> futureWithException(Class<EX> exceptionClass, Executor exec, Barrier[] staticBarriers, SupplierWithDelayAndException<R, EX> run) {
		return futureWithException(exceptionClass, exec, EMPTY_SYNCLOCK_ARRAY, staticBarriers, run);
	}
	
	public static <R, EX extends Throwable> TaskCreator<? extends FutureWithException<R, EX>> futureWithException(Class<EX> exceptionClass, Executor exec, SyncLock[] staticLocks, Barrier[] staticBarriers, SupplierWithDelayAndException<R, EX> run) {
		return (locks, barriers) -> new FutureTaskWithException<>(exceptionClass, mergeIfNeeded(SyncLock[]::new, staticLocks, locks), mergeIfNeeded(Barrier[]::new, staticBarriers, barriers)) {
			@Override
			protected synchronized void submit1(Runnable toRun) {
				exec.execute(toRun);
			}
			
			@Override
			protected R execute0() throws DelayTask, EX {
				return run.get();
			}
		};
	}
	
	//FutureWith2Exception
	@FunctionalInterface
	public interface SupplierWithDelayAnd2Exception<T, EX1 extends Throwable, EX2 extends Throwable> {
		
		T get() throws DelayTask, EX1, EX2;
	}
	
	public static <R, EX1 extends Throwable, EX2 extends Throwable> TaskCreator<? extends FutureWith2Exception<R, EX1, EX2>> futureWith2Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, SupplierWithDelayAnd2Exception<R, EX1, EX2> run) {
		return futureWith2Exception(exceptionClass1, exceptionClass2, pool(), EMPTY_SYNCLOCK_ARRAY, EMPTY_BARRIER_ARRAY, run);
	}
	
	public static <R, EX1 extends Throwable, EX2 extends Throwable> TaskCreator<? extends FutureWith2Exception<R, EX1, EX2>> futureWith2Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Barrier[] staticBarriers, SupplierWithDelayAnd2Exception<R, EX1, EX2> run) {
		return futureWith2Exception(exceptionClass1, exceptionClass2, pool(), EMPTY_SYNCLOCK_ARRAY, staticBarriers, run);
	}
	
	public static <R, EX1 extends Throwable, EX2 extends Throwable> TaskCreator<? extends FutureWith2Exception<R, EX1, EX2>> futureWith2Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, SyncLock[] staticLocks, Barrier[] staticBarriers, SupplierWithDelayAnd2Exception<R, EX1, EX2> run) {
		return futureWith2Exception(exceptionClass1, exceptionClass2, pool(), staticLocks, staticBarriers, run);
	}
	
	public static <R, EX1 extends Throwable, EX2 extends Throwable> TaskCreator<? extends FutureWith2Exception<R, EX1, EX2>> futureWith2Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Executor exec, SupplierWithDelayAnd2Exception<R, EX1, EX2> run) {
		return futureWith2Exception(exceptionClass1, exceptionClass2, exec, EMPTY_SYNCLOCK_ARRAY, EMPTY_BARRIER_ARRAY, run);
	}
	
	public static <R, EX1 extends Throwable, EX2 extends Throwable> TaskCreator<? extends FutureWith2Exception<R, EX1, EX2>> futureWith2Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Executor exec, Barrier[] staticBarriers, SupplierWithDelayAnd2Exception<R, EX1, EX2> run) {
		return futureWith2Exception(exceptionClass1, exceptionClass2, exec, EMPTY_SYNCLOCK_ARRAY, staticBarriers, run);
	}
	
	public static <R, EX1 extends Throwable, EX2 extends Throwable> TaskCreator<? extends FutureWith2Exception<R, EX1, EX2>> futureWith2Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Executor exec, SyncLock[] staticLocks, Barrier[] staticBarriers, SupplierWithDelayAnd2Exception<R, EX1, EX2> run) {
		return (locks, barriers) -> new FutureTaskWith2Exception<>(exceptionClass1,
																   exceptionClass2,
																   mergeIfNeeded(SyncLock[]::new, staticLocks, locks),
																   mergeIfNeeded(Barrier[]::new, staticBarriers, barriers)) {
			@Override
			protected synchronized void submit1(Runnable toRun) {
				exec.execute(toRun);
			}
			
			@Override
			protected R execute0() throws DelayTask, EX1, EX2 {
				return run.get();
			}
		};
	}
	
	//FutureWith3Exception
	@FunctionalInterface
	public interface SupplierWithDelayAnd3Exception<T, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable> {
		
		T get() throws DelayTask, EX1, EX2, EX3;
	}
	
	public static <R, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable> TaskCreator<? extends FutureWith3Exception<R, EX1, EX2, EX3>> futureWith3Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Class<EX3> exceptionClass3, SupplierWithDelayAnd3Exception<R, EX1, EX2, EX3> run) {
		return futureWith3Exception(exceptionClass1, exceptionClass2, exceptionClass3, pool(), EMPTY_SYNCLOCK_ARRAY, EMPTY_BARRIER_ARRAY, run);
	}
	
	public static <R, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable> TaskCreator<? extends FutureWith3Exception<R, EX1, EX2, EX3>> futureWith3Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Class<EX3> exceptionClass3, Barrier[] staticBarriers, SupplierWithDelayAnd3Exception<R, EX1, EX2, EX3> run) {
		return futureWith3Exception(exceptionClass1, exceptionClass2, exceptionClass3, pool(), EMPTY_SYNCLOCK_ARRAY, staticBarriers, run);
	}
	
	public static <R, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable> TaskCreator<? extends FutureWith3Exception<R, EX1, EX2, EX3>> futureWith3Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Class<EX3> exceptionClass3, SyncLock[] staticLocks, Barrier[] staticBarriers, SupplierWithDelayAnd3Exception<R, EX1, EX2, EX3> run) {
		return futureWith3Exception(exceptionClass1, exceptionClass2, exceptionClass3, pool(), staticLocks, staticBarriers, run);
	}
	
	public static <R, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable> TaskCreator<? extends FutureWith3Exception<R, EX1, EX2, EX3>> futureWith3Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Class<EX3> exceptionClass3, Executor exec, SupplierWithDelayAnd3Exception<R, EX1, EX2, EX3> run) {
		return futureWith3Exception(exceptionClass1, exceptionClass2, exceptionClass3, exec, EMPTY_SYNCLOCK_ARRAY, EMPTY_BARRIER_ARRAY, run);
	}
	
	public static <R, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable> TaskCreator<? extends FutureWith3Exception<R, EX1, EX2, EX3>> futureWith3Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Class<EX3> exceptionClass3, Executor exec, Barrier[] staticBarriers, SupplierWithDelayAnd3Exception<R, EX1, EX2, EX3> run) {
		return futureWith3Exception(exceptionClass1, exceptionClass2, exceptionClass3, exec, EMPTY_SYNCLOCK_ARRAY, staticBarriers, run);
	}
	
	public static <R, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable> TaskCreator<? extends FutureWith3Exception<R, EX1, EX2, EX3>> futureWith3Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Class<EX3> exceptionClass3, Executor exec, SyncLock[] staticLocks, Barrier[] staticBarriers, SupplierWithDelayAnd3Exception<R, EX1, EX2, EX3> run) {
		return (locks, barriers) -> new FutureTaskWith3Exception<>(exceptionClass1,
																   exceptionClass2,
																   exceptionClass3,
																   mergeIfNeeded(SyncLock[]::new, staticLocks, locks),
																   mergeIfNeeded(Barrier[]::new, staticBarriers, barriers)) {
			@Override
			protected synchronized void submit1(Runnable toRun) {
				exec.execute(toRun);
			}
			
			@Override
			protected R execute0() throws DelayTask, EX1, EX2, EX3 {
				return run.get();
			}
		};
	}
	
	//FutureWith4Exception
	@FunctionalInterface
	public interface SupplierWithDelayAnd4Exception<T, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable, EX4 extends Throwable> {
		
		T get() throws DelayTask, EX1, EX2, EX3, EX4;
	}
	
	public static <R, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable, EX4 extends Throwable> TaskCreator<? extends FutureWith4Exception<R, EX1, EX2, EX3, EX4>> futureWith4Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Class<EX3> exceptionClass3, Class<EX4> exceptionClass4, SupplierWithDelayAnd4Exception<R, EX1, EX2, EX3, EX4> run) {
		return futureWith4Exception(exceptionClass1, exceptionClass2, exceptionClass3, exceptionClass4, pool(), EMPTY_SYNCLOCK_ARRAY, EMPTY_BARRIER_ARRAY, run);
	}
	
	public static <R, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable, EX4 extends Throwable> TaskCreator<? extends FutureWith4Exception<R, EX1, EX2, EX3, EX4>> futureWith4Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Class<EX3> exceptionClass3, Class<EX4> exceptionClass4, Barrier[] staticBarriers, SupplierWithDelayAnd4Exception<R, EX1, EX2, EX3, EX4> run) {
		return futureWith4Exception(exceptionClass1, exceptionClass2, exceptionClass3, exceptionClass4, pool(), EMPTY_SYNCLOCK_ARRAY, staticBarriers, run);
	}
	
	public static <R, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable, EX4 extends Throwable> TaskCreator<? extends FutureWith4Exception<R, EX1, EX2, EX3, EX4>> futureWith4Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Class<EX3> exceptionClass3, Class<EX4> exceptionClass4, SyncLock[] staticLocks, Barrier[] staticBarriers, SupplierWithDelayAnd4Exception<R, EX1, EX2, EX3, EX4> run) {
		return futureWith4Exception(exceptionClass1, exceptionClass2, exceptionClass3, exceptionClass4, pool(), staticLocks, staticBarriers, run);
	}
	
	public static <R, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable, EX4 extends Throwable> TaskCreator<? extends FutureWith4Exception<R, EX1, EX2, EX3, EX4>> futureWith4Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Class<EX3> exceptionClass3, Class<EX4> exceptionClass4, Executor exec, SupplierWithDelayAnd4Exception<R, EX1, EX2, EX3, EX4> run) {
		return futureWith4Exception(exceptionClass1, exceptionClass2, exceptionClass3, exceptionClass4, exec, EMPTY_SYNCLOCK_ARRAY, EMPTY_BARRIER_ARRAY, run);
	}
	
	public static <R, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable, EX4 extends Throwable> TaskCreator<? extends FutureWith4Exception<R, EX1, EX2, EX3, EX4>> futureWith4Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Class<EX3> exceptionClass3, Class<EX4> exceptionClass4, Executor exec, Barrier[] staticBarriers, SupplierWithDelayAnd4Exception<R, EX1, EX2, EX3, EX4> run) {
		return futureWith4Exception(exceptionClass1, exceptionClass2, exceptionClass3, exceptionClass4, exec, EMPTY_SYNCLOCK_ARRAY, staticBarriers, run);
	}
	
	public static <R, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable, EX4 extends Throwable> TaskCreator<? extends FutureWith4Exception<R, EX1, EX2, EX3, EX4>> futureWith4Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Class<EX3> exceptionClass3, Class<EX4> exceptionClass4, Executor exec, SyncLock[] staticLocks, Barrier[] staticBarriers, SupplierWithDelayAnd4Exception<R, EX1, EX2, EX3, EX4> run) {
		return (locks, barriers) -> new FutureTaskWith4Exception<>(exceptionClass1,
																   exceptionClass2,
																   exceptionClass3,
																   exceptionClass4,
																   mergeIfNeeded(SyncLock[]::new, staticLocks, locks),
																   mergeIfNeeded(Barrier[]::new, staticBarriers, barriers)) {
			@Override
			protected synchronized void submit1(Runnable toRun) {
				exec.execute(toRun);
			}
			
			@Override
			protected R execute0() throws DelayTask, EX1, EX2, EX3, EX4 {
				return run.get();
			}
		};
	}
	
	//FutureWith5Exception
	@FunctionalInterface
	public interface SupplierWithDelayAnd5Exception<T, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable, EX4 extends Throwable, EX5 extends Throwable> {
		
		T get() throws DelayTask, EX1, EX2, EX3, EX4, EX5;
	}
	
	public static <R, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable, EX4 extends Throwable, EX5 extends Throwable> TaskCreator<? extends FutureWith5Exception<R, EX1, EX2, EX3, EX4, EX5>> futureWith5Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Class<EX3> exceptionClass3, Class<EX4> exceptionClass4, Class<EX5> exceptionClass5, SupplierWithDelayAnd5Exception<R, EX1, EX2, EX3, EX4, EX5> run) {
		return futureWith5Exception(exceptionClass1, exceptionClass2, exceptionClass3, exceptionClass4, exceptionClass5, pool(), EMPTY_SYNCLOCK_ARRAY, EMPTY_BARRIER_ARRAY, run);
	}
	
	public static <R, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable, EX4 extends Throwable, EX5 extends Throwable> TaskCreator<? extends FutureWith5Exception<R, EX1, EX2, EX3, EX4, EX5>> futureWith5Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Class<EX3> exceptionClass3, Class<EX4> exceptionClass4, Class<EX5> exceptionClass5, Barrier[] staticBarriers, SupplierWithDelayAnd5Exception<R, EX1, EX2, EX3, EX4, EX5> run) {
		return futureWith5Exception(exceptionClass1, exceptionClass2, exceptionClass3, exceptionClass4, exceptionClass5, pool(), EMPTY_SYNCLOCK_ARRAY, staticBarriers, run);
	}
	
	public static <R, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable, EX4 extends Throwable, EX5 extends Throwable> TaskCreator<? extends FutureWith5Exception<R, EX1, EX2, EX3, EX4, EX5>> futureWith5Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Class<EX3> exceptionClass3, Class<EX4> exceptionClass4, Class<EX5> exceptionClass5, SyncLock[] staticLocks, Barrier[] staticBarriers, SupplierWithDelayAnd5Exception<R, EX1, EX2, EX3, EX4, EX5> run) {
		return futureWith5Exception(exceptionClass1, exceptionClass2, exceptionClass3, exceptionClass4, exceptionClass5, pool(), staticLocks, staticBarriers, run);
	}
	
	public static <R, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable, EX4 extends Throwable, EX5 extends Throwable> TaskCreator<? extends FutureWith5Exception<R, EX1, EX2, EX3, EX4, EX5>> futureWith5Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Class<EX3> exceptionClass3, Class<EX4> exceptionClass4, Class<EX5> exceptionClass5, Executor exec, SupplierWithDelayAnd5Exception<R, EX1, EX2, EX3, EX4, EX5> run) {
		return futureWith5Exception(exceptionClass1, exceptionClass2, exceptionClass3, exceptionClass4, exceptionClass5, exec, EMPTY_SYNCLOCK_ARRAY, EMPTY_BARRIER_ARRAY, run);
	}
	
	public static <R, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable, EX4 extends Throwable, EX5 extends Throwable> TaskCreator<? extends FutureWith5Exception<R, EX1, EX2, EX3, EX4, EX5>> futureWith5Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Class<EX3> exceptionClass3, Class<EX4> exceptionClass4, Class<EX5> exceptionClass5, Executor exec, Barrier[] staticBarriers, SupplierWithDelayAnd5Exception<R, EX1, EX2, EX3, EX4, EX5> run) {
		return futureWith5Exception(exceptionClass1, exceptionClass2, exceptionClass3, exceptionClass4, exceptionClass5, exec, EMPTY_SYNCLOCK_ARRAY, staticBarriers, run);
	}
	
	public static <R, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable, EX4 extends Throwable, EX5 extends Throwable> TaskCreator<? extends FutureWith5Exception<R, EX1, EX2, EX3, EX4, EX5>> futureWith5Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Class<EX3> exceptionClass3, Class<EX4> exceptionClass4, Class<EX5> exceptionClass5, Executor exec, SyncLock[] staticLocks, Barrier[] staticBarriers, SupplierWithDelayAnd5Exception<R, EX1, EX2, EX3, EX4, EX5> run) {
		return (locks, barriers) -> new FutureTaskWith5Exception<>(exceptionClass1,
																   exceptionClass2,
																   exceptionClass3,
																   exceptionClass4,
																   exceptionClass5,
																   mergeIfNeeded(SyncLock[]::new, staticLocks, locks),
																   mergeIfNeeded(Barrier[]::new, staticBarriers, barriers)) {
			@Override
			protected synchronized void submit1(Runnable toRun) {
				exec.execute(toRun);
			}
			
			@Override
			protected R execute0() throws DelayTask, EX1, EX2, EX3, EX4, EX5 {
				return run.get();
			}
		};
	}
	
	//MultiTask custom
	public static TaskCreator<? extends Barrier> multiCustom(Function<Barrier, Barrier> setup) {
		return multiCustom(EMPTY_SYNCLOCK_ARRAY, EMPTY_BARRIER_ARRAY, setup);
	}
	
	public static TaskCreator<? extends Barrier> multiCustom(Barrier[] staticBarriers, Function<Barrier, Barrier> setup) {
		return multiCustom(EMPTY_SYNCLOCK_ARRAY, staticBarriers, setup);
	}
	
	public static TaskCreator<? extends Barrier> multiCustom(SyncLock[] staticLocks, Barrier[] staticBarriers, Function<Barrier, Barrier> setup) {
		return (locks, barriers) -> new MultiTask(mergeIfNeeded(SyncLock[]::new, staticLocks, locks), mergeIfNeeded(Barrier[]::new, staticBarriers, barriers)) {
			@Override
			protected Barrier setup(Barrier start) {
				return setup.apply(start);
			}
		};
	}
	
	//MultiTask sequential
	public static TaskCreator<? extends Barrier> sequential(Collection<? extends TaskCreator> tasks) {
		return sequential(EMPTY_SYNCLOCK_ARRAY, EMPTY_BARRIER_ARRAY, tasks);
	}
	
	public static TaskCreator<? extends Barrier> sequential(Barrier[] staticBarriers, Collection<? extends TaskCreator> tasks) {
		return sequential(EMPTY_SYNCLOCK_ARRAY, staticBarriers, tasks);
	}
	
	public static TaskCreator<? extends Barrier> sequential(SyncLock[] staticLocks, Barrier[] staticBarriers, Collection<? extends TaskCreator> tasks) {
		return (locks, barriers) -> new MultiTask(mergeIfNeeded(SyncLock[]::new, staticLocks, locks), mergeIfNeeded(Barrier[]::new, staticBarriers, barriers)) {
			@Override
			protected Barrier setup(Barrier start) {
				Barrier current = start;
				for (TaskCreator taskCreator : tasks) {
					current = taskCreator.submit(current);
				}
				return current;
			}
		};
	}
	
	//MultiTask parallel
	public static TaskCreator<? extends Barrier> parallel(Collection<? extends TaskCreator> tasks) {
		return parallel(EMPTY_SYNCLOCK_ARRAY, EMPTY_BARRIER_ARRAY, tasks);
	}
	
	public static TaskCreator<? extends Barrier> parallel(Barrier[] staticBarriers, Collection<? extends TaskCreator> tasks) {
		return parallel(EMPTY_SYNCLOCK_ARRAY, staticBarriers, tasks);
	}
	
	public static TaskCreator<? extends Barrier> parallel(SyncLock[] staticLocks, Barrier[] staticBarriers, Collection<? extends TaskCreator> tasks) {
		return (locks, barriers) -> new MultiTask(mergeIfNeeded(SyncLock[]::new, staticLocks, locks), mergeIfNeeded(Barrier[]::new, staticBarriers, barriers)) {
			@Override
			protected Barrier setup(Barrier start) {
				if (tasks.size() == 0) {
					return start;
				}
				
				//custom handler for fever object allocation
				BarrierImpl end = new BarrierImpl();
				//noinspection SuspiciousToArrayCall
				start.addHook(() -> Barrier.awaitAll(end::triggerNow, tasks.stream().map(TaskCreator::submit).toArray(Barrier[]::new)));
				return end;
			}
		};
	}
	
	//other functional interfaces with Delay
	@FunctionalInterface
	public interface ConsumerWithDelay<T> {
		
		void accept(T t) throws DelayTask;
	}
	
	@FunctionalInterface
	public interface FunctionWithDelay<T, R> {
		
		R apply(T t) throws DelayTask;
	}
	
	//RunnableCancelable using explicit cancel handling
	@FunctionalInterface
	public interface RunnableCancelableWithDelay {
		
		void run(CanceledCheck cancelCheck) throws DelayTask;
	}
	
	public static TaskCreator<? extends CancelableBarrier> runnableCancelable(RunnableCancelableWithDelay run) {
		return runnableCancelable(pool(), EMPTY_SYNCLOCK_ARRAY, EMPTY_BARRIER_ARRAY, run);
	}
	
	public static TaskCreator<? extends CancelableBarrier> runnableCancelable(Barrier[] staticBarriers, RunnableCancelableWithDelay run) {
		return runnableCancelable(pool(), EMPTY_SYNCLOCK_ARRAY, staticBarriers, run);
	}
	
	public static TaskCreator<? extends CancelableBarrier> runnableCancelable(SyncLock[] staticLocks, Barrier[] staticBarriers, RunnableCancelableWithDelay run) {
		return runnableCancelable(pool(), staticLocks, staticBarriers, run);
	}
	
	public static TaskCreator<? extends CancelableBarrier> runnableCancelable(Executor exec, Barrier[] staticBarriers, RunnableCancelableWithDelay run) {
		return runnableCancelable(exec, EMPTY_SYNCLOCK_ARRAY, staticBarriers, run);
	}
	
	public static TaskCreator<? extends CancelableBarrier> runnableCancelable(Executor exec, RunnableCancelableWithDelay run) {
		return runnableCancelable(exec, EMPTY_SYNCLOCK_ARRAY, EMPTY_BARRIER_ARRAY, run);
	}
	
	public static TaskCreator<? extends CancelableBarrier> runnableCancelable(Executor exec, SyncLock[] staticLocks, Barrier[] staticBarriers, RunnableCancelableWithDelay run) {
		return (locks, barriers) -> new RunnableCancelableTask(mergeIfNeeded(SyncLock[]::new, staticLocks, locks), mergeIfNeeded(Barrier[]::new, staticBarriers, barriers)) {
			@Override
			protected synchronized void submit1(Runnable toRun) {
				exec.execute(toRun);
			}
			
			@Override
			protected void execute() throws DelayTask {
				run.run(this);
			}
		};
	}
	
	//RunnableCancelableAutomatic using noop cancel handling
	public static TaskCreator<? extends CancelableBarrier> runnableCancelableAutomatic(RunnableWithDelay run) {
		return runnableCancelableAutomatic(pool(), EMPTY_SYNCLOCK_ARRAY, EMPTY_BARRIER_ARRAY, run);
	}
	
	public static TaskCreator<? extends CancelableBarrier> runnableCancelableAutomatic(Barrier[] staticBarriers, RunnableWithDelay run) {
		return runnableCancelableAutomatic(pool(), EMPTY_SYNCLOCK_ARRAY, staticBarriers, run);
	}
	
	public static TaskCreator<? extends CancelableBarrier> runnableCancelableAutomatic(SyncLock[] staticLocks, Barrier[] staticBarriers, RunnableWithDelay run) {
		return runnableCancelableAutomatic(pool(), staticLocks, staticBarriers, run);
	}
	
	public static TaskCreator<? extends CancelableBarrier> runnableCancelableAutomatic(Executor exec, Barrier[] staticBarriers, RunnableWithDelay run) {
		return runnableCancelableAutomatic(exec, EMPTY_SYNCLOCK_ARRAY, staticBarriers, run);
	}
	
	public static TaskCreator<? extends CancelableBarrier> runnableCancelableAutomatic(Executor exec, RunnableWithDelay run) {
		return runnableCancelableAutomatic(exec, EMPTY_SYNCLOCK_ARRAY, EMPTY_BARRIER_ARRAY, run);
	}
	
	public static TaskCreator<? extends CancelableBarrier> runnableCancelableAutomatic(Executor exec, SyncLock[] staticLocks, Barrier[] staticBarriers, RunnableWithDelay run) {
		return (locks, barriers) -> new RunnableCancelableTask(mergeIfNeeded(SyncLock[]::new, staticLocks, locks), mergeIfNeeded(Barrier[]::new, staticBarriers, barriers)) {
			@Override
			protected synchronized void submit1(Runnable toRun) {
				if (!isCanceled())
					exec.execute(toRun);
				else
					//the Runnable must be called, but is it's canceled it won't do anything and we save the overhead of submitting it into a queue
					toRun.run();
			}
			
			@Override
			protected void execute() throws DelayTask {
				if (!isCanceled())
					run.run();
			}
		};
	}
}
