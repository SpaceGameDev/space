package space.game.firstTriangle;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.JNI;
import org.lwjgl.vulkan.EXTDebugUtils;
import org.lwjgl.vulkan.VKCapabilitiesInstance;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackEXTI;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkLayerProperties;
import space.engine.buffer.Allocator;
import space.engine.buffer.AllocatorStack.Frame;
import space.engine.buffer.NioBufferWrapper;
import space.engine.buffer.StringConverter;
import space.engine.buffer.array.ArrayBufferPointer;
import space.engine.buffer.pointer.PointerBufferInt;
import space.engine.buffer.pointer.PointerBufferPointer;
import space.engine.delegate.collection.ObservableCollection;
import space.engine.freeableStorage.Freeable;
import space.engine.freeableStorage.Freeable.FreeableWrapper;
import space.engine.freeableStorage.FreeableStorage;
import space.engine.logger.LogLevel;
import space.engine.logger.Logger;
import space.engine.logger.NullLogger;
import space.engine.lwjgl.LwjglStructAllocator;
import space.engine.sync.barrier.Barrier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.lwjgl.system.JNI.callPJPV;
import static org.lwjgl.vulkan.EXTDebugUtils.*;
import static org.lwjgl.vulkan.VK10.*;
import static space.engine.buffer.Allocator.*;
import static space.engine.lwjgl.LwjglStructAllocator.mallocStruct;
import static space.engine.lwjgl.PointerBufferWrapper.wrapPointer;
import static space.game.firstTriangle.VkException.assertVk;

public class VkInstance extends org.lwjgl.vulkan.VkInstance implements FreeableWrapper {
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		
		//game
		private String gameName;
		private int gameVersion;
		
		public Builder setGame(String gameName, int gameVersion) {
			this.gameName = gameName;
			this.gameVersion = gameVersion;
			return this;
		}
		
		public String getGameName() {
			return gameName;
		}
		
		public int getGameVersion() {
			return gameVersion;
		}
		
		//engine
		private String engineName = "space-engine";
		private int engineVersion = 1;
		
		public Builder overrideEngine(@NotNull String engineName, int engineVersion) {
			this.engineName = engineName;
			this.engineVersion = engineVersion;
			return this;
		}
		
		public String getEngineName() {
			return engineName;
		}
		
		public int getEngineVersion() {
			return engineVersion;
		}
		
		//vkVersion
		private int vkVersion = VK_API_VERSION_1_0;
		
		public Builder setVkVersion(int vkVersion) {
			this.vkVersion = vkVersion;
			return this;
		}
		
		public int getVkVersion() {
			return vkVersion;
		}
		
		//layers
		@NotNull Collection<VkLayerProperties> layers = new ArrayList<>();
		
		public Builder addLayer(@NotNull VkLayerProperties layer) {
			layers.add(Objects.requireNonNull(layer));
			return this;
		}
		
		public Builder addLayer(@NotNull Collection<VkLayerProperties> layer) {
			layer.forEach(Objects::requireNonNull);
			layers.addAll(layer);
			return this;
		}
		
		public @NotNull Collection<VkLayerProperties> getLayers() {
			return layers;
		}
		
		//extensions
		@NotNull Collection<VkExtensionProperties> extensions = new ArrayList<>();
		
		public Builder addExtension(@NotNull VkExtensionProperties layer) {
			extensions.add(Objects.requireNonNull(layer));
			return this;
		}
		
		public Builder addExtension(@NotNull Collection<VkExtensionProperties> layer) {
			layer.forEach(Objects::requireNonNull);
			extensions.addAll(layer);
			return this;
		}
		
		public @NotNull Collection<VkExtensionProperties> getExtensions() {
			return extensions;
		}
		
		//logger
		private @NotNull Logger logger = NullLogger.NULL_LOGGER;
		
		public @NotNull Logger getLogger() {
			return logger;
		}
		
		public Builder setLogger(@NotNull Logger logger) {
			this.logger = logger;
			return this;
		}
		
		//initDebugCallback
		private boolean initDebugCallback = true;
		
		public boolean isInitDebugCallback() {
			return initDebugCallback;
		}
		
		public Builder setInitDebugCallback(boolean initDebugCallback) {
			this.initDebugCallback = initDebugCallback;
			return this;
		}
		
		//validate
		public void validate() {
			requireNonNull(gameName);
			if (vkVersion < VK_API_VERSION_1_0)
				throw new IllegalStateException("vkVersion invalid: " + vkVersion + " (0x" + Integer.toHexString(vkVersion) + ")");
		}
		
