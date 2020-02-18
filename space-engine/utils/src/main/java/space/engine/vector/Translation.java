package space.engine.vector;

import org.jetbrains.annotations.NotNull;

public class Translation {
	
	public final @NotNull Quaternion rotation;
	public final @NotNull Matrix3 matrix;
	public final @NotNull Vector3 offset;
	
	public Translation(@NotNull Quaternion rotation, @NotNull Vector3 offset) {
		this.rotation = rotation;
		this.matrix = this.rotation.toMatrix3();
		this.offset = offset;
	}
	
	public TranslationBuilder newBuilder() {
		return new TranslationBuilder(rotation, offset);
	}
	
	public float[] write4Aligned(float[] array, int offset) {
		this.matrix.write4Aligned(array, offset);
		this.offset.write4Aligned(array, offset + 12);
		return array;
	}
	
	public float[] write(float[] array, int offset) {
		this.matrix.write(array, offset);
		this.offset.write(array, offset + 9);
		return array;
	}
	
	@Override
	public String toString() {
		return "Translation{" +
				"" + rotation +
				", " + offset +
				'}';
	}
}
