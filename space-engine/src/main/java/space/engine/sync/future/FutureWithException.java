package space.engine.sync.future;

import org.jetbrains.annotations.NotNull;
import space.engine.sync.barrier.Barrier;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@SuppressWarnings("unused")
public interface FutureWithException<R, EX extends Throwable> extends BaseFuture<R>, Barrier {
	
	//abstract
	R awaitGet() throws InterruptedException, EX;
	
	R awaitGet(long time, TimeUnit unit) throws InterruptedException, TimeoutException, EX;
	
	R assertGet() throws FutureNotFinishedException, EX;
	
	//anyException
	@Override
	default R awaitGetAnyException() throws Throwable {
		return awaitGet();
	}
	
	@Override
	default R awaitGetAnyException(long time, TimeUnit unit) throws Throwable {
		return awaitGet(time, unit);
	}
	
	@Override
	default R assertGetAnyException() throws Throwable {
		return assertGet();
	}
	
	//default
	
	/**
	 * Rethrows the potential thrown Exception as a {@link RuntimeException}.
	 * An easy way to ensure that an operation has to be successful.
	 *
	 * @return a {@link Future} returning the expected Result or a silent {@link RuntimeException}
	 * @implNote It actually catches ALL {@link Exception Exceptions} raised except those thrown by the get() Methods themselves
	 * (eg. InterruptedException, TimeoutException and FutureNotFinishedException).
	 */
	default Future<R> rethrowAsRuntimeException() {
		return new Future<>() {
			@Override
			public R awaitGet() throws InterruptedException {
				try {
					return FutureWithException.this.awaitGet();
				} catch (InterruptedException e) {
					throw e;
				} catch (Throwable ex) {
					throw new RuntimeException(ex);
				}
			}
			
			@Override
			public R awaitGet(long time, TimeUnit unit) throws InterruptedException, TimeoutException {
				try {
					return FutureWithException.this.awaitGet(time, unit);
				} catch (InterruptedException | TimeoutException e) {
					throw e;
				} catch (Throwable ex) {
					throw new RuntimeException(ex);
				}
			}
			
			@Override
			public R assertGet() throws FutureNotFinishedException {
				try {
					return FutureWithException.this.assertGet();
				} catch (FutureNotFinishedException ex) {
					throw ex;
				} catch (Throwable ex) {
					throw new RuntimeException(ex);
				}
			}
			
			@Override
			public boolean isFinished() {
				return FutureWithException.this.isFinished();
			}
			
			@Override
			public void addHook(@NotNull Runnable run) {
				FutureWithException.this.addHook(run);
			}
			
			@Override
			public void removeHook(@NotNull Runnable run) {
				FutureWithException.this.removeHook(run);
			}
			
			@Override
			public void await() throws InterruptedException {
				FutureWithException.this.await();
			}
			
			@Override
			public void await(long time, TimeUnit unit) throws InterruptedException, TimeoutException {
				FutureWithException.this.await(time, unit);
			}
		};
	}
	
	//static
	static <R, EX extends Throwable> FutureWithException<R, EX> finished(R get) {
		return new FutureWithException<>() {
			@Override
			public R awaitGet() {
				return get;
			}
			
			@Override
			public R awaitGet(long time, TimeUnit unit) {
				return get;
			}
			
			@Override
			public R assertGet() throws FutureNotFinishedException {
				return get;
			}
			
			@Override
			public boolean isFinished() {
				return true;
			}
			
			@Override
			public void addHook(@NotNull Runnable run) {
				run.run();
			}
			
			@Override
			public void removeHook(@NotNull Runnable run) {
				run.run();
			}
			
			@Override
			public void await() {
			
			}
			
			@Override
			public void await(long time, TimeUnit unit) {
			
			}
		};
	}
	
