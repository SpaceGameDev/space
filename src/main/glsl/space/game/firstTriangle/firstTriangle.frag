#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(location = 0) in vec3 inPos;
layout(location = 1) in vec3 inNormal;
layout(location = 2) in vec3 inColor;

layout(location = 0) out vec4 outColor;

const vec3 lightPos = vec3(0, 0, -3);
const vec3 lightColor = vec3(1, 1, 1);
const float ambientStrength = 0.1;

void main() {
	vec3 ambient = ambientStrength * lightColor;

	vec3 lightDir = normalize(lightPos - inPos);
	float diff = max(dot(inNormal, lightDir), 0);
	vec3 diffuse = diff * lightColor;

	outColor= vec4((ambient + diffuse) * inColor, 1.0);
	//outColor = vec4(inNormal / 2 + 0.5, 1.0);
}
