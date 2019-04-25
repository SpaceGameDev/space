package space.game.firstTriangle.surface;

import org.jetbrains.annotations.NotNull;
import space.engine.freeableStorage.Freeable;
import space.engine.freeableStorage.Freeable.FreeableWrapper;
import space.engine.freeableStorage.FreeableStorage;
import space.engine.sync.barrier.Barrier;
import space.engine.window.Window;
import space.game.firstTriangle.VkInstance;

import java.util.function.BiFunction;

import static org.lwjgl.vulkan.KHRSurface.nvkDestroySurfaceKHR;
import static space.engine.freeableStorage.Freeable.addIfNotContained;

public class VkSurface<WINDOW extends Window> implements FreeableWrapper {
	
	//create
	public static <WINDOW extends Window> VkSurface<WINDOW> create(long address, @NotNull VkInstance instance, @NotNull WINDOW window, @NotNull Object[] parents) {
		return new VkSurface<>(address, instance, window, Storage::new, parents);
	}
	
	public static <WINDOW extends Window> VkSurface<WINDOW> wrap(long address, @NotNull VkInstance instance, @NotNull WINDOW window, @NotNull Object[] parents) {
		return new VkSurface<>(address, instance, window, Freeable::createDummy, parents);
	}
	
	//struct
	public VkSurface(long address, @NotNull VkInstance instance, @NotNull WINDOW window, @NotNull BiFunction<VkSurface, Object[], Freeable> storageCreator, @NotNull Object[] parents) {
		this.instance = instance;
		this.window = window;
		this.address = address;
		this.storage = storageCreator.apply(this, addIfNotContained(parents, instance, window));
	}
	
	//instance
	private final @NotNull VkInstance instance;
	
	public @NotNull VkInstance instance() {
		return instance;
	}
	
	//window
	private final @NotNull WINDOW window;
	
	public @NotNull WINDOW window() {
		return window;
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
	
	public static class Storage extends FreeableStorage {
		
		private final @NotNull VkInstance instance;
		private final long address;
		
		public Storage(@NotNull VkSurface surface, Object[] parents) {
			super(surface, parents);
			this.instance = surface.instance;
			this.address = surface.address;
		}
		
		@Override
		protected @NotNull Barrier handleFree() {
			nvkDestroySurfaceKHR(instance, address, 0);
			return Barrier.ALWAYS_TRIGGERED_BARRIER;
		}
	}
}
