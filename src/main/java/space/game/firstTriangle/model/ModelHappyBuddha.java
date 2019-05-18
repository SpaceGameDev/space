package space.game.firstTriangle.model;

import space.engine.vector.Matrix4f;

import java.io.IOException;

public class ModelHappyBuddha {
	
	public static float[] happyBuddha(Matrix4f scale) throws IOException {
		return ModelFromAssimp.loadModel(ModelHappyBuddha.class.getResourceAsStream("happy_vrip.ply"), scale);
	}
}
