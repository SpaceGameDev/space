#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(binding = 0) uniform UniformGlobal {
	mat4 projection;
	mat3 cameraRotation;
	vec3 cameraOffset;
} uniformGlobal;

//in per vertex
layout(location = 0) in vec3 inPos;
layout(location = 1) in vec3 inNormal;

//in per instance
layout(location = 8 /*9, 10*/) in mat3 modelRotation;
layout(location = 11) in vec3 modelOffset;

layout(location = 0) out vec3 fragPos;
layout(location = 1) out vec3 fragNormal;

void main() {
	vec3 worldPosition = (inPos * modelRotation) + modelOffset;
	vec3 cameraPosition = (worldPosition * uniformGlobal.cameraRotation) + uniformGlobal.cameraOffset;
	gl_Position = vec4(cameraPosition, 1.0) * uniformGlobal.projection;
	fragPos = cameraPosition;
	fragNormal = inNormal;
}
