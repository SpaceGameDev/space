package space.engine.vector;

public class Vector3f {
	
	public float x;
	public float y;
	public float z;
	
	public Vector3f() {
	}
	
	@SuppressWarnings("CopyConstructorMissesField")
	public Vector3f(Vector3f vector) {
		set(vector);
	}
	
	public Vector3f(float[] array, int offset) {
		set(array, offset);
	}
	
	public Vector3f(float x, float y, float z) {
		set(x, y, z);
	}
	
	public Vector3f set(Vector3f vec) {
		return set(vec.x, vec.y, vec.z);
	}
	
	public Vector3f set(float[] array, int offset) {
		return set(array[offset], array[offset + 1], array[offset + 2]);
	}
	
	public Vector3f set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}
	
	public Vector3f zero() {
		this.x = 0;
		this.y = 0;
		this.z = 0;
		return this;
	}
	
	public Vector3f add(Vector3f vec) {
		this.x += vec.x;
		this.y += vec.y;
		this.z += vec.z;
		return this;
	}
	
	public Vector3f add(float x, float y, float z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}
	
	public Vector3f sub(Vector3f vec) {
		this.x -= vec.x;
		this.y -= vec.y;
		this.z -= vec.z;
		return this;
	}
	
	public Vector3f sub(float x, float y, float z) {
		this.x -= x;
		this.y -= y;
		this.z -= z;
		return this;
	}
	
	public Vector3f multiply(float scalar) {
		this.x *= scalar;
		this.y *= scalar;
		this.z *= scalar;
		return this;
	}
	
	public Vector3f divide(float scalar) {
		this.x /= scalar;
		this.y /= scalar;
		this.z /= scalar;
		return this;
	}
	
	public Vector3f inverse() {
		x = -x;
		y = -y;
		z = -z;
		return this;
	}
	
	public Vector3f abs() {
		x = Math.abs(x);
		y = Math.abs(y);
		z = Math.abs(z);
		return this;
	}
	
	public Vector3f rotate(Matrix3f mat) {
		return set(
				mat.m00 * x + mat.m01 * y + mat.m02 * z,
				mat.m10 * x + mat.m11 * y + mat.m12 * z,
				mat.m20 * x + mat.m21 * y + mat.m22 * z
		);
	}
	
	/**
	 * Only works if the Matrix is "pure", aka only used for rotation and translation
	 */
	public Vector3f rotateInversePure(Matrix3f mat) {
		return set(
				mat.m00 * x + mat.m10 * y + mat.m20 * z,
				mat.m01 * x + mat.m11 * y + mat.m21 * z,
				mat.m02 * x + mat.m12 * y + mat.m22 * z
		);
	}
	
	public Vector3f rotate(Matrix4f mat) {
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
	public Vector3f rotateInversePure(Matrix4f mat) {
		//w = 1
		float mag = mat.m03 * x + mat.m13 * y + mat.m23 * z + mat.m33;
		return set(
				(mat.m00 * x + mat.m10 * y + mat.m20 * z + mat.m30) / mag,
				(mat.m01 * x + mat.m11 * y + mat.m21 * z + mat.m31) / mag,
				(mat.m02 * x + mat.m12 * y + mat.m22 * z + mat.m32) / mag
		);
	}
	
	/**
	 * Only use this fast-path if you're only doing one rotation. Otherwise use {@link Quaternionf#toMatrix3(Matrix3f)} and rotate with that {@link Matrix3f}.
	 * <p>
	 * faster Algorithm from: https://blog.molecular-matters.com/2013/05/24/a-faster-quaternion-vector-multiplication/
	 */
	public Vector3f rotate(Quaternionf q) {
		Vector3f vec = new Vector3f(
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
	 * Only use this fast-path if you're only doing one rotation. Otherwise use {@link Quaternionf#toMatrix3(Matrix3f)} and rotate with that {@link Matrix3f}.
	 * <p>
	 * faster Algorithm from: https://blog.molecular-matters.com/2013/05/24/a-faster-quaternion-vector-multiplication/
	 */
	public Vector3f rotateInverse(Quaternionf q) {
		//inverse: q.w is negated
		Vector3f vec = new Vector3f(
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
	
	public float length() {
		return (float) Math.sqrt(x * x + y * y + z * z);
	}
	
	public float lengthSquared() {
		return x * x + y * y + z * z;
	}
	
	public Vector3f normalize() {
		return divide(length());
	}
	
	public Vector3f cross(Vector3f vec1, Vector3f vec2) {
		return set(
				vec1.y * vec2.z - vec2.y * vec1.z,
				vec1.z * vec2.x - vec2.z * vec1.x,
				vec1.x * vec2.y - vec2.x * vec1.y
		);
	}
	
	public static float distance(Vector3f from, Vector3f to) {
		float x = to.x - from.x;
		float y = to.y - from.y;
		float z = to.z - from.z;
		return (float) Math.sqrt(x * x + y * y + z * z);
	}
	
	public static float dot(Vector3f vec1, Vector3f vec2) {
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
		return "{" + x + " " + y + " " + z + "}";
	}
}
