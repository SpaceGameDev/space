package space.game.asteroidsDemo.entity;

import space.engine.vector.Quaternion;
import space.engine.vector.TranslationBuilder;
import space.engine.vector.Vector3;

public class Entity {
	
	public Vector3 position = Vector3.zero();
	public Quaternion rotation = Quaternion.identity();
	
	public Entity() {
	}
	
	public Entity rotateRelative(Quaternion relative) {
		rotation = relative.multiply(rotation);
		return this;
	}
	
	public Entity translateRelative(Vector3 relative) {
		position = position.add(relative.rotateInverse(rotation));
		return this;
	}
	
	public TranslationBuilder toTranslation() {
		return new TranslationBuilder()
				.appendMove(position)
				.appendRotate(rotation);
	}
}
