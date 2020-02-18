package space.engine.vector;

import space.engine.vector.conversion.ToVector2i;

public class Vector2i implements ToVector2i {
	
	public final int x, y;
	
	public Vector2i(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public Vector2i add(Vector2i vec) {
		return new Vector2i(
				this.x + vec.x,
				this.y + vec.y
		);
	}
	
	public Vector2i add(int x, int y) {
		return new Vector2i(
				this.x + x,
				this.y + y
		);
	}
	
	public Vector2i sub(Vector2i vec) {
		return new Vector2i(
				this.x - vec.x,
				this.y - vec.y
		);
	}
	
	public Vector2i sub(int x, int y) {
		return new Vector2i(
				this.x - x,
				this.y - y
		);
	}
	
	public Vector2i multiply(int scalar) {
		return new Vector2i(
				this.x * scalar,
				this.y * scalar
		);
	}
	
	public Vector2i divide(int scalar) {
		return new Vector2i(
				this.x / scalar,
				this.y / scalar
		);
	}
	
	public Vector2i inverse() {
		return new Vector2i(
				-x,
				-y
		);
	}
	
	public Vector2i abs() {
		return new Vector2i(
				Math.abs(x),
				Math.abs(y)
		);
	}
	
	public int length() {
		return (int) Math.sqrt(x * x + y * y);
	}
	
	public int lengthSquared() {
		return x * x + y * y;
	}
	
	public Vector2i normalize() {
		return divide(length());
	}
	
	@Override
	public Vector2i toVector2i() {
		return this;
	}
	
	public static int distance(Vector2i vec1, Vector2i vec2) {
		int x = vec2.x - vec1.x;
		int y = vec2.y - vec1.y;
		return (int) Math.sqrt(x * x + y * y);
	}
	
	public static int dot(Vector2i vec1, Vector2i vec2) {
		return vec1.x * vec2.x + vec1.y * vec2.y;
	}
	
	/**
	 * linear interpolation. Glsl: = mix()
	 */
	public static Vector2i lerp(Vector2i vec1, Vector2i vec2, int value) {
		return vec1.multiply(1 - value).add(vec2.multiply(value));
	}
	
	public static Vector2i zero() {
		return new Vector2i(0, 0);
	}
	
	public static Vector2i read(int[] array, int offset) {
		return new Vector2i(array[offset], array[offset + 1]);
	}
	
	public int[] write(int[] array, int offset) {
		array[offset] = x;
		array[offset + 1] = y;
		return array;
	}
	
	public int[] write4Aligned(int[] array, int offset) {
		array[offset] = x;
		array[offset + 1] = y;
		array[offset + 2] = 0;
		array[offset + 3] = 0;
		return array;
	}
	
	@Override
	public String toString() {
		return "Vector2{" +
				"" + x +
				", " + y +
				'}';
	}
}