	static <R, EX extends Throwable> FutureWithException<R, EX> finishedException(EX ex) {
		return new FutureWithException<>() {
			@Override
			public R awaitGet() throws EX {
				throw ex;
			}
			
			@Override
			public R awaitGet(long time, TimeUnit unit) throws EX {
				throw ex;
			}
			
			@Override
			public R assertGet() throws FutureNotFinishedException, EX {
				throw ex;
			}
			
			@Override
			public boolean isFinished() {
				return true;
			}
			
			@Override
			public void addHook(@NotNull Runnable run) {
				run.run();
			}
			
			@Override
			public void removeHook(@NotNull Runnable run) {
				run.run();
			}
			
			@Override
			public void await() {
			
			}
			
			@Override
			public void await(long time, TimeUnit unit) {
			
			}
		};
	}
	
	interface FutureWith2Exception<R, EX1 extends Throwable, EX2 extends Throwable> extends BaseFuture<R>, Barrier {
		
		//abstract
		R awaitGet() throws InterruptedException, EX1, EX2;
		
		R awaitGet(long time, TimeUnit unit) throws InterruptedException, TimeoutException, EX1, EX2;
		
		R assertGet() throws FutureNotFinishedException, EX1, EX2;
		
		//anyException
		@Override
		default R awaitGetAnyException() throws Throwable {
			return awaitGet();
		}
		
		@Override
		default R awaitGetAnyException(long time, TimeUnit unit) throws Throwable {
			return awaitGet(time, unit);
		}
		
		@Override
		default R assertGetAnyException() throws Throwable {
			return assertGet();
		}
		
		//default
		
		/**
		 * Rethrows the potential thrown Exception as a {@link RuntimeException}.
		 * An easy way to ensure that an operation has to be successful.
		 *
		 * @return a {@link Future} returning the expected Result or a silent {@link RuntimeException}
		 * @implNote It actually catches ALL {@link Exception Exceptions} raised except those thrown by the get() Methods themselves
		 * (eg. InterruptedException, TimeoutException and FutureNotFinishedException).
		 */
		default Future<R> rethrowAsRuntimeException() {
			return new Future<>() {
				@Override
				public R awaitGet() throws InterruptedException {
					try {
						return FutureWith2Exception.this.awaitGet();
					} catch (InterruptedException e) {
						throw e;
					} catch (Throwable ex) {
						throw new RuntimeException(ex);
					}
				}
				
				@Override
				public R awaitGet(long time, TimeUnit unit) throws InterruptedException, TimeoutException {
					try {
						return FutureWith2Exception.this.awaitGet(time, unit);
					} catch (InterruptedException | TimeoutException e) {
						throw e;
					} catch (Throwable ex) {
						throw new RuntimeException(ex);
					}
				}
				
				@Override
				public R assertGet() throws FutureNotFinishedException {
					try {
						return FutureWith2Exception.this.assertGet();
					} catch (FutureNotFinishedException ex) {
						throw ex;
					} catch (Throwable ex) {
						throw new RuntimeException(ex);
					}
				}
				
				@Override
				public boolean isFinished() {
					return FutureWith2Exception.this.isFinished();
				}
				
				@Override
				public void addHook(@NotNull Runnable run) {
					FutureWith2Exception.this.addHook(run);
				}
				
				@Override
				public void removeHook(@NotNull Runnable run) {
					FutureWith2Exception.this.removeHook(run);
				}
				
				@Override
				public void await() throws InterruptedException {
					FutureWith2Exception.this.await();
				}
				
				@Override
				public void await(long time, TimeUnit unit) throws InterruptedException, TimeoutException {
					FutureWith2Exception.this.await(time, unit);
				}
			};
		}
		
