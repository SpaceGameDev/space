package space.engine.vulkan.managed.renderPass;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.vulkan.VkCommandBufferInheritanceInfo;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import space.engine.barrier.Barrier;
import space.engine.barrier.DelayTask;
import space.engine.barrier.future.Future;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.array.ArrayBufferLong;
import space.engine.freeable.Freeable;
import space.engine.freeable.Freeable.CleanerWrapper;
import space.engine.orderingGuarantee.SequentialOrderingGuarantee;
import space.engine.vulkan.VkCommandBuffer;
import space.engine.vulkan.VkCommandPool;
import space.engine.vulkan.VkFramebuffer;
import space.engine.vulkan.VkImageView;
import space.engine.vulkan.VkInstance;
import space.engine.vulkan.VkSemaphore;
import space.engine.vulkan.managed.device.ManagedDevice;
import space.engine.vulkan.managed.device.ManagedQueue;
import space.engine.vulkan.managed.renderPass.ManagedRenderPass.Subpass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.lwjgl.vulkan.VK10.*;
import static space.engine.barrier.Barrier.*;
import static space.engine.buffer.Allocator.heap;
import static space.engine.freeable.Freeable.addIfNotContained;
import static space.engine.lwjgl.LwjglStructAllocator.mallocStruct;

public class ManagedFrameBuffer<INFOS extends Infos> implements CleanerWrapper {
	
	public ManagedFrameBuffer(@NotNull ManagedRenderPass<INFOS> renderPass, @NotNull ManagedQueue queue, @NotNull Object[] images, int width, int height, int layers, Object[] parents) {
		this.renderPass = renderPass;
		this.queue = queue;
		this.imagesInput = images;
		this.imagesFlat = Arrays.stream(images)
								.flatMap(o -> {
									if (o instanceof VkImageView) {
										return Stream.of((VkImageView) o);
									} else if (o instanceof Object[]) {
										return Arrays.stream((Object[]) o).filter(o2 -> {
											if (!(o2 instanceof VkImageView))
												throw new IllegalArgumentException();
											return true;
										}).map(VkImageView.class::cast);
									}
									throw new IllegalArgumentException();
								})
								.toArray(VkImageView[]::new);
		//noinspection RedundantCast
		this.storage = Freeable.createDummy(this, addIfNotContained(addIfNotContained(parents, renderPass), (Object[]) imagesFlat));
		this.width = width;
		this.height = height;
		this.layers = layers;
		
		int outputWidth = -1;
		for (Object image : images) {
			if (image instanceof Object[]) {
				Object[] imageViews = (Object[]) image;
				if (outputWidth == -1) {
					outputWidth = imageViews.length;
				} else if (outputWidth != imageViews.length) {
					throw new IllegalArgumentException("images outputWidth differs! Found widths " + outputWidth + " and " + imageViews.length + ".");
				}
			}
		}
		if (outputWidth == -1)
			outputWidth = 1;
		this.outputWidth = outputWidth;
		
		this.framebuffers = IntStream
				.range(0, outputWidth)
				.mapToObj(i -> {
					try (AllocatorFrame frame = Allocator.frame()) {
						return VkFramebuffer.alloc(mallocStruct(frame, VkFramebufferCreateInfo::create, VkFramebufferCreateInfo.SIZEOF).set(
								VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO,
								0,
								0,
								renderPass.address(),
								ArrayBufferLong.alloc(heap(),
													  Arrays.stream(images)
															.map(image -> {
																if (image instanceof VkImageView) {
																	return (VkImageView) image;
																} else if (image instanceof Object[]) {
																	return ((VkImageView) ((Object[]) image)[i]);
																}
																throw new IllegalArgumentException();
															})
															.mapToLong(VkImageView::address)
															.toArray(),
													  new Object[] {frame}).nioBuffer(),
								width,
								height,
								layers
						), renderPass.device(), new Object[] {this});
					}
				})
				.toArray(VkFramebuffer[]::new);
		
		VkCommandPool mainBufferPool = VkCommandPool.alloc(VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT, queue.queueFamily(), queue.device(), new Object[] {this});
		this.mainBuffer = mainBufferPool.allocCommandBuffer(VK_COMMAND_BUFFER_LEVEL_PRIMARY, new Object[] {this});
		
		this.inheritanceInfos = Arrays
				.stream(framebuffers)
				.map(framebuffer -> Arrays
						.stream(renderPass.subpasses())
						.map(subpass -> mallocStruct(heap(), VkCommandBufferInheritanceInfo::create, VkCommandBufferInheritanceInfo.SIZEOF, new Object[] {this}).set(
								VK_STRUCTURE_TYPE_COMMAND_BUFFER_INHERITANCE_INFO,
								0,
								renderPass.address(),
								subpass.id(),
								framebuffer.address(),
								false,
								0,
								0
						))
						.toArray(VkCommandBufferInheritanceInfo[]::new))
				.toArray(VkCommandBufferInheritanceInfo[][]::new);
	}
	
	//parents
	private final @NotNull ManagedRenderPass<INFOS> renderPass;
	private final @NotNull ManagedQueue queue;
	private final @NotNull Object[] imagesInput;
	private final @NotNull VkImageView[] imagesFlat;
	
