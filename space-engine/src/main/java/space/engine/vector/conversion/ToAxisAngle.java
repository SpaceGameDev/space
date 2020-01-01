package space.engine.vector.conversion;

import space.engine.vector.AxisAngle;
import space.engine.vector.Matrix3;
import space.engine.vector.Matrix4;
import space.engine.vector.Quaternion;

import static java.lang.Math.*;

public interface ToAxisAngle extends ToQuaternion {
	
	AxisAngle toAxisAnglef();
	
	@Override
	default Quaternion toQuaternion() {
		AxisAngle axisAngle = toAxisAnglef();
		float s = (float) sin(axisAngle.angle / 2) / axisAngle.axis.length();
		return new Quaternion(axisAngle.axis.x * s, axisAngle.axis.y * s, axisAngle.axis.z * s, (float) cos(axisAngle.angle / 2));
	}
	
	@Override
	default Matrix3 toMatrix3() {
		AxisAngle axisAngle = toAxisAnglef();
		float s = (float) sin(axisAngle.angle);
		float c = (float) cos(axisAngle.angle);
		float c1 = 1 - c;
		
		float xx = axisAngle.axis.x * axisAngle.axis.x * c1;
		float xy = axisAngle.axis.x * axisAngle.axis.y * c1;
		float xz = axisAngle.axis.x * axisAngle.axis.z * c1;
		float yy = axisAngle.axis.y * axisAngle.axis.y * c1;
		float yz = axisAngle.axis.y * axisAngle.axis.z * c1;
		float zz = axisAngle.axis.z * axisAngle.axis.z * c1;
		
		float xs = axisAngle.axis.x * s;
		float ys = axisAngle.axis.y * s;
		float zs = axisAngle.axis.z * s;
		
		return new Matrix3(
				xx + c, xy - zs, xz + ys,
				xy + zs, yy + c, yz - xs,
				xz - ys, yz + xs, zz + c
		);
	}
	
	@Override
	default Matrix4 toMatrix4() {
		AxisAngle axisAngle = toAxisAnglef();
		float s = (float) sin(axisAngle.angle);
		float c = (float) cos(axisAngle.angle);
		float c1 = 1 - c;
		
		float xx = axisAngle.axis.x * axisAngle.axis.x * c1;
		float xy = axisAngle.axis.x * axisAngle.axis.y * c1;
		float xz = axisAngle.axis.x * axisAngle.axis.z * c1;
		float yy = axisAngle.axis.y * axisAngle.axis.y * c1;
		float yz = axisAngle.axis.y * axisAngle.axis.z * c1;
		float zz = axisAngle.axis.z * axisAngle.axis.z * c1;
		
		float xs = axisAngle.axis.x * s;
		float ys = axisAngle.axis.y * s;
		float zs = axisAngle.axis.z * s;
		
		return new Matrix4(
				xx + c, xy - zs, xz + ys, 0,
				xy + zs, yy + c, yz - xs, 0,
				xz - ys, yz + xs, zz + c, 0,
				0, 0, 0, 1
		);
	}
}
