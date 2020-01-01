package space.engine.vector;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Assert;

import static space.engine.math.MathUtils.abs;

public class VectorUtils {
	
	public static void assertEquals(Vector3 expected, Vector3 actual, float delta) {
		Assert.assertThat(actual, new BaseMatcher<>() {
			@Override
			public boolean matches(Object actual) {
				if (!(actual instanceof Vector3))
					return false;
				
				Vector3 vec = (Vector3) actual;
				return abs(expected.x - vec.x) < delta
						&& abs(expected.y - vec.y) < delta
						&& abs(expected.z - vec.z) < delta;
			}
			
			@Override
			public void describeTo(Description description) {
				description.appendValue(expected).appendText(" with delta:").appendValue(delta);
			}
		});
	}
	
	public static void assertEquals(Quaternion expected, Quaternion actual, float delta) {
		Assert.assertThat(actual, new BaseMatcher<>() {
			@Override
			public boolean matches(Object actual) {
				if (!(actual instanceof Quaternion))
					return false;
				return abs(Quaternion.angle(expected, (Quaternion) actual)) < delta;
			}
			
			@Override
			public void describeTo(Description description) {
				description.appendValue(expected).appendText(" with delta:").appendValue(delta);
			}
		});
	}
	
	public static void assertEquals(Matrix3 expected, Matrix3 actual, float delta) {
		Assert.assertThat(actual, new BaseMatcher<>() {
			@Override
			public boolean matches(Object actual) {
				if (!(actual instanceof Matrix3))
					return false;
				
				Matrix3 mat = (Matrix3) actual;
				return abs(expected.m00 - mat.m00) < delta
						&& abs(expected.m01 - mat.m01) < delta
						&& abs(expected.m02 - mat.m02) < delta
						&& abs(expected.m10 - mat.m10) < delta
						&& abs(expected.m11 - mat.m11) < delta
						&& abs(expected.m12 - mat.m12) < delta
						&& abs(expected.m20 - mat.m20) < delta
						&& abs(expected.m21 - mat.m21) < delta
						&& abs(expected.m22 - mat.m22) < delta;
			}
			
			@Override
			public void describeTo(Description description) {
				description.appendValue(expected).appendText(" with delta:").appendValue(delta);
			}
		});
	}
	
	public static void assertEquals(Matrix4 expected, Matrix4 actual, float delta) {
		Assert.assertThat(actual, new BaseMatcher<>() {
			@Override
			public boolean matches(Object actual) {
				if (!(actual instanceof Matrix4))
					return false;
				
				Matrix4 mat = (Matrix4) actual;
				return abs(expected.m00 - mat.m00) < delta
						&& abs(expected.m01 - mat.m01) < delta
						&& abs(expected.m02 - mat.m02) < delta
						&& abs(expected.m03 - mat.m03) < delta
						&& abs(expected.m10 - mat.m10) < delta
						&& abs(expected.m11 - mat.m11) < delta
						&& abs(expected.m12 - mat.m12) < delta
						&& abs(expected.m13 - mat.m13) < delta
						&& abs(expected.m20 - mat.m20) < delta
						&& abs(expected.m21 - mat.m21) < delta
						&& abs(expected.m22 - mat.m22) < delta
						&& abs(expected.m23 - mat.m23) < delta
						&& abs(expected.m30 - mat.m30) < delta
						&& abs(expected.m31 - mat.m31) < delta
						&& abs(expected.m32 - mat.m32) < delta
						&& abs(expected.m33 - mat.m33) < delta;
			}
			
			@Override
			public void describeTo(Description description) {
				description.appendValue(expected).appendText(" with delta:").appendValue(delta);
			}
		});
	}
	
	public static void assertEquals(AxisAngle expected, AxisAngle actual, float delta) {
		AxisAngle expected1 = expected.angle > 0 ? new AxisAngle(expected.axis.inverse(), -expected.angle) : expected;
		Assert.assertThat(actual, new BaseMatcher<>() {
			@Override
			public boolean matches(Object actual) {
				if (!(actual instanceof AxisAngle))
					return false;
				
				AxisAngle axisAngle = (AxisAngle) actual;
				axisAngle = axisAngle.angle > 0 ? new AxisAngle(axisAngle.axis.inverse(), -axisAngle.angle) : axisAngle;
				return abs(expected1.angle - axisAngle.angle) < delta
						&& abs(expected1.axis.x - axisAngle.axis.x) < delta
						&& abs(expected1.axis.y - axisAngle.axis.y) < delta
						&& abs(expected1.axis.z - axisAngle.axis.z) < delta;
			}
			
			@Override
			public void describeTo(Description description) {
				description.appendValue(expected).appendText(" with delta:").appendValue(delta);
			}
		});
	}
	
	public static void assertEquals(TranslationBuilder expected, TranslationBuilder actual, float delta) {
		Assert.assertThat(actual, new BaseMatcher<>() {
			@Override
			public boolean matches(Object actual) {
				if (!(actual instanceof TranslationBuilder))
					return false;
				
				TranslationBuilder translation = (TranslationBuilder) actual;
				
				return abs(Quaternion.angle(expected.rotation, translation.rotation)) < delta
						&& abs(expected.offset.x - translation.offset.x) < delta
						&& abs(expected.offset.y - translation.offset.y) < delta
						&& abs(expected.offset.z - translation.offset.z) < delta;
			}
			
			@Override
			public void describeTo(Description description) {
				description.appendValue(expected).appendText(" with delta:").appendValue(delta);
			}
		});
	}
	
	public static void assertEquals(Translation expected, Translation actual, float delta) {
		Assert.assertThat(actual, new BaseMatcher<>() {
			@Override
			public boolean matches(Object actual) {
				if (!(actual instanceof Translation))
					return false;
				
				Translation translation = (Translation) actual;
				
				return abs(expected.matrix.m00 - translation.matrix.m00) < delta
						&& abs(expected.matrix.m01 - translation.matrix.m01) < delta
						&& abs(expected.matrix.m02 - translation.matrix.m02) < delta
						&& abs(expected.matrix.m10 - translation.matrix.m10) < delta
						&& abs(expected.matrix.m11 - translation.matrix.m11) < delta
						&& abs(expected.matrix.m12 - translation.matrix.m12) < delta
						&& abs(expected.matrix.m20 - translation.matrix.m20) < delta
						&& abs(expected.matrix.m21 - translation.matrix.m21) < delta
						&& abs(expected.matrix.m22 - translation.matrix.m22) < delta
						&& abs(expected.offset.x - translation.offset.x) < delta
						&& abs(expected.offset.y - translation.offset.y) < delta
						&& abs(expected.offset.z - translation.offset.z) < delta;
			}
			
			@Override
			public void describeTo(Description description) {
				description.appendValue(expected).appendText(" with delta:").appendValue(delta);
			}
		});
	}
}
