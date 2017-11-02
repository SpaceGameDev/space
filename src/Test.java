import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkInstanceCreateInfo;

import static org.lwjgl.vulkan.VK10.*;

public class Test {
	
	public static void main(String[] args) {
		VkInstanceCreateInfo info = VkInstanceCreateInfo.calloc();
		PointerBuffer buffer = PointerBuffer.allocateDirect(1);
		System.out.println(vkCreateInstance(info, null, buffer) == VK_SUCCESS);
		long instance = buffer.get();
		System.out.println(instance);
		
		info.free();
		//freeing the PointerBuffer causes SIGSEGV because there is a Byebuffer with Cleaner behind that!
		//and yes I didn't free the VKInstance but oh well...
	}
}
