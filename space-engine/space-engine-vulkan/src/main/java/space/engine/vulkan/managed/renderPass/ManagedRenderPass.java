package space.engine.vulkan.managed.renderPass;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkClearColorValue;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBufferInheritanceInfo;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSubpassDependency;
import org.lwjgl.vulkan.VkSubpassDescription;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.array.ArrayBufferInt;
import space.engine.buffer.pointer.PointerBufferPointer;
import space.engine.event.Event;
import space.engine.event.SequentialEventBuilder;
import space.engine.sync.future.Future;
import space.engine.vulkan.VkCommandBuffer;
import space.engine.vulkan.VkDevice;
import space.engine.vulkan.VkRenderPass;
import space.engine.vulkan.managed.renderPass.ManagedRenderPass.Attachment.Reference;

import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.lwjgl.vulkan.VK10.*;
import static space.engine.buffer.Allocator.heap;
import static space.engine.lwjgl.LwjglStructAllocator.*;
import static space.engine.vulkan.VkException.assertVk;

public class ManagedRenderPass<INFOS extends Infos> extends VkRenderPass {
	
	//alloc
	public static class Attachment {
		
		public final int
				flags,
				format,
				samples,
				loadOp,
				storeOp,
				stencilLoadOp,
				stencilStoreOp,
				initialLayout,
				finalLayout;
		
		public final float[] clearColor;
		
		private @Nullable ManagedRenderPass renderPass;
		private int id = -1;
		
		//no clear
		public Attachment(int flags, int format, int samples, int loadOp, int storeOp, int stencilLoadOp, int stencilStoreOp, int initialLayout, int finalLayout) {
			this(flags, format, samples, loadOp, storeOp, stencilLoadOp, stencilStoreOp, initialLayout, finalLayout, new float[] {0, 0, 0, 0});
		}
		
		//clearDepthStencil
		public Attachment(int flags, int format, int samples, int loadOp, int storeOp, int stencilLoadOp, int stencilStoreOp, int initialLayout, int finalLayout, float clearDepth) {
			this(flags, format, samples, loadOp, storeOp, stencilLoadOp, stencilStoreOp, initialLayout, finalLayout, clearDepth, 0);
		}
		
		public Attachment(int flags, int format, int samples, int loadOp, int storeOp, int stencilLoadOp, int stencilStoreOp, int initialLayout, int finalLayout, float clearDepth, int clearStencil) {
			this(flags, format, samples, loadOp, storeOp, stencilLoadOp, stencilStoreOp, initialLayout, finalLayout, new float[] {
					clearDepth,
					Float.intBitsToFloat(clearStencil),
					0,
					0
			});
		}
		
		//clearColor
		public Attachment(int flags, int format, int samples, int loadOp, int storeOp, int stencilLoadOp, int stencilStoreOp, int initialLayout, int finalLayout, int[] clearColor) {
			this(flags, format, samples, loadOp, storeOp, stencilLoadOp, stencilStoreOp, initialLayout, finalLayout, new float[] {
					Float.intBitsToFloat(clearColor[0]),
					Float.intBitsToFloat(clearColor[1]),
					Float.intBitsToFloat(clearColor[2]),
					Float.intBitsToFloat(clearColor[3])
			});
		}
		
		public Attachment(int flags, int format, int samples, int loadOp, int storeOp, int stencilLoadOp, int stencilStoreOp, int initialLayout, int finalLayout, float[] clearColor) {
			this.flags = flags;
			this.format = format;
			this.samples = samples;
			this.loadOp = loadOp;
			this.storeOp = storeOp;
			this.stencilLoadOp = stencilLoadOp;
			this.stencilStoreOp = stencilStoreOp;
			this.initialLayout = initialLayout;
			this.finalLayout = finalLayout;
			
			this.clearColor = clearColor;
		}
		
		public @NotNull ManagedRenderPass renderPass() {
			if (renderPass == null)
				throw new IllegalStateException("Renderpass not yet allocated!");
			return renderPass;
		}
		