		//static
		static <R, EX1 extends Throwable, EX2 extends Throwable> FutureWith2Exception<R, EX1, EX2> finished(R get) {
			return new FutureWith2Exception<>() {
				@Override
				public R awaitGet() {
					return get;
				}
				
				@Override
				public R awaitGet(long time, TimeUnit unit) {
					return get;
				}
				
				@Override
				public R assertGet() throws FutureNotFinishedException {
					return get;
				}
				
				@Override
				public boolean isFinished() {
					return true;
				}
				
				@Override
				public void addHook(@NotNull Runnable run) {
					run.run();
				}
				
				@Override
				public void removeHook(@NotNull Runnable run) {
					run.run();
				}
				
				@Override
				public void await() {
				
				}
				
				@Override
				public void await(long time, TimeUnit unit) {
				
				}
			};
		}
	}
	
	interface FutureWith3Exception<R, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable> extends BaseFuture<R>, Barrier {
		
		//abstract
		R awaitGet() throws InterruptedException, EX1, EX2, EX3;
		
		R awaitGet(long time, TimeUnit unit) throws InterruptedException, TimeoutException, EX1, EX2, EX3;
		
		R assertGet() throws FutureNotFinishedException, EX1, EX2, EX3;
		
		//anyException
		@Override
		default R awaitGetAnyException() throws Throwable {
			return awaitGet();
		}
		
		@Override
		default R awaitGetAnyException(long time, TimeUnit unit) throws Throwable {
			return awaitGet(time, unit);
		}
		
		@Override
		default R assertGetAnyException() throws Throwable {
			return assertGet();
		}
		
		//default
		
		/**
		 * Rethrows the potential thrown Exception as a {@link RuntimeException}.
		 * An easy way to ensure that an operation has to be successful.
		 *
		 * @return a {@link Future} returning the expected Result or a silent {@link RuntimeException}
		 * @implNote It actually catches ALL {@link Exception Exceptions} raised except those thrown by the get() Methods themselves
		 * (eg. InterruptedException, TimeoutException and FutureNotFinishedException).
		 */
		default Future<R> rethrowAsRuntimeException() {
			return new Future<>() {
				@Override
				public R awaitGet() throws InterruptedException {
					try {
						return FutureWith3Exception.this.awaitGet();
					} catch (InterruptedException e) {
						throw e;
					} catch (Throwable ex) {
						throw new RuntimeException(ex);
					}
				}
				
				@Override
				public R awaitGet(long time, TimeUnit unit) throws InterruptedException, TimeoutException {
					try {
						return FutureWith3Exception.this.awaitGet(time, unit);
					} catch (InterruptedException | TimeoutException e) {
						throw e;
					} catch (Throwable ex) {
						throw new RuntimeException(ex);
					}
				}
				
				@Override
				public R assertGet() throws FutureNotFinishedException {
					try {
						return FutureWith3Exception.this.assertGet();
					} catch (FutureNotFinishedException ex) {
						throw ex;
					} catch (Throwable ex) {
						throw new RuntimeException(ex);
					}
				}
				
				@Override
				public boolean isFinished() {
					return FutureWith3Exception.this.isFinished();
				}
				
				@Override
				public void addHook(@NotNull Runnable run) {
					FutureWith3Exception.this.addHook(run);
				}
				
				@Override
				public void removeHook(@NotNull Runnable run) {
					FutureWith3Exception.this.removeHook(run);
				}
				
				@Override
				public void await() throws InterruptedException {
					FutureWith3Exception.this.await();
				}
				
				@Override
				public void await(long time, TimeUnit unit) throws InterruptedException, TimeoutException {
					FutureWith3Exception.this.await(time, unit);
				}
			};
		}
		
		//static
		static <R, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable> FutureWith3Exception<R, EX1, EX2, EX3> finished(R get) {
			return new FutureWith3Exception<>() {
				@Override
				public R awaitGet() {
					return get;
				}
				
				@Override
				public R awaitGet(long time, TimeUnit unit) {
					return get;
				}
				
				@Override
				public R assertGet() throws FutureNotFinishedException {
					return get;
				}
				
				@Override
				public boolean isFinished() {
					return true;
				}
				
				@Override
				public void addHook(@NotNull Runnable run) {
					run.run();
				}
				
				@Override
				public void removeHook(@NotNull Runnable run) {
					run.run();
				}
				
				@Override
				public void await() {
				
				}
				
				@Override
				public void await(long time, TimeUnit unit) {
				
				}
			};
		}
	}
	
