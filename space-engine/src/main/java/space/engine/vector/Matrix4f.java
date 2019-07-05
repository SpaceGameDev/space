package space.engine.vector;

/**
 * a row major ordered matrix
 */
public class Matrix4f {
	
	public float m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32, m33;
	
	public Matrix4f() {
		identity();
	}
	
	@SuppressWarnings("CopyConstructorMissesField")
	public Matrix4f(Matrix4f mat) {
		set(mat);
	}
	
	public Matrix4f(float[] array, int offset) {
		set(array, offset);
	}
	
	public Matrix4f(float m00, float m01, float m02, float m03, float m10, float m11, float m12, float m13, float m20, float m21, float m22, float m23, float m30, float m31, float m32, float m33) {
		set(m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32, m33);
	}
	
	public Matrix4f set(Matrix4f mat) {
		set(mat.m00, mat.m01, mat.m02, mat.m03, mat.m10, mat.m11, mat.m12, mat.m13, mat.m20, mat.m21, mat.m22, mat.m23, mat.m30, mat.m31, mat.m32, mat.m33);
		return this;
	}
	
	public Matrix4f set(float[] array, int offset) {
		return set(
				array[offset], array[offset + 1], array[offset + 2], array[offset + 3],
				array[offset + 4], array[offset + 5], array[offset + 6], array[offset + 7],
				array[offset + 8], array[offset + 9], array[offset + 10], array[offset + 11],
				array[offset + 12], array[offset + 13], array[offset + 14], array[offset + 15]
		);
	}
	
	public Matrix4f set(float m00, float m01, float m02, float m03, float m10, float m11, float m12, float m13, float m20, float m21, float m22, float m23, float m30, float m31, float m32, float m33) {
		this.m00 = m00;
		this.m01 = m01;
		this.m02 = m02;
		this.m03 = m03;
		this.m10 = m10;
		this.m11 = m11;
		this.m12 = m12;
		this.m13 = m13;
		this.m20 = m20;
		this.m21 = m21;
		this.m22 = m22;
		this.m23 = m23;
		this.m30 = m30;
		this.m31 = m31;
		this.m32 = m32;
		this.m33 = m33;
		return this;
	}
	
	public Matrix4f identity() {
		return set(
				1, 0, 0, 0,
				0, 1, 0, 0,
				0, 0, 1, 0,
				0, 0, 0, 1
		);
	}
	
	public Matrix4f modelOffset(Vector3f vector) {
		Vector3f rotated = new Vector3f(vector).rotate(this);
		this.m03 += rotated.x;
		this.m13 += rotated.y;
		this.m23 += rotated.z;
		return this;
	}
	
	public Matrix4f modelScale(Vector3f vector) {
		this.m00 *= vector.x;
		this.m01 *= vector.x;
		this.m02 *= vector.x;
		this.m10 *= vector.y;
		this.m11 *= vector.y;
		this.m12 *= vector.y;
		this.m20 *= vector.z;
		this.m21 *= vector.z;
		this.m22 *= vector.z;
		return this;
	}
	
	public Matrix4f multiply(Matrix4f mat) {
		return multiply(this, mat);
	}
	
