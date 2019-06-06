package space.game.asteroidsDemo.asteroid;

import space.engine.vector.Quaternionf;
import space.engine.vector.Vector3f;
import space.game.asteroidsDemo.entity.Entity;

public class Asteroid extends Entity {
	
	public final int modelId;
	
	public Asteroid(int modelId) {
		this.modelId = modelId;
	}
	
	@Override
	public Asteroid rotateRelative(Quaternionf relative) {
		super.rotateRelative(relative);
		return this;
	}
	
	@Override
	public Asteroid translateRelative(Vector3f relative) {
		super.translateRelative(relative);
		return this;
	}
}
