package space.engine.vector;

import space.engine.vector.conversion.ToMatrix3;

/**
 * a row major ordered matrix
 */
public class Matrix3 implements ToMatrix3 {
	
	public float m00, m01, m02, m10, m11, m12, m20, m21, m22;
	
	public Matrix3() {
		identity();
	}
	
	@SuppressWarnings("CopyConstructorMissesField")
	public Matrix3(Matrix3 mat) {
		set(mat);
	}
	
	public Matrix3(float[] array, int offset) {
		set(array, offset);
	}
	
	public Matrix3(float m00, float m01, float m02, float m10, float m11, float m12, float m20, float m21, float m22) {
		set(m00, m01, m02, m10, m11, m12, m20, m21, m22);
	}
	
	public Matrix3 set(Matrix3 mat) {
		return set(mat.m00, mat.m01, mat.m02, mat.m10, mat.m11, mat.m12, mat.m20, mat.m21, mat.m22);
	}
	
	public Matrix3 set(float[] array, int offset) {
		return set(
				array[offset], array[offset + 1], array[offset + 2],
				array[offset + 3], array[offset + 4], array[offset + 5],
				array[offset + 6], array[offset + 7], array[offset + 8]
		);
	}
	
	public Matrix3 set(float m00, float m01, float m02, float m10, float m11, float m12, float m20, float m21, float m22) {
		this.m00 = m00;
		this.m01 = m01;
		this.m02 = m02;
		this.m10 = m10;
		this.m11 = m11;
		this.m12 = m12;
		this.m20 = m20;
		this.m21 = m21;
		this.m22 = m22;
		return this;
	}
	
	public Matrix3 identity() {
		return set(
				1, 0, 0,
				0, 1, 0,
				0, 0, 1
		);
	}
	
	public Matrix3 multiply(Matrix3 mat) {
		return multiply(this, mat);
	}
	
	public Matrix3 multiply(Matrix3 mat1, Matrix3 mat2) {
		return set(
				mat1.m00 * mat2.m00 + mat1.m01 * mat2.m10 + mat1.m02 * mat2.m20,
				mat1.m00 * mat2.m01 + mat1.m01 * mat2.m11 + mat1.m02 * mat2.m21,
				mat1.m00 * mat2.m02 + mat1.m01 * mat2.m12 + mat1.m02 * mat2.m22,
				mat1.m10 * mat2.m00 + mat1.m11 * mat2.m10 + mat1.m12 * mat2.m20,
				mat1.m10 * mat2.m01 + mat1.m11 * mat2.m11 + mat1.m12 * mat2.m21,
				mat1.m10 * mat2.m02 + mat1.m11 * mat2.m12 + mat1.m12 * mat2.m22,
				mat1.m20 * mat2.m00 + mat1.m21 * mat2.m10 + mat1.m22 * mat2.m20,
				mat1.m20 * mat2.m01 + mat1.m21 * mat2.m11 + mat1.m22 * mat2.m21,
				mat1.m20 * mat2.m02 + mat1.m21 * mat2.m12 + mat1.m22 * mat2.m22
		);
	}
	
	/**
	 * Only works if the Matrix is "pure", aka only used for rotation and translation
	 */
	public Matrix3 inverse() {
		return set(
				m00, m10, m20,
				m01, m11, m21,
				m02, m12, m22
		);
	}
	
	public Matrix3 multiply(float scalar) {
		this.m00 *= scalar;
		this.m01 *= scalar;
		this.m02 *= scalar;
		this.m10 *= scalar;
		this.m11 *= scalar;
		this.m12 *= scalar;
		this.m20 *= scalar;
		this.m21 *= scalar;
		this.m22 *= scalar;
		return this;
	}
	
	public Matrix3 multiply(double scalar) {
		this.m00 *= scalar;
		this.m01 *= scalar;
		this.m02 *= scalar;
		this.m10 *= scalar;
		this.m11 *= scalar;
		this.m12 *= scalar;
		this.m20 *= scalar;
		this.m21 *= scalar;
		this.m22 *= scalar;
		return this;
	}
	
	public float[] write(float[] array, int offset) {
		array[offset] = m00;
		array[offset + 1] = m01;
		array[offset + 2] = m02;
		array[offset + 3] = m10;
		array[offset + 4] = m11;
		array[offset + 5] = m12;
		array[offset + 6] = m20;
		array[offset + 7] = m21;
		array[offset + 8] = m22;
		return array;
	}
	
	public float[] write4Aligned(float[] array, int offset) {
		array[offset] = m00;
		array[offset + 1] = m01;
		array[offset + 2] = m02;
		array[offset + 3] = 0;
		array[offset + 4] = m10;
		array[offset + 5] = m11;
		array[offset + 6] = m12;
		array[offset + 7] = 0;
		array[offset + 8] = m20;
		array[offset + 9] = m21;
		array[offset + 10] = m22;
		array[offset + 11] = 0;
		return array;
	}
	
	@Override
	public Matrix3 toMatrix3() {
		return this;
	}
	
	@Override
	public Matrix3 toMatrix3(Matrix3 mat) {
		return mat.set(this);
	}
	
	@Override
	public Matrix3 toMatrix3Inverse(Matrix3 mat) {
		return mat.set(this).inverse();
	}
	
	
	@Override
	public String toString() {
		return "Matrix3{(" + m00 + " " + m01 + " " + m02 + ") (" + m10 + " " + m11 + " " + m12 + ") (" + m20 + " " + m21 + " " + m22 + ")}";
	}
}
