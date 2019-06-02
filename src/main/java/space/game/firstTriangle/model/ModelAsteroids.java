package space.game.firstTriangle.model;

import space.game.firstTriangle.entity.Model;
import space.game.firstTriangle.entity.Point3D;
import space.game.firstTriangle.entity.Triangle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ModelAsteroids {
	
	public static Result generateAsteroid(float r, float[] config) {
		return generateAsteroid(r, config, System.currentTimeMillis());
	}
	
	public static Result generateAsteroid(float r, float[] config, long seed) {
		Random rand = new Random(seed);
		Model m = generateIcosphere(r);
		
		for(int i = 0; i < config.length; i++) {
			Map<String, Point3D> dictionary = new HashMap<>();
			List<Triangle> triangles = m.get();
			List<Triangle> newTriangles = new ArrayList<>();
			for(Triangle triangle : triangles) {
				Point3D a = Point3D.getMiddlePoint(triangle.p0, triangle.p1);
				Point3D b = Point3D.getMiddlePoint(triangle.p1, triangle.p2);
				Point3D c = Point3D.getMiddlePoint(triangle.p2, triangle.p0);
				
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
				newTriangles.add(new Triangle(triangle.p0, a, c));
				newTriangles.add(new Triangle(triangle.p1, b, a));
				newTriangles.add(new Triangle(triangle.p2, c, b));
				newTriangles.add(new Triangle(a, b, c));
			}
			m.set(newTriangles);
		}
		
		return new Result (
				m.getFloats(),
				m.getIndices()
		);
	}
	
	
	
	public static Model generateIcosphere(float radius) {
		List<Point3D> points = new ArrayList<>();
		float longerSide = (float) (2.0f * radius / Math.sqrt(5.0f));
		float shorterSide = longerSide / 2.0f;
		//var t = (float) (radius + Math.sqrt(5.0)) / 2.0f; //radius; //(float) Math.sqrt(Math.pow(radius, 2) - (Math.pow(radius/2, 2))); //(1.0f + Math.sqrt(radius)) / 2.0f;
		points.add(0, new Point3D(-shorterSide, longerSide, 0));
		points.add(1, new Point3D(shorterSide, longerSide, 0));
		points.add(2, new Point3D(-shorterSide, -longerSide, 0));
		points.add(3, new Point3D(shorterSide, -longerSide, 0));
		
		points.add(4, new Point3D(0, -shorterSide, longerSide));
		points.add(5, new Point3D(0, shorterSide, longerSide));
		points.add(6, new Point3D(0, -shorterSide, -longerSide));
		points.add(7, new Point3D(0, shorterSide, -longerSide));
		
		points.add(8, new Point3D(longerSide, 0 , -shorterSide));
		points.add(9, new Point3D(longerSide, 0, shorterSide));
		points.add(10, new Point3D(-longerSide, 0, -shorterSide));
		points.add(11, new Point3D(-longerSide, 0, shorterSide));
		
		Model m = new Model();
	
		m.add(new Triangle(points.get(0), points.get(11), points.get(5)));
		m.add(new Triangle(points.get(0), points.get(5), points.get(1)));
		m.add(new Triangle(points.get(0), points.get(1), points.get(7)));
		m.add(new Triangle(points.get(0), points.get(7), points.get(10)));
		m.add(new Triangle(points.get(0), points.get(10), points.get(11)));
		
		m.add(new Triangle(points.get(1), points.get(5), points.get(9)));
		m.add(new Triangle(points.get(5), points.get(11), points.get(4)));
		m.add(new Triangle(points.get(11), points.get(10), points.get(2)));
		m.add(new Triangle(points.get(10), points.get(7), points.get(6)));
		m.add(new Triangle(points.get(7), points.get(1), points.get(8)));
		
		m.add(new Triangle(points.get(3), points.get(9), points.get(4)));
		m.add(new Triangle(points.get(3), points.get(4), points.get(2)));
		m.add(new Triangle(points.get(3), points.get(2), points.get(6)));
		m.add(new Triangle(points.get(3), points.get(6), points.get(8)));
		m.add(new Triangle(points.get(3), points.get(8), points.get(9)));
		
		
		m.add(new Triangle(points.get(4), points.get(9), points.get(5)));
		m.add(new Triangle(points.get(2), points.get(4), points.get(11)));
		m.add(new Triangle(points.get(6), points.get(2), points.get(10)));
		m.add(new Triangle(points.get(8), points.get(6), points.get(7)));
		m.add(new Triangle(points.get(9), points.get(8), points.get(1)));
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
