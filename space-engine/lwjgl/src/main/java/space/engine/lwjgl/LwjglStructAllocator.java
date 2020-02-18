package space.engine.lwjgl;

import org.lwjgl.system.CustomBuffer;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;
import space.engine.buffer.AbstractBuffer.Storage;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.AllocatorFrame;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;

import static space.engine.Empties.EMPTY_OBJECT_ARRAY;

public class LwjglStructAllocator {
	
	//struct
	public static <T extends Struct> T mallocStruct(AllocatorFrame allocator, StructCreator<T> create, long sizeOf) {
		return mallocStruct(allocator, create, sizeOf, EMPTY_OBJECT_ARRAY);
	}
	
	public static <T extends Struct> T mallocStruct(Allocator allocator, StructCreator<T> create, long sizeOf, Object[] parents) {
		long address = allocator.malloc(sizeOf);
		T t = create.create(address);
		Attachment.setAttachment(t, new Storage(t, allocator, address, parents));
		return t;
	}
	
	public static <T extends Struct> T callocStruct(AllocatorFrame allocator, StructCreator<T> create, long sizeOf) {
		return callocStruct(allocator, create, sizeOf, EMPTY_OBJECT_ARRAY);
	}
	
	public static <T extends Struct> T callocStruct(Allocator allocator, StructCreator<T> create, long sizeOf, Object[] parents) {
		long address = allocator.calloc(sizeOf);
		T t = create.create(address);
		Attachment.setAttachment(t, new Storage(t, allocator, address, parents));
		return t;
	}
	
	public static <T extends Struct> T createStruct(Allocator allocator, StructCreator<T> create, long address, Object[] parents) {
		T t = create.create(address);
		Attachment.setAttachment(t, new Storage(t, allocator, address, parents));
		return t;
	}
	
	public static <T extends Struct> T wrapStruct(StructCreator<T> create, long address) {
		return wrapStruct(create, address, EMPTY_OBJECT_ARRAY);
	}
	
	public static <T extends Struct> T wrapStruct(StructCreator<T> create, long address, Object[] parents) {
		return createStruct(Allocator.noop(), create, address, parents);
	}
	
	@FunctionalInterface
	public interface StructCreator<T extends Struct> {
		
		T create(long address);
	}
	
	//buffer generator
	@SafeVarargs
	public static <T extends Struct, B extends StructBuffer<T, B>> B allocBuffer(AllocatorFrame allocator, BufferCreator<B> create, long sizeOf, Consumer<T>... generator) {
		return allocBuffer(allocator, create, sizeOf, EMPTY_OBJECT_ARRAY, generator);
	}
	
	@SafeVarargs
	public static <T extends Struct, B extends StructBuffer<T, B>> B allocBuffer(Allocator allocator, BufferCreator<B> create, long sizeOf, Object[] parents, Consumer<T>... generator) {
		B b = mallocBuffer(allocator, create, sizeOf, generator.length, parents);
		for (int i = 0; i < generator.length; i++)
			generator[i].accept(b.get(i));
		return b;
	}
	
	public static <T extends Struct, B extends StructBuffer<T, B>> B allocBuffer(AllocatorFrame allocator, BufferCreator<B> create, long sizeOf, Collection<Consumer<T>> generator) {
		return allocBuffer(allocator, create, sizeOf, EMPTY_OBJECT_ARRAY, generator);
	}
	
	public static <T extends Struct, B extends StructBuffer<T, B>> B allocBuffer(Allocator allocator, BufferCreator<B> create, long sizeOf, Object[] parents, Collection<Consumer<T>> generator) {
		B b = mallocBuffer(allocator, create, sizeOf, generator.size(), parents);
		Iterator<Consumer<T>> iter = generator.iterator();
		for (int i = 0; iter.hasNext(); i++)
			iter.next().accept(b.get(i));
		return b;
	}
	
	//buffer wrap
	public static <T extends Struct, B extends StructBuffer<T, B>> B wrapBuffer(BufferCreator<B> create, T struct) {
		B buffer = create.create(struct.address(), 1);
		Attachment.setAttachment(buffer, Attachment.getAttachment(struct));
		return buffer;
	}
	
	//buffer
	public static <T extends Struct, B extends StructBuffer<T, B>> B allocBuffer(AllocatorFrame allocator, BufferCreator<B> create, long sizeOf, T[] array) {
		return allocBuffer(allocator, create, sizeOf, array, EMPTY_OBJECT_ARRAY);
	}
	
	public static <T extends Struct, B extends StructBuffer<T, B>> B allocBuffer(Allocator allocator, BufferCreator<B> create, long sizeOf, T[] array, Object[] parents) {
		B b = mallocBuffer(allocator, create, sizeOf, array.length, parents);
		for (int i = 0; i < array.length; i++)
			b.put(i, array[i]);
		return b;
	}
	
	public static <B extends CustomBuffer<B>> B mallocBuffer(AllocatorFrame allocator, BufferCreator<B> create, long sizeOf, int length) {
		return mallocBuffer(allocator, create, sizeOf, length, EMPTY_OBJECT_ARRAY);
	}
	
	public static <B extends CustomBuffer<B>> B mallocBuffer(Allocator allocator, BufferCreator<B> create, long sizeOf, int length, Object[] parents) {
		long address = allocator.malloc(sizeOf * length);
		B b = create.create(address, length);
		Attachment.setAttachment(b, new Storage(b, allocator, address, parents));
		return b;
	}
	
	public static <B extends CustomBuffer<B>> B callocBuffer(AllocatorFrame allocator, BufferCreator<B> create, long sizeOf, int length) {
		return callocBuffer(allocator, create, sizeOf, length, EMPTY_OBJECT_ARRAY);
	}
	
	public static <B extends CustomBuffer<B>> B callocBuffer(Allocator allocator, BufferCreator<B> create, long sizeOf, int length, Object[] parents) {
		long address = allocator.calloc(sizeOf * length);
		B b = create.create(address, length);
		Attachment.setAttachment(b, new Storage(b, allocator, address, parents));
		return b;
	}
	
	public static <B extends CustomBuffer<B>> B createBuffer(Allocator allocator, BufferCreator<B> create, long address, int length, Object[] parents) {
		B b = create.create(address, length);
		Attachment.setAttachment(b, new Storage(b, allocator, address, parents));
		return b;
	}
	
	public static <B extends CustomBuffer<B>> B wrapBuffer(BufferCreator<B> create, long address, int length) {
		return wrapBuffer(create, address, length, EMPTY_OBJECT_ARRAY);
	}
	
	public static <B extends CustomBuffer<B>> B wrapBuffer(BufferCreator<B> create, long address, int length, Object[] parents) {
		return createBuffer(Allocator.noop(), create, address, length, parents);
	}
	
	@FunctionalInterface
	public interface BufferCreator<T extends CustomBuffer<T>> {
		
		T create(long address, int length);
	}
}
