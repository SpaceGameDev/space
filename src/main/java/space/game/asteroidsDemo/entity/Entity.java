package space.game.asteroidsDemo.entity;

import space.engine.vector.Matrix3f;
import space.engine.vector.Quaternionf;
import space.engine.vector.Translation;
import space.engine.vector.Vector3f;

public class Entity {
	
	public final Vector3f position = new Vector3f();
	public final Quaternionf rotation = new Quaternionf();
	
	public Entity() {
	}
	
	public Entity rotateRelative(Quaternionf relative) {
		rotation.multiply(relative);
		return this;
	}
	
	public Entity translateRelative(Vector3f relative) {
		position.add(new Vector3f(relative).rotate(rotation));
		return this;
	}
	
	public Translation toTranslation(Translation translation) {
		return translation
				.zero()
				.moveForwards(position)
				.rotateForwards(rotation.toMatrix3(new Matrix3f()));
	}
}