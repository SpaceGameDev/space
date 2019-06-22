#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(binding = 0) uniform UniformGlobal {
	mat4 projection;
	mat3 cameraRotation;
	vec3 cameraOffset;
} uniformGlobal;

layout(location = 0) in vec3 inPos;
layout(location = 1) in vec3 inNormal;

layout(location = 0) out vec4 outColor;

void main() {
	outColor = vec4(inNormal / 2 + 0.5, 1.0);
}
