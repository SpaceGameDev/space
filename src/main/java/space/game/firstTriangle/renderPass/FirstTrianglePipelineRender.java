package space.game.firstTriangle.renderPass;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import org.lwjgl.vulkan.VkViewport;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.StringConverter;
import space.engine.buffer.array.ArrayBufferFloat;
import space.engine.buffer.array.ArrayBufferLong;
import space.engine.buffer.pointer.PointerBufferLong;
import space.engine.freeableStorage.Freeable;
import space.engine.freeableStorage.Freeable.FreeableWrapper;
import space.engine.vulkan.VkCommandBuffer;
import space.engine.vulkan.VkDescriptorSet;
import space.engine.vulkan.VkDescriptorSetLayout;
import space.engine.vulkan.VkGraphicsPipeline;
import space.engine.vulkan.VkPipelineLayout;
import space.engine.vulkan.VkShaderModule;
import space.engine.vulkan.managed.device.ManagedDevice;

import java.io.IOException;
import java.util.Objects;

import static org.lwjgl.vulkan.VK10.*;
import static space.engine.lwjgl.LwjglStructAllocator.*;
import static space.engine.primitive.Primitives.FP32;

public class FirstTrianglePipelineRender implements FreeableWrapper {
	
	public FirstTrianglePipelineRender(@NotNull FirstTriangleRenderPass renderPass, @NotNull Object[] parents) {
		this.renderPass = renderPass;
		this.storage = Freeable.createDummy(this, parents);
		
		ManagedDevice device = renderPass.device();
		VkRect2D swapExtend = renderPass.swapExtend();
		
		try (AllocatorFrame frame = Allocator.frame()) {
			descriptorSetLayout = VkDescriptorSetLayout.alloc(mallocStruct(frame, VkDescriptorSetLayoutCreateInfo::create, VkDescriptorSetLayoutCreateInfo.SIZEOF).set(
					VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO,
					0,
					0,
					allocBuffer(frame, VkDescriptorSetLayoutBinding::create, VkDescriptorSetLayoutBinding.SIZEOF,
								vkDescriptorSetLayoutBinding -> vkDescriptorSetLayoutBinding.set(
										0,
										VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER,
										1,
										VK_SHADER_STAGE_VERTEX_BIT,
										null
								)
					)
			
			), device, new Object[] {this});
		}
		
		try (AllocatorFrame frame = Allocator.frame()) {
			pipelineLayout = VkPipelineLayout.alloc(mallocStruct(frame, VkPipelineLayoutCreateInfo::create, VkPipelineLayoutCreateInfo.SIZEOF).set(
					VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO,
					0,
					0,
					ArrayBufferLong.alloc(frame, new long[] {descriptorSetLayout.address()}).nioBuffer(),
					null
			), device, new VkDescriptorSetLayout[0], new Object[] {this});
		}
		
		try (AllocatorFrame frame = Allocator.frame()) {
			VkShaderModule shaderModuleVert = VkShaderModule.alloc(device,
																   Objects.requireNonNull(FirstTrianglePipelineRender.class.getResourceAsStream("render.vert.spv")).readAllBytes(),
																   new Object[] {frame});
			VkShaderModule shaderModuleFrag = VkShaderModule.alloc(device,
																   Objects.requireNonNull(FirstTrianglePipelineRender.class.getResourceAsStream("render.frag.spv")).readAllBytes(),
																   new Object[] {frame});
			
			this.pipeline = VkGraphicsPipeline.alloc(mallocStruct(frame, VkGraphicsPipelineCreateInfo::create, VkGraphicsPipelineCreateInfo.SIZEOF).set(
					VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO,
					0,
					0,
					allocBuffer(frame, VkPipelineShaderStageCreateInfo::create, VkPipelineShaderStageCreateInfo.SIZEOF,
								vkPipelineShaderStageCreateInfo -> vkPipelineShaderStageCreateInfo.set(
										VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO,
										0,
										0,
										VK_SHADER_STAGE_VERTEX_BIT,
										shaderModuleVert.address(),
										StringConverter.stringToUTF8(frame, "main", true).nioBuffer(),
										null
								),
								vkPipelineShaderStageCreateInfo -> vkPipelineShaderStageCreateInfo.set(
										VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO,
										0,
										0,
										VK_SHADER_STAGE_FRAGMENT_BIT,
										shaderModuleFrag.address(),
										StringConverter.stringToUTF8(frame, "main", true).nioBuffer(),
										null
								)
					),
					mallocStruct(frame, VkPipelineVertexInputStateCreateInfo::create, VkPipelineVertexInputStateCreateInfo.SIZEOF).set(
							VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO,
							0,
							0,
							allocBuffer(frame, VkVertexInputBindingDescription::create, VkVertexInputBindingDescription.SIZEOF, vkVertexInputBindingDescription -> vkVertexInputBindingDescription
									.binding(0)
									.stride(FP32.bytes * 9)
									.inputRate(VK_VERTEX_INPUT_RATE_VERTEX)
							),
							allocBuffer(frame, VkVertexInputAttributeDescription::create, VkVertexInputAttributeDescription.SIZEOF,
										inPosition -> inPosition.set(
												0,
												0,
												VK_FORMAT_R32G32B32_SFLOAT,
												0
										),
										inNormal -> inNormal.set(
												1,
												0,
												VK_FORMAT_R32G32B32_SFLOAT,
												FP32.multiply(3)
										),
										inColor -> inColor.set(
												2,
												0,
												VK_FORMAT_R32G32B32_SFLOAT,
												FP32.multiply(6)
										)
							)
					),
					mallocStruct(frame, VkPipelineInputAssemblyStateCreateInfo::create, VkPipelineInputAssemblyStateCreateInfo.SIZEOF).set(
							VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO,
							0,
							0,
							VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST,
							false
					
					),
					null,
					mallocStruct(frame, VkPipelineViewportStateCreateInfo::create, VkPipelineViewportStateCreateInfo.SIZEOF).set(
							VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO,
							0,
							0,
							1,
							allocBuffer(frame, VkViewport::create, VkViewport.SIZEOF, vkViewPort -> vkViewPort.set(
									swapExtend.offset().x(),
									swapExtend.offset().y(),
									swapExtend.extent().width(),
									swapExtend.extent().height(),
									0,
									1
							)),
							1,
							wrapBuffer(VkRect2D::create, swapExtend)
					),
					mallocStruct(frame, VkPipelineRasterizationStateCreateInfo::create, VkPipelineRasterizationStateCreateInfo.SIZEOF).set(
							VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO,
							0,
							0,
							false,
							false,
							VK_POLYGON_MODE_FILL,
							VK_CULL_MODE_BACK_BIT,
							VK_FRONT_FACE_CLOCKWISE,
							false,
							0,
							0,
							0,
							1
					),
					mallocStruct(frame, VkPipelineMultisampleStateCreateInfo::create, VkPipelineMultisampleStateCreateInfo.SIZEOF).set(
							VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO,
							0,
							0,
							1,
							false,
							1.0f,
							null,
							false,
							false
					),
					null,
					mallocStruct(frame, VkPipelineColorBlendStateCreateInfo::create, VkPipelineColorBlendStateCreateInfo.SIZEOF).set(
							VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO,
							0,
							0,
							false,
							VK_LOGIC_OP_COPY,
							allocBuffer(frame, VkPipelineColorBlendAttachmentState::create, VkPipelineColorBlendAttachmentState.SIZEOF,
										vkPipelineColorBlendAttachmentState -> vkPipelineColorBlendAttachmentState.set(
												false,
												VK_BLEND_FACTOR_ONE,
												VK_BLEND_FACTOR_ZERO,
												VK_BLEND_OP_ADD,
												VK_BLEND_FACTOR_ONE,
												VK_BLEND_FACTOR_ZERO,
												VK_BLEND_OP_ADD,
												VK_COLOR_COMPONENT_R_BIT | VK_COLOR_COMPONENT_G_BIT | VK_COLOR_COMPONENT_B_BIT | VK_COLOR_COMPONENT_A_BIT
										)
							),
							ArrayBufferFloat.alloc(frame, new float[] {0, 0, 0, 0}).nioBuffer()
					),
					null,
					pipelineLayout.address(),
					renderPass.renderPass().address(),
					0,
					0,
					-1
			), renderPass.renderPass(), new Object[] {this});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	//parents
	private final @NotNull FirstTriangleRenderPass renderPass;
	
	public @NotNull FirstTriangleRenderPass renderPass() {
		return renderPass;
	}
	
	//storage
	private final @NotNull Freeable storage;
	
	@Override
	public @NotNull Freeable getStorage() {
		return storage;
	}
	
	//pipeline
	private final VkDescriptorSetLayout descriptorSetLayout;
	private final VkPipelineLayout pipelineLayout;
	private final VkGraphicsPipeline pipeline;
	
	public VkDescriptorSetLayout descriptorSetLayout() {
		return descriptorSetLayout;
	}
	
	public VkPipelineLayout pipelineLayout() {
		return pipelineLayout;
	}
	
	public VkGraphicsPipeline pipeline() {
		return pipeline;
	}
	
	public void bindPipeline(VkCommandBuffer cmdBuffer, VkDescriptorSet translation) {
		try (AllocatorFrame frame = Allocator.frame()) {
			vkCmdBindPipeline(cmdBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline.address());
			vkCmdBindDescriptorSets(cmdBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, pipelineLayout.address(), 0, PointerBufferLong.alloc(frame, translation.address()).nioBuffer(), null);
		}
	}
}
