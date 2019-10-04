package space.engine.vulkan.util;

import org.jetbrains.annotations.NotNull;
import space.engine.barrier.Barrier;
import space.engine.barrier.BarrierImpl;
import space.engine.barrier.DelayTask;
import space.engine.barrier.future.Future;
import space.engine.barrier.timer.BarrierTimerWithTimeControl;
import space.engine.freeable.Cleaner;
import space.engine.freeable.Freeable;
import space.engine.freeable.Freeable.CleanerWrapper;
import space.engine.orderingGuarantee.SequentialOrderingGuarantee;
import space.engine.vulkan.VkSemaphore;
import space.engine.vulkan.managed.device.ManagedDevice;
import space.engine.vulkan.managed.renderPass.Infos;
import space.engine.vulkan.managed.renderPass.ManagedFrameBuffer;
import space.engine.vulkan.managed.surface.ManagedSwapchain;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.lwjgl.vulkan.VK10.*;
import static space.engine.Empties.EMPTY_OBJECT_ARRAY;
import static space.engine.barrier.Barrier.*;

public class FpsRenderer<INFOS extends Infos> implements CleanerWrapper {
	
	public FpsRenderer(@NotNull ManagedDevice device, @NotNull ManagedSwapchain<?> swapchain, @NotNull ManagedFrameBuffer<INFOS> frameBuffer, @NotNull InfoCreator<INFOS> infoCreator, float fps, Object[] parents) {
		this.device = device;
		this.swapchain = swapchain;
		this.frameBuffer = frameBuffer;
		this.infoCreator = infoCreator;
		
		//all explicitly freed in Storage
		this.timer = new BarrierTimerWithTimeControl(fps / 1_000_000_000f, -System.nanoTime(), EMPTY_OBJECT_ARRAY);
		this.semaphoreImageReady = device.vkSemaphorePool().allocate();
		this.semaphoreRenderDone = device.vkSemaphorePool().allocate();
		this.storage = new Storage(this, Freeable.addIfNotContained(parents, swapchain, frameBuffer));
		
		start();
	}
	
	//parents
	private final ManagedDevice device;
	private final @NotNull ManagedSwapchain<?> swapchain;
	private final @NotNull ManagedFrameBuffer<INFOS> frameBuffer;
	
	public ManagedDevice device() {
		return device;
	}
	
	public @NotNull ManagedSwapchain<?> swapchain() {
		return swapchain;
	}
	
	public @NotNull ManagedFrameBuffer<INFOS> frameBuffer() {
		return frameBuffer;
	}
	
	//storage
	private final @NotNull Freeable storage;
	
	@Override
	public @NotNull Freeable getStorage() {
		return storage;
	}
	
	public static class Storage extends Cleaner {
		
		private final @NotNull AtomicBoolean isRunning;
		private final @NotNull Barrier exitBarrier;
		private final @NotNull Freeable[] intermediary;
		
		public Storage(@NotNull FpsRenderer fpsRenderer, @NotNull Object[] parents) {
			super(fpsRenderer, parents);
			this.isRunning = fpsRenderer.isRunning;
			this.exitBarrier = fpsRenderer.exitBarrier;
			this.intermediary = new Freeable[] {fpsRenderer.timer, fpsRenderer.semaphoreImageReady, fpsRenderer.semaphoreRenderDone};
		}
		
		@Override
		protected @NotNull Barrier handleFree() {
			isRunning.set(false);
			return exitBarrier.thenStart(() -> Barrier.when(Arrays.stream(intermediary).map(Freeable::free)));
		}
	}
	
	//timer fields
	private final @NotNull BarrierTimerWithTimeControl timer;
	private final @NotNull InfoCreator<INFOS> infoCreator;
	private final @NotNull SequentialOrderingGuarantee orderingGuarantee = new SequentialOrderingGuarantee();
	private final @NotNull VkSemaphore semaphoreImageReady, semaphoreRenderDone;
	
	public BarrierTimerWithTimeControl timer() {
		return timer;
	}
	
	public void setFps(float fps) {
		timer.setSpeed(fps / 1_000_000_000f);
	}
	
	//stop
	private final @NotNull AtomicBoolean isRunning = new AtomicBoolean(true);
	private final @NotNull BarrierImpl exitBarrier = new BarrierImpl();
	
	//timer methods
	private void start() {
		run(timer.currTime());
	}
	
	private void run(long eventTime) {
		if (!isRunning.get()) {
			exitBarrier.triggerNow();
			return;
		}
		
		Barrier barrierTime = timer.create(eventTime);
		orderingGuarantee.next(prev -> {
			Barrier frameDone = when(prev, barrierTime).thenRun(() -> {
				int imageIndex = swapchain.acquire(Long.MAX_VALUE, semaphoreImageReady, null);
				Future<INFOS> infosFuture = infoCreator.apply(imageIndex, eventTime);
				
				throw new DelayTask(infosFuture.thenRun(() -> {
					Future<Barrier> barrierRenderSubmitted = frameBuffer.render(
							infosFuture.assertGet(),
							new VkSemaphore[] {semaphoreImageReady},
							new int[] {VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT},
							new VkSemaphore[] {semaphoreRenderDone}
					);
					Barrier barrierRenderDone = inner(barrierRenderSubmitted);
					
					Barrier barrierPresented = barrierRenderSubmitted.thenStart(() -> swapchain.present(new VkSemaphore[] {semaphoreRenderDone}, imageIndex));
					
					throw new DelayTask(Barrier.when(barrierPresented, barrierRenderDone));
				}));
			});
			frameDone.thenStart(() -> {
				run(Long.max(eventTime + 1, timer.currTime()));
				return done();
			});
			return frameDone;
		});
	}
	
	@FunctionalInterface
	public interface InfoCreator<INFOS extends Infos> {
		
		Future<INFOS> apply(int imageIndex, long frameEventTime);
	}
}