	public ManagedRenderPass<INFOS> renderPass() {
		return renderPass;
	}
	
	public ManagedQueue queue() {
		return queue;
	}
	
	public @NotNull ManagedDevice device() {
		return queue.device();
	}
	
	public @NotNull VkInstance instance() {
		return queue.instance();
	}
	
	public @NotNull Object[] imagesInput() {
		return imagesInput;
	}
	
	public @NotNull VkImageView[] imagesFlat() {
		return imagesFlat;
	}
	
	//storage
	private final @NotNull Freeable storage;
	
	@Override
	public @NotNull Freeable getStorage() {
		return storage;
	}
	
	//dimensions
	private final int width, height, layers;
	private final int outputWidth;
	
	public int width() {
		return width;
	}
	
	public int height() {
		return height;
	}
	
	public int layers() {
		return layers;
	}
	
	public int outputWidth() {
		return outputWidth;
	}
	
	//framebuffers
	private final @NotNull VkFramebuffer[] framebuffers;
	
	public @NotNull VkFramebuffer[] framebuffers() {
		return framebuffers;
	}
	
	//render
	private final @NotNull SequentialOrderingGuarantee orderingGuarantee = new SequentialOrderingGuarantee();
	private final VkCommandBuffer mainBuffer;
	
	public Future<Barrier> render(INFOS infos, VkSemaphore[] waitSemaphores, int[] waitDstStageMasks, VkSemaphore[] signalSemaphores) {
		return orderingGuarantee.next(prev -> prev.thenFuture(() -> {
			@NotNull Subpass[] subpasses = renderPass.subpasses();
			
			List<List<Future<VkCommandBuffer[]>>> cmdBuffersInput = new ArrayList<>();
			renderPass.callbacks.runImmediatelyThrowIfWait(callback -> cmdBuffersInput.add(callback.getCmdBuffers(this, infos)));
			
			List<Future<VkCommandBuffer[]>> cmdBuffersSorted = Arrays
					.stream(renderPass.subpasses())
					.map(subpass -> {
							 List<Future<VkCommandBuffer[]>> futures = cmdBuffersInput
									 .stream()
									 .map(list -> list.get(subpass.id()))
									 .collect(Collectors.toUnmodifiableList());
						return when(futures).thenFuture(() -> futures
									 .stream()
									 .map(Future::assertGet)
									 .flatMap(Arrays::stream)
									 .toArray(VkCommandBuffer[]::new)
						);
						 }
					)
					.collect(Collectors.toUnmodifiableList());
			
			throw new DelayTask(when(cmdBuffersSorted).<Barrier>thenFuture(() -> {
				Future<Barrier> ret;
				try (AllocatorFrame frame = Allocator.frame()) {
					mainBuffer.begin(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
					vkCmdBeginRenderPass(mainBuffer, mallocStruct(frame, VkRenderPassBeginInfo::create, VkRenderPassBeginInfo.SIZEOF).set(
							VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO,
							0,
							renderPass.address(),
							framebuffers[infos.frameBufferIndex].address(),
							mallocStruct(frame, VkRect2D::create, VkRect2D.SIZEOF).set(
									mallocStruct(frame, VkOffset2D::create, VkOffset2D.SIZEOF).set(
											0, 0
									),
									mallocStruct(frame, VkExtent2D::create, VkExtent2D.SIZEOF).set(
											width, height
									)
							),
							renderPass.vkClearValues()
					), VK_SUBPASS_CONTENTS_SECONDARY_COMMAND_BUFFERS);
					
					for (int i = 0; i < subpasses.length; i++) {
						VkCommandBuffer[] vkCommandBuffers = cmdBuffersSorted.get(i).assertGet();
						if (vkCommandBuffers.length > 0) {
							ArrayBufferLong vkCommandBufferPtrs = ArrayBufferLong.alloc(heap(), Arrays.stream(vkCommandBuffers).mapToLong(VkCommandBuffer::address).toArray(), new Object[] {frame});
							nvkCmdExecuteCommands(mainBuffer, (int) vkCommandBufferPtrs.length(), vkCommandBufferPtrs.address());
						}
						
						if (i != subpasses.length - 1) //all except last
							vkCmdNextSubpass(mainBuffer, VK_SUBPASS_CONTENTS_SECONDARY_COMMAND_BUFFERS);
					}
					
					vkCmdEndRenderPass(mainBuffer);
					mainBuffer.end();
					
					ret = queue.submit(
							waitSemaphores,
							waitDstStageMasks,
							new VkCommandBuffer[] {mainBuffer},
							signalSemaphores
					);
				}
				
				inner(ret).addHook(() -> {
					mainBuffer.reset(0);
					infos.frameDone.triggerNow();
				});
				throw new DelayTask(ret);
			}));
		}));
	}
	
	//inheritanceInfo
	private final VkCommandBufferInheritanceInfo[][] inheritanceInfos;
	
	public VkCommandBufferInheritanceInfo inheritanceInfo(INFOS infos, Subpass subpass) {
		if (subpass.renderPass() != renderPass)
			throw new IllegalArgumentException("Renderpass don't match!");
		return inheritanceInfos[infos.frameBufferIndex][subpass.id()];
	}
}
