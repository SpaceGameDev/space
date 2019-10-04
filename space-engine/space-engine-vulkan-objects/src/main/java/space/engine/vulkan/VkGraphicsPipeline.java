package space.engine.vulkan;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import space.engine.barrier.Barrier;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.pointer.PointerBufferPointer;
import space.engine.freeable.Cleaner;
import space.engine.freeable.Freeable;
import space.engine.freeable.Freeable.CleanerWrapper;

import java.util.function.BiFunction;

import static org.lwjgl.vulkan.VK10.*;
import static space.engine.freeable.Freeable.addIfNotContained;
import static space.engine.vulkan.VkException.assertVk;

public class VkGraphicsPipeline implements CleanerWrapper {
	
	//alloc
	public static @NotNull VkGraphicsPipeline alloc(VkGraphicsPipelineCreateInfo info, @NotNull VkRenderPass renderPass, @NotNull Object[] parents) {
		try (AllocatorFrame frame = Allocator.frame()) {
			PointerBufferPointer ptr = PointerBufferPointer.malloc(frame);
			assertVk(nvkCreateGraphicsPipelines(renderPass.device(), 0, 1, info.address(), 0, ptr.address()));
			return create(ptr.getPointer(), renderPass, parents);
		}
	}
	
	//create
	public static @NotNull VkGraphicsPipeline create(long address, @NotNull VkRenderPass renderPass, @NotNull Object[] parents) {
		return new VkGraphicsPipeline(address, renderPass, Storage::new, parents);
	}
	
	public static @NotNull VkGraphicsPipeline wrap(long address, @NotNull VkRenderPass renderPass, @NotNull Object[] parents) {
		return new VkGraphicsPipeline(address, renderPass, Freeable::createDummy, parents);
	}
	
	//const
	public VkGraphicsPipeline(long address, @NotNull VkRenderPass renderPass, @NotNull BiFunction<VkGraphicsPipeline, Object[], Freeable> storageCreator, @NotNull Object[] parents) {
		this.address = address;
		this.renderPass = renderPass;
		this.storage = storageCreator.apply(this, addIfNotContained(parents, renderPass));
	}
	
	//parents
	private final @NotNull VkRenderPass renderPass;
	
	public VkRenderPass renderPass() {
		return renderPass;
	}
	
	public VkDevice device() {
		return renderPass.device();
	}
	
	public VkInstance instance() {
		return renderPass.instance();
	}
	
	//address
	private final long address;
	
	public long address() {
		return address;
	}
	
	//storage
	private final @NotNull Freeable storage;
	
	@Override
	public @NotNull Freeable getStorage() {
		return storage;
	}
	
	public static class Storage extends Cleaner {
		
		private final @NotNull VkDevice device;
		private final long address;
		
		public Storage(@NotNull VkGraphicsPipeline o, @NotNull Object[] parents) {
			super(o, parents);
			this.device = o.device();
			this.address = o.address;
		}
		
		@Override
		protected @NotNull Barrier handleFree() {
			vkDestroyPipeline(device, address, null);
			return Barrier.DONE_BARRIER;
		}
	}
	
	//methods
	public void vkCmdBindPipeline(VkCommandBuffer cmdBuffer) {
		VK10.vkCmdBindPipeline(cmdBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, address);
	}
}
