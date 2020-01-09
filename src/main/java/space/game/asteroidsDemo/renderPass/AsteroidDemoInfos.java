package space.game.asteroidsDemo.renderPass;

import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.array.ArrayBufferFloat;
import space.engine.vector.Matrix4;
import space.engine.vector.Translation;
import space.engine.vector.Vector3;
import space.engine.vulkan.managed.renderPass.Infos;
import space.engine.vulkan.vma.VmaMappedBuffer;
import space.game.asteroidsDemo.asteroid.Asteroid;
import space.game.asteroidsDemo.entity.Camera;

public class AsteroidDemoInfos extends Infos {
	
	public final Matrix4 projection;
	public final Camera camera;
	public final Translation cameraTranslation;
	public final Asteroid gasgiant;
	public final Translation gasgiantTranslation;
	public final Vector3 lightDir;
	public final float frameTimeSeconds;
	public final VmaMappedBuffer uniformGlobal;
	
	public AsteroidDemoInfos(int frameBufferIndex, Matrix4 projection, Camera camera, Asteroid gasgiant, Vector3 lightDirBase, float frameTimeSeconds, VmaMappedBuffer uniformGlobal) {
		super(frameBufferIndex);
		this.projection = projection;
		this.camera = camera;
		this.cameraTranslation = camera.toTranslation().build();
		this.gasgiant = gasgiant;
		this.gasgiantTranslation = gasgiant.toTranslation(frameTimeSeconds).build();
		this.lightDir = lightDirBase.rotate(gasgiantTranslation.rotation);
		this.frameTimeSeconds = frameTimeSeconds;
		this.uniformGlobal = uniformGlobal;
		
		float[] uniformGlobalArray = new float[(4 + 3 + 1 + 1) * 4];
		projection.write(uniformGlobalArray, 0);
		cameraTranslation.matrix.write4Aligned(uniformGlobalArray, (4) * 4);
		cameraTranslation.offset.write(uniformGlobalArray, (4 + 3) * 4);
		lightDir.write(uniformGlobalArray, (4 + 3 + 1) * 4);
		
		try (AllocatorFrame frame = Allocator.frame()) {
			ArrayBufferFloat uniformGlobalBuffer = ArrayBufferFloat.alloc(frame, uniformGlobalArray);
			uniformGlobal.uploadData(uniformGlobalBuffer);
		}
	}
}
