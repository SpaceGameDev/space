package space.engine.vector;

import space.engine.vector.conversion.ToQuaternion;

import static java.lang.Math.*;

/**
 * The Quaternion is always normalized.
 */
public class Quaternion implements ToQuaternion {
	
	public float x, y, z, w;
	
	public Quaternion() {
		identity();
	}
	
	@SuppressWarnings("CopyConstructorMissesField")
	public Quaternion(Quaternion quaternion) {
		set(quaternion);
	}
	
	public Quaternion(float[] array, int offset) {
		set(array, offset);
	}
	
	public Quaternion(float x, float y, float z, float w) {
		set(x, y, z, w);
	}
	
	public Quaternion set(Quaternion q) {
		return set(q.x, q.y, q.z, q.w);
	}
	
	public Quaternion set(float[] array, int offset) {
		return set(array[offset], array[offset + 1], array[offset + 2], array[offset + 3]);
	}
	
	public Quaternion set(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
		return this;
	}
	
	public Quaternion identity() {
		return set(0, 0, 0, 1);
	}
	
	public Quaternion inverse() {
		w = -w;
		return this;
	}
	
	public Quaternion multiply(AxisAngle axisAndAngle) {
		return multiply(axisAndAngle.toQuaternion(new Quaternion()));
	}
	
	public Quaternion multiply(Quaternion quaternion) {
		return set(
				this.x * quaternion.w + this.w * quaternion.x + this.y * quaternion.z - this.z * quaternion.y,
				this.y * quaternion.w + this.w * quaternion.y + this.z * quaternion.x - this.x * quaternion.z,
				this.z * quaternion.w + this.w * quaternion.z + this.x * quaternion.y - this.y * quaternion.x,
				this.w * quaternion.w - this.x * quaternion.x - this.y * quaternion.y - this.z * quaternion.z
		);
	}
	
	public Quaternion multiplyInverse(AxisAngle axisAndAngle) {
		return multiply(axisAndAngle.toQuaternion(new Quaternion()));
	}
	
	public Quaternion multiplyInverse(Quaternion quaternion) {
		return set(
				this.x * -quaternion.w + this.w * quaternion.x + this.y * quaternion.z - this.z * quaternion.y,
				this.y * -quaternion.w + this.w * quaternion.y + this.z * quaternion.x - this.x * quaternion.z,
				this.z * -quaternion.w + this.w * quaternion.z + this.x * quaternion.y - this.y * quaternion.x,
				this.w * -quaternion.w - this.x * quaternion.x - this.y * quaternion.y - this.z * quaternion.z
		);
	}
	
	public Quaternion rotate(Matrix3 mat) {
		return set(
				mat.m00 * x + mat.m01 * y + mat.m02 * z,
				mat.m10 * x + mat.m11 * y + mat.m12 * z,
				mat.m20 * x + mat.m21 * y + mat.m22 * z,
				w
		);
	}
	
	/**
	 * Only works if the Matrix is "pure", aka only used for rotation and translation
	 */
	public Quaternion rotateInversePure(Matrix3 mat) {
		return set(
				mat.m00 * x + mat.m10 * y + mat.m20 * z,
				mat.m01 * x + mat.m11 * y + mat.m21 * z,
				mat.m02 * x + mat.m12 * y + mat.m22 * z,
				w
		);
	}
	
	public Quaternion rotate(Matrix4 mat) {
		//w = 1
		float mag = mat.m30 * x + mat.m31 * y + mat.m32 * z + mat.m33;
		return set(
				(mat.m00 * x + mat.m01 * y + mat.m02 * z + mat.m03) / mag,
				(mat.m10 * x + mat.m11 * y + mat.m12 * z + mat.m13) / mag,
				(mat.m20 * x + mat.m21 * y + mat.m22 * z + mat.m23) / mag,
				w
		);
	}
	
	/**
	 * Only works if the Matrix is "pure", aka only used for rotation and translation
	 */
	public Quaternion rotateInversePure(Matrix4 mat) {
		//w = 1
		float mag = mat.m03 * x + mat.m13 * y + mat.m23 * z + mat.m33;
		return set(
				(mat.m00 * x + mat.m10 * y + mat.m20 * z + mat.m30) / mag,
				(mat.m01 * x + mat.m11 * y + mat.m21 * z + mat.m31) / mag,
				(mat.m02 * x + mat.m12 * y + mat.m22 * z + mat.m32) / mag,
				w
		);
	}
	
	/**
	 * Only use this fast-path if you're only doing one rotation. Otherwise use {@link Quaternion#toMatrix3(Matrix3)} and rotate with that {@link Matrix3}.
	 * <p>
	 * faster Algorithm from: https://blog.molecular-matters.com/2013/05/24/a-faster-quaternion-vector-multiplication/
	 */
	public Quaternion rotate(Quaternion q) {
		Vector3 vec = new Vector3(
				(q.y * z - y * q.z) * 2,
				(q.z * x - z * q.x) * 2,
				(q.x * y - x * q.y) * 2
		);
		return set(
				x + (vec.x * q.w) + (q.y * vec.z - vec.y * q.z),
				y + (vec.y * q.w) + (q.z * vec.x - vec.z * q.x),
				z + (vec.z * q.w) + (q.x * vec.y - vec.x * q.y),
				w
		);
	}
	
	/**
	 * Only use this fast-path if you're only doing one rotation. Otherwise use {@link Quaternion#toMatrix3(Matrix3)} and rotate with that {@link Matrix3}.
	 * <p>
	 * faster Algorithm from: https://blog.molecular-matters.com/2013/05/24/a-faster-quaternion-vector-multiplication/
	 */
	public Quaternion rotateInverse(Quaternion q) {
		//inverse: q.w is negated
		Vector3 vec = new Vector3(
				(q.y * z - y * q.z) * 2,
				(q.z * x - z * q.x) * 2,
				(q.x * y - x * q.y) * 2
		);
		return set(
				x + (vec.x * -q.w) + (q.y * vec.z - vec.y * q.z),
				y + (vec.y * -q.w) + (q.z * vec.x - vec.z * q.x),
				z + (vec.z * -q.w) + (q.x * vec.y - vec.x * q.y),
				w
		);
	}
	
	@Override
	public Quaternion toQuaternion() {
		return this;
	}
	
	@Override
	public Quaternion toQuaternion(Quaternion q) {
		return q.set(this);
	}
	
	public float length() {
		return (float) Math.sqrt(x * x + y * y + z * z + w * w);
	}
	
	public Quaternion normalize() {
		float length = length();
		x /= length;
		y /= length;
		z /= length;
		w /= length;
		return this;
	}
	
	public float angle() {
		return angle(new Quaternion(), this);
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
	
	public static final float SLERP_THRESHOLD = 0.001f;
	
	public Quaternion slerp(Quaternion q2, float t) {
		return slerp(this, q2, t);
	}
	
	public Quaternion slerp(Quaternion q1, Quaternion q2, float t) {
		float cosHalfAngle = dot(q1, q2);
		if (abs(cosHalfAngle) >= 1) {
			//quaternions are equal, so no change
			if (this != q1 && this != q2)
				set(q1);
			return this;
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
		
		return set(
				q1.x * m1 + q2.x * m2,
				q1.y * m1 + q2.y * m2,
				q1.z * m1 + q2.z * m2,
				q1.w * m1 + q2.w * m2
		);
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
				"x=" + x +
				", y=" + y +
				", z=" + z +
				", w=" + w +
				'}';
	}
}
