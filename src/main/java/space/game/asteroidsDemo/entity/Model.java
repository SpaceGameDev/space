package space.game.firstTriangle.entity;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Model {
	private List<Triangle> triangleList = new ArrayList<Triangle>();
	
	public void clear() {
		this.triangleList.clear();
	}
	
	public void add(Triangle triangle) {
		this.triangleList.add(triangle);
	}
	
	public List<Triangle> get() {
		return this.triangleList;
	}
	
	public void set(List<Triangle> triangleList) {
		this.triangleList = triangleList;
	}
	
	public void move(Point3D point3D) {
		for(Triangle triangle: triangleList) {
			triangle.p0.move(point3D);
			triangle.p1.move(point3D);
			triangle.p2.move(point3D);
		}
	}
	
	public float[] getFloats() {
		float[] floats = new float[triangleList.size() * 27];
		int index = 0;
		for(Triangle triangle: triangleList) {
			floats[index++] = triangle.p0.x;
			floats[index++] = triangle.p0.y;
			floats[index++] = triangle.p0.z;
			floats[index++] = triangle.normal.x;
			floats[index++] = triangle.normal.y;
			floats[index++] = triangle.normal.z;
			floats[index++] = triangle.color.x;
			floats[index++] = triangle.color.y;
			floats[index++] = triangle.color.z;
			
			floats[index++] = triangle.p1.x;
			floats[index++] = triangle.p1.y;
			floats[index++] = triangle.p1.z;
			floats[index++] = triangle.normal.x;
			floats[index++] = triangle.normal.y;
			floats[index++] = triangle.normal.z;
			floats[index++] = triangle.color.x;
			floats[index++] = triangle.color.y;
			floats[index++] = triangle.color.z;
			
			floats[index++] = triangle.p2.x;
			floats[index++] = triangle.p2.y;
			floats[index++] = triangle.p2.z;
			floats[index++] = triangle.normal.x;
			floats[index++] = triangle.normal.y;
			floats[index++] = triangle.normal.z;
			floats[index++] = triangle.color.x;
			floats[index++] = triangle.color.y;
			floats[index++] = triangle.color.z;
			
			
		}
		return floats;
	}
	
	public float[] getNormalFaceFloats() {
		float[] floats = new float[triangleList.size() * 27];
		int index = 0;
		for(Triangle triangle: triangleList) {
			
			Point3D normal0 = this.getNormal(triangle.p0);
			floats[index++] = triangle.p0.x;
			floats[index++] = triangle.p0.y;
			floats[index++] = triangle.p0.z;
			floats[index++] = normal0.x;
			floats[index++] = normal0.y;
			floats[index++] = normal0.z;
			floats[index++] = triangle.color.x;
			floats[index++] = triangle.color.y;
			floats[index++] = triangle.color.z;
			
			Point3D normal1 = this.getNormal(triangle.p1);
			floats[index++] = triangle.p1.x;
			floats[index++] = triangle.p1.y;
			floats[index++] = triangle.p1.z;
			floats[index++] = normal1.x;
			floats[index++] = normal1.y;
			floats[index++] = normal1.z;
			floats[index++] = triangle.color.x;
			floats[index++] = triangle.color.y;
			floats[index++] = triangle.color.z;
			
			Point3D normal2 = this.getNormal(triangle.p2);
			floats[index++] = triangle.p2.x;
			floats[index++] = triangle.p2.y;
			floats[index++] = triangle.p2.z;
			floats[index++] = normal2.x;
			floats[index++] = normal2.y;
			floats[index++] = normal2.z;
			floats[index++] = triangle.color.x;
			floats[index++] = triangle.color.y;
			floats[index++] = triangle.color.z;
			
		}
		return floats;
	}
	
	public int[] getIndices() {
		int[] indices = new int[this.triangleList.size() * 3];
		for(int i = 0; i < this.triangleList.size() * 3; i++) {
			indices[i] = i;
		}
		return indices;
	}
	
	private Point3D getNormal(Point3D p) {
		float x = 0,y = 0,z = 0;
		for(Triangle triangle: triangleList) {
			if(triangle.contains(p)){
				x += triangle.normal.x;
				y += triangle.normal.y;
				z += triangle.normal.z;
			}
		}
		return new Point3D(x,y,z);
	}
}
