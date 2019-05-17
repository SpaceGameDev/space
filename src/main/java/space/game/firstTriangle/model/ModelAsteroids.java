package space.game.firstTriangle.model;

import java.util.Random;

public class ModelAsteroids {
	
	public static Result generateAsteroid(float r, float[] config) {
		return generateAsteroid(r, config, System.currentTimeMillis());
	}
	
	public static Result generateAsteroid(float r, float[] config, long seed) {
		Random rand = new Random(seed);
//		float randomValue = rand.nextFloat() * config[0]
		return new Result(
				new float[] {
						0, -1, 0, 0, 0, 1, 1.0f, 1.0f, 1.0f,
						1, 1, 0, 0, 0, 1, 0.0f, 1.0f, 0.0f,
						-1, 1, 0, 0, 0, 1, 0.0f, 0.0f, 1.0f,
						0, -1, 0, 0, 0, -1, 1.0f, 1.0f, 1.0f,
						-1, 1, 0, 0, 0, -1, 0.0f, 0.0f, 1.0f,
						1, 1, 0, 0, 0, -1, 0.0f, 1.0f, 0.0f,
				},
				new int[] {
						0, 1, 2, 3, 4, 5
				}
		);
	}
	
	public static class Result {
		
		//layout: 3f vertex, 3f normal, 3f color
		public final float[] vertices;
		public final int[] indices;
		
		public Result(float[] vertices, int[] indices) {
			this.vertices = vertices;
			this.indices = indices;
		}
		
		public float[] unpackIndexBuffer() {
			return UnpackIndexBuffer.unpackIndexBuffer(vertices, 0, 9, indices);
		}
	}
}
