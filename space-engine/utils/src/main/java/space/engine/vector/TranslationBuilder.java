package space.engine.vector;

import org.jetbrains.annotations.NotNull;
import space.engine.vector.conversion.ToQuaternion;
import space.engine.vector.conversion.ToVector3;

public class TranslationBuilder {
	
	public @NotNull Quaternion rotation;
	public @NotNull Vector3 offset;
	
	public TranslationBuilder() {
		this(Quaternion.identity(), Vector3.zero());
	}
	
	protected TranslationBuilder(@NotNull Quaternion rotation, @NotNull Vector3 offset) {
		this.rotation = rotation;
		this.offset = offset;
	}
	
	//set
	public TranslationBuilder set(TranslationBuilder translation) {
		return set(translation.rotation, translation.offset);
	}
	
	public TranslationBuilder set(Quaternion rotation, Vector3 offset) {
		this.rotation = rotation;
		this.offset = offset;
		return this;
	}
	
	//append
	public TranslationBuilder appendRotate(ToQuaternion q) {
		return appendRotate(q.toQuaternion());
	}
	
	public TranslationBuilder appendRotate(Quaternion q) {
		rotation = rotation.multiply(q);
		return this;
	}
	
	public TranslationBuilder appendRotateInverse(ToQuaternion q) {
		return appendRotateInverse(q.toQuaternion());
	}
	
	public TranslationBuilder appendRotateInverse(Quaternion q) {
		rotation = rotation.multiplyInverse(q);
		return this;
	}
	
	public TranslationBuilder appendMove(ToVector3 vec) {
		return appendMove(vec.toVector3());
	}
	
	public TranslationBuilder appendMove(Vector3 vec) {
		offset = offset.add(vec.rotateInverse(rotation));
		return this;
	}
	
	public TranslationBuilder appendMoveInverse(ToVector3 vec) {
		return appendMoveInverse(vec.toVector3());
	}
	
	public TranslationBuilder appendMoveInverse(Vector3 vec) {
		offset = offset.add(vec.inverse().rotateInverse(rotation));
		return this;
	}
	
	public TranslationBuilder appendTranslation(TranslationBuilder translation) {
		appendMove(translation.offset);
		appendRotate(translation.rotation);
		return this;
	}
	
	public TranslationBuilder appendTranslationInverse(TranslationBuilder translation) {
		appendRotateInverse(translation.rotation);
		appendMoveInverse(translation.offset);
		return this;
	}
	
	//prepend
	public TranslationBuilder prependRotate(ToQuaternion q) {
		return prependRotate(q.toQuaternion());
	}
	
	public TranslationBuilder prependRotate(Quaternion q) {
		offset = offset.rotateInverse(q);
		rotation = q.multiply(rotation);
		return this;
	}
	
	public TranslationBuilder prependRotateInverse(ToQuaternion q) {
		return prependRotateInverse(q.toQuaternion());
	}
	
	public TranslationBuilder prependRotateInverse(Quaternion q) {
		//need to inverse it anyway so not worth it
		return prependRotate(q.inverse());
	}
	
	public TranslationBuilder prependMove(ToVector3 vec) {
		return prependMove(vec.toVector3());
	}
	
	public TranslationBuilder prependMove(Vector3 vec) {
		offset = offset.add(vec);
		return this;
	}
	
	public TranslationBuilder prependMoveInverse(ToVector3 vec) {
		return prependMoveInverse(vec.toVector3());
	}
	
	public TranslationBuilder prependMoveInverse(Vector3 vec) {
		offset = offset.sub(vec);
		return this;
	}
	
	public TranslationBuilder prependTranslation(TranslationBuilder translation) {
		prependRotate(translation.rotation);
		prependMove(translation.offset);
		return this;
	}
	
	public TranslationBuilder prependTranslationInverse(TranslationBuilder translation) {
		prependMoveInverse(translation.offset);
		prependRotateInverse(translation.rotation);
		return this;
	}
	
	//inverse
	public TranslationBuilder inverse() {
		return set(new TranslationBuilder().appendTranslationInverse(this));
	}
	
	//build
	public Translation build() {
		return new Translation(rotation, offset);
	}
	
	public Translation buildInverse() {
		return new TranslationBuilder().appendTranslationInverse(this).build();
	}
	
	@Override
	public String toString() {
		return "TranslationBuilder{" +
				"" + rotation +
				", " + offset +
				'}';
	}
}
