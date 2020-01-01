package space.engine.vulkan.managed.device;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.vulkan.VkSubmitInfo;
import space.engine.barrier.Barrier;
import space.engine.barrier.future.Future;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.array.ArrayBufferInt;
import space.engine.buffer.array.ArrayBufferLong;
import space.engine.buffer.array.ArrayBufferPointer;
import space.engine.buffer.pointer.PointerBufferPointer;
import space.engine.freeable.Freeable;
import space.engine.simpleQueue.pool.SimpleThreadPool;
import space.engine.vulkan.VkCommandBuffer;
import space.engine.vulkan.VkCommandPool;
import space.engine.vulkan.VkFence;
import space.engine.vulkan.VkQueue;
import space.engine.vulkan.VkQueueFamilyProperties;
import space.engine.vulkan.VkSemaphore;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.lwjgl.vulkan.VK10.*;
import static space.engine.Empties.EMPTY_OBJECT_ARRAY;
import static space.engine.barrier.Barrier.*;
import static space.engine.lwjgl.LwjglStructAllocator.mallocStruct;
import static space.engine.lwjgl.PointerBufferWrapper.wrapPointer;

public class ManagedQueue extends VkQueue {
	
	private static final AtomicInteger MANAGED_QUEUE_THREAD_COUNTER = new AtomicInteger();
	
	//alloc
	public static @NotNull ManagedQueue alloc(@NotNull ManagedDevice device, @NotNull VkQueueFamilyProperties family, int queueIndex, @NotNull Object[] parents) {
		try (AllocatorFrame frame = Allocator.frame()) {
			PointerBufferPointer ptr = PointerBufferPointer.malloc(frame);
			nvkGetDeviceQueue(device, family.index(), queueIndex, ptr.address());
			return new ManagedQueue(ptr.getPointer(), device, family, parents);
		}
	}
	
	//object
	protected ManagedQueue(long address, @NotNull ManagedDevice device, @NotNull VkQueueFamilyProperties queueFamily, @NotNull Object[] parents) {
		super(address, device, queueFamily);
		this.device = device;
		init(Freeable::createDummy, parents);
		this.commandPools = ThreadLocal.withInitial(() -> new SharedCmdPool(
				VkCommandPool.alloc(VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT, queueFamily, device, new Object[] {this}),
				VkCommandPool.alloc(VK_COMMAND_POOL_CREATE_TRANSIENT_BIT | VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT, queueFamily, device, new Object[] {this})
		));
		
		//submit
		this.pool = new SimpleThreadPool(
				1,
				r -> new Thread(r, "ManagedQueue-" + MANAGED_QUEUE_THREAD_COUNTER.getAndIncrement())
		);
		this.pool.createStopFreeable(new Object[] {this});
	}
	
	//parents
	private final ManagedDevice device;
	
	@Override
	public ManagedDevice device() {
		return device;
	}
	
	//submit
	private final SimpleThreadPool pool;
	
	/**
	 * Executes vkQueueSubmit() on a {@link VkSubmitInfo}
	 *
	 * @return a {@link Future}, triggered when cmd is submitted, containing a {@link Barrier}, triggered when execution of cmd finished
	 */
	public Future<Barrier> submit(Entry cmd) {
		return nowFuture(pool, () -> cmd.run(ManagedQueue.this));
	}
	
	/**
	 * Executes vkQueueSubmit() on a {@link VkSubmitInfo}
	 *
	 * @return a {@link Future}, triggered when cmd is submitted, containing a {@link Barrier}, triggered when execution of cmd finished
	 */
	public Future<Barrier> submit(@NotNull VkCommandBuffer... commandBuffers) {
		return submit(null, null, commandBuffers, null);
	}
	
