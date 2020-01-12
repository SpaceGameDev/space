package space.game.asteroidsDemo.renderPass;

import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.array.ArrayBufferFloat;
import space.engine.vector.Matrix4;
import space.engine.vector.Translation;
import space.engine.vector.Vector3;
import space.engine.vulkan.managed.renderPass.Infos;
import space.engine.vulkan.vma.VmaMappedBuffer;
import space.game.asteroidsDemo.entity.Camera;
import space.game.asteroidsDemo.gasgiant.Gasgiant;

public class AsteroidDemoInfos extends Infos {
	
	public static final long UNIFORM_GLOBAL_SIZEOF = (16 + 16 + 16 + 4) * 4;
	
	public final Matrix4 projection;
	public final Camera camera;
	public final Translation cameraTranslation;
	public final Gasgiant gasgiant;
	public final Translation gasgiantTranslation;
	public final Vector3 lightDir;
	public final float frameTimeSeconds;
	public final VmaMappedBuffer uniformGlobal;
	
	public AsteroidDemoInfos(int frameBufferIndex, Matrix4 projection, Camera camera, Gasgiant gasgiant, Vector3 lightDirBase, float frameTimeSeconds, VmaMappedBuffer uniformGlobal) {
		super(frameBufferIndex);
		this.projection = projection;
		this.camera = camera;
		this.cameraTranslation = camera.toTranslation().build();
		this.gasgiant = gasgiant;
		this.gasgiantTranslation = gasgiant.toTranslation(frameTimeSeconds);
		this.lightDir = lightDirBase.rotate(gasgiantTranslation.rotation);
		this.frameTimeSeconds = frameTimeSeconds;
		this.uniformGlobal = uniformGlobal;
		
		float[] uniformGlobalArray = new float[16 + 16 + 16 + 4];
		projection.write(uniformGlobalArray, 0);
		cameraTranslation.write4Aligned(uniformGlobalArray, 16);
		gasgiantTranslation.write4Aligned(uniformGlobalArray, 16 + 16);
		lightDir.write4Aligned(uniformGlobalArray, 16 + 16 + 16);
		
		try (AllocatorFrame frame = Allocator.frame()) {
			ArrayBufferFloat uniformGlobalBuffer = ArrayBufferFloat.alloc(frame, uniformGlobalArray);
			uniformGlobal.uploadData(uniformGlobalBuffer);
		}
	}
}
