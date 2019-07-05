package space.engine.lwjgl;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.CustomBuffer;
import org.lwjgl.system.Struct;
import space.engine.event.EventEntry;
import space.engine.freeableStorage.Freeable;
import space.engine.unsafe.UnsafeInstance;
import sun.misc.Unsafe;

import java.util.function.Function;

import static space.engine.freeableStorage.Freeable.GET_SUBLIST_EVENT;

class Attachment {
	
	private static final Unsafe UNSAFE = UnsafeInstance.getUnsafe();
	
	private static final long OFFSET_STRUCT_CONTAINER;
	private static final long OFFSET_CUSTOM_BUFFER_CONTAINER;
	
	public static final EventEntry<Function<Object, Freeable>> FUNCTION_LWJGL_STRUCT;
	public static final EventEntry<Function<Object, Freeable>> FUNCTION_LWJGL_CUSTOM_BUFFER;
	
	static {
		try {
			OFFSET_STRUCT_CONTAINER = UNSAFE.objectFieldOffset(Struct.class.getDeclaredField("container"));
			OFFSET_CUSTOM_BUFFER_CONTAINER = UNSAFE.objectFieldOffset(CustomBuffer.class.getDeclaredField("container"));
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
		
		GET_SUBLIST_EVENT.addHook(FUNCTION_LWJGL_STRUCT = new EventEntry<>(o -> {
			if (o instanceof Struct) {
				Object container = UNSAFE.getObject(o, OFFSET_STRUCT_CONTAINER);
				if (container instanceof Freeable)
					return (Freeable) container;
			}
			return null;
		}));
		GET_SUBLIST_EVENT.addHook(FUNCTION_LWJGL_CUSTOM_BUFFER = new EventEntry<>(o -> {
			if (o instanceof CustomBuffer<?>) {
				Object container = UNSAFE.getObject(o, OFFSET_CUSTOM_BUFFER_CONTAINER);
				if (container instanceof Freeable)
					return (Freeable) container;
			}
			return null;
		}, FUNCTION_LWJGL_STRUCT));
	}
	
	public static void setAttachment(@NotNull Struct struct, Object att) {
		UNSAFE.putObject(struct, OFFSET_STRUCT_CONTAINER, att);
	}
	
	public static void setAttachment(@NotNull CustomBuffer<?> struct, Object att) {
		UNSAFE.putObject(struct, OFFSET_CUSTOM_BUFFER_CONTAINER, att);
	}
	
	public static Object getAttachment(@NotNull Struct struct) {
		return UNSAFE.getObject(struct, OFFSET_STRUCT_CONTAINER);
	}
	
	public static Object getAttachment(@NotNull CustomBuffer<?> struct) {
		return UNSAFE.getObject(struct, OFFSET_CUSTOM_BUFFER_CONTAINER);
	}
}
