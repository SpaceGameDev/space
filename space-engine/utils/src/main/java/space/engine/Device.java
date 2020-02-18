package space.engine;

import java.util.Properties;

public class Device {
	
	public static final String OS;
	public static final String USER_DIR;
	public static final int ARCH_BITS;
	public static final boolean IS_64_BIT;
	public static final int CORE_CNT;
	public static final long MEMORY_MAX;
	
	static {
		Properties properties = System.getProperties();
		Runtime runtime = Runtime.getRuntime();
		
		OS = properties.getProperty("sun.desktop");
		USER_DIR = properties.getProperty("user.dir");
		IS_64_BIT = properties.getProperty("os.arch").contains("64");
		ARCH_BITS = IS_64_BIT ? 64 : 32;
		CORE_CNT = runtime.availableProcessors();
		MEMORY_MAX = runtime.maxMemory();
	}
}
