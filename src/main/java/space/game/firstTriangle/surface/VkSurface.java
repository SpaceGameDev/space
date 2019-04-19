package space.game.firstTriangle.surface;

import org.jetbrains.annotations.NotNull;
import space.engine.freeableStorage.Freeable;
import space.engine.freeableStorage.Freeable.FreeableWrapper;
import space.engine.freeableStorage.FreeableStorage;
import space.engine.sync.barrier.Barrier;
import space.engine.window.Window;
import space.game.firstTriangle.VkInstance;

import static org.lwjgl.vulkan.KHRSurface.nvkDestroySurfaceKHR;
import static space.engine.freeableStorage.Freeable.addIfNotContained;

public class VkSurface<WINDOW extends Window> implements FreeableWrapper {
	
	private final VkInstance instance;
	private final WINDOW window;
	private final Storage storage;
	private final long surface;
	
	public VkSurface(VkInstance instance, WINDOW window, long surface, Object[] parents) {
		this.instance = instance;
		this.window = window;
		this.storage = new Storage(this, instance, surface, addIfNotContained(parents, instance, window));
		this.surface = surface;
	}
	
	//storage
	public static class Storage extends FreeableStorage {
		
		private final VkInstance instance;
		private final long surface;
		
		public Storage(VkSurface referent, VkInstance instance, long surface, Object[] parents) {
			super(referent, parents);
			this.instance = instance;
			this.surface = surface;
		}
		
		@Override
		protected @NotNull Barrier handleFree() {
			nvkDestroySurfaceKHR(instance, surface, 0);
			return Barrier.ALWAYS_TRIGGERED_BARRIER;
		}
	}
	
	@Override
	public @NotNull Freeable getStorage() {
		return storage;
	}
	
	//getter
	public VkInstance getInstance() {
		return instance;
	}
	
	public WINDOW getWindow() {
		return window;
	}
	
	public long getSurface() {
		return surface;
	}
}
