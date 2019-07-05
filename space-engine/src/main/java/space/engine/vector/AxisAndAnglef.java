package space.engine.vector;

import org.jetbrains.annotations.NotNull;

import static java.lang.Math.*;

public class AxisAndAnglef {
	
	public @NotNull Vector3f axis = new Vector3f();
	/**
	 * in radians
	 */
	public float angle;
	
	public AxisAndAnglef() {
	}
	
	@SuppressWarnings("CopyConstructorMissesField")
	public AxisAndAnglef(AxisAndAnglef axisAndAngle) {
		set(axisAndAngle.axis, axisAndAngle.angle);
	}
	
	public AxisAndAnglef(@NotNull Vector3f axis, float angle) {
		set(axis, angle);
	}
	
	public AxisAndAnglef(float[] array, int offset) {
		set(array, offset);
	}
	
	public AxisAndAnglef(float x, float y, float z, float angle) {
		set(x, y, z, angle);
	}
	
	public AxisAndAnglef set(AxisAndAnglef axisAndAngle) {
		return set(axisAndAngle.axis.x, axisAndAngle.axis.y, axisAndAngle.axis.z, axisAndAngle.angle);
	}
	
	public AxisAndAnglef set(Vector3f axis, float angle) {
		return set(axis.x, axis.y, axis.z, angle);
	}
	
	public AxisAndAnglef set(float[] array, int offset) {
		return set(array[offset], array[offset + 1], array[offset + 2], array[offset + 3]);
	}
	
	public AxisAndAnglef set(float x, float y, float z, float angle) {
		this.axis.x = x;
		this.axis.y = y;
		this.axis.z = z;
		this.angle = angle;
		return this;
	}
	
	public Matrix3f toMatrix3(Matrix3f mat) {
		float s = (float) sin(angle);
		float c = (float) cos(angle);
		float c1 = 1 - c;
		
		float xx = axis.x * axis.x * c1;
		float xy = axis.x * axis.y * c1;
		float xz = axis.x * axis.z * c1;
		float yy = axis.y * axis.y * c1;
		float yz = axis.y * axis.z * c1;
		float zz = axis.z * axis.z * c1;
		
		float xs = axis.x * s;
		float ys = axis.y * s;
		float zs = axis.z * s;
		
		return mat.set(
				xx + c, xy - zs, xz + ys,
				xy + zs, yy + c, yz - xs,
				xz - ys, yz + xs, zz + c
		);
	}
	
	public Matrix4f toMatrix4(Matrix4f mat) {
		float s = (float) sin(angle);
		float c = (float) cos(angle);
		float c1 = 1 - c;
		
		float xx = axis.x * axis.x * c1;
		float xy = axis.x * axis.y * c1;
		float xz = axis.x * axis.z * c1;
		float yy = axis.y * axis.y * c1;
		float yz = axis.y * axis.z * c1;
		float zz = axis.z * axis.z * c1;
		
		float xs = axis.x * s;
		float ys = axis.y * s;
		float zs = axis.z * s;
		
		return mat.set(
				xx + c, xy - zs, xz + ys, 0,
				xy + zs, yy + c, yz - xs, 0,
				xz - ys, yz + xs, zz + c, 0,
				0, 0, 0, 1
		);
	}
	
	public Quaternionf toQuaternion(Quaternionf quaternion) {
		float s = (float) sin(angle / 2) / axis.length();
		return quaternion.set(axis.x * s, axis.y * s, axis.z * s, (float) cos(angle / 2));
	}
	
	@Override
	public String toString() {
		return "{a " + axis.x + " " + axis.y + " " + axis.z + " " + angle + "}";
	}
	
	private static final float DEGREE_TO_RADIANS = (float) (PI / 180);
	
	public static float toRadians(float degree) {
		return degree * DEGREE_TO_RADIANS;
	}
}
