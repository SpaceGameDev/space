package space.engine.vector.conversion;

import space.engine.vector.Matrix3;
import space.engine.vector.Matrix4;

public interface ToMatrix3 extends ToMatrix4 {
	
	default Matrix3 toMatrix3() {
		return toMatrix3(new Matrix3());
	}
	
	default Matrix3 toMatrix3Inverse() {
		return toMatrix3Inverse(new Matrix3());
	}
	
	Matrix3 toMatrix3(Matrix3 mat);
	
	Matrix3 toMatrix3Inverse(Matrix3 mat);
	
	@Override
	default Matrix4 toMatrix4(Matrix4 mat) {
		Matrix3 src = toMatrix3();
		return mat.set(
				src.m00, src.m01, src.m02, 0,
				src.m10, src.m11, src.m12, 0,
				src.m20, src.m21, src.m22, 0,
				0, 0, 0, 1
		);
	}
	
	@Override
	default Matrix4 toMatrix4Inverse(Matrix4 mat) {
		Matrix3 src = toMatrix3();
		return mat.set(
				src.m00, src.m10, src.m20, 0,
				src.m01, src.m11, src.m21, 0,
				src.m02, src.m12, src.m22, 0,
				0, 0, 0, 1
		);
	}
}
