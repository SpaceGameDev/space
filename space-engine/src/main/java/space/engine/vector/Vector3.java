package space.engine.vector;

import space.engine.vector.conversion.ToVector3;

public class Vector3 implements ToVector3 {
	
	public float x;
	public float y;
	public float z;
	
	public Vector3() {
	}
	
	public Vector3(ToVector3 vector) {
		set(vector);
	}
	
	public Vector3(float[] array, int offset) {
		set(array, offset);
	}
	
	public Vector3(float x, float y, float z) {
		set(x, y, z);
	}
	
	public Vector3 set(ToVector3 vec) {
		Vector3 vector3 = vec.toVector3();
		return set(vector3.x, vector3.y, vector3.z);
	}
	
	public Vector3 set(float[] array, int offset) {
		return set(array[offset], array[offset + 1], array[offset + 2]);
	}
	
	public Vector3 set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}
	
	public Vector3 zero() {
		this.x = 0;
		this.y = 0;
		this.z = 0;
		return this;
	}
	
	public Vector3 add(Vector3 vec) {
		this.x += vec.x;
		this.y += vec.y;
		this.z += vec.z;
		return this;
	}
	
	public Vector3 add(float x, float y, float z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}
	
	public Vector3 sub(Vector3 vec) {
		this.x -= vec.x;
		this.y -= vec.y;
		this.z -= vec.z;
		return this;
	}
	
	public Vector3 sub(float x, float y, float z) {
		this.x -= x;
		this.y -= y;
		this.z -= z;
		return this;
	}
	
	public Vector3 multiply(float scalar) {
		this.x *= scalar;
		this.y *= scalar;
		this.z *= scalar;
		return this;
	}
	
	public Vector3 divide(float scalar) {
		this.x /= scalar;
		this.y /= scalar;
		this.z /= scalar;
		return this;
	}
	
	public Vector3 inverse() {
		x = -x;
		y = -y;
		z = -z;
		return this;
	}
	
	public Vector3 abs() {
		x = Math.abs(x);
		y = Math.abs(y);
		z = Math.abs(z);
		return this;
	}
	
	public Vector3 rotate(Matrix3 mat) {
		return set(
				mat.m00 * x + mat.m01 * y + mat.m02 * z,
				mat.m10 * x + mat.m11 * y + mat.m12 * z,
				mat.m20 * x + mat.m21 * y + mat.m22 * z
		);
	}
	
	/**
	 * Only works if the Matrix is "pure", aka only used for rotation and translation
	 */
	public Vector3 rotateInverse(Matrix3 mat) {
		return set(
				mat.m00 * x + mat.m10 * y + mat.m20 * z,
				mat.m01 * x + mat.m11 * y + mat.m21 * z,
				mat.m02 * x + mat.m12 * y + mat.m22 * z
		);
	}
	
	public Vector3 rotate(Matrix4 mat) {
		//w = 1
		float mag = mat.m30 * x + mat.m31 * y + mat.m32 * z + mat.m33;
		return set(
				(mat.m00 * x + mat.m01 * y + mat.m02 * z + mat.m03) / mag,
				(mat.m10 * x + mat.m11 * y + mat.m12 * z + mat.m13) / mag,
				(mat.m20 * x + mat.m21 * y + mat.m22 * z + mat.m23) / mag
		);
	}
	
	/**
	 * Only works if the Matrix is "pure", aka only used for rotation and translation
	 */
	public Vector3 rotateInverse(Matrix4 mat) {
		//w = 1
		float mag = mat.m03 * x + mat.m13 * y + mat.m23 * z + mat.m33;
		return set(
				(mat.m00 * x + mat.m10 * y + mat.m20 * z + mat.m30) / mag,
				(mat.m01 * x + mat.m11 * y + mat.m21 * z + mat.m31) / mag,
				(mat.m02 * x + mat.m12 * y + mat.m22 * z + mat.m32) / mag
		);
	}
	
	/**
	 * Only use this fast-path if you're only doing one rotation. Otherwise use {@link Quaternion#toMatrix3(Matrix3)} and rotate with that {@link Matrix3}.
	 * <p>
	 * faster Algorithm from: https://blog.molecular-matters.com/2013/05/24/a-faster-quaternion-vector-multiplication/
	 */
	public Vector3 rotate(Quaternion q) {
		Vector3 vec = new Vector3(
				(q.y * z - y * q.z) * 2,
				(q.z * x - z * q.x) * 2,
				(q.x * y - x * q.y) * 2
		);
		return set(
				x + (vec.x * q.w) + (q.y * vec.z - vec.y * q.z),
				y + (vec.y * q.w) + (q.z * vec.x - vec.z * q.x),
				z + (vec.z * q.w) + (q.x * vec.y - vec.x * q.y)
		);
	}
	
	/**
	 * Only use this fast-path if you're only doing one rotation. Otherwise use {@link Quaternion#toMatrix3(Matrix3)} and rotate with that {@link Matrix3}.
	 * <p>
	 * faster Algorithm from: https://blog.molecular-matters.com/2013/05/24/a-faster-quaternion-vector-multiplication/
	 */
	public Vector3 rotateInverse(Quaternion q) {
		//inverse: q.w is negated
		Vector3 vec = new Vector3(
				(q.y * z - y * q.z) * 2,
				(q.z * x - z * q.x) * 2,
				(q.x * y - x * q.y) * 2
		);
		return set(
				x + (vec.x * -q.w) + (q.y * vec.z - vec.y * q.z),
				y + (vec.y * -q.w) + (q.z * vec.x - vec.z * q.x),
				z + (vec.z * -q.w) + (q.x * vec.y - vec.x * q.y)
		);
	}
	
	public Vector3 translate(Translation t) {
		return this.add(t.offset).rotate(t.matrix);
	}
	
	public Vector3 translateInverse(Translation t) {
		return this.rotateInverse(t.matrix).sub(t.offset);
	}
	
	public Vector3 translateRelative(Translation t) {
		return this.rotateInverse(t.matrix).add(t.offset);
	}
	
	public Vector3 translateRelativeInverse(Translation t) {
		return this.sub(t.offset).rotate(t.matrix);
	}
	
	public float length() {
		return (float) Math.sqrt(x * x + y * y + z * z);
	}
	
	public float lengthSquared() {
		return x * x + y * y + z * z;
	}
	
	public Vector3 normalize() {
		return divide(length());
	}
	
	public Vector3 cross(Vector3 vec1, Vector3 vec2) {
		return set(
				vec1.y * vec2.z - vec2.y * vec1.z,
				vec1.z * vec2.x - vec2.z * vec1.x,
				vec1.x * vec2.y - vec2.x * vec1.y
		);
	}
	
	@Override
	public Vector3 toVector3() {
		return this;
	}
	
	@Override
	public Vector3 toVector3(Vector3 vec) {
		return vec.set(this);
	}
	
	public static float distance(Vector3 vec1, Vector3 vec2) {
		float x = vec2.x - vec1.x;
		float y = vec2.y - vec1.y;
		float z = vec2.z - vec1.z;
		return (float) Math.sqrt(x * x + y * y + z * z);
	}
	
	public static float dot(Vector3 vec1, Vector3 vec2) {
		return vec1.x * vec2.x + vec1.y * vec2.y + vec1.z * vec2.z;
	}
	
	public float[] write(float[] array, int offset) {
		array[offset] = x;
		array[offset + 1] = y;
		array[offset + 2] = z;
		return array;
	}
	
	public float[] write4Aligned(float[] array, int offset) {
		array[offset] = x;
		array[offset + 1] = y;
		array[offset + 2] = z;
		array[offset + 3] = 0;
		return array;
	}
	
	@Override
	public String toString() {
		return "Vector3{" +
				"" + x +
				", " + y +
				", " + z +
				'}';
	}
}
