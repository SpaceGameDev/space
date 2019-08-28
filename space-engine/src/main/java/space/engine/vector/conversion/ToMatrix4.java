package space.engine.vector.conversion;

import space.engine.vector.Matrix4;

public interface ToMatrix4 {
	
	default Matrix4 toMatrix4() {
		return toMatrix4(new Matrix4());
	}
	
	default Matrix4 toMatrix4Inverse() {
		return toMatrix4Inverse(new Matrix4());
	}
	
	Matrix4 toMatrix4(Matrix4 mat);
	
	Matrix4 toMatrix4Inverse(Matrix4 mat);
}
