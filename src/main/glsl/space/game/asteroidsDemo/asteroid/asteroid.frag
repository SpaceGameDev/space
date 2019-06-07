#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(binding = 0) uniform UniformGlobal {
	mat4 projection;
	mat3 cameraRotation;
	vec3 cameraOffset;
} uniformGlobal;

layout(location = 0) in vec3 inPos;
layout(location = 1) in vec3 inNormal;
layout(location = 2) in vec3 fragVertexDistance;

layout(location = 0) out vec4 outColor;

const float ambientStrength = 0.2;
const float specularStrength = 0.5;
const vec3 lightDir = normalize(vec3(1, 1, 0));
const vec3 lightColor = vec3(1, 0.6, 0.6);

void main() {
	vec3 light = vec3(ambientStrength);

	//diffuse
	vec3 viewDir = normalize(uniformGlobal.cameraOffset - inPos);
	float diff = max(dot(inNormal, lightDir), 0);
	light += diff * 2;

	//specular
	vec3 reflectDir = reflect(-lightDir, inNormal);
	float spec = pow(max(dot(viewDir, reflectDir), 0.0), 16);
	light += specularStrength * spec;

	float effect = 1 - (fragVertexDistance.x*fragVertexDistance.x + fragVertexDistance.y*fragVertexDistance.y + fragVertexDistance.z*fragVertexDistance.z);
	effect = effect * 0.3 + 0.7;
	outColor = vec4(light * lightColor * effect, 1.0);
}