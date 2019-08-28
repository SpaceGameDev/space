package space.engine.vector;

import org.jetbrains.annotations.NotNull;
import space.engine.vector.conversion.ToQuaternion;
import space.engine.vector.conversion.ToVector3;

public class TranslationBuilder {
	
	public @NotNull Quaternion rotation = new Quaternion();
	public @NotNull Vector3 offset = new Vector3();
	
	public TranslationBuilder() {
	}
	
	public TranslationBuilder(Quaternion rotation, Vector3 offset) {
		set(rotation, offset);
	}
	
	@SuppressWarnings("CopyConstructorMissesField")
	public TranslationBuilder(TranslationBuilder translation) {
		set(translation);
	}
	
	//set
	public TranslationBuilder set(TranslationBuilder translation) {
		return set(translation.rotation, translation.offset);
	}
	
	public TranslationBuilder set(Quaternion rotation, Vector3 offset) {
		this.rotation.set(rotation);
		this.offset.set(offset);
		return this;
	}
	
	public TranslationBuilder identity() {
		rotation.identity();
		offset.zero();
		return this;
	}
	
	//append
	public TranslationBuilder appendRotate(ToQuaternion q) {
		rotation.multiply(q.toQuaternion());
		return this;
	}
	
	public TranslationBuilder appendRotateInverse(ToQuaternion q) {
		rotation.multiplyInverse(q.toQuaternion());
		return this;
	}
	
	public TranslationBuilder appendMove(ToVector3 vec) {
		offset.add(new Vector3(vec).rotateInverse(rotation));
		return this;
	}
	
	public TranslationBuilder appendMoveInverse(ToVector3 vec) {
		offset.add(new Vector3(vec).inverse().rotateInverse(rotation));
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
		Quaternion q2 = q.toQuaternion(new Quaternion());
		offset.rotateInverse(q2);
		rotation.set(q2.multiply(rotation));
		return this;
	}
	
	public TranslationBuilder prependRotateInverse(ToQuaternion q) {
		//need to inverse it anyway so not worth it
		return prependRotate(q.toQuaternion(new Quaternion()).inverse());
	}
	
	public TranslationBuilder prependMove(ToVector3 vec) {
		offset.add(vec.toVector3());
		return this;
	}
	
	public TranslationBuilder prependMoveInverse(ToVector3 vec) {
		offset.add(vec.toVector3(new Vector3()).inverse());
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
