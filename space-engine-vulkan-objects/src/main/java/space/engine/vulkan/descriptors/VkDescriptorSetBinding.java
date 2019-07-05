package space.engine.vulkan.descriptors;

import org.jetbrains.annotations.Nullable;

public class VkDescriptorSetBinding {
	
	public final int
			binding,
			descriptorType,
			descriptorCount,
			stageFlags;
	public final @Nullable long[] immutableSamplers;
	
	public VkDescriptorSetBinding(int binding, int descriptorType, int descriptorCount, int stageFlags) {
		this(binding, descriptorType, descriptorCount, stageFlags, null);
	}
	
	public VkDescriptorSetBinding(int binding, int descriptorType, int descriptorCount, int stageFlags, @Nullable long[] immutableSamplers) {
		this.binding = binding;
		this.descriptorType = descriptorType;
		this.descriptorCount = descriptorCount;
		this.stageFlags = stageFlags;
		this.immutableSamplers = immutableSamplers;
	}
}
