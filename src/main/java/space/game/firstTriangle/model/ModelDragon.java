package space.game.firstTriangle.model;

import space.engine.vector.Matrix4f;

import java.io.IOException;

public class ModelDragon {
	
	public static float[] dragon(Matrix4f scale) throws IOException {
		return ModelFromAssimp.loadModel(ModelDragon.class.getResourceAsStream("dragon_vrip.ply"), scale);
	}
}
