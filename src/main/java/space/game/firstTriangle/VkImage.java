package space.game.firstTriangle;

import org.jetbrains.annotations.NotNull;
import space.engine.freeableStorage.Freeable;
import space.engine.freeableStorage.Freeable.FreeableWrapper;
import space.engine.freeableStorage.FreeableStorage;
import space.engine.sync.barrier.Barrier;

import java.util.function.BiFunction;

import static org.lwjgl.vulkan.VK10.nvkDestroyImage;
import static space.engine.freeableStorage.Freeable.addIfNotContained;

public class VkImage implements FreeableWrapper {
	
	//create
	public static VkImage create(VkDevice device, long address, Object[] parents) {
		return new VkImage(device, address, Storage::new, parents);
	}
	
	public static VkImage wrap(VkDevice device, long address, Object[] parents) {
		return new VkImage(device, address, Freeable::createDummy, parents);
	}
	
	//const
	public VkImage(VkDevice device, long address, BiFunction<VkImage, Object[], Freeable> storageCreator, Object[] parents) {
		this.device = device;
		this.address = address;
		this.storage = storageCreator.apply(this, addIfNotContained(parents, device));
	}
	
	//parents
	private final VkDevice device;
	
	public VkDevice device() {
		return device;
	}
	
	public VkInstance instance() {
		return device.instance();
	}
	
	//address
	private final long address;
	
	public long address() {
		return address;
	}
	
	//storage
	private final Freeable storage;
	
	@Override
	public @NotNull Freeable getStorage() {
		return storage;
	}
	
	public static class Storage extends FreeableStorage {
		
		private final VkDevice device;
		private final long address;
		
		public Storage(@NotNull VkImage image, @NotNull Object[] parents) {
			super(image, parents);
			this.device = image.device();
			this.address = image.address();
		}
		
		@Override
		protected @NotNull Barrier handleFree() {
			nvkDestroyImage(device, address, 0);
			return Barrier.ALWAYS_TRIGGERED_BARRIER;
		}
	}
}