		public int id() {
			if (id == -1)
				throw new IllegalStateException("Renderpass not yet allocated!");
			return id;
		}
		
		public Reference reference(int layout) {
			return new Reference(layout);
		}
		
		public class Reference {
			
			public final int layout;
			
			private Reference(int layout) {
				this.layout = layout;
			}
			
			public Attachment attachment() {
				return Attachment.this;
			}
		}
	}
	
	public static class Subpass {
		
		public final int
				flags,
				pipelineBindPoint;
		public final @Nullable Reference[]
				inputAttachments,
				colorAttachments,
				resolveAttachments;
		public final @Nullable Reference depthStencilAttachment;
		public final @Nullable Attachment[] preserveAttachments;
		
		private @Nullable ManagedRenderPass renderPass;
		private int id = -1;
		private @Nullable VkCommandBufferInheritanceInfo inheritanceInfo;
		
		public Subpass(int flags, int pipelineBindPoint, @Nullable Reference[] inputAttachments, @Nullable Reference[] colorAttachments, @Nullable Reference[] resolveAttachments, @Nullable Reference depthStencilAttachment, @Nullable Attachment[] preserveAttachments) {
			this.flags = flags;
			this.pipelineBindPoint = pipelineBindPoint;
			this.inputAttachments = inputAttachments;
			this.colorAttachments = colorAttachments;
			this.resolveAttachments = resolveAttachments;
			this.depthStencilAttachment = depthStencilAttachment;
			this.preserveAttachments = preserveAttachments;
		}
		
		public @NotNull ManagedRenderPass renderPass() {
			if (renderPass == null)
				throw new IllegalStateException("Renderpass not yet allocated!");
			return renderPass;
		}
		
		public int id() {
			if (id == -1)
				throw new IllegalStateException("Renderpass not yet allocated!");
			return id;
		}
		
		/**
		 * @deprecated because RADV driver bug requiring inheritanceInfo.framebuffer to be set. see https://bugs.freedesktop.org/show_bug.cgi?id=110810
		 * Use {@link ManagedFrameBuffer#inheritanceInfo(Infos, Subpass)}  instead
		 */
		@Deprecated
		public @NotNull VkCommandBufferInheritanceInfo inheritanceInfo() {
			if (inheritanceInfo == null)
				throw new IllegalStateException("Renderpass not yet allocated!");
			return inheritanceInfo;
		}
	}
	
	public static class SubpassDependency {
		
		public final @Nullable Subpass
				srcSubpass,
				dstSubpass;
		public final int
				srcStageMask,
				dstStageMask,
				srcAccessMask,
				dstAccessMask,
				dependencyFlags;
		
		private @Nullable ManagedRenderPass renderPass;
		private int id = -1;
		
		public SubpassDependency(@Nullable Subpass srcSubpass, @Nullable Subpass dstSubpass, int srcStageMask, int dstStageMask, int srcAccessMask, int dstAccessMask, int dependencyFlags) {
			this.srcSubpass = srcSubpass;
			this.dstSubpass = dstSubpass;
			this.srcStageMask = srcStageMask;
			this.dstStageMask = dstStageMask;
			this.srcAccessMask = srcAccessMask;
			this.dstAccessMask = dstAccessMask;
			this.dependencyFlags = dependencyFlags;
		}
		
		public @NotNull ManagedRenderPass renderPass() {
			if (renderPass == null)
				throw new IllegalStateException("Renderpass not yet allocated!");
			return renderPass;
		}
		
		public int id() {
			if (id == -1)
				throw new IllegalStateException("Renderpass not yet allocated!");
			return id;
		}
	}
	
