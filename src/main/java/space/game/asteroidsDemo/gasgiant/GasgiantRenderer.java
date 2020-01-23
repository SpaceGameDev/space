package space.game.asteroidsDemo.gasgiant;

import org.jetbrains.annotations.NotNull;
import space.engine.barrier.future.Future;
import space.engine.freeable.Freeable;
import space.engine.freeable.Freeable.CleanerWrapper;
import space.engine.indexmap.IndexMap;
import space.engine.vulkan.VkBuffer;
import space.engine.vulkan.VkCommandBuffer;
import space.engine.vulkan.managed.descriptorSet.StaticDescriptorSetPool;
import space.engine.vulkan.managed.renderPass.ManagedFrameBuffer;
import space.engine.vulkan.managed.renderPass.ManagedRenderPass.Callback;
import space.game.asteroidsDemo.renderPass.AsteroidDemoInfos;
import space.game.asteroidsDemo.renderPass.AsteroidDemoRenderPass;

import static org.lwjgl.vulkan.VK10.*;
import static space.engine.Empties.EMPTY_OBJECT_ARRAY;
import static space.engine.barrier.Barrier.nowFuture;
import static space.engine.freeable.Freeable.addIfNotContained;
import static space.engine.primitive.Primitives.FP32;

public class GasgiantRenderer implements CleanerWrapper, Callback<AsteroidDemoInfos> {
	
	private final AsteroidDemoRenderPass renderPass;
	private final GasgiantPipeline gasgiantPipeline;
	private final VkBuffer model;
	public final Gasgiant gasgiant;
	
	private final StaticDescriptorSetPool descriptorSetPool;
	
	public GasgiantRenderer(AsteroidDemoRenderPass renderPass, GasgiantPipeline gasgiantPipeline, VkBuffer model, Gasgiant gasgiant, Object[] parents) {
		this.renderPass = renderPass;
		this.gasgiantPipeline = gasgiantPipeline;
		this.model = model;
		this.gasgiant = gasgiant;
		
		this.storage = Freeable.createDummy(addIfNotContained(parents, renderPass, gasgiantPipeline, model));
		
		this.descriptorSetPool = new StaticDescriptorSetPool(renderPass.device(), gasgiantPipeline.descriptorSetLayout(), 1, new Object[] {this});
	}
	
	@Override
	public @NotNull Future<IndexMap<VkCommandBuffer[]>> getCmdBuffers(@NotNull ManagedFrameBuffer<AsteroidDemoInfos> render, AsteroidDemoInfos infos) {
		return nowFuture(() -> IndexMap.of(
				renderPass.subpassRender.id(),
				new VkCommandBuffer[] {
						render.queue().poolShortLived().allocAndRecordCommandBuffer(
								VK_COMMAND_BUFFER_LEVEL_SECONDARY,
								EMPTY_OBJECT_ARRAY,
								VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT | VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT,
								render.inheritanceInfo(infos, renderPass.subpassRender),
								cmd -> {
									gasgiantPipeline.bindPipeline(cmd, descriptorSetPool.sets()[0], infos);
									vkCmdBindVertexBuffers(cmd, 0, new long[] {model.address()}, new long[] {0});
									vkCmdDraw(cmd, (int) (model.sizeOf() / (FP32.bytes * 6)), 1, 0, 0);
									return null;
								}
						)
				}
		));
	}
	
	//storage
	private final Freeable storage;
	
	@Override
	public @NotNull Freeable getStorage() {
		return storage;
	}
}
