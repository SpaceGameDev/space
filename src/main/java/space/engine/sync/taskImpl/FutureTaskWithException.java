package space.engine.sync.taskImpl;

import org.jetbrains.annotations.NotNull;
import space.engine.sync.DelayTask;
import space.engine.sync.barrier.Barrier;
import space.engine.sync.future.BaseFuture;
import space.engine.sync.future.FutureNotFinishedException;
import space.engine.sync.future.FutureWithException;
import space.engine.sync.lock.SyncLock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class FutureTaskWithException<R, EX extends Throwable> extends RunnableTask implements FutureWithException<R, EX> {
	
	public interface Callable<V> {
		
		V call() throws Throwable;
	}
	
	protected final Class<EX> exceptionClass;
	
	protected R ret;
	protected EX exception;
	
	public FutureTaskWithException(Class<EX> exceptionClass, @NotNull SyncLock[] locks, @NotNull Barrier[] barriers) {
		this(exceptionClass, locks);
		init(barriers);
	}
	
	/**
	 * REQUIRES calling {@link #init(Barrier[])} later to start execution
	 */
	protected FutureTaskWithException(Class<EX> exceptionClass, @NotNull SyncLock[] locks) {
		super(locks);
		this.exceptionClass = exceptionClass;
	}
	
	//execute
	@Override
	protected void execute() throws DelayTask {
		callAndStoreResult(this::execute0);
	}
	
	protected abstract R execute0() throws EX, DelayTask;
	
	@Override
	protected void executionFinished(Barrier awaitTask) {
		try {
			callAndStoreResult(() -> {
				if (awaitTask instanceof BaseFuture<?>)
					//noinspection unchecked
					return ((BaseFuture<R>) awaitTask).assertGetAnyException();
				return null;
			});
		} catch (DelayTask e) {
			throw new RuntimeException(e);
		}
		
		super.executionFinished(awaitTask);
	}
	
	protected void callAndStoreResult(Callable<R> callable) throws DelayTask {
		try {
			callable.call();
		} catch (DelayTask e) {
			throw e;
		} catch (RuntimeException | Error e) {
			if (exceptionClass.isInstance(e))
				exception = (EX) e;
			else
				throw e;
		} catch (Throwable e) {
			if (exceptionClass.isInstance(e))
				//noinspection unchecked
				exception = (EX) e;
			else
				throw new RuntimeException("Exception caught that cannot have been thrown", e);
		}
	}
	
	//get
	@Override
	public R awaitGet() throws InterruptedException, EX {
		await();
		return getInternal();
	}
	
	@Override
	public R awaitGet(long time, TimeUnit unit) throws InterruptedException, TimeoutException, EX {
		await(time, unit);
		return getInternal();
	}
	
	@Override
	public R assertGet() throws FutureNotFinishedException, EX {
		if (state != TaskState.FINISHED)
			throw new FutureNotFinishedException(this);
		return getInternal();
	}
	
	protected synchronized R getInternal() throws EX {
		if (exception != null)
			throw exception;
		return ret;
	}
	
	public static abstract class FutureTaskWith2Exception<R, EX1 extends Throwable, EX2 extends Throwable> extends RunnableTask implements FutureWith2Exception<R, EX1, EX2> {
		
		protected final Class<EX1> exceptionClass1;
		protected final Class<EX2> exceptionClass2;
		
		protected R ret;
		protected EX1 exception1;
		protected EX2 exception2;
		
		public FutureTaskWith2Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, @NotNull SyncLock[] locks, @NotNull Barrier[] barriers) {
			this(exceptionClass1, exceptionClass2, locks);
			init(barriers);
		}
		
		/**
		 * REQUIRES calling {@link #init(Barrier[])} later to start execution
		 */
		protected FutureTaskWith2Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, @NotNull SyncLock[] locks) {
			super(locks);
			this.exceptionClass1 = exceptionClass1;
			this.exceptionClass2 = exceptionClass2;
		}
		
		//execute
		@Override
		protected void execute() throws DelayTask {
			callAndStoreResult(this::execute0);
		}
		
		protected abstract R execute0() throws EX1, EX2, DelayTask;
		
		@Override
		protected void executionFinished(Barrier awaitTask) {
			try {
				callAndStoreResult(() -> {
					if (awaitTask instanceof BaseFuture<?>)
						//noinspection unchecked
						return ((BaseFuture<R>) awaitTask).assertGetAnyException();
					return null;
				});
			} catch (DelayTask e) {
				throw new RuntimeException(e);
			}
			
			super.executionFinished(awaitTask);
		}
		
		protected void callAndStoreResult(Callable<R> callable) throws DelayTask {
			try {
				callable.call();
			} catch (DelayTask e) {
				throw e;
			} catch (RuntimeException | Error e) {
				if (exceptionClass1.isInstance(e))
					exception1 = (EX1) e;
				else if (exceptionClass2.isInstance(e))
					exception2 = (EX2) e;
				else
					throw e;
			} catch (Throwable e) {
				if (exceptionClass1.isInstance(e))
					//noinspection unchecked
					exception1 = (EX1) e;
				else if (exceptionClass2.isInstance(e))
					//noinspection unchecked
					exception2 = (EX2) e;
				else
					throw new IllegalStateException("Exception caught that cannot have been thrown", e);
			}
		}
		
		//get
		@Override
		public R awaitGet() throws InterruptedException, EX1, EX2 {
			await();
			return getInternal();
		}
		
		@Override
		public R awaitGet(long time, TimeUnit unit) throws InterruptedException, TimeoutException, EX1, EX2 {
			await(time, unit);
			return getInternal();
		}
		
		@Override
		public R assertGet() throws FutureNotFinishedException, EX1, EX2 {
			if (state != TaskState.FINISHED)
				throw new FutureNotFinishedException(this);
			return getInternal();
		}
		
		protected synchronized R getInternal() throws EX1, EX2 {
			if (exception1 != null)
				throw exception1;
			if (exception2 != null)
				throw exception2;
			return ret;
		}
	}
	
	public static abstract class FutureTaskWith3Exception<R, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable> extends RunnableTask implements FutureWith3Exception<R, EX1, EX2, EX3> {
		
		protected final Class<EX1> exceptionClass1;
		protected final Class<EX2> exceptionClass2;
		protected final Class<EX3> exceptionClass3;
		
		protected R ret;
		protected EX1 exception1;
		protected EX2 exception2;
		protected EX3 exception3;
		
		public FutureTaskWith3Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Class<EX3> exceptionClass3, @NotNull SyncLock[] locks, @NotNull Barrier[] barriers) {
			this(exceptionClass1, exceptionClass2, exceptionClass3, locks);
			init(barriers);
		}
		
		/**
		 * REQUIRES calling {@link #init(Barrier[])} later to start execution
		 */
		protected FutureTaskWith3Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Class<EX3> exceptionClass3, @NotNull SyncLock[] locks) {
			super(locks);
			this.exceptionClass1 = exceptionClass1;
			this.exceptionClass2 = exceptionClass2;
			this.exceptionClass3 = exceptionClass3;
		}
		
		//execute
		@Override
		protected void execute() throws DelayTask {
			callAndStoreResult(this::execute0);
		}
		
		protected abstract R execute0() throws EX1, EX2, EX3, DelayTask;
		
		@Override
		protected void executionFinished(Barrier awaitTask) {
			try {
				callAndStoreResult(() -> {
					if (awaitTask instanceof BaseFuture<?>)
						//noinspection unchecked
						return ((BaseFuture<R>) awaitTask).assertGetAnyException();
					return null;
				});
			} catch (DelayTask e) {
				throw new RuntimeException(e);
			}
			
			super.executionFinished(awaitTask);
		}
		
		protected void callAndStoreResult(Callable<R> callable) throws DelayTask {
			try {
				callable.call();
			} catch (DelayTask e) {
				throw e;
			} catch (RuntimeException | Error e) {
				if (exceptionClass1.isInstance(e))
					exception1 = (EX1) e;
				else if (exceptionClass2.isInstance(e))
					exception2 = (EX2) e;
				else if (exceptionClass3.isInstance(e))
					exception3 = (EX3) e;
				else
					throw e;
			} catch (Throwable e) {
				if (exceptionClass1.isInstance(e))
					//noinspection unchecked
					exception1 = (EX1) e;
				else if (exceptionClass2.isInstance(e))
					//noinspection unchecked
					exception2 = (EX2) e;
				else if (exceptionClass3.isInstance(e))
					//noinspection unchecked
					exception3 = (EX3) e;
				else
					throw new IllegalStateException("Exception caught that cannot have been thrown", e);
			}
		}
		
		//get
		@Override
		public R awaitGet() throws InterruptedException, EX1, EX2, EX3 {
			await();
			return getInternal();
		}
		
		@Override
		public R awaitGet(long time, TimeUnit unit) throws InterruptedException, TimeoutException, EX1, EX2, EX3 {
			await(time, unit);
			return getInternal();
		}
		
		@Override
		public R assertGet() throws FutureNotFinishedException, EX1, EX2, EX3 {
			if (state != TaskState.FINISHED)
				throw new FutureNotFinishedException(this);
			return getInternal();
		}
		
		protected synchronized R getInternal() throws EX1, EX2, EX3 {
			if (exception1 != null)
				throw exception1;
			if (exception2 != null)
				throw exception2;
			if (exception3 != null)
				throw exception3;
			return ret;
		}
	}
	
	public static abstract class FutureTaskWith4Exception<R, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable, EX4 extends Throwable> extends RunnableTask implements FutureWith4Exception<R, EX1, EX2, EX3, EX4> {
		
		protected final Class<EX1> exceptionClass1;
		protected final Class<EX2> exceptionClass2;
		protected final Class<EX3> exceptionClass3;
		protected final Class<EX4> exceptionClass4;
		
		protected R ret;
		protected EX1 exception1;
		protected EX2 exception2;
		protected EX3 exception3;
		protected EX4 exception4;
		
		public FutureTaskWith4Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Class<EX3> exceptionClass3, Class<EX4> exceptionClass4, @NotNull SyncLock[] locks, @NotNull Barrier[] barriers) {
			this(exceptionClass1, exceptionClass2, exceptionClass3, exceptionClass4, locks);
			init(barriers);
		}
		
		/**
		 * REQUIRES calling {@link #init(Barrier[])} later to start execution
		 */
		protected FutureTaskWith4Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Class<EX3> exceptionClass3, Class<EX4> exceptionClass4, @NotNull SyncLock[] locks) {
			super(locks);
			this.exceptionClass1 = exceptionClass1;
			this.exceptionClass2 = exceptionClass2;
			this.exceptionClass3 = exceptionClass3;
			this.exceptionClass4 = exceptionClass4;
		}
		
		//execute
		@Override
		protected void execute() throws DelayTask {
			callAndStoreResult(this::execute0);
		}
		
		protected abstract R execute0() throws EX1, EX2, EX3, EX4, DelayTask;
		
		@Override
		protected void executionFinished(Barrier awaitTask) {
			try {
				callAndStoreResult(() -> {
					if (awaitTask instanceof BaseFuture<?>)
						//noinspection unchecked
						return ((BaseFuture<R>) awaitTask).assertGetAnyException();
					return null;
				});
			} catch (DelayTask e) {
				throw new RuntimeException(e);
			}
			
			super.executionFinished(awaitTask);
		}
		
		protected void callAndStoreResult(Callable<R> callable) throws DelayTask {
			try {
				callable.call();
			} catch (DelayTask e) {
				throw e;
			} catch (RuntimeException | Error e) {
				if (exceptionClass1.isInstance(e))
					exception1 = (EX1) e;
				else if (exceptionClass2.isInstance(e))
					exception2 = (EX2) e;
				else if (exceptionClass3.isInstance(e))
					exception3 = (EX3) e;
				else if (exceptionClass4.isInstance(e))
					exception4 = (EX4) e;
				else
					throw e;
			} catch (Throwable e) {
				if (exceptionClass1.isInstance(e))
					//noinspection unchecked
					exception1 = (EX1) e;
				else if (exceptionClass2.isInstance(e))
					//noinspection unchecked
					exception2 = (EX2) e;
				else if (exceptionClass3.isInstance(e))
					//noinspection unchecked
					exception3 = (EX3) e;
				else if (exceptionClass4.isInstance(e))
					//noinspection unchecked
					exception4 = (EX4) e;
				else
					throw new IllegalStateException("Exception caught that cannot have been thrown", e);
			}
		}
		
		//get
		@Override
		public R awaitGet() throws InterruptedException, EX1, EX2, EX3, EX4 {
			await();
			return getInternal();
		}
		
		@Override
		public R awaitGet(long time, TimeUnit unit) throws InterruptedException, TimeoutException, EX1, EX2, EX3, EX4 {
			await(time, unit);
			return getInternal();
		}
		
		@Override
		public R assertGet() throws FutureNotFinishedException, EX1, EX2, EX3, EX4 {
			if (state != TaskState.FINISHED)
				throw new FutureNotFinishedException(this);
			return getInternal();
		}
		
		protected synchronized R getInternal() throws EX1, EX2, EX3, EX4 {
			if (exception1 != null)
				throw exception1;
			if (exception2 != null)
				throw exception2;
			if (exception3 != null)
				throw exception3;
			if (exception4 != null)
				throw exception4;
			return ret;
		}
	}
	
	public static abstract class FutureTaskWith5Exception<R, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable, EX4 extends Throwable, EX5 extends Throwable> extends RunnableTask implements FutureWith5Exception<R, EX1, EX2, EX3, EX4, EX5> {
		
		protected final Class<EX1> exceptionClass1;
		protected final Class<EX2> exceptionClass2;
		protected final Class<EX3> exceptionClass3;
		protected final Class<EX4> exceptionClass4;
		protected final Class<EX5> exceptionClass5;
		
		protected R ret;
		protected EX1 exception1;
		protected EX2 exception2;
		protected EX3 exception3;
		protected EX4 exception4;
		protected EX5 exception5;
		
		public FutureTaskWith5Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Class<EX3> exceptionClass3, Class<EX4> exceptionClass4, Class<EX5> exceptionClass5, @NotNull SyncLock[] locks, @NotNull Barrier[] barriers) {
			this(exceptionClass1, exceptionClass2, exceptionClass3, exceptionClass4, exceptionClass5, locks);
			init(barriers);
		}
		
		/**
		 * REQUIRES calling {@link #init(Barrier[])} later to start execution
		 */
		protected FutureTaskWith5Exception(Class<EX1> exceptionClass1, Class<EX2> exceptionClass2, Class<EX3> exceptionClass3, Class<EX4> exceptionClass4, Class<EX5> exceptionClass5, @NotNull SyncLock[] locks) {
			super(locks);
			this.exceptionClass1 = exceptionClass1;
			this.exceptionClass2 = exceptionClass2;
			this.exceptionClass3 = exceptionClass3;
			this.exceptionClass4 = exceptionClass4;
			this.exceptionClass5 = exceptionClass5;
		}
		
		//execute
		@Override
		protected void execute() throws DelayTask {
			callAndStoreResult(this::execute0);
		}
		
		protected abstract R execute0() throws EX1, EX2, EX3, EX4, EX5, DelayTask;
		
		@Override
		protected void executionFinished(Barrier awaitTask) {
			try {
				callAndStoreResult(() -> {
					if (awaitTask instanceof BaseFuture<?>)
						//noinspection unchecked
						return ((BaseFuture<R>) awaitTask).assertGetAnyException();
					return null;
				});
			} catch (DelayTask e) {
				throw new RuntimeException(e);
			}
			
			super.executionFinished(awaitTask);
		}
		
		protected void callAndStoreResult(Callable<R> callable) throws DelayTask {
			try {
				callable.call();
			} catch (DelayTask e) {
				throw e;
			} catch (RuntimeException | Error e) {
				if (exceptionClass1.isInstance(e))
					exception1 = (EX1) e;
				else if (exceptionClass2.isInstance(e))
					exception2 = (EX2) e;
				else if (exceptionClass3.isInstance(e))
					exception3 = (EX3) e;
				else if (exceptionClass4.isInstance(e))
					exception4 = (EX4) e;
				else if (exceptionClass5.isInstance(e))
					exception5 = (EX5) e;
				else
					throw e;
			} catch (Throwable e) {
				if (exceptionClass1.isInstance(e))
					//noinspection unchecked
					exception1 = (EX1) e;
				else if (exceptionClass2.isInstance(e))
					//noinspection unchecked
					exception2 = (EX2) e;
				else if (exceptionClass3.isInstance(e))
					//noinspection unchecked
					exception3 = (EX3) e;
				else if (exceptionClass4.isInstance(e))
					//noinspection unchecked
					exception4 = (EX4) e;
				else if (exceptionClass5.isInstance(e))
					//noinspection unchecked
					exception5 = (EX5) e;
				else
					throw new IllegalStateException("Exception caught that cannot have been thrown", e);
			}
		}
		
		//get
		@Override
		public R awaitGet() throws InterruptedException, EX1, EX2, EX3, EX4, EX5 {
			await();
			return getInternal();
		}
		
		@Override
		public R awaitGet(long time, TimeUnit unit) throws InterruptedException, TimeoutException, EX1, EX2, EX3, EX4, EX5 {
			await(time, unit);
			return getInternal();
		}
		
		@Override
		public R assertGet() throws FutureNotFinishedException, EX1, EX2, EX3, EX4, EX5 {
			if (state != TaskState.FINISHED)
				throw new FutureNotFinishedException(this);
			return getInternal();
		}
		
		protected synchronized R getInternal() throws EX1, EX2, EX3, EX4, EX5 {
			if (exception1 != null)
				throw exception1;
			if (exception2 != null)
				throw exception2;
			if (exception3 != null)
				throw exception3;
			if (exception4 != null)
				throw exception4;
			if (exception5 != null)
				throw exception5;
			return ret;
		}
	}
}
