package space.game.firstTriangle.entity;

public class Triangle {
	public Point3D p0;
	public Point3D p1;
	public Point3D p2;
	
	public Point3D normal;
	public Point3D color;
	
	public Triangle(Point3D p0, Point3D p1, Point3D p2, Point3D normal, Point3D color) {
		this.p0 = p0;
		this.p1 = p1;
		this.p2 = p2;
		this.normal = normal;
		this.color = color;
	}
	
	public Triangle(Point3D p0, Point3D p1, Point3D p2) {
		this.p0 = p0;
		this.p1 = p1;
		this.p2 = p2;
		this.calculateNormal();
		this.setRandomColor();
	}
	
	private void calculateNormal() {
		Point3D v0 = new Point3D(
				this.p1.x - this.p0.x,
				this.p1.y - this.p0.y,
				this.p1.z - this.p0.z
		);
		Point3D v1 = new Point3D(
				this.p2.x - this.p0.x,
				this.p2.y - this.p0.y,
				this.p2.z - this.p0.z
		);
		Point3D normal = v0.cross(v1);
		this.normal = normal;
	}
	
	private void setRandomColor() {
		//this.color = new Point3D(1.0f, 1.0f, 1.0f );
		this.color = new Point3D((float) Math.round(Math.random() * 100) / 100, (float) Math.round(Math.random() * 100) / 100, (float) Math.round(Math.random() * 100) / 100);
		//System.out.println(color.toString());
	}
	
	public void setColor(Point3D color) {
		this.color = color;
	}
	
	public boolean contains(Point3D p) {
		return this.p0 == p || this.p1 == p || this.p2 == p;
	}
}
