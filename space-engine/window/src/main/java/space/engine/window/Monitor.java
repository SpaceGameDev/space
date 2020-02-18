package space.engine.window;

import org.jetbrains.annotations.NotNull;

/**
 * The Monitor allows you to get physical properties of the actual Monitor on a system.
 */
public interface Monitor {
	
	/**
	 * name of the Monitor for identification
	 */
	@NotNull String name();
	
	/**
	 * width in mm
	 */
	int physicalWidth();
	
	/**
	 * height in mm
	 */
	int physicalHeight();
	
	/**
	 * relative position to other monitors
	 */
	int posX();
	
	/**
	 * relative position to other monitors
	 */
	int posY();
	
	/**
	 * the default video mode used of the desktop environment
	 */
	@NotNull VideoMode getDefaultVideoMode();
	
	/**
	 * get all available video modes
	 */
	@NotNull VideoMode[] getAvailableVideoModes();
	
	interface VideoMode {
		
		Monitor monitor();
		
		int width();
		
		int height();
		
		int refreshRate();
		
		int bitsR();
		
		int bitsG();
		
		int bitsB();
	}
	
}
