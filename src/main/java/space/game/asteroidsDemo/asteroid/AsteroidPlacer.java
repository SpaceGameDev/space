package space.game.asteroidsDemo.asteroid;

import space.engine.vector.AxisAndAnglef;
import space.engine.vector.Quaternionf;
import space.engine.vector.Vector3f;

import java.util.Random;

public class AsteroidPlacer {
	
	public static final int ASTEROID_COUNT = 50;
	public static final Vector3f ASTEROID_AREA_OFFSET = new Vector3f(-50, -50, -50);
	public static final Vector3f ASTEROID_AREA_EXTEND = new Vector3f(100, 100, 100);
	
	public static void placeAsteroids(AsteroidRenderer asteroidRenderer, int asteroidModelCount) {
		placeAsteroids(asteroidRenderer, asteroidModelCount, System.nanoTime());
	}
	
	public static void placeAsteroids(AsteroidRenderer asteroidRenderer, int asteroidModelCount, long seed) {
		Random r = new Random(seed);
		for (int i = 0; i < ASTEROID_COUNT; i++) {
			Asteroid asteroid = new Asteroid(r.nextInt(asteroidModelCount));
			
			randomVector(r, asteroid.position, ASTEROID_AREA_OFFSET, ASTEROID_AREA_EXTEND);
			randomQuaternion(r, asteroid.rotation[0]);
			randomRotation(r, (float) Math.PI / 16, asteroid.rotation[1]);
			
			asteroidRenderer.addAsteroid(asteroid);
		}
	}
	
	private static Vector3f randomVector(Random r, Vector3f vec, Vector3f areaOffset, Vector3f areaExtend) {
		return vec.set(
				r.nextFloat() * areaExtend.x + areaOffset.x,
				r.nextFloat() * areaExtend.y + areaOffset.y,
				r.nextFloat() * areaExtend.z + areaOffset.z
		);
	}
	
	private static Vector3f randomVectorNormalized(Random r, Vector3f vec) {
		return vec.set(
				r.nextFloat() * 2 - 1,
				r.nextFloat() * 2 - 1,
				r.nextFloat() * 2 - 1
		).normalize();
	}
	
	private static Quaternionf randomQuaternion(Random r, Quaternionf q) {
		//this random is a bit crap; it's not a normal distributed but it'll work just fine
		return q.set(
				r.nextFloat() * 2 - 1,
				r.nextFloat() * 2 - 1,
				r.nextFloat() * 2 - 1,
				r.nextFloat() * 2 - 1
		).normalize();
	}
	
	private static Quaternionf randomRotation(Random r, float maxAngle, Quaternionf q) {
		//this random is a bit crap; it's not a normal distributed but it'll work just fine
		return new AxisAndAnglef(
				randomVectorNormalized(r, new Vector3f()),
				r.nextFloat() * maxAngle
		).toQuaternion(q);
	}
}
