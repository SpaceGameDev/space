#version 450
#extension GL_ARB_separate_shader_objects : enable

#include "translation.glsl"

layout(binding = 0) uniform UniformGlobal {
	mat4 projection;
	Translation cameraTranslation;
	vec3 lightDir;
} uniformGlobal;

layout(location = 0) in vec3 inPosScreenspace;
layout(location = 1) in vec3 inPosWorldspace;
layout(location = 2) in vec3 inNormal;
layout(location = 3) in vec3 fragVertexDistance;

layout(location = 0) out vec4 outColor;

const vec3 gasgiantPos = vec3(0, 0, 2000 * 4);
const float gasgiantRadius = 3000;
const float ambientStrength = 0.2;
const float specularStrength = 0.5;
const vec3 lightColor = vec3(1, 0.6, 0.6);

void main() {
	//effect
	float effect = min(min(fragVertexDistance.x, fragVertexDistance.y), fragVertexDistance.z);
	effect = min(effect * 5, 1.0);

	vec3 light = vec3(ambientStrength * (effect * 0.025 + 0.975));

	//shadow
	float gasgiantShadowPlaneDistanceToFrag = dot(gasgiantPos - inPosWorldspace, uniformGlobal.lightDir);
	vec3 gasgiantShadowPlanePoint = inPosWorldspace + (uniformGlobal.lightDir * gasgiantShadowPlaneDistanceToFrag);
	float gasgiantShadowPlaneRadius = length(gasgiantPos - gasgiantShadowPlanePoint);
	if (!(gasgiantShadowPlaneDistanceToFrag > 0 && gasgiantShadowPlaneRadius < gasgiantRadius)) {

		//diffuse
		vec3 viewDir = normalize(uniformGlobal.cameraTranslation.offset - inPosWorldspace);
		float diff = max(dot(inNormal, uniformGlobal.lightDir), 0);
		light += diff * (effect * 0.075 + 0.925);

		//specular
		vec3 reflectDir = reflect(-uniformGlobal.lightDir, inNormal);
		float spec = pow(max(dot(viewDir, reflectDir), 0.0), 16);
		light += specularStrength * spec;
	}

	//outColor
	outColor = vec4(light * lightColor, 1.0);
}
