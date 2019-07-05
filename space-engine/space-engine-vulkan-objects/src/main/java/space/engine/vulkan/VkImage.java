package space.engine.vulkan;

import org.jetbrains.annotations.NotNull;
import space.engine.buffer.Buffer;
import space.engine.freeableStorage.Freeable;
import space.engine.sync.barrier.Barrier;

import java.util.function.BiFunction;

public interface VkImage extends Freeable {
	
	//wrap
	static @NotNull VkImage wrap(long address, int imageType, int imageFormat, int width, int height, int depth, int mipLevels, int arrayLayers, int samples, int tiling, @NotNull VkDevice device, @NotNull Object[] parents) {
		return new VkImage.Default(address, imageType, imageFormat, width, height, depth, mipLevels, arrayLayers, samples, tiling, device, Freeable::createDummy, parents);
	}
	
	//parents
	@NotNull VkDevice device();
	
	//address
	long address();
	
	int imageType();
	
	int imageFormat();
	
	int width();
	
	int height();
	
	int depth();
	
	int mipLevels();
	
	int arrayLayers();
	
	int samples();
	
	int tiling();
	
	@NotNull Barrier uploadData(@NotNull Buffer data, int bufferRowLength, int bufferImageHeight, int dstImageLayout, int aspectMask, int mipLevel, int baseArrayLayer, int layerCount);
	
	class Default implements VkImage, FreeableWrapper {
		
		//const
		public Default(long address, int imageType, int imageFormat, int width, int height, int depth, int mipLevels, int arrayLayers, int samples, int tiling, @NotNull VkDevice device, @NotNull BiFunction<? super Default, Object[], Freeable> storageCreator, @NotNull Object[] parents) {
			this.device = device;
			this.address = address;
			this.imageType = imageType;
			this.imageFormat = imageFormat;
			this.width = width;
			this.height = height;
			this.depth = depth;
			this.mipLevels = mipLevels;
			this.arrayLayers = arrayLayers;
			this.samples = samples;
			this.tiling = tiling;
			this.storage = storageCreator.apply(this, parents);
		}
		
		//parents
		private final VkDevice device;
		
		public @NotNull VkDevice device() {
			return device;
		}
		
		//address
		private final long address;
		private final int imageType, imageFormat, width, height, depth, mipLevels, arrayLayers, samples, tiling;
		
		public long address() {
			return address;
		}
		
		@Override
		public int imageType() {
			return imageType;
		}
		
		@Override
		public int imageFormat() {
			return imageFormat;
		}
		
		@Override
		public int width() {
			return width;
		}
		
		@Override
		public int height() {
			return height;
		}
		
		@Override
		public int depth() {
			return depth;
		}
		
		@Override
		public int mipLevels() {
			return mipLevels;
		}
		
		@Override
		public int arrayLayers() {
			return arrayLayers;
		}
		
		@Override
		public int samples() {
			return samples;
		}
		
		@Override
		public int tiling() {
			return tiling;
		}
		
		//storage
		private final @NotNull Freeable storage;
		
		@Override
		public @NotNull Freeable getStorage() {
			return storage;
		}
		
		//uploadData
		@Override
		public @NotNull Barrier uploadData(@NotNull Buffer data, int bufferRowLength, int bufferImageHeight, int dstImageLayout, int aspectMask, int mipLevel, int baseArrayLayer, int layerCount) {
			//we cannot resolve the memory (and offset) of a random buffer or image
			throw new UnsupportedOperationException();
		}
	}
}
