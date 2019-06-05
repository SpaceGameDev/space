package space.game.firstTriangle.renderPass;

import org.jetbrains.annotations.NotNull;
import space.engine.freeableStorage.Freeable;
import space.engine.freeableStorage.Freeable.FreeableWrapper;
import space.engine.indexmap.IndexMap;
import space.engine.indexmap.IndexMapArray;
import space.engine.sync.future.Future;
import space.engine.vulkan.VkBuffer;
import space.engine.vulkan.VkCommandBuffer;
import space.engine.vulkan.managed.descriptorSet.ManagedDescriptorSetPool;
import space.engine.vulkan.managed.renderPass.ManagedFrameBuffer;
import space.engine.vulkan.managed.renderPass.ManagedRenderPass.Callback;
import space.game.firstTriangle.entity.Asteroid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;
import static space.engine.freeableStorage.Freeable.addIfNotContained;
import static space.engine.primitive.Primitives.FP32;
import static space.engine.sync.Tasks.future;

public class AsteroidRenderer implements FreeableWrapper, Callback<AsteroidDemoInfos> {
	
	private final AsteroidDemoRenderPass renderPass;
	private final AsteroidPipeline asteroidPipeline;
	private final VkBuffer[] asteroidModels;
	private final IndexMap<Collection<Asteroid>> asteroids = new IndexMapArray<>();
	
	private final ManagedDescriptorSetPool descriptorSetPool;
	
	public AsteroidRenderer(AsteroidDemoRenderPass renderPass, AsteroidPipeline asteroidPipeline, VkBuffer[] asteroidModels, Object[] parents) {
		this.renderPass = renderPass;
		this.asteroidPipeline = asteroidPipeline;
		this.asteroidModels = asteroidModels;
		
		this.storage = Freeable.createDummy(this, addIfNotContained(parents, renderPass, asteroidPipeline));
		
		this.descriptorSetPool = new ManagedDescriptorSetPool(renderPass.device(), asteroidPipeline.descriptorSetLayout(), asteroidModels.length, new Object[] {this});
	}
	
	public void addAsteroid(Asteroid asteroid) {
		asteroids.computeIfAbsent(asteroid.modelId, ArrayList::new).add(asteroid);
	}
	
	@Override
	public @NotNull List<Future<VkCommandBuffer[]>> getCmdBuffers(@NotNull ManagedFrameBuffer<AsteroidDemoInfos> render, AsteroidDemoInfos infos) {
		return List.of(
				future(() -> {
					VkCommandBuffer cmd = render.queue().commandPool().allocCommandBuffer(VK_COMMAND_BUFFER_LEVEL_SECONDARY, new Object[] {infos});
					cmd.record(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT | VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT, renderPass.subpassRender.inheritanceInfo(), () -> {
						asteroids.entrySet().forEach(entry -> {
							int index = entry.getIndex();
							VkBuffer model = asteroidModels[index];

//							entry.getValue().stream()
							
							asteroidPipeline.bindPipeline(cmd, descriptorSetPool.sets()[index], infos);
							vkCmdBindVertexBuffers(cmd, 0, new long[] {model.address()}, new long[] {0});
							vkCmdDraw(cmd, (int) (model.sizeOf() / (FP32.bytes)), 1, 0, 0);
						});
					});
					
					return new VkCommandBuffer[] {
							cmd
					};
				}).submit()
		);
	}
	
	//storage
	private final Freeable storage;
	
	@Override
	public @NotNull Freeable getStorage() {
		return storage;
	}
}