	interface FutureWith4Exception<R, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable, EX4 extends Throwable> extends BaseFuture<R>, Barrier {
		
		//abstract
		R awaitGet() throws InterruptedException, EX1, EX2, EX3, EX4;
		
		R awaitGet(long time, TimeUnit unit) throws InterruptedException, TimeoutException, EX1, EX2, EX3, EX4;
		
		R assertGet() throws FutureNotFinishedException, EX1, EX2, EX3, EX4;
		
		//anyException
		@Override
		default R awaitGetAnyException() throws Throwable {
			return awaitGet();
		}
		
		@Override
		default R awaitGetAnyException(long time, TimeUnit unit) throws Throwable {
			return awaitGet(time, unit);
		}
		
		@Override
		default R assertGetAnyException() throws Throwable {
			return assertGet();
		}
		
		//default
		
		/**
		 * Rethrows the potential thrown Exception as a {@link RuntimeException}.
		 * An easy way to ensure that an operation has to be successful.
		 *
		 * @return a {@link Future} returning the expected Result or a silent {@link RuntimeException}
		 * @implNote It actually catches ALL {@link Exception Exceptions} raised except those thrown by the get() Methods themselves
		 * (eg. InterruptedException, TimeoutException and FutureNotFinishedException).
		 */
		default Future<R> rethrowAsRuntimeException() {
			return new Future<>() {
				@Override
				public R awaitGet() throws InterruptedException {
					try {
						return FutureWith4Exception.this.awaitGet();
					} catch (InterruptedException e) {
						throw e;
					} catch (Throwable ex) {
						throw new RuntimeException(ex);
					}
				}
				
				@Override
				public R awaitGet(long time, TimeUnit unit) throws InterruptedException, TimeoutException {
					try {
						return FutureWith4Exception.this.awaitGet(time, unit);
					} catch (InterruptedException | TimeoutException e) {
						throw e;
					} catch (Throwable ex) {
						throw new RuntimeException(ex);
					}
				}
				
				@Override
				public R assertGet() throws FutureNotFinishedException {
					try {
						return FutureWith4Exception.this.assertGet();
					} catch (FutureNotFinishedException ex) {
						throw ex;
					} catch (Throwable ex) {
						throw new RuntimeException(ex);
					}
				}
				
				@Override
				public boolean isFinished() {
					return FutureWith4Exception.this.isFinished();
				}
				
				@Override
				public void addHook(@NotNull Runnable run) {
					FutureWith4Exception.this.addHook(run);
				}
				
				@Override
				public void removeHook(@NotNull Runnable run) {
					FutureWith4Exception.this.removeHook(run);
				}
				
				@Override
				public void await() throws InterruptedException {
					FutureWith4Exception.this.await();
				}
				
				@Override
				public void await(long time, TimeUnit unit) throws InterruptedException, TimeoutException {
					FutureWith4Exception.this.await(time, unit);
				}
			};
		}
		
		//static
		static <R, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable, EX4 extends Throwable> FutureWith4Exception<R, EX1, EX2, EX3, EX4> finished(R get) {
			return new FutureWith4Exception<>() {
				@Override
				public R awaitGet() {
					return get;
				}
				
				@Override
				public R awaitGet(long time, TimeUnit unit) {
					return get;
				}
				
				@Override
				public R assertGet() throws FutureNotFinishedException {
					return get;
				}
				
				@Override
				public boolean isFinished() {
					return true;
				}
				
				@Override
				public void addHook(@NotNull Runnable run) {
					run.run();
				}
				
				@Override
				public void removeHook(@NotNull Runnable run) {
					run.run();
				}
				
				@Override
				public void await() {
				
				}
				
				@Override
				public void await(long time, TimeUnit unit) {
				
				}
			};
		}
	}
	
