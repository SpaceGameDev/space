package space.engine.vector;

import space.engine.vector.conversion.ToMatrix3;

/**
 * a row major ordered matrix
 */
public class Matrix3 implements ToMatrix3 {
	
	public final float m00, m01, m02, m10, m11, m12, m20, m21, m22;
	
	public Matrix3(float m00, float m01, float m02, float m10, float m11, float m12, float m20, float m21, float m22) {
		this.m00 = m00;
		this.m01 = m01;
		this.m02 = m02;
		this.m10 = m10;
		this.m11 = m11;
		this.m12 = m12;
		this.m20 = m20;
		this.m21 = m21;
		this.m22 = m22;
	}
	
	public Matrix3 multiply(Matrix3 mat) {
		return new Matrix3(
				m00 * mat.m00 + m01 * mat.m10 + m02 * mat.m20,
				m00 * mat.m01 + m01 * mat.m11 + m02 * mat.m21,
				m00 * mat.m02 + m01 * mat.m12 + m02 * mat.m22,
				m10 * mat.m00 + m11 * mat.m10 + m12 * mat.m20,
				m10 * mat.m01 + m11 * mat.m11 + m12 * mat.m21,
				m10 * mat.m02 + m11 * mat.m12 + m12 * mat.m22,
				m20 * mat.m00 + m21 * mat.m10 + m22 * mat.m20,
				m20 * mat.m01 + m21 * mat.m11 + m22 * mat.m21,
				m20 * mat.m02 + m21 * mat.m12 + m22 * mat.m22
		);
	}
	
	/**
	 * = transpose
	 */
	public Matrix3 inverse() {
		return new Matrix3(
				m00, m10, m20,
				m01, m11, m21,
				m02, m12, m22
		);
	}
	
	public static Matrix3 identity() {
		return new Matrix3(
				1, 0, 0,
				0, 1, 0,
				0, 0, 1
		);
	}
	
	public static Matrix3 read(float[] array, int offset) {
		return new Matrix3(
				array[offset], array[offset + 1], array[offset + 2],
				array[offset + 3], array[offset + 4], array[offset + 5],
				array[offset + 6], array[offset + 7], array[offset + 8]
		);
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
	public Matrix3 toMatrix3Inverse() {
		return inverse();
	}
	
	@Override
	public String toString() {
		return "Matrix3{(" + m00 + " " + m01 + " " + m02 + ") (" + m10 + " " + m11 + " " + m12 + ") (" + m20 + " " + m21 + " " + m22 + ")}";
	}
}
