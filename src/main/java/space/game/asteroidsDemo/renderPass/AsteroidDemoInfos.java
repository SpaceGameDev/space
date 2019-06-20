package space.game.asteroidsDemo.renderPass;

import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.array.ArrayBufferFloat;
import space.engine.vector.Matrix4f;
import space.engine.vector.Translation;
import space.engine.vector.Vector3f;
import space.engine.vulkan.managed.renderPass.Infos;
import space.engine.vulkan.vma.VmaMappedBuffer;
import space.game.asteroidsDemo.asteroid.Asteroid;
import space.game.asteroidsDemo.entity.Camera;

public class AsteroidDemoInfos extends Infos {
	
	public final Matrix4f projection;
	public final Camera camera;
	public final Translation cameraTranslation;
	public final Asteroid gasgiant;
	public final Translation gasgiantTranslation;
	public final Vector3f lightDir;
	public final float frameTimeSeconds;
	public final VmaMappedBuffer uniformGlobal;
	
	public AsteroidDemoInfos(int frameBufferIndex, Matrix4f projection, Camera camera, Translation cameraTranslation, Asteroid gasgiant, Vector3f lightDirBase, float frameTimeSeconds, VmaMappedBuffer uniformGlobal) {
		super(frameBufferIndex);
		this.projection = projection;
		this.camera = camera;
		this.cameraTranslation = cameraTranslation;
		this.gasgiant = gasgiant;
		this.gasgiantTranslation = gasgiant.toTranslation(new Translation(), frameTimeSeconds);
		this.lightDir = new Vector3f(lightDirBase).rotate(gasgiantTranslation.rotation);
		this.frameTimeSeconds = frameTimeSeconds;
		this.uniformGlobal = uniformGlobal;
		
		float[] uniformGlobalArray = new float[(4 + 3 + 1 + 1) * 4];
		projection.write(uniformGlobalArray, 0);
		cameraTranslation.rotation.write4Aligned(uniformGlobalArray, (4) * 4);
		cameraTranslation.offset.write(uniformGlobalArray, (4 + 3) * 4);
		lightDir.write(uniformGlobalArray, (4 + 3 + 1) * 4);
		
		try (AllocatorFrame frame = Allocator.frame()) {
			ArrayBufferFloat uniformGlobalBuffer = ArrayBufferFloat.alloc(frame, uniformGlobalArray);
			uniformGlobal.uploadData(uniformGlobalBuffer);
		}
	}
}
