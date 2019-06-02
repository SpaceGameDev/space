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
}
