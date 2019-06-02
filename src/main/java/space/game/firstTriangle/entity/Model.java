package space.game.firstTriangle.entity;

import java.util.ArrayList;
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
	
	public int[] getIndices() {
		int[] indices = new int[this.triangleList.size() * 3];
		for(int i = 0; i < this.triangleList.size() * 3; i++) {
			indices[i] = i;
		}
		return indices;
	}
}
