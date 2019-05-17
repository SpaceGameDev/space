#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(binding = 0) uniform Translation {
	mat4 projection;
	mat4 model;
	mat4 modelInverse;
} translation;

layout(location = 0) in vec3 inPos;
layout(location = 1) in vec3 inNormal;
layout(location = 2) in vec3 inColor;

layout(location = 0) out vec3 fragPos;
layout(location = 1) out vec3 fragNormal;
layout(location = 2) out vec3 fragColor;

void main() {
	vec4 worldPosition = vec4(inPos, 1.0) * translation.model;
	gl_Position = worldPosition * translation.projection;
	fragPos = vec3(worldPosition);
	fragNormal = mat3(translation.modelInverse) * inNormal;
	fragColor = inColor;
}
