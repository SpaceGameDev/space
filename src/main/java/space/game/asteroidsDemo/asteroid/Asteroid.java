package space.game.asteroidsDemo.asteroid;

import space.engine.vector.Matrix3f;
import space.engine.vector.Quaternionf;
import space.engine.vector.Translation;
import space.engine.vector.Vector3f;

public class Asteroid {
	
	public final Vector3f[] position = {new Vector3f(), new Vector3f()};
	public final Quaternionf[] rotation = {new Quaternionf(), new Quaternionf()};
	public final int modelId;
	
	public Asteroid(int modelId) {
		this.modelId = modelId;
	}
	
	public Translation toTranslation(Translation translation, float timeSeconds) {
		return translation
				.zero()
				.moveForwards(new Vector3f(position[1]).multiply(timeSeconds).add(position[0]))
				.rotateForwards(new Quaternionf().slerp(rotation[1], timeSeconds).multiply(rotation[0]).toMatrix3(new Matrix3f()));
	}
}
