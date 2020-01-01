package space.engine.vector;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import space.engine.vector.conversion.ToQuaternion;

import static space.engine.vector.AxisAngle.toRadians;
import static space.engine.vector.VectorUtils.assertEquals;

public class TranslationTestSequence {
	
	public interface Operation {
	
	}
	
	public static class OperationMove implements Operation {
		
		private final @NotNull Vector3 vec;
		
		public OperationMove(float x, float y, float z) {
			this(new Vector3(x, y, z));
		}
		
		public OperationMove(@NotNull Vector3 vec) {
			this.vec = vec;
		}
	}
	
	public static class OperationRotate implements Operation {
		
		private final @NotNull Quaternion q;
		
		public OperationRotate(@NotNull ToQuaternion q) {
			this.q = q.toQuaternion();
		}
	}
	
	public static final Vector3[] TYPICAL_TEST_VECTORS = new Vector3[] {
			new Vector3(0, 0, 0),
			new Vector3(1, 0, 0),
			new Vector3(0, 1, 0),
			new Vector3(0, 0, 1),
			new Vector3(-5, 9, 2),
			new Vector3(-3, -7, 5),
			new Vector3(10, 15, -20),
	};
	
	@Test
	public void testSequence1() {
		testSequence(
				new Vector3[] {
						new Vector3(0, 0, 0),
						new Vector3(1, 0, 0),
						new Vector3(0, 1, 0),
						new Vector3(0, 0, 1),
				},
				new Vector3[] {
						new Vector3(5, 0, 5),
						new Vector3(5, 0, 6),
						new Vector3(5, 1, 5),
						new Vector3(4, 0, 5),
				},
				new Operation[] {
						new OperationMove(5, 0, 0),
						new OperationRotate(new AxisAngle(0, -1, 0, toRadians(90))),
						new OperationMove(5, 0, 0),
				}
		);
	}
	
	@Test
	public void testSequence2() {
		testSequence(
				new Vector3[] {
						new Vector3(0, 0, 0),
						new Vector3(1, 0, 0),
						new Vector3(0, 1, 0),
						new Vector3(0, 0, 1),
				},
				new Vector3[] {
						new Vector3(0, 0, 5),
						new Vector3(-1, 0, 5),
						new Vector3(0, 1, 5),
						new Vector3(0, 0, 4),
				},
				new Operation[] {
						new OperationMove(5, 0, 0),
						new OperationRotate(new AxisAngle(0, -1, 0, toRadians(90))),
						new OperationMove(5, 0, 0),
						new OperationRotate(new AxisAngle(0, -1, 0, toRadians(90))),
						new OperationMove(5, 0, 0),
				}
		);
	}
	
	@Test
	public void testSequenceIdentity1() {
		testSequence(
				TYPICAL_TEST_VECTORS,
				TYPICAL_TEST_VECTORS,
				new Operation[] {
						new OperationMove(5, 0, 0),
						new OperationRotate(new AxisAngle(0, 1, 0, toRadians(180))),
						new OperationMove(5, 0, 0),
						new OperationRotate(new AxisAngle(0, 1, 0, toRadians(180))),
				}
		);
	}
	
	@Test
	public void testSequenceIdentity2() {
		testSequence(
				TYPICAL_TEST_VECTORS,
				TYPICAL_TEST_VECTORS,
				new Operation[] {
						new OperationMove(5, 0, 0),
						new OperationRotate(new AxisAngle(0, 1, 0, toRadians(90))),
						new OperationMove(5, 0, 0),
						new OperationRotate(new AxisAngle(0, 1, 0, toRadians(-90))),
						new OperationMove(-5, 0, -5)
				}
		);
	}
	
	@Test
	public void testSequenceIdentity3() {
		testSequence(
				TYPICAL_TEST_VECTORS,
				TYPICAL_TEST_VECTORS,
				new Operation[] {
						new OperationMove(5, 0, 0),
						new OperationRotate(new AxisAngle(0, 1, 0, toRadians(90))),
						new OperationMove(5, 0, 0),
						new OperationRotate(new AxisAngle(0, 1, 0, toRadians(90))),
						new OperationMove(5, 0, 0),
						new OperationRotate(new AxisAngle(0, 1, 0, toRadians(90))),
						new OperationMove(5, 0, 0),
						new OperationRotate(new AxisAngle(0, 1, 0, toRadians(90))),
				}
		);
	}
	
	public void testSequence(Vector3[] starts, Vector3[] ends, Operation[] sequence) {
		testSequenceAppend(starts, ends, sequence);
		testSequenceAppendInverse(starts, ends, sequence);
		testSequencePrepend(starts, ends, sequence);
		testSequencePrependInverse(starts, ends, sequence);
	}
	
	public void testSequenceAppend(@NotNull Vector3[] starts, @NotNull Vector3[] ends, @NotNull Operation[] sequence) {
		TranslationBuilder b = new TranslationBuilder();
		for (int i = 0; i < sequence.length; i++) {
			Operation operation = sequence[i];
			if (operation instanceof OperationMove) {
				Vector3 offset = ((OperationMove) operation).vec;
				b.appendMove(offset);
				for (Vector3 start : starts)
					assertEquals(applySequence(sequence, 0, i + 1, start), start.translate(b.build()), 0.01f);
			} else if (operation instanceof OperationRotate) {
				Quaternion offset = ((OperationRotate) operation).q;
				b.appendRotate(offset);
				for (Vector3 start : starts)
					assertEquals(applySequence(sequence, 0, i + 1, start), start.translate(b.build()), 0.01f);
			} else {
				throw new RuntimeException("Invalid Operation type");
			}
		}
		
		Translation build = b.build();
		Translation buildInverse = b.buildInverse();
		for (int j = 0; j < starts.length; j++) {
			Vector3 start = starts[j];
			Vector3 end = ends[j];
			assertEquals(end, applySequence(sequence, 0, sequence.length, start), 0.01f);
			assertEquals(end, start.translate(build), 0.01f);
			assertEquals(start, end.translateInverse(build), 0.01f);
			assertEquals(start, end.translate(buildInverse), 0.01f);
			assertEquals(end, start.translateInverse(buildInverse), 0.01f);
		}
	}
	
