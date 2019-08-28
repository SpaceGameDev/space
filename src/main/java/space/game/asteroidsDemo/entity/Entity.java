package space.game.asteroidsDemo.entity;

import space.engine.vector.Quaternion;
import space.engine.vector.TranslationBuilder;
import space.engine.vector.Vector3;

public class Entity {
	
	public final Vector3 position = new Vector3();
	public final Quaternion rotation = new Quaternion();
	
	public Entity() {
	}
	
	public Entity rotateRelative(Quaternion relative) {
		rotation.set(new Quaternion(relative).inverse().multiply(rotation));
		return this;
	}
	
	public Entity translateRelative(Vector3 relative) {
		position.add(new Vector3(relative).rotateInverse(rotation));
		return this;
	}
	
	public TranslationBuilder toTranslation(TranslationBuilder translation) {
		return translation
				.identity()
				.appendMove(position)
				.appendRotate(rotation);
	}
}
