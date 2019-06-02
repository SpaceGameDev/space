package space.game.firstTriangle.entity;

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
	
	public final double distance(Point3D p1)
	{
		double dx, dy, dz;
		
		dx = this.x-p1.x;
		dy = this.y-p1.y;
		dz = this.z-p1.z;
		return Math.sqrt(dx*dx+dy*dy+dz*dz);
	}
	
	public Point3D cross(Point3D p) {
		return new Point3D(
			y*p.z - z*p.y,
			z*p.x - x*p.z,
			x*p.y - y*p.x
		);
	}
	
	public String toString() {
		return "(x: "+this.x+", y: "+this.y+", z: "+this.z+")";
	}
	
	public void multiply(float f) {
		this.x *= f;
		this.y *= f;
		this.z *= f;
	}
	
	public void divide(float f) {
		this.x /= f;
		this.y /= f;
		this.z /= f;
	}
	
	public static Point3D getMiddlePoint(Point3D p0, Point3D p1) {
		return new Point3D(
				(p0.x + p1.x) / 2,
				(p0.y + p1.y) / 2,
				(p0.z + p1.z) / 2
		);
	}
	
	public double length() {
		return Math.sqrt(Math.pow(this.x,2) + Math.pow(this.y,2) + Math.pow(this.z,2));
	}
}