	public static <INFOS extends Infos> ManagedRenderPass<INFOS> alloc(VkDevice device, @NotNull Attachment[] attachments, @NotNull Subpass[] subpasses, @NotNull SubpassDependency[] subpassDependencies, Object[] parents) {
		for (int i = 0; i < attachments.length; i++) {
			if (attachments[i].renderPass != null)
				throw new IllegalArgumentException("Attachment attachments[" + i + "] already used!");
			attachments[i].id = i;
		}
		for (int i = 0; i < subpasses.length; i++) {
			if (subpasses[i].renderPass != null)
				throw new IllegalArgumentException("Attachment attachments[" + i + "] already used!");
			subpasses[i].id = i;
		}
		for (int i = 0; i < subpassDependencies.length; i++) {
			if (subpassDependencies[i].renderPass != null)
				throw new IllegalArgumentException("Attachment attachments[" + i + "] already used!");
			subpassDependencies[i].id = i;
		}
		
		ManagedRenderPass<INFOS> renderPass;
		try (AllocatorFrame frame = Allocator.frame()) {
			VkRenderPassCreateInfo info = mallocStruct(frame, VkRenderPassCreateInfo::create, VkRenderPassCreateInfo.SIZEOF).set(
					VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO,
					0,
					0,
					allocBuffer(frame, VkAttachmentDescription::create, VkAttachmentDescription.SIZEOF,
								Arrays.stream(attachments)
									  .map(att -> (Consumer<VkAttachmentDescription>) attachmentDescription -> attachmentDescription.set(
											  att.flags,
											  att.format,
											  att.samples,
											  att.loadOp,
											  att.storeOp,
											  att.stencilLoadOp,
											  att.stencilStoreOp,
											  att.initialLayout,
											  att.finalLayout
									  ))
									  .collect(Collectors.toUnmodifiableList())
					),
					allocBuffer(frame, VkSubpassDescription::create, VkSubpassDescription.SIZEOF,
								Arrays.stream(subpasses)
									  .map(subpass -> (Consumer<VkSubpassDescription>) subpassDescription -> subpassDescription.set(
											  subpass.flags,
											  subpass.pipelineBindPoint,
											  createAttachmentReferenceBuffer(frame, subpass.inputAttachments),
											  subpass.colorAttachments != null ? subpass.colorAttachments.length : 0,
											  createAttachmentReferenceBuffer(frame, subpass.colorAttachments),
											  createAttachmentReferenceBuffer(frame, subpass.resolveAttachments),
											  subpass.depthStencilAttachment == null ? null :
													  mallocStruct(frame, VkAttachmentReference::create, VkAttachmentReference.SIZEOF).set(
															  subpass.depthStencilAttachment.attachment().id,
															  subpass.depthStencilAttachment.layout
													  ),
											  createAttachmentBuffer(frame, subpass.preserveAttachments)
									  ))
									  .collect(Collectors.toUnmodifiableList())
					),
					allocBuffer(frame, VkSubpassDependency::create, VkSubpassDependency.SIZEOF,
								Arrays.stream(subpassDependencies)
									  .map(subpassDependency -> (Consumer<VkSubpassDependency>) dependencyDescription -> dependencyDescription.set(
											  subpassDependency.srcSubpass == null ? VK_SUBPASS_EXTERNAL : subpassDependency.srcSubpass.id,
											  subpassDependency.dstSubpass == null ? VK_SUBPASS_EXTERNAL : subpassDependency.dstSubpass.id,
											  subpassDependency.srcStageMask,
											  subpassDependency.dstStageMask,
											  subpassDependency.srcAccessMask,
											  subpassDependency.dstAccessMask,
											  subpassDependency.dependencyFlags
									  ))
									  .collect(Collectors.toUnmodifiableList())
					)
			);
			
			PointerBufferPointer ptr = PointerBufferPointer.malloc(frame);
			assertVk(nvkCreateRenderPass(device, info.address(), 0, ptr.address()));
			renderPass = new ManagedRenderPass<>(ptr.getPointer(), device, attachments, subpasses, subpassDependencies, parents);
		}
		
		for (Attachment attachment : attachments)
			attachment.renderPass = renderPass;
		for (Subpass subpass : subpasses) {
			subpass.renderPass = renderPass;
			subpass.inheritanceInfo = mallocStruct(heap(), VkCommandBufferInheritanceInfo::create, VkCommandBufferInheritanceInfo.SIZEOF, new Object[] {renderPass}).set(
					VK_STRUCTURE_TYPE_COMMAND_BUFFER_INHERITANCE_INFO,
					0,
					renderPass.address(),
					subpass.id(),
					0,
					false,
					0,
					0
			);
		}
		for (SubpassDependency subpassDependency : subpassDependencies)
			subpassDependency.renderPass = renderPass;
		
		return renderPass;
	}
	
