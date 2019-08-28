package space.engine.vector;

import org.jetbrains.annotations.NotNull;
import space.engine.vector.conversion.ToAxisAngle;

import static java.lang.Math.PI;

public class AxisAngle implements ToAxisAngle {
	
	private static final float DEGREE_TO_RADIANS = (float) (PI / 180);
	
	public static float toRadians(float degree) {
		return degree * DEGREE_TO_RADIANS;
	}
	
	//object
	public @NotNull Vector3 axis = new Vector3();
	/**
	 * in radians
	 */
	public float angle;
	
	public AxisAngle() {
	}
	
	@SuppressWarnings("CopyConstructorMissesField")
	public AxisAngle(AxisAngle axisAndAngle) {
		set(axisAndAngle.axis, axisAndAngle.angle);
	}
	
	public AxisAngle(@NotNull Vector3 axis, float angle) {
		set(axis, angle);
	}
	
	public AxisAngle(float[] array, int offset) {
		set(array, offset);
	}
	
	public AxisAngle(float x, float y, float z, float angle) {
		set(x, y, z, angle);
	}
	
	public AxisAngle set(AxisAngle axisAndAngle) {
		return set(axisAndAngle.axis.x, axisAndAngle.axis.y, axisAndAngle.axis.z, axisAndAngle.angle);
	}
	
	public AxisAngle set(Vector3 axis, float angle) {
		return set(axis.x, axis.y, axis.z, angle);
	}
	
	public AxisAngle set(float[] array, int offset) {
		return set(array[offset], array[offset + 1], array[offset + 2], array[offset + 3]);
	}
	
	public AxisAngle set(float x, float y, float z, float angle) {
		this.axis.x = x;
		this.axis.y = y;
		this.axis.z = z;
		this.angle = angle;
		return this;
	}
	
	@Override
	public AxisAngle toAxisAnglef() {
		return this;
	}
	
	@Override
	public AxisAngle toAxisAnglef(AxisAngle axisAngle) {
		return axisAngle.set(this);
	}
	
	@Override
	public String toString() {
		return "AxisAngle{" +
				"" + axis.x +
				", " + axis.y +
				", " + axis.z +
				", " + angle +
				'}';
	}
}
