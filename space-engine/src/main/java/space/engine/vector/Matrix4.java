package space.engine.vector;

import space.engine.vector.conversion.ToMatrix4;

/**
 * a row major ordered matrix
 */
public class Matrix4 implements ToMatrix4 {
	
	public final float m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32, m33;
	
	public Matrix4(float m00, float m01, float m02, float m03, float m10, float m11, float m12, float m13, float m20, float m21, float m22, float m23, float m30, float m31, float m32, float m33) {
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
	}
	
	public Matrix4 multiply(Matrix4 mat) {
		return new Matrix4(
				m00 * mat.m00 + m01 * mat.m10 + m02 * mat.m20 + m03 * mat.m30,
				m00 * mat.m01 + m01 * mat.m11 + m02 * mat.m21 + m03 * mat.m31,
				m00 * mat.m02 + m01 * mat.m12 + m02 * mat.m22 + m03 * mat.m32,
				m00 * mat.m03 + m01 * mat.m13 + m02 * mat.m23 + m03 * mat.m33,
				m10 * mat.m00 + m11 * mat.m10 + m12 * mat.m20 + m13 * mat.m30,
				m10 * mat.m01 + m11 * mat.m11 + m12 * mat.m21 + m13 * mat.m31,
				m10 * mat.m02 + m11 * mat.m12 + m12 * mat.m22 + m13 * mat.m32,
				m10 * mat.m03 + m11 * mat.m13 + m12 * mat.m23 + m13 * mat.m33,
				m20 * mat.m00 + m21 * mat.m10 + m22 * mat.m20 + m23 * mat.m30,
				m20 * mat.m01 + m21 * mat.m11 + m22 * mat.m21 + m23 * mat.m31,
				m20 * mat.m02 + m21 * mat.m12 + m22 * mat.m22 + m23 * mat.m32,
				m20 * mat.m03 + m21 * mat.m13 + m22 * mat.m23 + m23 * mat.m33,
				m30 * mat.m00 + m31 * mat.m10 + m32 * mat.m20 + m33 * mat.m30,
				m30 * mat.m01 + m31 * mat.m11 + m32 * mat.m21 + m33 * mat.m31,
				m30 * mat.m02 + m31 * mat.m12 + m32 * mat.m22 + m33 * mat.m32,
				m30 * mat.m03 + m31 * mat.m13 + m32 * mat.m23 + m33 * mat.m33
		);
	}
	
	public Matrix4 inverse() {
		return new Matrix4(
				m00, m10, m20, -m03,
				m01, m11, m21, -m13,
				m02, m12, m22, -m23,
				0, 0, 0, 1
		);
	}
	
	@Override
	public Matrix4 toMatrix4() {
		return this;
	}
	
	@Override
	public Matrix4 toMatrix4Inverse() {
		return inverse();
	}
	
	public static Matrix4 identity() {
		return new Matrix4(
				1, 0, 0, 0,
				0, 1, 0, 0,
				0, 0, 1, 0,
				0, 0, 0, 1
		);
	}
	
	public static Matrix4 read(float[] array, int offset) {
		return new Matrix4(
				array[offset], array[offset + 1], array[offset + 2], array[offset + 3],
				array[offset + 4], array[offset + 5], array[offset + 6], array[offset + 7],
				array[offset + 8], array[offset + 9], array[offset + 10], array[offset + 11],
				array[offset + 12], array[offset + 13], array[offset + 14], array[offset + 15]
		);
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
		return "Matrix4{(" + m00 + " " + m01 + " " + m02 + " " + m03 + ") (" + m10 + " " + m11 + " " + m12 + " " + m13 + ") (" + m20 + " " + m21 + " " + m22 + " " + m23 + ") (" + m30 + " " + m31 + " " + m32 + " " + m33 + ")}";
	}
}
