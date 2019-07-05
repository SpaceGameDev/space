package space.engine.vector;

public class Translation {
	
	public Matrix3f rotation = new Matrix3f();
	public Vector3f offset = new Vector3f();
	
	public Translation() {
	}
	
	public Translation(Matrix3f rotation, Vector3f offset) {
		set(rotation, offset);
	}
	
	@SuppressWarnings("CopyConstructorMissesField")
	public Translation(Translation translation) {
		set(translation);
	}
	
	//set
	public Translation set(Translation translation) {
		return set(translation.rotation, translation.offset);
	}
	
	public Translation set(Matrix3f rotation, Vector3f offset) {
		this.rotation.set(rotation);
		this.offset.set(offset);
		return this;
	}
	
	public Translation zero() {
		rotation.identity();
		offset.zero();
		return this;
	}
	
	//rotate move apply
	public Translation rotateForwards(Matrix3f mat) {
		rotation.multiply(mat);
		return this;
	}
	
	public Translation rotateBackwards(Matrix3f mat) {
		rotation.multiply(new Matrix3f(mat).inversePure());
		return this;
	}
	
	public Translation moveForwards(Vector3f vec) {
		offset.add(new Vector3f(vec).rotate(rotation));
		return this;
	}
	
	public Translation moveBackwards(Vector3f vec) {
		offset.add(new Vector3f(vec).inverse().rotate(rotation));
		return this;
	}
	
	public Translation applyForwards(Translation translation) {
		moveForwards(translation.offset);
		rotateForwards(translation.rotation);
		return this;
	}
	
	public Translation applyBackwards(Translation translation) {
		rotateBackwards(translation.rotation);
		moveBackwards(translation.offset);
		return this;
	}
	
	//inverse
	public Translation inverse() {
		return set(new Translation().applyBackwards(this));
	}
	
	//toMatrix
	public Matrix4f toMatrix4f(Matrix4f mat) {
		return mat.set(
				rotation.m00, rotation.m01, rotation.m02, offset.x,
				rotation.m10, rotation.m11, rotation.m12, offset.y,
				rotation.m20, rotation.m21, rotation.m22, offset.z,
				0, 0, 0, 1
		);
	}
	
	//applyTranslation
	public Vector3f applyTranslationForward(Vector3f vec) {
		return vec.rotate(rotation).add(offset);
	}
	
	public Vector3f applyTranslationBackward(Vector3f vec) {
		return vec.sub(offset).rotateInversePure(rotation);
	}
}
