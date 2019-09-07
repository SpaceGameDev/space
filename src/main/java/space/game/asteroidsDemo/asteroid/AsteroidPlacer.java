package space.game.asteroidsDemo.asteroid;

import space.engine.vector.AxisAngle;
import space.engine.vector.Quaternion;
import space.engine.vector.Vector3;

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
	public static final float PROBABILITY_FACTOR = 1f / 300;
	public static final float RADIUS_FACTOR = 4f;
	private static final float PI = (float) Math.PI;
	public static final float MAX_ROTATION_SPEED = PI / 16;
	public static final Vector3 MIDDLE_POINT = new Vector3(0, 0, 1).normalize().multiply(2000 * RADIUS_FACTOR);
	
	public static void placeAsteroids(AsteroidRenderer asteroidRenderer, float[] distribution) {
		placeAsteroids(asteroidRenderer, distribution, System.nanoTime());
	}
	
	public static void placeAsteroids(AsteroidRenderer asteroidRenderer, float[] distribution, long seed) {
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
				float probability = interpolerate(lower[1], upper[1], factor) * PROBABILITY_FACTOR;
				
				float circleLength = 2 * PI * radiusIndex;
				for (int circularIndex = 0; circularIndex < circleLength; circularIndex++) {
					if (r.nextFloat() > probability)
						continue;
					
					float typeFloat = r.nextFloat();
					int typeIndex = 0;
					for (; typeIndex < distributionNorm.length; typeIndex++)
						if ((typeFloat -= distributionNorm[typeIndex]) < 0)
							break;
					Asteroid ast = new Asteroid(typeIndex);
					Quaternion mat = new AxisAngle(0, 1, 0, (float) circularIndex / radiusIndex).toQuaternion();
					ast.position[0] = MIDDLE_POINT
							.add(new Vector3(
									0,
									(r.nextFloat() * 2 - 1) * interpolerate(lower[2], upper[2], factor),
									-interpolerate(lower[0], upper[0], factor) * RADIUS_FACTOR
							).rotate(mat));
					ast.position[1] = new Vector3(1, 0, 0)
							.multiply(1f)
							.rotate(mat);
					ast.rotation[0] = randomOrientation(r);
					ast.rotation[1] = randomRotation(r, MAX_ROTATION_SPEED);
					asteroidRenderer.addAsteroid(ast);
				}
			}
		}
	}
	
	private static float interpolerate(float a, float b, float factor) {
		return a * (1 - factor) + b * factor;
	}
	
	private static Vector3 randomVector(Random r, Vector3 areaOffset, Vector3 areaExtend) {
		return new Vector3(
				r.nextFloat() * areaExtend.x + areaOffset.x,
				r.nextFloat() * areaExtend.y + areaOffset.y,
				r.nextFloat() * areaExtend.z + areaOffset.z
		);
	}
	
	private static Vector3 randomVectorNormalized(Random r) {
		return new Vector3(
				r.nextFloat() * 2 - 1,
				r.nextFloat() * 2 - 1,
				r.nextFloat() * 2 - 1
		).normalize();
	}
	
	private static Quaternion randomOrientation(Random r) {
		//this random is a bit crap; it's not a normal distributed but it'll work just fine
		return new Quaternion(
				r.nextFloat() * 2 - 1,
				r.nextFloat() * 2 - 1,
				r.nextFloat() * 2 - 1,
				r.nextFloat() * 2 - 1
		).normalize();
	}
	
	private static Quaternion randomRotation(Random r, float maxAngle) {
		//this random is a bit crap; it's not a normal distributed but it'll work just fine
		return new AxisAngle(
				randomVectorNormalized(r),
				r.nextFloat() * maxAngle
		).toQuaternion();
	}
}
