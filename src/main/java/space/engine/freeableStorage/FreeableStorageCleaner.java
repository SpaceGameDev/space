package space.engine.freeableStorage;

import org.jetbrains.annotations.Nullable;
import space.engine.Side;
import space.engine.event.EventEntry;
import space.engine.logger.LogLevel;
import space.engine.logger.Logger;
import space.engine.logger.NullLogger;
import space.engine.string.StringBuilder2D;
import space.engine.sync.DelayTask;
import space.engine.sync.Tasks.RunnableWithDelay;
import space.engine.sync.barrier.BarrierImpl;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.Objects;

import static space.engine.logger.LogLevel.INFO;

public final class FreeableStorageCleaner {
	
	//the QUEUE
	public static final ReferenceQueue<Object> QUEUE = new ReferenceQueue<>();
	
	//exit event entry
	public static final EventEntry<RunnableWithDelay> EXIT_EVENT_ENTRY_FREEABLE_ROOT_LIST_FREE;
	
	//instance management
	@Nullable
	private static ThreadInfo cleanupThreadInfo;
	
	private static final class ThreadInfo {
		
		Thread thread;
		volatile boolean doRun = true;
	}
	
	//logger
	public static volatile Logger cleanupLogger = NullLogger.NULL_LOGGER;
	public static volatile boolean cleanupLoggerDebug = false;
	
	public static void setCleanupLogger(Logger baseLogger) {
		setCleanupLogger(baseLogger, false);
	}
	
	public static void setCleanupLogger(Logger baseLogger, boolean debug) {
		FreeableStorageCleaner.cleanupLogger = baseLogger.subLogger("Cleanup");
		FreeableStorageCleaner.cleanupLoggerDebug = debug;
	}
	
	//static init
	static {
		startCleanupThread();
		
		Side.EVENT_EXIT.addHook(EXIT_EVENT_ENTRY_FREEABLE_ROOT_LIST_FREE = new EventEntry<>(() -> {
			BarrierImpl finishBarrier = new BarrierImpl();
			new Thread(() -> {
				Logger logger = cleanupLogger.subLogger("final");
				logger.log(INFO, "Final cleanup...");
				logger.log(INFO, "1. Stopping cleanup thread");
				try {
					stopAndJoinCleanupThread();
				} catch (InterruptedException ignore) {
				
				}
				
				logger.log(INFO, "2. ROOT_LIST free");
				Freeable.ROOT_LIST.free().awaitUninterrupted();
				
				logger.log(INFO, "3. gc free");
				System.gc();
				System.runFinalization();
				while (true) {
					try {
						handle(100);
						break;
					} catch (InterruptedException ignored) {
					}
				}
				
				logger.log(INFO, "Final cleanup complete!");
				finishBarrier.triggerNow();
			}, "FreeableStorageCleaner-Final").start();
			throw new DelayTask(finishBarrier);
		}, new EventEntry<?>[] {Side.EXIT_EVENT_ENTRY_POOL_EXIT}, new EventEntry<?>[] {Side.EXIT_EVENT_ENTRY_BEFORE_APPLICATION_SHUTDOWN}));
	}
	
	//start
	public static synchronized void startCleanupThread() throws IllegalStateException {
		if (cleanupThreadInfo != null)
			stopCleanupThread();
		
		ThreadInfo info = new ThreadInfo();
		Thread thread = info.thread = new Thread(() -> {
			while (info.doRun) {
				try {
					handle(0);
				} catch (InterruptedException ignore) {
				
				} catch (Throwable e) {
					Thread th = Thread.currentThread();
					th.getUncaughtExceptionHandler().uncaughtException(th, e);
				}
			}
		});
		thread.setName("FreeableStorageCleaner");
		thread.setPriority(8);
		thread.setDaemon(true);
		thread.start();
		
		cleanupThreadInfo = info;
	}
	
	//handle
	private static void handle(long timeout) throws InterruptedException {
		Reference<?> ref1 = QUEUE.remove(timeout);
		if (ref1 == null)
			return;
		handle(ref1);
		
		//handle all remaining objects in the queue
		int count = 1;
		Reference<?> ref;
		for (; (ref = QUEUE.poll()) != null; count++) {
			handle(ref);
		}
		
		//log object count
		cleanupLogger.log(INFO, new StringBuilder2D().append("Cleaning up ").append(count).append(" Objects via GC").toString());
	}
	
	private static void handle(Reference<?> ref) {
		if (cleanupLoggerDebug)
			cleanupLogger.log(LogLevel.FINEST, "Cleaning up " + ref);
		if (ref instanceof Freeable)
			((Freeable) ref).free();
		else
			throw new IllegalArgumentException("Inappropriate Reference of type " + ref.getClass().getName() + ": " + ref);
	}
	
	//stop
	public static synchronized boolean stopCleanupThread() {
		if (cleanupThreadInfo == null)
			return false;
		
		cleanupThreadInfo.doRun = false;
		cleanupThreadInfo.thread.interrupt();
		cleanupThreadInfo = null;
		return true;
	}
	
	public static boolean stopAndJoinCleanupThread() throws InterruptedException {
		Thread thread;
		synchronized (FreeableStorageCleaner.class) {
			ThreadInfo cleanupThreadInfo = FreeableStorageCleaner.cleanupThreadInfo;
			if (!stopCleanupThread())
				return false;
			thread = Objects.requireNonNull(cleanupThreadInfo).thread;
		}
		thread.join();
		return true;
	}
	
	//isRunning
	public static synchronized boolean hasCleanupThread() {
		return cleanupThreadInfo != null;
	}
}
