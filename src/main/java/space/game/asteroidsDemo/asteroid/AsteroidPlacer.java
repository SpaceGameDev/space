package space.game.asteroidsDemo.asteroid;

import space.engine.vector.AxisAndAnglef;
import space.engine.vector.Matrix3f;
import space.engine.vector.Quaternionf;
import space.engine.vector.Vector3f;

import java.util.Random;

public class AsteroidPlacer {
	
	public static final float[][] CONFIG = new float[][] {
			{0, 0, 30},
			{1200, 0, 30},
			{1400, 0.25f, 150},
			{1800, 0.125f, 120},
			{2200, 0.075f, 90},
			{2600, 0.075f, 30},
			{3000, 0.25f, 210},
			{3200, 0.2f, 90},
			{3600, 0, 30}
	};
	public static final float PROBABILITY_FACTOR = 1f / 50;
	public static final float RADIUS_FACTOR = 1f;
	private static final float PI = (float) Math.PI;
	public static final float MAX_ROTATION_SPEED = PI / 16;
	public static final Vector3f MIDDLE_POINT = new Vector3f(0, 0, 1).normalize().multiply(2000 * RADIUS_FACTOR);
	
	public static void placeAsteroids(AsteroidRenderer asteroidRenderer, float[] distribution, int gasGiantId) {
		placeAsteroids(asteroidRenderer, distribution, gasGiantId, System.nanoTime());
	}
	
	public static void placeAsteroids(AsteroidRenderer asteroidRenderer, float[] distribution, int gasGiantId, long seed) {
		//gas giant
		Asteroid gasGiant = new Asteroid(gasGiantId);
		gasGiant.position[0].set(0, 0, 2000);
		gasGiant.rotation[1].multiply(new AxisAndAnglef(0, 1, 0, PI / 200));
		asteroidRenderer.addAsteroid(gasGiant);
		
		//asteroids
		Random r = new Random(seed);
		float distributionTotal = 0;
		for (float v : distribution)
			distributionTotal += v;
		float[] distributionNorm = new float[distribution.length];
		for (int i = 0; i < distribution.length; i++)
			distributionNorm[i] = distribution[i] / distributionTotal;
		
		for (int c = 1; c < CONFIG.length; c++) {
			float[] lower = CONFIG[c - 1];
			float[] upper = CONFIG[c];
			if (lower[1] == 0 && upper[1] == 0)
				continue;
			
			float distance = (upper[0] - lower[0]) * RADIUS_FACTOR;
			for (int radiusIndex = 0; radiusIndex < distance; radiusIndex++) {
				float factor = radiusIndex / distance;
				float propability = interpolerate(lower[1], upper[1], factor) * PROBABILITY_FACTOR;
				
				float circleLength = 2 * PI * radiusIndex;
				for (int circularIndex = 0; circularIndex < circleLength; circularIndex++) {
					if (r.nextFloat() > propability)
						continue;
					
					float typeFloat = r.nextFloat();
					int typeIndex = 0;
					for (; typeIndex < distributionNorm.length; typeIndex++)
						if ((typeFloat -= distributionNorm[typeIndex]) < 0)
							break;
					Asteroid ast = new Asteroid(typeIndex);
					Matrix3f mat = new AxisAndAnglef(0, 1, 0, (float) circularIndex / radiusIndex).toMatrix3(new Matrix3f());
					ast.position[0]
							.set(MIDDLE_POINT)
							.add(new Vector3f(
									0,
									(r.nextFloat() * 2 - 1) * interpolerate(lower[2], upper[2], factor),
									-interpolerate(lower[0], upper[0], factor) * RADIUS_FACTOR
							).rotate(mat));
					randomOrientation(r, ast.rotation[0]);
					randomRotation(r, MAX_ROTATION_SPEED, ast.rotation[1]);
					asteroidRenderer.addAsteroid(ast);
				}
			}
		}
	}
	
	private static float interpolerate(float a, float b, float factor) {
		return a * (1 - factor) + b * factor;
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
	
	private static Quaternionf randomOrientation(Random r, Quaternionf q) {
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
