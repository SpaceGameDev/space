package space.game.asteroidsDemo.model;

public class Point3D {
	public float x;
	public float y;
	public float z;
	
	public Point3D(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	
	public final void set(float x, float y, float z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public final float distance(Point3D p1) {
		float dx, dy, dz;
		
		dx = this.x-p1.x;
		dy = this.y-p1.y;
		dz = this.z-p1.z;
		return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
	}
	
	public Point3D cross(Point3D p) {
		return new Point3D(
			y*p.z - z*p.y,
			z*p.x - x*p.z,
			x*p.y - y*p.x
		);
	}
	
	public Point3D normalize() {
		float length = (float) Math.sqrt(x * x + y * y + z * z);
		this.x /= length;
		this.y /= length;
		this.z /= length;
		return this;
	}
	
	public String toString() {
		return "(x: "+this.x+", y: "+this.y+", z: "+this.z+")";
	}
	
	public Point3D multiply(float f) {
		this.x *= f;
		this.y *= f;
		this.z *= f;
		return this;
	}
	
	public Point3D divide(float f) {
		this.x /= f;
		this.y /= f;
		this.z /= f;
		return this;
	}
	
	public Point3D move(Point3D point3D) {
		this.x += point3D.x;
		this.y += point3D.y;
		this.z += point3D.z;
		return this;
	}
	
	public static Point3D getMiddlePoint(Point3D p0, Point3D p1) {
		return new Point3D(
				(p0.x + p1.x) / 2,
				(p0.y + p1.y) / 2,
				(p0.z + p1.z) / 2
		);
	}
	
	public static Point3D getMiddlePointSpherical(Point3D p0, Point3D p1) {
		float l0 = (float) p0.length();
		float l1 = (float) p1.length();
		return getMiddlePoint(p0, p1).normalize().multiply((l0 + l1) / 2);
	}
	
	public double length() {
		return Math.sqrt(Math.pow(this.x,2) + Math.pow(this.y,2) + Math.pow(this.z,2));
	}
}
