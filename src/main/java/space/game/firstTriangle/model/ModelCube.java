package space.game.firstTriangle.model;

public class ModelCube {
	
	public static final float[] CUBE;
	
	static {
		CUBE = new float[6 * 2 * 3 * 9];
		int index = 0;
		
		for (Function face : new Function[] {
				(x, y) -> new float[] {x, y, 1, 0, 0, 1, 1, 0, 0},
				(x, y) -> new float[] {x, -y, -1, 0, 0, -1, 0, 1, 0},
				(x, y) -> new float[] {x, 1, -y, 0, 1, 0, 0, 0, 1},
				(x, y) -> new float[] {x, -1, y, 0, -1, 0, 0, 0.5f, 0.5f},
				(x, y) -> new float[] {1, x, y, 1, 0, 0, 0.5f, 0, 0.5f},
				(x, y) -> new float[] {-1, x, -y, -1, 0, 0, 0.5f, 0.5f, 0},
		}) {
			for (float[] input : new float[][] {
					{-1, -1},
					{1, 1},
					{-1, 1},
					{1, 1},
					{-1, -1},
					{1, -1}
			}) {
				for (float f : face.apply(input[0], input[1])) {
					CUBE[index++] = f;
				}
			}
		}
		
		if (index != CUBE.length)
			throw new ExceptionInInitializerError();
	}
	
	@FunctionalInterface
	private interface Function {
		
		float[] apply(float x, float y);
	}
}
