package space.engine.lwjgl;

import org.lwjgl.PointerBuffer;
import space.engine.buffer.Buffer;
import space.engine.buffer.array.ArrayBufferPointer;
import space.engine.buffer.pointer.PointerBufferPointer;
import space.engine.freeableStorage.Freeable;

import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static space.engine.primitive.Primitives.POINTER;

public class PointerBufferWrapper {
	
	public static PointerBuffer wrapPointer(ArrayBufferPointer buffer) {
		return wrapPointer(buffer, 0, buffer.length());
	}
	
	public static PointerBuffer wrapPointer(PointerBufferPointer buffer) {
		return wrapPointer(buffer, 0, 1);
	}
	
	public static PointerBuffer wrapPointer(Buffer buffer, long length) {
		return wrapPointer(buffer, 0, length);
	}
	
	public static PointerBuffer wrapPointer(Buffer buffer, long offset, long length) {
		Buffer.checkFromIndexSize(offset, length * POINTER.bytes, buffer.sizeOf());
		int lengthInt = (int) length;
		if (lengthInt != length)
			throw new RuntimeException("Buffer sizeOf " + length + " exceeds 32bit sizeOf limit of DirectBuffer!");
		
		PointerBuffer ret = PointerBuffer.create(buffer.address() + offset, lengthInt);
		Attachment.setAttachment(ret, Freeable.createDummy(new Object[] {buffer}));
		return ret;
	}
	
	public static LongStream streamPointerBuffer(PointerBuffer pb) {
		return IntStream.range(0, pb.capacity()).mapToLong(pb::get);
	}
}
