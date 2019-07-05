package space.engine.sync.test;

import space.engine.sync.TaskCreator;
import space.engine.sync.barrier.Barrier;
import space.engine.sync.future.Future;
import space.engine.sync.future.FutureWithException;
import space.engine.sync.taskImpl.FutureTaskWithException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static space.engine.sync.Tasks.*;

public class TaskTestUseCaseRendering {
	
	public static final Executor GLOBAL_POOL = new ThreadPoolExecutor(4, 4, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
	public static final Executor WINDOW_POOL = GLOBAL_POOL;
	public static final Executor COMPUTE_POOL = GLOBAL_POOL;
	public static final boolean VERTEX_DATA_FORCE_IO_EXCEPTION = false;
	public static final boolean DO_SLEEP_BETWEEN_FRAMES = false;
	public static final boolean DO_AWAIT_BETWEEN_FRAMES = false;
	
	public static void println(String str) {
		long nanoTime = System.nanoTime();
		System.out.println(NANOSECONDS.toSeconds(nanoTime) + "." + NANOSECONDS.toMillis(nanoTime) % 1000 + "." + NANOSECONDS.toNanos(nanoTime) % 1000 + ": " + str);
	}
	
	//setup
	public static TaskCreator<?> createWindow(String name) {
		return runnable(WINDOW_POOL, () -> println("create Window: '" + name + "'"));
	}
	
	public static TaskCreator<?> closeWindow() {
		return runnable(WINDOW_POOL, () -> println("close Window"));
	}
	
	public static TaskCreator<? extends FutureWithException<float[], IOException>> loadVertexData() {
		return (locks, barriers) -> new FutureTaskWithException<>(IOException.class, locks, barriers) {
			@Override
			protected float[] execute0() throws IOException {
				if (VERTEX_DATA_FORCE_IO_EXCEPTION)
					throw new IOException("Forced IO Exception for testing");
				return new float[] {0f, 0.5f, 0.5f, -0.5f, -0.5f, 0.5f};
			}
			
			@Override
			protected synchronized void submit1(Runnable toRun) {
				COMPUTE_POOL.execute(toRun);
			}
		};
	}
	
	//render loop
	public static TaskCreator renderFrame(Barrier contextCreation, Future<float[]> triangles) {
		return sequential(new Barrier[] {contextCreation}, List.of(
				runnable(WINDOW_POOL, () -> println("clear buffer")),
				runnable(WINDOW_POOL, new Barrier[] {triangles}, () -> println("render triangles: " + Arrays.toString(triangles.assertGet())))
		));
	}
	
	public static TaskCreator swapBuffers(Barrier contextCreation) {
		return runnable(WINDOW_POOL, new Barrier[] {contextCreation}, () -> println("swap buffer"));
	}
	
	public static void main(String[] args) throws Exception {
		try {
			System.out.print(""); //initialization
			test();
		} finally {
			if (GLOBAL_POOL instanceof ThreadPoolExecutor)
				((ThreadPoolExecutor) GLOBAL_POOL).shutdown();
		}
	}
	
	public static void test() throws InterruptedException {
		Barrier windowCreation = createWindow("My Window").submit();
		
		try {
			//loading and rendering
			Future<float[]> vertexData = loadVertexData().submit().rethrowAsRuntimeException();
			
			TaskCreator creatorRenderFrame = renderFrame(windowCreation, vertexData);
			TaskCreator taskCreatorSwapBuffers = swapBuffers(windowCreation);
			
			Barrier taskSwapBuffers = null;
			for (int i = 0; i < 10; i++) {
				Barrier taskRenderFrame = !DO_AWAIT_BETWEEN_FRAMES && taskSwapBuffers != null ? creatorRenderFrame.submit(taskSwapBuffers) : creatorRenderFrame.submit();
				taskSwapBuffers = taskCreatorSwapBuffers.submit(taskRenderFrame);
				if (DO_AWAIT_BETWEEN_FRAMES)
					taskSwapBuffers.await();
				
				if (DO_SLEEP_BETWEEN_FRAMES)
					Thread.sleep(1000L / 60);
			}
			taskSwapBuffers.await();
		} finally {
			closeWindow().submit(windowCreation).await();
		}
	}
}
