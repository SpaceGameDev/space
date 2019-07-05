package space.engine.buffer;

import space.engine.buffer.AllocatorStack.AllocatorFrame;
import space.engine.buffer.array.ArrayBufferByte;
import space.engine.buffer.array.ArrayBufferPointer;

import static java.nio.charset.StandardCharsets.*;
import static space.engine.Empties.EMPTY_OBJECT_ARRAY;

public class StringConverter {
	
	public static final byte NULL_CHARACTER = 0;
	
	//string[] to pointerbuffer
	public static ArrayBufferPointer stringArrayToUTF8(AllocatorFrame allocator, String[] strings, boolean nullTerm) {
		return stringArrayToUTF8(allocator, strings, nullTerm, EMPTY_OBJECT_ARRAY);
	}
	
	public static ArrayBufferPointer stringArrayToUTF8(Allocator allocator, String[] strings, boolean nullTerm, Object[] parents) {
		ArrayBufferPointer ptrBuffer = ArrayBufferPointer.malloc(allocator, strings.length, parents);
		ArrayBufferByte[] array = new ArrayBufferByte[strings.length];
		for (int i = 0; i < strings.length; i++)
			array[i] = stringToUTF8(allocator, strings[i], nullTerm, new Object[] {ptrBuffer});
		ptrBuffer.copyFrom(array);
		Buffer.setContainer(ptrBuffer, array);
		return ptrBuffer;
	}
	
	public static ArrayBufferPointer stringArrayToUTF16(AllocatorFrame allocator, String[] strings, boolean nullTerm) {
		return stringArrayToUTF8(allocator, strings, nullTerm, EMPTY_OBJECT_ARRAY);
	}
	
	public static ArrayBufferPointer stringArrayToUTF16(Allocator allocator, String[] strings, boolean nullTerm, Object[] parents) {
		ArrayBufferPointer ptrBuffer = ArrayBufferPointer.malloc(allocator, strings.length, parents);
		ArrayBufferByte[] array = new ArrayBufferByte[strings.length];
		for (int i = 0; i < strings.length; i++)
			array[i] = stringToUTF16(allocator, strings[i], nullTerm, new Object[] {ptrBuffer});
		ptrBuffer.copyFrom(array);
		Buffer.setContainer(ptrBuffer, array);
		return ptrBuffer;
	}
	
	public static ArrayBufferPointer stringArrayToASCII(AllocatorFrame allocator, String[] strings, boolean nullTerm) {
		return stringArrayToUTF8(allocator, strings, nullTerm, EMPTY_OBJECT_ARRAY);
	}
	
	public static ArrayBufferPointer stringArrayToASCII(Allocator allocator, String[] strings, boolean nullTerm, Object[] parents) {
		ArrayBufferPointer ptrBuffer = ArrayBufferPointer.malloc(allocator, strings.length, parents);
		ArrayBufferByte[] array = new ArrayBufferByte[strings.length];
		for (int i = 0; i < strings.length; i++)
			array[i] = stringToASCII(allocator, strings[i], nullTerm, new Object[] {ptrBuffer});
		ptrBuffer.copyFrom(array);
		Buffer.setContainer(ptrBuffer, array);
		return ptrBuffer;
	}
	
	//string to buffer
	public static ArrayBufferByte stringToUTF8(AllocatorFrame allocator, String str, boolean nullTerm) {
		return stringToUTF8(allocator, str, nullTerm, EMPTY_OBJECT_ARRAY);
	}
	
	public static ArrayBufferByte stringToUTF8(Allocator allocator, String str, boolean nullTerm, Object[] parents) {
		byte[] bytes = str.getBytes(UTF_8);
		ArrayBufferByte buffer = ArrayBufferByte.malloc(allocator, bytes.length + (nullTerm ? 1 : 0), parents);
		buffer.copyFrom(bytes);
		if (nullTerm)
			buffer.putByte(bytes.length, (byte) 0);
		return buffer;
	}
	
