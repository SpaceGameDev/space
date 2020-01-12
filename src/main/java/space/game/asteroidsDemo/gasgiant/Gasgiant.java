package space.game.asteroidsDemo.gasgiant;

import space.engine.vector.Quaternion;
import space.engine.vector.Translation;
import space.engine.vector.TranslationBuilder;
import space.engine.vector.Vector3;

public class Gasgiant {
	
	public final Vector3[] position = {Vector3.zero(), Vector3.zero()};
	public final Quaternion[] rotation = {Quaternion.identity(), Quaternion.identity()};
	public final int modelId;
	
	public Gasgiant(int modelId) {
		this.modelId = modelId;
	}
	
	public Translation toTranslation(float timeSeconds) {
		return new TranslationBuilder()
				.appendMove(position[1].multiply(timeSeconds).add(position[0]))
				.appendRotate(Quaternion.slerp(rotation[1], timeSeconds).multiply(rotation[0]))
				.build();
	}
}
