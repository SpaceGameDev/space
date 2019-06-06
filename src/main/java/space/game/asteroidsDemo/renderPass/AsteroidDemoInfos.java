package space.game.asteroidsDemo.renderPass;

import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.array.ArrayBufferFloat;
import space.engine.vector.Matrix4f;
import space.engine.vector.Translation;
import space.engine.vulkan.managed.renderPass.Infos;
import space.engine.vulkan.vma.VmaMappedBuffer;
import space.game.asteroidsDemo.entity.Camera;

public class AsteroidDemoInfos extends Infos {
	
	public final Matrix4f projection;
	public final Camera camera;
	public final Translation cameraTranslation;
	public final long frameTimeNanos;
	public final float frameTimeSeconds;
	public final VmaMappedBuffer uniformGlobal;
	
	public AsteroidDemoInfos(int frameBufferIndex, Matrix4f projection, Camera camera, Translation cameraTranslation, long frameTimeNanos, VmaMappedBuffer uniformGlobal) {
		super(frameBufferIndex);
		this.projection = projection;
		this.camera = camera;
		this.cameraTranslation = cameraTranslation;
		this.frameTimeNanos = frameTimeNanos;
		this.frameTimeSeconds = frameTimeNanos / 1_000_000_000f;
		this.uniformGlobal = uniformGlobal;
		
		float[] uniformGlobalArray = new float[(4 + 3 + 1) * 4];
		projection.write(uniformGlobalArray, 0);
		cameraTranslation.rotation.write4Aligned(uniformGlobalArray, (4) * 4);
		cameraTranslation.offset.write(uniformGlobalArray, (4 + 3) * 4);
		
		try (AllocatorFrame frame = Allocator.frame()) {
			ArrayBufferFloat uniformGlobalBuffer = ArrayBufferFloat.alloc(frame, uniformGlobalArray);
			uniformGlobal.uploadData(uniformGlobalBuffer);
		}
	}
}
