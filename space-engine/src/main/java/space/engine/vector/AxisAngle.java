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
	public @NotNull Vector3 axis;
	/**
	 * in radians
	 */
	public float angle;
	
	public AxisAngle(float x, float y, float z, float angle) {
		this(new Vector3(x, y, z), angle);
	}
	
	public AxisAngle(@NotNull Vector3 axis, float angle) {
		this.axis = axis;
		this.angle = angle;
	}
	
	@Override
	public AxisAngle toAxisAnglef() {
		return this;
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
