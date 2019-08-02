package space.game.asteroidsDemo.model;

import space.engine.vector.Vector3f;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;
import java.util.stream.IntStream;

import static java.lang.Math.sqrt;

public class ModelAsteroids {
	
	public static Result generateAsteroid(float radius, float[] config) {
		return generateAsteroid(radius, config, System.currentTimeMillis());
	}
	
	public static Result generateAsteroid(float radius, float[] config, long seed) {
		Result result = icosaeder(radius, seed, config.length > 0 ? config[0] : 0);
		for (int i = 1; i < config.length; i++)
			result = subdivision(result, radius, seed, config[i]);
		return result;
	}
	
	public static Result icosaeder(float radius, long seed, float randomness) {
		float t = (1 + (float) sqrt(5)) / 2;
		float[] position = {
				-1, t, 0,
				1, t, 0,
				-1, -t, 0,
				1, -t, 0,
				
				0, -1, t,
				0, 1, t,
				0, -1, -t,
				0, 1, -t,
				
				t, 0, -1,
				t, 0, 1,
				-t, 0, -1,
				-t, 0, 1,
		};
		
		float[] vertex = new float[position.length * 2];
		Random r = new Random(seed);
		for (int i = 0; i < position.length; i += 3) {
			Vector3f normalized = new Vector3f(position, i).normalize();
			normalized.write(vertex, i * 2 + 3);
			normalized.multiply(radius + (randomness == 0 ? 0 : ((r.nextFloat() * 2 - 1) * randomness * radius)));
			normalized.write(vertex, i * 2);
		}
		
		return new Result(vertex, new int[] {
				0, 11, 5,
				0, 5, 1,
				0, 1, 7,
				0, 7, 10,
				0, 10, 11,
				
				1, 5, 9,
				5, 11, 4,
				11, 10, 2,
				10, 7, 6,
				7, 1, 8,
				
				3, 9, 4,
				3, 4, 2,
				3, 2, 6,
				3, 6, 8,
				3, 8, 9,
				
				4, 9, 5,
				2, 4, 11,
				6, 2, 10,
				8, 6, 7,
				9, 8, 1,
		});
	}
	
	public static Result subdivision(Result from, float radius, long seed, float randomness) {
		
		class Key {
			
			//x < y
			public final int x;
			public final int y;
			
			@SuppressWarnings("SuspiciousNameCombination")
			public Key(int x, int y) {
				if (x < y) {
					this.x = x;
					this.y = y;
				} else {
					this.x = y;
					this.y = x;
				}
			}
			
			@Override
			public boolean equals(Object o) {
				if (this == o)
					return true;
				if (!(o instanceof Key))
					return false;
				Key key = (Key) o;
				return x == key.x &&
						y == key.y;
			}
			
			@Override
			public int hashCode() {
				return Objects.hash(x, y);
			}
		}
		
		class Value {
			
			public final int id;
			public final Vector3f vector;
			
			public Value(int id, Vector3f vector) {
				this.id = id;
				this.vector = vector;
			}
		}
		
		Random r = new Random(seed);
		HashMap<Key, Value> map = new HashMap<>();
		int vertexCount = from.vertices.length / 6;
		int[] vertexIdCounter = new int[] {vertexCount};
		int[] outIndex = new int[from.indices.length * 4];
		
		for (int i = 0; i < from.indices.length; i += 3) {
			final int i2 = i;
			Value[] outer = IntStream.range(0, 3)
									 .mapToObj(j -> new Value(from.indices[i2 + j], new Vector3f(from.vertices, from.indices[i2 + j] * 6)))
									 .toArray(Value[]::new);
			
			Value[] middle = Arrays.stream(new Key[] {
					new Key(from.indices[i], from.indices[i + 1]),
					new Key(from.indices[i + 1], from.indices[i + 2]),
					new Key(from.indices[i + 2], from.indices[i]),
			}).map(line -> map.computeIfAbsent(line, pair -> {
				Vector3f p0 = new Vector3f(from.vertices, pair.x * 6);
				Vector3f p1 = new Vector3f(from.vertices, pair.y * 6);
				//lerp 0.5f
				Vector3f middle2 = new Vector3f(p1).sub(p0).multiply(0.5f).add(p0);
				//slerp 0.5f + randomness
				middle2.normalize().multiply((p0.length() + p1.length()) / 2 + (randomness == 0 ? 0 : ((r.nextFloat() * 2 - 1) * randomness * radius)));
				return new Value(vertexIdCounter[0]++, middle2);
			})).toArray(Value[]::new);
			
			Value[] triangles = {
					outer[0], middle[0], middle[2],
					outer[1], middle[1], middle[0],
					outer[2], middle[2], middle[1],
					middle[0], middle[1], middle[2],
			};
			for (int t = 0; t < triangles.length; t++) {
				outIndex[i * 4 + t] = triangles[t].id;
			}
		}
		
		float[] outVertex = new float[vertexIdCounter[0] * 6];
		System.arraycopy(from.vertices, 0, outVertex, 0, from.vertices.length);
		for (Value value : map.values()) {
			int offset = value.id * 6;
			value.vector.write(outVertex, offset);
			value.vector.normalize().write(outVertex, offset + 3);
		}
		
		return new Result(outVertex, outIndex);
	}
	
	public static class Result {
		
		//layout: 3f vertex, 3f normal
		public final float[] vertices;
		public final int[] indices;
		
		public Result(float[] vertices, int[] indices) {
			this.vertices = vertices;
			this.indices = indices;
		}
		
		public float[] unpackIndexBuffer() {
			return UnpackIndexBuffer.unpackIndexBuffer(vertices, 0, 6, indices);
		}
	}
}
