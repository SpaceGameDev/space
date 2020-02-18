package space.engine.vector;

import space.engine.vector.conversion.ToQuaternion;

import static java.lang.Math.*;

/**
 * The Quaternion is always normalized.
 */
public class Quaternion implements ToQuaternion {
	
	public final float x, y, z, w;
	
	public Quaternion(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}
	
	public Quaternion inverse() {
		return new Quaternion(x, y, z, -w);
	}
	
	public Quaternion multiply(ToQuaternion q) {
		return multiply(q.toQuaternion());
	}
	
	public Quaternion multiply(Quaternion q) {
		return new Quaternion(
				this.x * q.w + this.w * q.x + this.y * q.z - this.z * q.y,
				this.y * q.w + this.w * q.y + this.z * q.x - this.x * q.z,
				this.z * q.w + this.w * q.z + this.x * q.y - this.y * q.x,
				this.w * q.w - this.x * q.x - this.y * q.y - this.z * q.z
		);
	}
	
	public Quaternion multiplyInverse(ToQuaternion q) {
		return multiply(q.toQuaternion());
	}
	
	public Quaternion multiplyInverse(Quaternion q) {
		return new Quaternion(
				this.x * -q.w + this.w * q.x + this.y * q.z - this.z * q.y,
				this.y * -q.w + this.w * q.y + this.z * q.x - this.x * q.z,
				this.z * -q.w + this.w * q.z + this.x * q.y - this.y * q.x,
				this.w * -q.w - this.x * q.x - this.y * q.y - this.z * q.z
		);
	}
	
	@Override
	public Quaternion toQuaternion() {
		return this;
	}
	
	public float length() {
		return (float) Math.sqrt(x * x + y * y + z * z + w * w);
	}
	
	public Quaternion normalize() {
		float l = length();
		return new Quaternion(x / l, y / l, z / l, w / l);
	}
	
	public float angle() {
		return angle(identity(), this);
	}
	
	public static float angle(Quaternion q1, Quaternion q2) {
		float cosHalfAngle = abs(dot(q1, q2));
		if (cosHalfAngle >= 1.0f)
			//quaternions are equal
			return 0;
		
		return (float) acos(cosHalfAngle) * 2;
	}
	
	public static float dot(Quaternion q1, Quaternion q2) {
		return q1.w * q2.w + q1.x * q2.x + q1.y * q2.y + q1.z * q2.z;
	}
	
	public static Quaternion identity() {
		return new Quaternion(0, 0, 0, 1);
	}
	
	public static final float SLERP_THRESHOLD = 0.001f;
	
	public static Quaternion slerp(Quaternion q2, float t) {
		return slerp(identity(), q2, t);
	}
	
	public static Quaternion slerp(Quaternion q1, Quaternion q2, float t) {
		float cosHalfAngle = dot(q1, q2);
		if (abs(cosHalfAngle) >= 1) {
			//quaternions are equal, so no change
			return q1;
		}
		
		float halfAngle = (float) acos(cosHalfAngle);
		float sinHalfAngle = (float) sqrt(1 - (cosHalfAngle * cosHalfAngle));
		
		float m1;
		float m2;
		if (abs(sinHalfAngle) < SLERP_THRESHOLD) {
			m1 = 0.5f;
			m2 = 0.5f;
		} else {
			m1 = (float) sin((1 - t) * halfAngle) / sinHalfAngle;
			m2 = (float) sin(t * halfAngle) / sinHalfAngle;
		}
		
		return new Quaternion(
				q1.x * m1 + q2.x * m2,
				q1.y * m1 + q2.y * m2,
				q1.z * m1 + q2.z * m2,
				q1.w * m1 + q2.w * m2
		);
	}
	
	public static Quaternion read(float[] array, int offset) {
		return new Quaternion(array[offset], array[offset + 1], array[offset + 2], array[offset + 3]);
	}
	
	public float[] write(float[] array, int offset) {
		array[offset] = x;
		array[offset + 1] = y;
		array[offset + 2] = z;
		array[offset + 3] = w;
		return array;
	}
	
	@Override
	public String toString() {
		return "Quaternion{" +
				"" + x +
				", " + y +
				", " + z +
				", " + w +
				'}';
	}
}
