package space.game.firstTriangle.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import space.game.firstTriangle.entity.Model;
import space.game.firstTriangle.entity.Point3D;
import space.game.firstTriangle.entity.Triangle;

public class ModelAsteroids {
	
	public static Result generateAsteroid(float r, float[] config) {
		return generateAsteroid2(r, config, System.currentTimeMillis());
	}
	
	public static Result generateAsteroid(float r, float[] config, long seed) {
		Random rand = new Random(seed);
//		float randomValue = rand.nextFloat() * config[0]
		return new Result(
				new float[] {
						0, -1, 0, 0, 0, 1, 1.0f, 1.0f, 1.0f,
						1, 1, 0, 0, 0, 1, 0.0f, 1.0f, 0.0f,
						-1, 1, 0, 0, 0, 1, 0.0f, 0.0f, 1.0f,
						0, -1, 0, 0, 0, -1, 1.0f, 1.0f, 1.0f,
						-1, 1, 0, 0, 0, -1, 0.0f, 0.0f, 1.0f,
						1, 1, 0, 0, 0, -1, 0.0f, 1.0f, 0.0f,
				},
				new int[] {
						0, 1, 2, 3, 4, 5
				}
		);
	}
	
	public static Result generateAsteroid2(float r, float[] config, long seed) {
		Random rand = new Random(seed);
		Model m = generateIcosphere(r);
		
		for(int i = 0; i < config.length; i++) {
			Map<String, Point3D> dictionary = new HashMap<>();
			List<Triangle> triangles = m.get();
			List<Triangle> newTriangles = new ArrayList<>();
			for(Triangle triangle : triangles) {
				Point3D a = getMiddlePoint(triangle.p0, triangle.p1);
				Point3D b = getMiddlePoint(triangle.p1, triangle.p2);
				Point3D c = getMiddlePoint(triangle.p2, triangle.p0);
				
				if(dictionary.containsKey(a.toString())){
					a = dictionary.get(a.toString());
				}else{
					String key = a.toString();
					a.multiply(1f + rand.nextFloat() * config[i]);
					dictionary.put(key, a);
				}
				if(dictionary.containsKey(b.toString())){
					b = dictionary.get(b.toString());
				}else{
					String key = b.toString();
					b.multiply(1f + rand.nextFloat() * config[i]);
					dictionary.put(key, b);
				}
				
				if(dictionary.containsKey(c.toString())){
					c = dictionary.get(c.toString());
				}else{
					String key = c.toString();
					c.multiply(1f + rand.nextFloat() * config[i]);
					dictionary.put(key, c);
				}
				newTriangles.add(new Triangle(triangle.p0, c, a));
				newTriangles.add(new Triangle(triangle.p1, a, b));
				newTriangles.add(new Triangle(triangle.p2, b, c));
				newTriangles.add(new Triangle(a, c, b));
			}
			m.set(newTriangles);
		}
		
		return new Result (
				m.getFloats(),
				m.getIndices()
		);
	}
	
	public static Point3D getMiddlePoint(Point3D p0, Point3D p1) {
		return new Point3D(
				(p0.x + p1.x) / 2,
				(p0.y + p1.y) / 2,
				(p0.z + p1.z) / 2
		);
	}
	
	public static Model generateIcosphere(float radius) {
		List<Point3D> points = new ArrayList<>();
		var t = (float) (radius + Math.sqrt(5.0)) / 2.0f; //radius; //(float) Math.sqrt(Math.pow(radius, 2) - (Math.pow(radius/2, 2))); //(1.0f + Math.sqrt(radius)) / 2.0f;
		points.add(0, new Point3D(-1, t, 0));
		points.add(1, new Point3D(1, t, 0));
		points.add(2, new Point3D(-1, -t, 0));
		points.add(3, new Point3D(1, -t, 0));
		
		points.add(4, new Point3D(0, -1, t));
		points.add(5, new Point3D(0, 1, t));
		points.add(6, new Point3D(0, -1, -t));
		points.add(7, new Point3D(0, 1, -t));
		
		points.add(8, new Point3D(t, 0 , -1));
		points.add(9, new Point3D(t, 0, 1));
		points.add(10, new Point3D(-t, 0, -1));
		points.add(11, new Point3D(-t, 0, 1));
		
		Model m = new Model();
		
		/*m.add(new Triangle(
				new Point3D(0, -1, 0),
				new Point3D(1, 1, 0),
				new Point3D(-1, 1, 0),
				new Point3D(0, 0, 1f),
				new Point3D(1,1,1)
		));*/
		
		/*m.add(new Triangle(
				new Point3D(0, -1, 0),
				new Point3D(1, 1, 0),
				new Point3D(-1, 1, 0)
		));*/
	
		m.add(new Triangle(points.get(0), points.get(5), points.get(11)));
		m.add(new Triangle(points.get(0), points.get(1), points.get(5)));
		m.add(new Triangle(points.get(0), points.get(7), points.get(1)));
		m.add(new Triangle(points.get(0), points.get(10), points.get(7)));
		m.add(new Triangle(points.get(0), points.get(11), points.get(10)));
		
		m.add(new Triangle(points.get(1), points.get(9), points.get(5)));
		m.add(new Triangle(points.get(5), points.get(4), points.get(11)));
		m.add(new Triangle(points.get(11), points.get(2), points.get(10)));
		m.add(new Triangle(points.get(10), points.get(6), points.get(7)));
		m.add(new Triangle(points.get(7), points.get(8), points.get(1)));
		
		m.add(new Triangle(points.get(3), points.get(4), points.get(9)));
		m.add(new Triangle(points.get(3), points.get(2), points.get(4)));
		m.add(new Triangle(points.get(3), points.get(6), points.get(2)));
		m.add(new Triangle(points.get(3), points.get(8), points.get(6)));
		m.add(new Triangle(points.get(3), points.get(9), points.get(8)));
		
		
		m.add(new Triangle(points.get(4), points.get(5), points.get(9)));
		m.add(new Triangle(points.get(2), points.get(11), points.get(4)));
		m.add(new Triangle(points.get(6), points.get(10), points.get(2)));
		m.add(new Triangle(points.get(8), points.get(7), points.get(6)));
		m.add(new Triangle(points.get(9), points.get(1), points.get(8)));
		return m;
	}
	
	
	
	public static class Result {
		
		//layout: 3f vertex, 3f normal, 3f color
		public final float[] vertices;
		public final int[] indices;
		
		public Result(float[] vertices, int[] indices) {
			this.vertices = vertices;
			this.indices = indices;
		}
		
		public float[] unpackIndexBuffer() {
			return UnpackIndexBuffer.unpackIndexBuffer(vertices, 0, 9, indices);
		}
	}
}