	public static ArrayBufferByte stringToUTF16(AllocatorFrame allocator, String str, boolean nullTerm) {
		return stringToUTF16(allocator, str, nullTerm, EMPTY_OBJECT_ARRAY);
	}
	
	public static ArrayBufferByte stringToUTF16(Allocator allocator, String str, boolean nullTerm, Object[] parents) {
		byte[] bytes = str.getBytes(UTF_16);
		ArrayBufferByte buffer = ArrayBufferByte.malloc(allocator, bytes.length + (nullTerm ? 2 : 0), parents);
		buffer.copyFrom(bytes);
		if (nullTerm) {
			buffer.putByte(bytes.length, (byte) 0);
			buffer.putByte(bytes.length + 1, (byte) 0);
		}
		return buffer;
	}
	
	public static ArrayBufferByte stringToASCII(AllocatorFrame allocator, String str, boolean nullTerm) {
		return stringToASCII(allocator, str, nullTerm, EMPTY_OBJECT_ARRAY);
	}
	
	public static ArrayBufferByte stringToASCII(Allocator allocator, String str, boolean nullTerm, Object[] parents) {
		byte[] bytes = str.getBytes(US_ASCII);
		ArrayBufferByte buffer = ArrayBufferByte.malloc(allocator, bytes.length + (nullTerm ? 1 : 0), parents);
		buffer.copyFrom(bytes);
		if (nullTerm)
			buffer.putByte(bytes.length, (byte) 0);
		return buffer;
	}
	
	//address to string
	public static String UTF8ToString(long address) {
		return UTF8ToString(ArrayBufferByte.wrap(address, Long.MAX_VALUE, new Object[0]));
	}
	
	public static String UTF16ToString(long address) {
		return UTF16ToString(ArrayBufferByte.wrap(address, Long.MAX_VALUE, new Object[0]));
	}
	
	public static String ASCIIToString(long address) {
		return ASCIIToString(ArrayBufferByte.wrap(address, Long.MAX_VALUE, new Object[0]));
	}
	
	//buffer to string
	public static String UTF8ToString(ArrayBufferByte buffer) {
		return UTF8ToString(buffer, findNullCharacter(buffer, 1));
	}
	
	public static String UTF16ToString(ArrayBufferByte buffer) {
		return UTF16ToString(buffer, findNullCharacter(buffer, 2));
	}
	
	public static String ASCIIToString(ArrayBufferByte buffer) {
		return ASCIIToString(buffer, findNullCharacter(buffer, 1));
	}
	
	//buffer with length to string
	public static String UTF8ToString(ArrayBufferByte buffer, int length) {
		byte[] bytes = new byte[length];
		buffer.copyInto(0, bytes, 0, length);
		return new String(bytes, 0, length, UTF_8);
	}
	
	public static String UTF16ToString(ArrayBufferByte buffer, int length) {
		byte[] bytes = new byte[length];
		buffer.copyInto(0, bytes, 0, length);
		return new String(bytes, 0, length, UTF_16);
	}
	
	public static String ASCIIToString(ArrayBufferByte buffer, int length) {
		byte[] bytes = new byte[length];
		buffer.copyInto(0, bytes, 0, length);
		return new String(bytes, 0, length, US_ASCII);
	}
	
	//other
	private static int findNullCharacter(ArrayBufferByte buffer, int step) {
		for (int i = 0; i < buffer.sizeOf(); i += step)
			if (buffer.getByte(i) == NULL_CHARACTER)
				return i;
		throw new StringBufferNotNullTerminatedException(buffer);
	}
	
	public static class StringBufferNotNullTerminatedException extends RuntimeException {
		
		public StringBufferNotNullTerminatedException(Buffer buffer) {
			super("Buffer not terminated with null character: \n" + buffer.dump().toString());
		}
	}
}