	public void testSequenceAppendInverse(@NotNull Vector3[] starts, @NotNull Vector3[] ends, @NotNull Operation[] sequence) {
		TranslationBuilder b = new TranslationBuilder();
		for (int i = 0; i < sequence.length; i++) {
			Operation operation = sequence[i];
			if (operation instanceof OperationMove) {
				Vector3 offset = ((OperationMove) operation).vec;
				b.appendMoveInverse(offset.inverse());
				for (Vector3 start : starts)
					assertEquals(applySequence(sequence, 0, i + 1, start), start.translate(b.build()), 0.01f);
			} else if (operation instanceof OperationRotate) {
				Quaternion offset = ((OperationRotate) operation).q;
				b.appendRotateInverse(offset.inverse());
				for (Vector3 start : starts)
					assertEquals(applySequence(sequence, 0, i + 1, start), start.translate(b.build()), 0.01f);
			} else {
				throw new RuntimeException("Invalid Operation type");
			}
		}
		
		Translation build = b.build();
		Translation buildInverse = b.buildInverse();
		for (int j = 0; j < starts.length; j++) {
			Vector3 start = starts[j];
			Vector3 end = ends[j];
			assertEquals(end, applySequence(sequence, 0, sequence.length, start), 0.01f);
			assertEquals(end, start.translate(build), 0.01f);
			assertEquals(start, end.translateInverse(build), 0.01f);
			assertEquals(start, end.translate(buildInverse), 0.01f);
			assertEquals(end, start.translateInverse(buildInverse), 0.01f);
		}
	}
	
	public void testSequencePrepend(@NotNull Vector3[] starts, @NotNull Vector3[] ends, @NotNull Operation[] sequence) {
		TranslationBuilder b = new TranslationBuilder();
		for (int i = sequence.length - 1; i >= 0; i--) {
			Operation operation = sequence[i];
			if (operation instanceof OperationMove) {
				Vector3 offset = ((OperationMove) operation).vec;
				b.prependMove(offset);
				for (Vector3 end : ends)
					assertEquals(applySequence(sequence, i, sequence.length, end), end.translate(b.build()), 0.01f);
			} else if (operation instanceof OperationRotate) {
				Quaternion offset = ((OperationRotate) operation).q;
				b.prependRotate(offset);
				for (Vector3 end : ends)
					assertEquals(applySequence(sequence, i, sequence.length, end), end.translate(b.build()), 0.01f);
			} else {
				throw new RuntimeException("Invalid Operation type");
			}
		}
		
		Translation build = b.build();
		Translation buildInverse = b.buildInverse();
		for (int j = 0; j < ends.length; j++) {
			Vector3 start = starts[j];
			Vector3 end = ends[j];
			assertEquals(end, applySequence(sequence, 0, sequence.length, start), 0.01f);
			assertEquals(end, start.translate(build), 0.01f);
			assertEquals(start, end.translateInverse(build), 0.01f);
			assertEquals(start, end.translate(buildInverse), 0.01f);
			assertEquals(end, start.translateInverse(buildInverse), 0.01f);
		}
	}
	
	public void testSequencePrependInverse(@NotNull Vector3[] starts, @NotNull Vector3[] ends, @NotNull Operation[] sequence) {
		TranslationBuilder b = new TranslationBuilder();
		for (int i = sequence.length - 1; i >= 0; i--) {
			Operation operation = sequence[i];
			if (operation instanceof OperationMove) {
				Vector3 offset = ((OperationMove) operation).vec;
				b.prependMoveInverse(offset.inverse());
				for (Vector3 end : ends)
					assertEquals(applySequence(sequence, i, sequence.length, end), end.translate(b.build()), 0.01f);
			} else if (operation instanceof OperationRotate) {
				Quaternion offset = ((OperationRotate) operation).q;
				b.prependRotateInverse(offset.inverse());
				for (Vector3 end : ends)
					assertEquals(applySequence(sequence, i, sequence.length, end), end.translate(b.build()), 0.01f);
			} else {
				throw new RuntimeException("Invalid Operation type");
			}
		}
		
		Translation build = b.build();
		Translation buildInverse = b.buildInverse();
		for (int j = 0; j < ends.length; j++) {
			Vector3 start = starts[j];
			Vector3 end = ends[j];
			assertEquals(end, applySequence(sequence, 0, sequence.length, start), 0.01f);
			assertEquals(end, start.translate(build), 0.01f);
			assertEquals(start, end.translateInverse(build), 0.01f);
			assertEquals(start, end.translate(buildInverse), 0.01f);
			assertEquals(end, start.translateInverse(buildInverse), 0.01f);
		}
	}
	
	@Contract(value = "_, _, _, _ -> param4", mutates = "param4")
	public static Vector3 applySequence(@NotNull Operation[] sequence, int from, int to, Vector3 vec) {
		for (int i = from; i < to; i++) {
			Operation operation = sequence[i];
			if (operation instanceof OperationMove) {
				vec = vec.add(((OperationMove) operation).vec);
			} else if (operation instanceof OperationRotate) {
				vec = vec.rotate(((OperationRotate) operation).q);
			} else {
				throw new RuntimeException("Invalid Operation type");
			}
		}
		return vec;
	}
}
