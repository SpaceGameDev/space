package space.game.firstTriangle.model;

public class UnpackIndexBuffer {
	
	public static float[] unpackIndexBuffer(float[] vertices, int offset, int stride, int[] indices) {
		float[] ret = new float[indices.length * stride];
		int index = 0;
		
		for (int i : indices)
			for (int j = 0; j < stride; j++)
				ret[index++] = vertices[offset + i * stride + j];
		
		return ret;
	}
}
