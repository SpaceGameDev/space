package space.game.asteroidsDemo.asteroid;

import org.jetbrains.annotations.NotNull;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.array.ArrayBufferFloat;
import space.engine.freeableStorage.Freeable;
import space.engine.freeableStorage.Freeable.FreeableWrapper;
import space.engine.indexmap.IndexMap;
import space.engine.indexmap.IndexMapArray;
import space.engine.sync.future.Future;
import space.engine.vector.Translation;
import space.engine.vulkan.VkBuffer;
import space.engine.vulkan.VkCommandBuffer;
import space.engine.vulkan.managed.descriptorSet.ManagedDescriptorSetPool;
import space.engine.vulkan.managed.renderPass.ManagedFrameBuffer;
import space.engine.vulkan.managed.renderPass.ManagedRenderPass.Callback;
import space.engine.vulkan.vma.VmaMappedBuffer;
import space.game.asteroidsDemo.renderPass.AsteroidDemoInfos;
import space.game.asteroidsDemo.renderPass.AsteroidDemoRenderPass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.lwjgl.util.vma.Vma.VMA_MEMORY_USAGE_CPU_TO_GPU;
import static org.lwjgl.vulkan.VK10.*;
import static space.engine.buffer.Allocator.heap;
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
					cmd.record(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT | VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT, renderPass.subpassRender.inheritanceInfo(), () ->
							asteroids.entrySet().stream().map(entry -> {
								int index = entry.getIndex();
								VkBuffer model = asteroidModels[index];
								Asteroid[] asteroids = entry.getValue().toArray(new Asteroid[0]);
								
								float[] instanceData = new float[asteroids.length * 16];
								Translation translation = new Translation();
								for (int i = 0; i < asteroids.length; i++) {
									asteroids[i].toTranslation(translation, infos.frameTimeSeconds);
									translation.rotation.write4Aligned(instanceData, i * 16);
									translation.offset.write4Aligned(instanceData, i * 16 + 12);
								}
								
								VmaMappedBuffer instanceBuffer = VmaMappedBuffer.alloc(0, instanceData.length * FP32.bytes, VK_BUFFER_USAGE_VERTEX_BUFFER_BIT, 0, VMA_MEMORY_USAGE_CPU_TO_GPU, renderPass.device(), new Object[] {infos});
								try (AllocatorFrame frame = Allocator.frame()) {
									instanceBuffer.uploadData(ArrayBufferFloat.alloc(heap(), instanceData, new Object[] {frame}));
								}
								
								asteroidPipeline.bindPipeline(cmd, descriptorSetPool.sets()[index], infos);
								vkCmdBindVertexBuffers(cmd, 0, new long[] {
										model.address(),
										instanceBuffer.address()
								}, new long[] {
										0,
										0
								});
								vkCmdDraw(cmd, (int) (model.sizeOf() / (FP32.bytes)), asteroids.length, 0, 0);
								
								return instanceBuffer;
							}).toArray(Object[]::new)
					);
					
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