	private static @Nullable IntBuffer createAttachmentBuffer(AllocatorFrame frame, @Nullable Attachment[] attachments) {
		if (attachments == null || attachments.length == 0)
			return null;
		return ArrayBufferInt.alloc(frame,
									Arrays.stream(attachments)
										  .mapToInt(att -> att.id)
										  .toArray()
		).nioBuffer();
	}
	
	private static @Nullable VkAttachmentReference.Buffer createAttachmentReferenceBuffer(AllocatorFrame frame, @Nullable Reference[] references) {
		if (references == null || references.length == 0)
			return null;
		return allocBuffer(frame, VkAttachmentReference::create, VkAttachmentReference.SIZEOF,
						   Arrays.stream(references)
								 .map(ref -> (Consumer<VkAttachmentReference>) attachmentReference -> attachmentReference.set(
										 ref.attachment().id,
										 ref.layout
								 ))
								 .collect(Collectors.toUnmodifiableList())
		);
	}
	
	//object
	private ManagedRenderPass(long address, @NotNull VkDevice device, @NotNull Attachment[] attachments, @NotNull Subpass[] subpasses, @NotNull SubpassDependency[] subpassDependencies, @NotNull Object[] parents) {
		super(address, device, VkRenderPass.Storage::new, parents);
		this.attachments = attachments;
		this.subpasses = subpasses;
		this.subpassDependencies = subpassDependencies;
		
		this.vkClearValues = allocBuffer(heap(), VkClearValue::create, VkClearValue.SIZEOF, new Object[] {this},
										 Arrays.stream(attachments)
											   .map(attachment -> (Consumer<VkClearValue>) vkClearValue -> {
												   try (AllocatorFrame frame = Allocator.frame()) {
													   vkClearValue.color(
															   mallocStruct(frame, VkClearColorValue::create, VkClearColorValue.SIZEOF)
																	   .float32(0, attachment.clearColor[0])
																	   .float32(1, attachment.clearColor[0])
																	   .float32(2, attachment.clearColor[0])
																	   .float32(3, attachment.clearColor[0])
													   );
												   }
											   })
											   .collect(Collectors.toUnmodifiableList())
		);
	}
	
	//sub-resources
	private final @NotNull Attachment[] attachments;
	private final @NotNull Subpass[] subpasses;
	private final @NotNull SubpassDependency[] subpassDependencies;
	
	public @NotNull Attachment[] attachments() {
		return attachments;
	}
	
	public @NotNull Subpass[] subpasses() {
		return subpasses;
	}
	
	public @NotNull SubpassDependency[] subpassDependencies() {
		return subpassDependencies;
	}
	
	//callbacks
	final @NotNull SequentialEventBuilder<Callback<INFOS>> callbacks = new SequentialEventBuilder<>();
	
	public interface Callback<INFOS extends Infos> {
		
		@NotNull List<Future<VkCommandBuffer[]>> getCmdBuffers(@NotNull ManagedFrameBuffer<INFOS> render, INFOS infos);
	}
	
	public Event<Callback<INFOS>> callbacks() {
		return callbacks;
	}
	
	//vkClearValues
	private final VkClearValue.Buffer vkClearValues;
	
	public VkClearValue.Buffer vkClearValues() {
		return vkClearValues;
	}
}
