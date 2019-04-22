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
	
	public static VkImage create(VkDevice device, long image, Object[] parents) {
		return new VkImage(device, (vkImage, objects) -> new Storage(vkImage, parents), image, parents);
	}
	
	public static VkImage wrap(VkDevice device, long image, Object[] parents) {
		return new VkImage(device, (vkImage, objects) -> Freeable.createDummy(vkImage, parents), image, parents);
	}
	
	protected VkImage(VkDevice device, BiFunction<VkImage, Object[], Freeable> storage, long image, Object[] parents) {
		this.device = device;
		this.storage = storage.apply(this, addIfNotContained(parents, device));
		this.image = image;
	}
	
	//device
	private final VkDevice device;
	
	public VkDevice getDevice() {
		return device;
	}
	
	//storage
	private final Freeable storage;
	
	@Override
	public @NotNull Freeable getStorage() {
		return storage;
	}
	
	//image
	private final long image;
	
	public long getImage() {
		return image;
	}
	
	public static class Storage extends FreeableStorage {
		
		private final VkDevice device;
		private final long image;
		
		public Storage(@NotNull VkImage image, @NotNull Object[] parents) {
			super(image, parents);
			this.device = image.getDevice();
			this.image = image.getImage();
		}
		
		@Override
		protected @NotNull Barrier handleFree() {
			nvkDestroyImage(device, image, 0);
			return Barrier.ALWAYS_TRIGGERED_BARRIER;
		}
	}
}
