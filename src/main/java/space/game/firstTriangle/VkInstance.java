package space.game.firstTriangle;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.JNI;
import org.lwjgl.vulkan.VKCapabilitiesInstance;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackEXTI;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import space.engine.buffer.AllocatorStack.Frame;
import space.engine.buffer.array.ArrayBufferPointer;
import space.engine.buffer.pointer.PointerBufferInt;
import space.engine.buffer.pointer.PointerBufferPointer;
import space.engine.delegate.collection.ObservableCollection;
import space.engine.freeableStorage.Freeable;
import space.engine.freeableStorage.Freeable.FreeableWrapper;
import space.engine.freeableStorage.FreeableStorage;
import space.engine.logger.LogLevel;
import space.engine.logger.Logger;
import space.engine.lwjgl.LwjglStructAllocator;
import space.engine.sync.barrier.Barrier;

import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static org.lwjgl.system.JNI.callPJPV;
import static org.lwjgl.vulkan.EXTDebugUtils.*;
import static org.lwjgl.vulkan.VK10.*;
import static space.engine.buffer.Allocator.*;
import static space.engine.lwjgl.LwjglStructAllocator.mallocStruct;
import static space.game.firstTriangle.VkException.assertVk;

public class VkInstance extends org.lwjgl.vulkan.VkInstance implements FreeableWrapper {
	
	//alloc
	public static VkInstance alloc(@NotNull VkInstanceCreateInfo info, @NotNull Logger logger, boolean initDebugCallback, @NotNull Object[] parents) {
		try (Frame frame = allocatorStack().frame()) {
			PointerBufferPointer instance = PointerBufferPointer.malloc(frame);
			assertVk(nvkCreateInstance(info.address(), 0, instance.address()));
			return create(instance.getPointer(), info, logger, initDebugCallback, parents);
		}
	}
	
	//create
	public static VkInstance create(long handle, @NotNull VkInstanceCreateInfo ci, @NotNull Logger logger, boolean initDebugCallback, @NotNull Object[] parents) {
		return new VkInstance(handle, ci, logger, initDebugCallback, Storage::new, parents);
	}
	
	public static VkInstance wrap(long handle, @NotNull VkInstanceCreateInfo ci, @NotNull Logger logger, boolean initDebugCallback, @NotNull Object[] parents) {
		return new VkInstance(handle, ci, logger, initDebugCallback, Freeable::createDummy, parents);
	}
	
	//object
	public VkInstance(long handle, @NotNull VkInstanceCreateInfo ci, @NotNull Logger logger, boolean initDebugCallback, @NotNull BiFunction<VkInstance, Object[], Freeable> storageCreator, @NotNull Object[] parents) {
		super(handle, ci);
		
		//logger
		this.logger = logger;
		
		//debugMessenger
		if (initDebugCallback) {
			try (Frame frame = allocatorStack().frame()) {
				VkDebugUtilsMessengerCreateInfoEXT debugInfo = mallocStruct(frame, VkDebugUtilsMessengerCreateInfoEXT::create, VkDebugUtilsMessengerCreateInfoEXT.SIZEOF).set(
						VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT,
						0,
						0,
						VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT,
						VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT,
						this.debugMessengerCallback,
						0
				);
				PointerBufferPointer debugMessengerPtr = PointerBufferPointer.malloc(frame);
				assertVk(nvkCreateDebugUtilsMessengerEXT(this, debugInfo.address(), 0, debugMessengerPtr.address()));
				debugMessenger = debugMessengerPtr.getPointer();
			}
		} else {
			debugMessenger = 0;
		}
		
		//storage
		this.storage = storageCreator.apply(this, parents);
		
		//physical devices
		while (true) {
			try (Frame frame = allocatorStack().frame()) {
				PointerBufferInt count = PointerBufferInt.malloc(frame);
				assertVk(nvkEnumeratePhysicalDevices(this, count.address(), 0));
				ArrayBufferPointer devices = ArrayBufferPointer.malloc(allocatorHeap(), Integer.toUnsignedLong(count.getInt()), new Object[] {frame});
				if (assertVk(nvkEnumeratePhysicalDevices(this, count.address(), devices.address())) == VK_SUCCESS) {
					this.physicalDevices = devices.stream()
												  .mapToObj(phyPtr -> VkPhysicalDevice.wrap(phyPtr, this, new Object[] {this}))
												  .collect(Collectors.toCollection(() -> new ObservableCollection<>(new ArrayList<>())));
					break;
				}
			}
		}
	}
	
	//logger
	private final @NotNull Logger logger;
	
	public @NotNull Logger getLogger() {
		return logger;
	}
	
	//debugMessenger
	@SuppressWarnings("FieldCanBeLocal")
	private final @NotNull DebugMessenger debugMessengerCallback = new DebugMessenger();
	private final long debugMessenger;
	
	public long getDebugMessenger() {
		return debugMessenger;
	}
	
	public class DebugMessenger implements VkDebugUtilsMessengerCallbackEXTI {
		
		@Override
		public int invoke(int messageSeverity, int messageTypes, long pCallbackData, long pUserData) {
			LogLevel logLevel;
			switch (messageSeverity) {
				case VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT:
					logLevel = LogLevel.ERROR;
					break;
				case VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT:
					logLevel = LogLevel.WARNING;
					break;
				case VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT:
					logLevel = LogLevel.INFO;
					break;
				case VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT:
					logLevel = LogLevel.FINE;
					break;
				default:
					throw new IllegalArgumentException("messageSeverity: " + messageSeverity);
			}
			
			VkDebugUtilsMessengerCallbackDataEXT message = LwjglStructAllocator.wrapStruct(VkDebugUtilsMessengerCallbackDataEXT::create, pCallbackData);
			logger.log(logLevel, message.pMessageString());
			
			return VK_FALSE;
		}
	}
	
	//storage
	private final @NotNull Freeable storage;
	
	@Override
	public @NotNull Freeable getStorage() {
		return storage;
	}
	
	/**
	 * destory objects without reference to vkInstance
	 */
	public static class Storage extends FreeableStorage {
		
		private final long function_vkDestroyInstance;
		private final long function_vkDestroyDebugUtilsMessengerEXT;
		private final long instance;
		private final long debugMessenger;
		
		public Storage(@NotNull VkInstance instance, @NotNull Object[] parents) {
			super(instance, parents);
			
			VKCapabilitiesInstance capabilities = instance.getCapabilities();
			this.function_vkDestroyInstance = capabilities.vkDestroyInstance;
			this.function_vkDestroyDebugUtilsMessengerEXT = instance.debugMessenger != 0 ? capabilities.vkDestroyDebugUtilsMessengerEXT : 0;
			this.instance = instance.address();
			this.debugMessenger = instance.debugMessenger;
		}
		
		@Override
		protected @NotNull Barrier handleFree() {
			//vkCreateDebugUtilsMessengerEXT
			if (function_vkDestroyDebugUtilsMessengerEXT != 0)
				callPJPV(function_vkDestroyDebugUtilsMessengerEXT, instance, debugMessenger, 0);
			//nvkDestroyInstance
			JNI.callPPV(function_vkDestroyInstance, instance, 0);
			return Barrier.ALWAYS_TRIGGERED_BARRIER;
		}
	}
	
	//physical devices
	private final @NotNull ObservableCollection<VkPhysicalDevice> physicalDevices;
	
	public @NotNull ObservableCollection<VkPhysicalDevice> physicalDevices() {
		return physicalDevices;
	}
}
