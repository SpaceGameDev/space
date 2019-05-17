#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(binding = 0) uniform Translation {
	mat4 matrix;
} translation;

layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec3 inNormal;
layout(location = 2) in vec3 inColor;

layout(location = 0) out vec3 fragNormal;
layout(location = 1) out vec3 fragColor;

void main() {
	gl_Position = vec4(inPosition, 1.0) * translation.matrix;
	fragColor = inColor;
}