		public VkInstance build(Object[] parents) {
			validate();
			
			if (initDebugCallback)
				addExtension(VkExtensions.extensionNameMap().get(EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME));
			
			try (Frame frame = Allocator.allocatorStack().frame()) {
				VkInstanceCreateInfo vkInstanceCreateInfo = mallocStruct(frame, VkInstanceCreateInfo::create, VkInstanceCreateInfo.SIZEOF).set(
						VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO,
						0,
						0,
						mallocStruct(frame, VkApplicationInfo::create, VkApplicationInfo.SIZEOF).set(
								0,
								0,
								StringConverter.stringToUTF8(frame, gameName, true).nioBuffer(),
								gameVersion,
								StringConverter.stringToUTF8(frame, engineName, true).nioBuffer(),
								engineVersion,
								vkVersion
						),
						wrapPointer(ArrayBufferPointer.alloc(frame, layers.stream()
																		  .mapToLong(layer -> NioBufferWrapper.getAddress(layer.layerName()))
																		  .toArray()
						)),
						wrapPointer(ArrayBufferPointer.alloc(frame, extensions.stream()
																			  .mapToLong(layer -> NioBufferWrapper.getAddress(layer.extensionName()))
																			  .toArray()
						))
				);
				
				PointerBufferPointer instance = PointerBufferPointer.malloc(frame);
				assertVk(nvkCreateInstance(vkInstanceCreateInfo.address(), 0, instance.address()));
				return new VkInstance(instance.getPointer(), vkInstanceCreateInfo, logger, initDebugCallback, parents);
			}
		}
	}
	
	//object
	public VkInstance(long handle, @NotNull VkInstanceCreateInfo ci, @NotNull Logger logger, boolean initDebugCallback, @NotNull Object[] parents) {
		super(handle, ci);
		
		//logger
		this.logger = logger;
		
		//debugCallback
		long debugMessenger;
		if (initDebugCallback) {
			try (Frame frame = allocatorStack().frame()) {
				VkDebugUtilsMessengerCreateInfoEXT debugInfo = mallocStruct(frame, VkDebugUtilsMessengerCreateInfoEXT::create, VkDebugUtilsMessengerCreateInfoEXT.SIZEOF).set(
						VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT,
						0,
						0,
						VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT,
						VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT,
						debugCallback,
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
		this.storage = new Storage(this, debugMessenger, parents);
		
		//physical devices
		while (true) {
			try (Frame frame = allocatorStack().frame()) {
				PointerBufferInt count = PointerBufferInt.malloc(frame);
				assertVk(nvkEnumeratePhysicalDevices(this, count.address(), 0));
				ArrayBufferPointer devices = ArrayBufferPointer.malloc(allocatorHeap(), Integer.toUnsignedLong(count.getInt()), new Object[] {frame});
				if (assertVk(nvkEnumeratePhysicalDevices(this, count.address(), devices.address())) == VK_SUCCESS) {
					this.physicalDevices = devices.stream()
												  .mapToObj(p -> new VkPhysicalDevice(p, this, new Object[] {this}))
												  .collect(Collectors.toCollection(() -> new ObservableCollection<>(new ArrayList<>())));
					break;
				}
			}
		}
	}
	
	//storage
	private final Storage storage;
	
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
		
		public Storage(@NotNull VkInstance instance, long debugMessenger, @NotNull Object[] parents) {
			super(instance, parents);
			
			VKCapabilitiesInstance capabilities = instance.getCapabilities();
			this.function_vkDestroyInstance = capabilities.vkDestroyInstance;
			this.function_vkDestroyDebugUtilsMessengerEXT = debugMessenger != 0 ? capabilities.vkDestroyDebugUtilsMessengerEXT : 0;
			this.instance = instance.address();
			this.debugMessenger = debugMessenger;
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
	
	//logger
	private final @NotNull Logger logger;
	
	public @NotNull Logger getLogger() {
		return logger;
	}
	
	//debugCallback
	@SuppressWarnings("FieldCanBeLocal")
	private final @NotNull DebugCallback debugCallback = new DebugCallback();
	
	public class DebugCallback implements VkDebugUtilsMessengerCallbackEXTI {
		
		@Override
		public int invoke(int messageSeverity, int messageTypes, long pCallbackData, long pUserData) {
			if (messageSeverity == VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT) {
				try (Frame frame = allocatorStack().frame()) {
					VkDebugUtilsMessengerCallbackDataEXT message = LwjglStructAllocator.wrapStruct(VkDebugUtilsMessengerCallbackDataEXT::create, pCallbackData);
					throw new VkException("DebugCallback error: " + message.pMessageString());
				}
			}
			
			LogLevel logLevel;
			switch (messageSeverity) {
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
			
			try (Frame frame = allocatorStack().frame()) {
				VkDebugUtilsMessengerCallbackDataEXT message = LwjglStructAllocator.wrapStruct(VkDebugUtilsMessengerCallbackDataEXT::create, pCallbackData);
				logger.log(logLevel, message.pMessageString());
			}
			
			return VK_FALSE;
		}
	}
	
	//physical devices
	private final @NotNull ObservableCollection<VkPhysicalDevice> physicalDevices;
	
	public @NotNull ObservableCollection<VkPhysicalDevice> getPhysicalDevices() {
		return physicalDevices;
	}
}
