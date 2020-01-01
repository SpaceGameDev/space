package space.engine.vector;

import space.engine.vector.conversion.ToVector2;

public class Vector2 implements ToVector2 {
	
	public final float x, y;
	
	public Vector2(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public Vector2 add(Vector2 vec) {
		return new Vector2(
				this.x + vec.x,
				this.y + vec.y
		);
	}
	
	public Vector2 add(float x, float y) {
		return new Vector2(
				this.x + x,
				this.y + y
		);
	}
	
	public Vector2 sub(Vector2 vec) {
		return new Vector2(
				this.x - vec.x,
				this.y - vec.y
		);
	}
	
	public Vector2 sub(float x, float y) {
		return new Vector2(
				this.x - x,
				this.y - y
		);
	}
	
	public Vector2 multiply(float scalar) {
		return new Vector2(
				this.x * scalar,
				this.y * scalar
		);
	}
	
	public Vector2 divide(float scalar) {
		return new Vector2(
				this.x / scalar,
				this.y / scalar
		);
	}
	
	public Vector2 inverse() {
		return new Vector2(
				-x,
				-y
		);
	}
	
	public Vector2 abs() {
		return new Vector2(
				Math.abs(x),
				Math.abs(y)
		);
	}
	
	public float length() {
		return (float) Math.sqrt(x * x + y * y);
	}
	
	public float lengthSquared() {
		return x * x + y * y;
	}
	
	public Vector2 normalize() {
		return divide(length());
	}
	
	@Override
	public Vector2 toVector2() {
		return this;
	}
	
	public static float distance(Vector2 vec1, Vector2 vec2) {
		float x = vec2.x - vec1.x;
		float y = vec2.y - vec1.y;
		return (float) Math.sqrt(x * x + y * y);
	}
	
	public static float dot(Vector2 vec1, Vector2 vec2) {
		return vec1.x * vec2.x + vec1.y * vec2.y;
	}
	
	/**
	 * linear interpolation. Glsl: = mix()
	 */
	public static Vector2 lerp(Vector2 vec1, Vector2 vec2, float value) {
		return vec1.multiply(1 - value).add(vec2.multiply(value));
	}
	
	public static Vector2 zero() {
		return new Vector2(0, 0);
	}
	
	public static Vector2 read(float[] array, int offset) {
		return new Vector2(array[offset], array[offset + 1]);
	}
	
	public float[] write(float[] array, int offset) {
		array[offset] = x;
		array[offset + 1] = y;
		return array;
	}
	
	public float[] write4Aligned(float[] array, int offset) {
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
