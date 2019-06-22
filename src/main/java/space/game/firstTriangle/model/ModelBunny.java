package space.game.firstTriangle.model;

import space.engine.vector.Matrix4f;

import java.io.IOException;

public class ModelBunny {
	
	public static float[] bunny(Matrix4f scale) throws IOException {
		return ModelFromAssimp.loadModel(ModelBunny.class.getResourceAsStream("bun_zipper.ply"), scale);
	}
}
