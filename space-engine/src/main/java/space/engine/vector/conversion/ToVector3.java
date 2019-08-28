package space.engine.vector.conversion;

import space.engine.vector.Vector3;

public interface ToVector3 {
	
	default Vector3 toVector3() {
		return toVector3(new Vector3());
	}
	
	Vector3 toVector3(Vector3 vec);
}
