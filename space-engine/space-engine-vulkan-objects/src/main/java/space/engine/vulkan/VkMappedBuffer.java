package space.engine.vulkan;

import org.jetbrains.annotations.NotNull;
import space.engine.buffer.Buffer;
import space.engine.sync.barrier.Barrier;

public interface VkMappedBuffer extends VkBuffer {
	
	//mapping
	Buffer mapMemory(Object[] parents);
	
	/**
	 * always completes when this Method returns -> always returns {@link Barrier#ALWAYS_TRIGGERED_BARRIER}
	 */
	@Override
	default @NotNull Barrier uploadData(Buffer src) {
		return uploadData(src, 0, 0, src.sizeOf());
	}
	
	/**
	 * always completes when this Method returns -> always returns {@link Barrier#ALWAYS_TRIGGERED_BARRIER}
	 */
	@Override
	@NotNull Barrier uploadData(Buffer src, long srcOffset, long dstOffset, long length);
}
