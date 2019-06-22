package space.game.firstTriangle;

import space.engine.vector.AxisAndAnglef;
import space.engine.vector.Matrix4f;
import space.engine.vector.Quaternionf;
import space.engine.vector.Vector3f;

public class Entity {
	
	public Vector3f position;
	public Quaternionf rotation;
	
	public Entity() {
		this(new Vector3f(), new Quaternionf());
	}
	
	public Entity(Vector3f position, Quaternionf rotation) {
		this.position = position;
		this.rotation = rotation;
	}
	
	//translate
	public void translateAbsolute(Vector3f vector) {
		position.add(vector);
	}
	
	public void translateRelative(Vector3f vector) {
		position.add(new Vector3f(vector).rotate(rotation));
	}
	
	//rotate
	public void rotateAbsolute(AxisAndAnglef axisAndAngle) {
		rotateAbsolute(axisAndAngle.toQuaternion(new Quaternionf()));
	}
	
	public void rotateAbsolute(Quaternionf quaternion) {
		rotation.multiply(quaternion);
	}
	
	public void rotateRelative(AxisAndAnglef axisAndAngle) {
		rotateRelative(axisAndAngle.toQuaternion(new Quaternionf()));
	}
	
	public void rotateRelative(Quaternionf quaternion) {
		rotation.multiply(new Quaternionf(quaternion));
	}
	
	//toMatrix4
	public Matrix4f toMatrix4(Matrix4f mat) {
		rotation.toMatrix4(mat);
		mat.modelOffset(position);
		return mat;
	}
	
	public Matrix4f toMatrix4Inverse(Matrix4f mat) {
		mat.identity();
		mat.multiply(rotation.toMatrix4Inverse(new Matrix4f()));
		mat.modelOffset(new Vector3f(position).inverse());
		return mat;
	}
	
	public void bla() {
	
	}
}
