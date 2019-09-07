package space.engine.vector.conversion;

import space.engine.vector.Matrix3;
import space.engine.vector.Matrix4;
import space.engine.vector.Quaternion;

public interface ToQuaternion extends ToMatrix3 {
	
	Quaternion toQuaternion();
	
	@Override
	default Matrix3 toMatrix3() {
		Quaternion q = toQuaternion();
		float xx = q.x * q.x * 2;
		float xy = q.x * q.y * 2;
		float xz = q.x * q.z * 2;
		float xw = q.x * q.w * 2;
		float yy = q.y * q.y * 2;
		float yz = q.y * q.z * 2;
		float yw = q.y * q.w * 2;
		float zz = q.z * q.z * 2;
		float zw = q.z * q.w * 2;
		
		return new Matrix3(
				1 - yy - zz, xy - zw, xz + yw,
				xy + zw, 1 - xx - zz, yz - xw,
				xz - yw, yz + xw, 1 - xx - yy
		);
	}
	
	@Override
	default Matrix3 toMatrix3Inverse() {
		Quaternion q = toQuaternion();
		float xx = q.x * q.x * 2;
		float xy = q.x * q.y * 2;
		float xz = q.x * q.z * 2;
		float xw = q.x * q.w * 2;
		float yy = q.y * q.y * 2;
		float yz = q.y * q.z * 2;
		float yw = q.y * q.w * 2;
		float zz = q.z * q.z * 2;
		float zw = q.z * q.w * 2;
		
		return new Matrix3(
				1 - yy - zz, xy + zw, xz - yw,
				xy - zw, 1 - xx - zz, yz + xw,
				xz + yw, yz - xw, 1 - xx - yy
		);
	}
	
	@Override
	default Matrix4 toMatrix4() {
		Quaternion q = toQuaternion();
		float xx = q.x * q.x * 2;
		float xy = q.x * q.y * 2;
		float xz = q.x * q.z * 2;
		float xw = q.x * q.w * 2;
		float yy = q.y * q.y * 2;
		float yz = q.y * q.z * 2;
		float yw = q.y * q.w * 2;
		float zz = q.z * q.z * 2;
		float zw = q.z * q.w * 2;
		
		return new Matrix4(
				1 - yy - zz, xy - zw, xz + yw, 0,
				xy + zw, 1 - xx - zz, yz - xw, 0,
				xz - yw, yz + xw, 1 - xx - yy, 0,
				0, 0, 0, 1
		);
	}
	
	@Override
	default Matrix4 toMatrix4Inverse() {
		Quaternion q = toQuaternion();
		float xx = q.x * q.x * 2;
		float xy = q.x * q.y * 2;
		float xz = q.x * q.z * 2;
		float xw = q.x * q.w * 2;
		float yy = q.y * q.y * 2;
		float yz = q.y * q.z * 2;
		float yw = q.y * q.w * 2;
		float zz = q.z * q.z * 2;
		float zw = q.z * q.w * 2;
		
		return new Matrix4(
				1 - yy - zz, xy + zw, xz - yw, 0,
				xy - zw, 1 - xx - zz, yz + xw, 0,
				xz + yw, yz - xw, 1 - xx - yy, 0,
				0, 0, 0, 1
		);
	}
}
