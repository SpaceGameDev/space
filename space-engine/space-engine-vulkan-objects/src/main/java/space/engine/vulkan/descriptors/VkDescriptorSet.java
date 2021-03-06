package space.engine.vulkan.descriptors;

import org.jetbrains.annotations.NotNull;
import space.engine.barrier.Barrier;
import space.engine.freeable.Cleaner;
import space.engine.freeable.Freeable;
import space.engine.freeable.Freeable.CleanerWrapper;
import space.engine.vulkan.VkDevice;
import space.engine.vulkan.VkInstance;

import java.util.function.BiFunction;

import static org.lwjgl.vulkan.VK10.vkFreeDescriptorSets;
import static space.engine.freeable.Freeable.addIfNotContained;

public class VkDescriptorSet implements CleanerWrapper {
	
	//create
	public static @NotNull VkDescriptorSet create(long address, @NotNull VkDescriptorPool pool, @NotNull Object[] parents) {
		return new VkDescriptorSet(address, pool, Storage::new, parents);
	}
	
	public static @NotNull VkDescriptorSet wrap(long address, @NotNull VkDescriptorPool pool, @NotNull Object[] parents) {
		return new VkDescriptorSet(address, pool, Freeable::createDummy, parents);
	}
	
	//const
	public VkDescriptorSet(long address, @NotNull VkDescriptorPool pool, @NotNull BiFunction<VkDescriptorSet, Object[], Freeable> storageCreator, @NotNull Object[] parents) {
		this.address = address;
		this.pool = pool;
		this.storage = storageCreator.apply(this, addIfNotContained(parents, pool));
	}
	
	//parents
	private final @NotNull VkDescriptorPool pool;
	
	public VkDescriptorPool pool() {
		return pool;
	}
	
	public VkDevice device() {
		return pool.device();
	}
	
	public VkInstance instance() {
		return pool.instance();
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
		
		private final @NotNull VkDescriptorPool pool;
		private final long address;
		
		public Storage(@NotNull VkDescriptorSet o, @NotNull Object[] parents) {
			super(o, parents);
			this.pool = o.pool;
			this.address = o.address;
		}
		
		@Override
		protected @NotNull Barrier handleFree() {
			vkFreeDescriptorSets(pool.device(), pool.address(), address);
			return Barrier.DONE_BARRIER;
		}
	}
}