	public Matrix4f multiply(Matrix4f mat1, Matrix4f mat2) {
		return set(
				mat1.m00 * mat2.m00 + mat1.m01 * mat2.m10 + mat1.m02 * mat2.m20 + mat1.m03 * mat2.m30,
				mat1.m00 * mat2.m01 + mat1.m01 * mat2.m11 + mat1.m02 * mat2.m21 + mat1.m03 * mat2.m31,
				mat1.m00 * mat2.m02 + mat1.m01 * mat2.m12 + mat1.m02 * mat2.m22 + mat1.m03 * mat2.m32,
				mat1.m00 * mat2.m03 + mat1.m01 * mat2.m13 + mat1.m02 * mat2.m23 + mat1.m03 * mat2.m33,
				mat1.m10 * mat2.m00 + mat1.m11 * mat2.m10 + mat1.m12 * mat2.m20 + mat1.m13 * mat2.m30,
				mat1.m10 * mat2.m01 + mat1.m11 * mat2.m11 + mat1.m12 * mat2.m21 + mat1.m13 * mat2.m31,
				mat1.m10 * mat2.m02 + mat1.m11 * mat2.m12 + mat1.m12 * mat2.m22 + mat1.m13 * mat2.m32,
				mat1.m10 * mat2.m03 + mat1.m11 * mat2.m13 + mat1.m12 * mat2.m23 + mat1.m13 * mat2.m33,
				mat1.m20 * mat2.m00 + mat1.m21 * mat2.m10 + mat1.m22 * mat2.m20 + mat1.m23 * mat2.m30,
				mat1.m20 * mat2.m01 + mat1.m21 * mat2.m11 + mat1.m22 * mat2.m21 + mat1.m23 * mat2.m31,
				mat1.m20 * mat2.m02 + mat1.m21 * mat2.m12 + mat1.m22 * mat2.m22 + mat1.m23 * mat2.m32,
				mat1.m20 * mat2.m03 + mat1.m21 * mat2.m13 + mat1.m22 * mat2.m23 + mat1.m23 * mat2.m33,
				mat1.m30 * mat2.m00 + mat1.m31 * mat2.m10 + mat1.m32 * mat2.m20 + mat1.m33 * mat2.m30,
				mat1.m30 * mat2.m01 + mat1.m31 * mat2.m11 + mat1.m32 * mat2.m21 + mat1.m33 * mat2.m31,
				mat1.m30 * mat2.m02 + mat1.m31 * mat2.m12 + mat1.m32 * mat2.m22 + mat1.m33 * mat2.m32,
				mat1.m30 * mat2.m03 + mat1.m31 * mat2.m13 + mat1.m32 * mat2.m23 + mat1.m33 * mat2.m33
		);
	}
	
	/**
	 * Only works if the Matrix is "pure", aka only used for rotation and translation
	 */
	public Matrix4f inversePure() {
		return set(
				m00, m10, m20, -m03,
				m01, m11, m21, -m13,
				m02, m12, m22, -m23,
				0, 0, 0, 1
		);
	}
	
	public Matrix4f multiply(float scalar) {
		this.m00 *= scalar;
		this.m01 *= scalar;
		this.m02 *= scalar;
		this.m03 *= scalar;
		this.m10 *= scalar;
		this.m11 *= scalar;
		this.m12 *= scalar;
		this.m13 *= scalar;
		this.m20 *= scalar;
		this.m21 *= scalar;
		this.m22 *= scalar;
		this.m23 *= scalar;
		this.m30 *= scalar;
		this.m31 *= scalar;
		this.m32 *= scalar;
		this.m33 *= scalar;
		return this;
	}
	
	public Matrix4f multiply(double scalar) {
		this.m00 *= scalar;
		this.m01 *= scalar;
		this.m02 *= scalar;
		this.m03 *= scalar;
		this.m10 *= scalar;
		this.m11 *= scalar;
		this.m12 *= scalar;
		this.m13 *= scalar;
		this.m20 *= scalar;
		this.m21 *= scalar;
		this.m22 *= scalar;
		this.m23 *= scalar;
		this.m30 *= scalar;
		this.m31 *= scalar;
		this.m32 *= scalar;
		this.m33 *= scalar;
		return this;
	}
	
	public float[] write(float[] array, int offset) {
		array[offset] = m00;
		array[offset + 1] = m01;
		array[offset + 2] = m02;
		array[offset + 3] = m03;
		array[offset + 4] = m10;
		array[offset + 5] = m11;
		array[offset + 6] = m12;
		array[offset + 7] = m13;
		array[offset + 8] = m20;
		array[offset + 9] = m21;
		array[offset + 10] = m22;
		array[offset + 11] = m23;
		array[offset + 12] = m30;
		array[offset + 13] = m31;
		array[offset + 14] = m32;
		array[offset + 15] = m33;
		return array;
	}
	
	@Override
	public String toString() {
		return "{" + m00 + " " + m01 + " " + m02 + " " + m03 + "} {" + m10 + " " + m11 + " " + m12 + " " + m13 + "} {" + m20 + " " + m21 + " " + m22 + " " + m23 + "} {" + m30 + " " + m31 + " " + m32 + " " + m33 + "}";
	}
}
