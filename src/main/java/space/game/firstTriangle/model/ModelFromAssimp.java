package space.game.firstTriangle.model;

import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIVector3D;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.array.ArrayBufferByte;
import space.engine.vector.Matrix4f;
import space.engine.vector.Vector3f;

import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.lwjgl.assimp.Assimp.*;
import static space.engine.lwjgl.PointerBufferWrapper.streamPointerBuffer;

public class ModelFromAssimp {
	
	public static float[] loadModel(InputStream stream, Matrix4f scale) throws IOException {
		byte[] bytes = stream.readAllBytes();
		
		try (AllocatorFrame frame = Allocator.frame()) {
			ArrayBufferByte content = ArrayBufferByte.alloc(Allocator.heap(), bytes, new Object[] {frame});
			
			AIScene scene = AIScene.createSafe(naiImportFileFromMemory(content.address(), (int) content.length(), 0, 0));
			if (scene == null)
				throw new IOException("Assimp couldn't load scene: " + aiGetErrorString());
			
			List<AIMesh> meshes = streamPointerBuffer(Objects.requireNonNull(scene.mMeshes()))
					.mapToObj(AIMesh::create)
					.collect(Collectors.toUnmodifiableList());
			
			float[] ret = new float[meshes.stream().mapToInt(AIMesh::mNumFaces).sum() * 3 * 9];
			int index = 0;
			
			for (AIMesh mesh : meshes) {
				AIVector3D.Buffer vertices = mesh.mVertices();
				AIVector3D.Buffer normals = mesh.mVertices();
				AIFace.Buffer faces = mesh.mFaces();
				for (AIFace face : faces) {
					IntBuffer indices = face.mIndices();
					for (int i : new int[] {0, 2, 1}) {
						AIVector3D vertexAssimp = vertices.get(indices.get(i));
						AIVector3D normalAssimp = normals.get(indices.get(i));
						Vector3f vertex = new Vector3f(vertexAssimp.x(), vertexAssimp.y(), vertexAssimp.z()).rotate(scale);
						ret[index++] = vertex.x;
						ret[index++] = vertex.y;
						ret[index++] = vertex.z;
						ret[index++] = normalAssimp.x();
						ret[index++] = normalAssimp.y();
						ret[index++] = normalAssimp.z();
						ret[index++] = 1.0f;
						ret[index++] = 1.0f;
						ret[index++] = 1.0f;
					}
				}
			}
			return ret;
		}
	}
}