	/**
	 * Executes vkQueueSubmit() on a {@link VkSubmitInfo}
	 *
	 * @return a {@link Future}, triggered when cmd is submitted, containing a {@link Barrier}, triggered when execution of cmd finished
	 */
	public Future<Barrier> submit(@Nullable VkSemaphore[] waitSemaphores, @Nullable int[] waitDstStageMask, @NotNull VkCommandBuffer[] commandBuffers, @Nullable VkSemaphore[] signalSemaphores) {
		return submit(new SubmitQueueEntry(waitSemaphores, waitDstStageMask, commandBuffers, signalSemaphores));
	}
	
	//Entry
	@FunctionalInterface
	public interface Entry {
		
		Barrier run(ManagedQueue queue);
	}
	
	public static class SubmitQueueEntry implements Entry {
		
		private final @Nullable VkSemaphore[] waitSemaphores;
		private final @Nullable int[] waitDstStageMask;
		private final @NotNull VkCommandBuffer[] commandBuffers;
		private final @Nullable VkSemaphore[] signalSemaphores;
		
		public SubmitQueueEntry(@Nullable VkSemaphore[] waitSemaphores, @Nullable int[] waitDstStageMask, @NotNull VkCommandBuffer[] commandBuffers, @Nullable VkSemaphore[] signalSemaphores) {
			this.waitSemaphores = waitSemaphores;
			this.waitDstStageMask = waitDstStageMask;
			this.commandBuffers = commandBuffers;
			this.signalSemaphores = signalSemaphores;
		}
		
		@Override
		public Barrier run(ManagedQueue queue) {
			try (AllocatorFrame frame = Allocator.frame()) {
				VkFence fence = queue.device().vkFencePool().allocate();
				nvkQueueSubmit(queue, 1, mallocStruct(frame, VkSubmitInfo::create, VkSubmitInfo.SIZEOF).set(
						VK_STRUCTURE_TYPE_SUBMIT_INFO,
						0,
						waitSemaphores != null ? waitSemaphores.length : 0,
						waitSemaphores != null ? ArrayBufferLong.alloc(frame, Arrays.stream(waitSemaphores).mapToLong(VkSemaphore::address).toArray()).nioBuffer() : null,
						waitDstStageMask != null ? ArrayBufferInt.alloc(frame, waitDstStageMask).nioBuffer() : null,
						wrapPointer(ArrayBufferPointer.alloc(frame, Arrays.stream(commandBuffers).mapToLong(VkCommandBuffer::address).toArray())),
						signalSemaphores != null ? ArrayBufferLong.alloc(frame, Arrays.stream(signalSemaphores).mapToLong(VkSemaphore::address).toArray()).nioBuffer() : null
				).address(), fence.address());
				Barrier doneBarrier = queue.device().eventAwaiter().add(fence);
				doneBarrier.addHook(fence::free);
				return doneBarrier;
			}
		}
	}
	
	//commandPool
	private final ThreadLocal<SharedCmdPool> commandPools;
	
	public SharedCmdPool poolShared() {
		return commandPools.get();
	}
	
	public VkCommandPool poolShortLived() {
		return commandPools.get().shortLived;
	}
	
	public VkCommandPool poolLongTerm() {
		return commandPools.get().shortLived;
	}
	
	public Barrier recordAndSubmit(Consumer<VkCommandBuffer> function) {
		return recordAndSubmit(cmd -> {
			function.accept(cmd);
			return null;
		});
	}
	
	public Barrier recordAndSubmit(Function<VkCommandBuffer, Object> function) {
		VkCommandBuffer cmd = poolShortLived().allocAndRecordCommandBuffer(VK_COMMAND_BUFFER_LEVEL_PRIMARY, EMPTY_OBJECT_ARRAY, VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT, function);
		Barrier ret = inner(submit(cmd));
		ret.addHook(cmd::free);
		return ret;
	}
	
	public static class SharedCmdPool {
		
		public final VkCommandPool longTerm;
		public final VkCommandPool shortLived;
		
		public SharedCmdPool(VkCommandPool longTerm, VkCommandPool shortLived) {
			this.longTerm = longTerm;
			this.shortLived = shortLived;
		}
	}
}
