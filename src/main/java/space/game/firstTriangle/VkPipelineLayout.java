package space.game.firstTriangle;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import space.engine.buffer.AllocatorStack.Frame;
import space.engine.buffer.pointer.PointerBufferPointer;
import space.engine.freeableStorage.Freeable;
import space.engine.freeableStorage.Freeable.FreeableWrapper;
import space.engine.freeableStorage.FreeableStorage;
import space.engine.sync.barrier.Barrier;

import java.util.function.BiFunction;

import static org.lwjgl.vulkan.VK10.*;
import static space.engine.buffer.Allocator.allocatorStack;
import static space.engine.freeableStorage.Freeable.addIfNotContained;
import static space.game.firstTriangle.VkException.assertVk;

public class VkPipelineLayout implements FreeableWrapper {
	
	//alloc
	public static @NotNull VkPipelineLayout alloc(VkPipelineLayoutCreateInfo info, @NotNull VkDevice device, @NotNull Object[] parents) {
		try (Frame frame = allocatorStack().frame()) {
			PointerBufferPointer ptr = PointerBufferPointer.malloc(frame);
			assertVk(nvkCreatePipelineLayout(device, info.address(), 0, ptr.address()));
			return create(ptr.getPointer(), device, parents);
		}
	}
	
	//create
	public static @NotNull VkPipelineLayout create(long address, @NotNull VkDevice device, @NotNull Object[] parents) {
		return new VkPipelineLayout(address, device, Storage::new, parents);
	}
	
	public static @NotNull VkPipelineLayout wrap(long address, @NotNull VkDevice device, @NotNull Object[] parents) {
		return new VkPipelineLayout(address, device, Freeable::createDummy, parents);
	}
	
	//const
	public VkPipelineLayout(long address, @NotNull VkDevice device, @NotNull BiFunction<VkPipelineLayout, Object[], Freeable> storageCreator, @NotNull Object[] parents) {
		this.address = address;
		this.device = device;
		this.storage = storageCreator.apply(this, addIfNotContained(parents, device));
	}
	
	//parents
	private final @NotNull VkDevice device;
	
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
	private final @NotNull Freeable storage;
	
	@Override
	public @NotNull Freeable getStorage() {
		return storage;
	}
	
	public static class Storage extends FreeableStorage {
		
		private final @NotNull VkDevice device;
		private final long address;
		
		public Storage(@NotNull VkPipelineLayout o, @NotNull Object[] parents) {
			super(o, parents);
			this.device = o.device;
			this.address = o.address;
		}
		
		@Override
		protected @NotNull Barrier handleFree() {
			vkDestroyPipelineLayout(device, address, null);
			return Barrier.ALWAYS_TRIGGERED_BARRIER;
		}
	}
}