	interface FutureWith5Exception<R, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable, EX4 extends Throwable, EX5 extends Throwable> extends BaseFuture<R>, Barrier {
		
		//abstract
		R awaitGet() throws InterruptedException, EX1, EX2, EX3, EX4, EX5;
		
		R awaitGet(long time, TimeUnit unit) throws InterruptedException, TimeoutException, EX1, EX2, EX3, EX4, EX5;
		
		R assertGet() throws FutureNotFinishedException, EX1, EX2, EX3, EX4, EX5;
		
		//anyException
		@Override
		default R awaitGetAnyException() throws Throwable {
			return awaitGet();
		}
		
		@Override
		default R awaitGetAnyException(long time, TimeUnit unit) throws Throwable {
			return awaitGet(time, unit);
		}
		
		@Override
		default R assertGetAnyException() throws Throwable {
			return assertGet();
		}
		
		//default
		
		/**
		 * Rethrows the potential thrown Exception as a {@link RuntimeException}.
		 * An easy way to ensure that an operation has to be successful.
		 *
		 * @return a {@link Future} returning the expected Result or a silent {@link RuntimeException}
		 * @implNote It actually catches ALL {@link Exception Exceptions} raised except those thrown by the get() Methods themselves
		 * (eg. InterruptedException, TimeoutException and FutureNotFinishedException).
		 */
		default Future<R> rethrowAsRuntimeException() {
			return new Future<>() {
				@Override
				public R awaitGet() throws InterruptedException {
					try {
						return FutureWith5Exception.this.awaitGet();
					} catch (InterruptedException e) {
						throw e;
					} catch (Throwable ex) {
						throw new RuntimeException(ex);
					}
				}
				
				@Override
				public R awaitGet(long time, TimeUnit unit) throws InterruptedException, TimeoutException {
					try {
						return FutureWith5Exception.this.awaitGet(time, unit);
					} catch (InterruptedException | TimeoutException e) {
						throw e;
					} catch (Throwable ex) {
						throw new RuntimeException(ex);
					}
				}
				
				@Override
				public R assertGet() throws FutureNotFinishedException {
					try {
						return FutureWith5Exception.this.assertGet();
					} catch (FutureNotFinishedException ex) {
						throw ex;
					} catch (Throwable ex) {
						throw new RuntimeException(ex);
					}
				}
				
				@Override
				public boolean isFinished() {
					return FutureWith5Exception.this.isFinished();
				}
				
				@Override
				public void addHook(@NotNull Runnable run) {
					FutureWith5Exception.this.addHook(run);
				}
				
				@Override
				public void removeHook(@NotNull Runnable run) {
					FutureWith5Exception.this.removeHook(run);
				}
				
				@Override
				public void await() throws InterruptedException {
					FutureWith5Exception.this.await();
				}
				
				@Override
				public void await(long time, TimeUnit unit) throws InterruptedException, TimeoutException {
					FutureWith5Exception.this.await(time, unit);
				}
			};
		}
		
		//static
		static <R, EX1 extends Throwable, EX2 extends Throwable, EX3 extends Throwable, EX4 extends Throwable, EX5 extends Throwable> FutureWith5Exception<R, EX1, EX2, EX3, EX4, EX5> finished(R get) {
			return new FutureWith5Exception<>() {
				@Override
				public R awaitGet() {
					return get;
				}
				
				@Override
				public R awaitGet(long time, TimeUnit unit) {
					return get;
				}
				
				@Override
				public R assertGet() throws FutureNotFinishedException {
					return get;
				}
				
				@Override
				public boolean isFinished() {
					return true;
				}
				
				@Override
				public void addHook(@NotNull Runnable run) {
					run.run();
				}
				
				@Override
				public void removeHook(@NotNull Runnable run) {
					run.run();
				}
				
				@Override
				public void await() {
				
				}
				
				@Override
				public void await(long time, TimeUnit unit) {
				
				}
			};
		}
	}
}
