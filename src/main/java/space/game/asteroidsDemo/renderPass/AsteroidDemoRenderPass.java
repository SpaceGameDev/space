package space.game.asteroidsDemo.renderPass;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.vulkan.VkRect2D;
import space.engine.event.Event;
import space.engine.freeableStorage.Freeable;
import space.engine.freeableStorage.Freeable.FreeableWrapper;
import space.engine.freeableStorage.FreeableStorage;
import space.engine.vulkan.VkImageView;
import space.engine.vulkan.managed.device.ManagedDevice;
import space.engine.vulkan.managed.device.ManagedQueue;
import space.engine.vulkan.managed.renderPass.ManagedFrameBuffer;
import space.engine.vulkan.managed.renderPass.ManagedRenderPass;
import space.engine.vulkan.managed.renderPass.ManagedRenderPass.Attachment;
import space.engine.vulkan.managed.renderPass.ManagedRenderPass.Attachment.Reference;
import space.engine.vulkan.managed.renderPass.ManagedRenderPass.Callback;
import space.engine.vulkan.managed.renderPass.ManagedRenderPass.Subpass;
import space.engine.vulkan.managed.renderPass.ManagedRenderPass.SubpassDependency;
import space.engine.vulkan.managed.surface.ManagedSwapchain;
import space.engine.vulkan.vma.VmaImage;

import static org.lwjgl.util.vma.Vma.*;
import static org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
import static org.lwjgl.vulkan.VK10.*;

public class AsteroidDemoRenderPass implements FreeableWrapper {
	
	public AsteroidDemoRenderPass(ManagedDevice device, VkRect2D swapExtend, int outputImageFormat, Object[] parents) {
		this.device = device;
		this.swapExtend = swapExtend;
		this.storage = Freeable.createDummy(this, parents);
		
		//renderPass
		attachmentOutputColor = new Attachment(
				0,
				outputImageFormat,
				VK_SAMPLE_COUNT_1_BIT,
				VK_ATTACHMENT_LOAD_OP_CLEAR,
				VK_ATTACHMENT_STORE_OP_STORE,
				VK_ATTACHMENT_LOAD_OP_DONT_CARE,
				VK_ATTACHMENT_STORE_OP_DONT_CARE,
				VK_IMAGE_LAYOUT_UNDEFINED,
				VK_IMAGE_LAYOUT_PRESENT_SRC_KHR
		);
		attachmentDepth = new Attachment(
				0,
				VK_FORMAT_D32_SFLOAT,
				VK_SAMPLE_COUNT_1_BIT,
				VK_ATTACHMENT_LOAD_OP_CLEAR,
				VK_ATTACHMENT_STORE_OP_STORE,
				VK_ATTACHMENT_LOAD_OP_DONT_CARE,
				VK_ATTACHMENT_STORE_OP_DONT_CARE,
				VK_IMAGE_LAYOUT_UNDEFINED,
				VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL,
				1.0f
		);
		subpassRender = new Subpass(
				0,
				VK_PIPELINE_BIND_POINT_GRAPHICS,
				null,
				new Reference[] {attachmentOutputColor.reference(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)},
				null,
				attachmentDepth.reference(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL),
				null
		);
		dependencySubpassRenderAttachmentOutputColorExternal = new SubpassDependency(
				null,
				subpassRender,
				VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT,
				VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT,
				0,
				VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT,
				0
		);
		dependencySubpassRenderAttachmentDepthExternal = new SubpassDependency(
				null,
				subpassRender,
				VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT,
				VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT,
				0,
				VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_READ_BIT | VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT,
				0
		);
		
		this.renderPass = ManagedRenderPass.alloc(device, new Attachment[] {
				attachmentOutputColor, attachmentDepth
		}, new Subpass[] {
				subpassRender
		}, new SubpassDependency[] {
				dependencySubpassRenderAttachmentOutputColorExternal, dependencySubpassRenderAttachmentDepthExternal
		}, new Object[] {this});
	}
	
	//components
	public final Attachment attachmentOutputColor, attachmentDepth;
	public final Subpass subpassRender;
	public final SubpassDependency dependencySubpassRenderAttachmentOutputColorExternal, dependencySubpassRenderAttachmentDepthExternal;
	
	//parents
	private final ManagedDevice device;
	private final VkRect2D swapExtend;
	
	public ManagedDevice device() {
		return device;
	}
	
	public VkRect2D swapExtend() {
		return swapExtend;
	}
	
	//storage
	private final @NotNull Freeable storage;
	
	@Override
	public @NotNull Freeable getStorage() {
		return storage;
	}
	
	//renderPass
	private final ManagedRenderPass<AsteroidDemoInfos> renderPass;
	
	public ManagedRenderPass<AsteroidDemoInfos> renderPass() {
		return renderPass;
	}
	
	public Event<Callback<AsteroidDemoInfos>> callbacks() {
		return renderPass.callbacks();
	}
	
	public ManagedFrameBuffer<AsteroidDemoInfos> createManagedFrameBuffer(ManagedSwapchain<?> swapchain, ManagedQueue queue, Object[] parents) {
		FreeableStorage side = Freeable.createDummy(parents);
		
		VmaImage depthImage = VmaImage.alloc(
				0,
				VK_IMAGE_TYPE_2D,
				VK_FORMAT_D32_SFLOAT,
				swapchain.width(),
				swapchain.height(),
				1,
				1,
				1,
				1,
				VK_IMAGE_TILING_OPTIMAL,
				VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT,
				VK_IMAGE_LAYOUT_UNDEFINED,
				VMA_ALLOCATION_CREATE_DEDICATED_MEMORY_BIT,
				VMA_MEMORY_USAGE_GPU_ONLY,
				device,
				new Object[] {side}
		);
		VkImageView depthImageView = VkImageView.alloc(
				depthImage,
				0,
				VK_IMAGE_VIEW_TYPE_2D,
				VK_IMAGE_ASPECT_DEPTH_BIT,
				0,
				1,
				0,
				1,
				new Object[] {side}
		);
		
		return new ManagedFrameBuffer<>(
				renderPass(),
				queue,
				new Object[] {
						swapchain.imageViews(),
						depthImageView
				},
				swapchain.width(),
				swapchain.height(),
				1,
				new Object[] {side}
		);
	}
}
