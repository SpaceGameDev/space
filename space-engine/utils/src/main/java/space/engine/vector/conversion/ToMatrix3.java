package space.engine.vector.conversion;

import space.engine.vector.Matrix3;
import space.engine.vector.Matrix4;

public interface ToMatrix3 extends ToMatrix4 {
	
	Matrix3 toMatrix3();
	
	Matrix3 toMatrix3Inverse();
	
	@Override
	default Matrix4 toMatrix4() {
		Matrix3 src = toMatrix3();
		return new Matrix4(
				src.m00, src.m01, src.m02, 0,
				src.m10, src.m11, src.m12, 0,
				src.m20, src.m21, src.m22, 0,
				0, 0, 0, 1
		);
	}
	
	@Override
	default Matrix4 toMatrix4Inverse() {
		Matrix3 src = toMatrix3();
		return new Matrix4(
				src.m00, src.m10, src.m20, 0,
				src.m01, src.m11, src.m21, 0,
				src.m02, src.m12, src.m22, 0,
				0, 0, 0, 1
		);
	}
}
