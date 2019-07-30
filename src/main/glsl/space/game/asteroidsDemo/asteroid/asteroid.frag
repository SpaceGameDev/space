#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(binding = 0) uniform UniformGlobal {
	mat4 projection;
	mat3 cameraRotation;
	vec3 cameraOffset;
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
	vec3 light = vec3(ambientStrength);

	//shadow
	float gasgiantShadowPlaneDistanceToFrag = dot(gasgiantPos - inPosWorldspace, uniformGlobal.lightDir);
	vec3 gasgiantShadowPlanePoint = inPosWorldspace + (uniformGlobal.lightDir * gasgiantShadowPlaneDistanceToFrag);
	float gasgiantShadowPlaneRadius = length(gasgiantPos - gasgiantShadowPlanePoint);
	if (!(gasgiantShadowPlaneDistanceToFrag > 0 && gasgiantShadowPlaneRadius < gasgiantRadius)) {

		//diffuse
		vec3 viewDir = normalize(uniformGlobal.cameraOffset - inPosWorldspace);
		float diff = max(dot(inNormal, uniformGlobal.lightDir), 0);
		light += diff * 2;

		//specular
		vec3 reflectDir = reflect(-uniformGlobal.lightDir, inNormal);
		float spec = pow(max(dot(viewDir, reflectDir), 0.0), 16);
		light += specularStrength * spec;
	}

	//effect
	float effect = 1 - (fragVertexDistance.x*fragVertexDistance.x + fragVertexDistance.y*fragVertexDistance.y + fragVertexDistance.z*fragVertexDistance.z);
	effect = effect * 0.3 + 0.7;
//	float effect = 1.0;

	//outColor
	outColor = vec4(light * lightColor * effect, 1.0);
}