package space.game.asteroidsDemo.asteroid;

import space.engine.vector.Quaternion;
import space.engine.vector.TranslationBuilder;
import space.engine.vector.Vector3;

public class Asteroid {
	
	public final Vector3[] position = {new Vector3(), new Vector3()};
	public final Quaternion[] rotation = {new Quaternion(), new Quaternion()};
	public final int modelId;
	
	public Asteroid(int modelId) {
		this.modelId = modelId;
	}
	
	public TranslationBuilder toTranslation(TranslationBuilder translation, float timeSeconds) {
		return translation
				.identity()
				.appendMove(new Vector3(position[1]).multiply(timeSeconds).add(position[0]))
				.appendRotate(new Quaternion().slerp(rotation[1], timeSeconds).multiply(rotation[0]));
	}
}
