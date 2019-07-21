package space.engine.vulkan;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;
import space.engine.barrier.Barrier;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.array.ArrayBufferByte;
import space.engine.buffer.pointer.PointerBufferPointer;
import space.engine.freeableStorage.Freeable;
import space.engine.freeableStorage.Freeable.FreeableWrapper;
import space.engine.freeableStorage.FreeableStorage;

import java.util.function.BiFunction;

import static org.lwjgl.vulkan.VK10.*;
import static space.engine.freeableStorage.Freeable.addIfNotContained;
import static space.engine.lwjgl.LwjglStructAllocator.mallocStruct;

public class VkShaderModule implements FreeableWrapper {
	
	//alloc
	public static VkShaderModule alloc(@NotNull VkDevice device, @NotNull byte[] src, @NotNull Object[] parents) {
		try (AllocatorFrame frame = Allocator.frame()) {
			ArrayBufferByte buffer = ArrayBufferByte.alloc(Allocator.heap(), src, new Object[] {frame});
			VkShaderModuleCreateInfo info = mallocStruct(frame, VkShaderModuleCreateInfo::create, VkShaderModuleCreateInfo.SIZEOF).set(
					VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO,
					0,
					0,
					buffer.nioBuffer()
			);
			
			PointerBufferPointer shaderModulePtr = PointerBufferPointer.malloc(frame);
			nvkCreateShaderModule(device, info.address(), 0, shaderModulePtr.address());
			return create(shaderModulePtr.getPointer(), device, parents);
		}
	}
	
	//create
	public static VkShaderModule create(long address, @NotNull VkDevice device, @NotNull Object[] parents) {
		return new VkShaderModule(address, device, Storage::new, parents);
	}
	
	public static VkShaderModule wrap(long address, @NotNull VkDevice device, @NotNull Object[] parents) {
		return new VkShaderModule(address, device, Freeable::createDummy, parents);
	}
	
	//const
	public VkShaderModule(long address, @NotNull VkDevice device, @NotNull BiFunction<VkShaderModule, Object[], Freeable> storageCreator, @NotNull Object[] parents) {
		this.device = device;
		this.address = address;
		this.storage = storageCreator.apply(this, addIfNotContained(parents, device));
	}
	
	//parents
	private final @NotNull VkDevice device;
	
	public @NotNull VkDevice device() {
		return device;
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
		
		private final VkDevice device;
		private final long address;
		
		public Storage(@NotNull VkShaderModule shaderModule, @NotNull Object[] parents) {
			super(shaderModule, parents);
			this.device = shaderModule.device();
			this.address = shaderModule.address();
		}
		
		@Override
		protected @NotNull Barrier handleFree() {
			vkDestroyShaderModule(device, address, null);
			return Barrier.DONE_BARRIER;
		}
	}
}
