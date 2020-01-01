package space.engine.vector;

import org.junit.Test;

import static space.engine.vector.AxisAngle.toRadians;
import static space.engine.vector.VectorUtils.assertEquals;

public class TranslationTest {
	
	@Test
	public void testAppendMoveOnly() {
		TranslationBuilder b = new TranslationBuilder()
				.appendMove(new Vector3(1, 2, 3))
				.appendMove(new Vector3(4, 5, 6));
		Translation translation = b.build();
		assertEquals(new Vector3(5, 7, 9), new Vector3(0, 0, 0).translate(translation), 0.01f);
		assertEquals(new Vector3(6, 9, 12), new Vector3(1, 2, 3).translate(translation), 0.01f);
	}
	
	/**
	 * = testAppendMoveOnly with append -> prepend
	 */
	@Test
	public void testPrependMoveOnly() {
		TranslationBuilder b = new TranslationBuilder()
				.prependMove(new Vector3(1, 2, 3))
				.prependMove(new Vector3(4, 5, 6));
		Translation translation = b.build();
		assertEquals(new Vector3(5, 7, 9), new Vector3(0, 0, 0).translate(translation), 0.01f);
		assertEquals(new Vector3(6, 9, 12), new Vector3(1, 2, 3).translate(translation), 0.01f);
	}
	
	@Test
	public void testAppendRotationOnly() {
		TranslationBuilder b = new TranslationBuilder()
				.appendRotate(new AxisAngle(0, 1, 0, toRadians(90)));
		Translation translation = b.build();
		assertEquals(new Vector3(0, 0, 0), new Vector3(0, 0, 0).translate(translation), 0.01f);
		assertEquals(new Vector3(3, 2, -1), new Vector3(1, 2, 3).translate(translation), 0.01f);
	}
	
	/**
	 * = testAppendRotationOnly with append -> prepend
	 */
	@Test
	public void testPrependRotationOnly() {
		TranslationBuilder b = new TranslationBuilder()
				.prependRotate(new AxisAngle(0, 1, 0, toRadians(90)));
		Translation translation = b.build();
		assertEquals(new Vector3(0, 0, 0), new Vector3(0, 0, 0).translate(translation), 0.01f);
		assertEquals(new Vector3(3, 2, -1), new Vector3(1, 2, 3).translate(translation), 0.01f);
	}
	
	@Test
	public void testAppendRotationInverse() {
		AxisAngle rotate90 = new AxisAngle(0, 1, 0, toRadians(90));
		AxisAngle rotateN90 = new AxisAngle(0, 1, 0, toRadians(-90));
		
		assertEquals(new TranslationBuilder().appendRotate(rotate90), new TranslationBuilder().appendRotateInverse(rotateN90), 0.01f);
		assertEquals(new TranslationBuilder().appendRotate(rotateN90), new TranslationBuilder().appendRotateInverse(rotate90), 0.01f);
		assertEquals(new TranslationBuilder(), new TranslationBuilder().appendRotate(rotate90).appendRotate(rotateN90), 0.01f);
		assertEquals(new TranslationBuilder(), new TranslationBuilder().appendRotate(rotate90).appendRotateInverse(rotate90), 0.01f);
		
		assertEquals(new TranslationBuilder().appendRotate(rotate90).build(), new TranslationBuilder().appendRotateInverse(rotateN90).build(), 0.01f);
		assertEquals(new TranslationBuilder().appendRotate(rotate90).build(), new TranslationBuilder().appendRotateInverse(rotate90).buildInverse(), 0.01f);
		assertEquals(new TranslationBuilder().appendRotate(rotate90).build(), new TranslationBuilder().appendRotate(rotateN90).buildInverse(), 0.01f);
	}
	
	/**
	 * = testAppendRotationInverse with append -> prepend
	 */
	@Test
	public void testPrependRotationInverse() {
		AxisAngle rotate90 = new AxisAngle(0, 1, 0, toRadians(90));
		AxisAngle rotateN90 = new AxisAngle(0, 1, 0, toRadians(-90));
		
		assertEquals(new TranslationBuilder().prependRotate(rotate90), new TranslationBuilder().prependRotateInverse(rotateN90), 0.01f);
		assertEquals(new TranslationBuilder().prependRotate(rotateN90), new TranslationBuilder().prependRotateInverse(rotate90), 0.01f);
		assertEquals(new TranslationBuilder(), new TranslationBuilder().prependRotate(rotate90).prependRotate(rotateN90), 0.01f);
		assertEquals(new TranslationBuilder(), new TranslationBuilder().prependRotate(rotate90).prependRotateInverse(rotate90), 0.01f);
		
		assertEquals(new TranslationBuilder().prependRotate(rotate90).build(), new TranslationBuilder().prependRotateInverse(rotateN90).build(), 0.01f);
		assertEquals(new TranslationBuilder().prependRotate(rotate90).build(), new TranslationBuilder().prependRotateInverse(rotate90).buildInverse(), 0.01f);
		assertEquals(new TranslationBuilder().prependRotate(rotate90).build(), new TranslationBuilder().prependRotate(rotateN90).buildInverse(), 0.01f);
	}
}
