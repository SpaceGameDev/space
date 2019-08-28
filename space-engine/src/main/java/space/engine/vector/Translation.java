package space.engine.vector;

import org.jetbrains.annotations.NotNull;

public class Translation {
	
	public final @NotNull Quaternion rotation;
	public final @NotNull Matrix3 matrix;
	public final @NotNull Vector3 offset;
	
	public Translation(Quaternion rotation, Vector3 offset) {
		this.rotation = new Quaternion(rotation);
		this.matrix = this.rotation.toMatrix3();
		this.offset = new Vector3(offset);
	}
	
	public TranslationBuilder newBuilder() {
		return new TranslationBuilder(rotation, offset);
	}
	
	@Override
	public String toString() {
		return "Translation{" +
				"" + rotation +
				", " + offset +
				'}';
	}
}
