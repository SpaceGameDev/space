#version 450
#extension GL_ARB_separate_shader_objects : enable

#include "../Translation.glsl"

//uniform
#include "../renderPass/UniformGlobal.glsl"

//in
layout(location = 0) in vec3 inPosScreenspace;
layout(location = 1) in vec3 inPosWorldspace;
layout(location = 2) in vec3 inNormal;
layout(location = 3) in vec3 inPosModel;
layout(location = 4) in vec3 inNormalModel;

//out
layout(location = 0) out vec4 outColor;

const float ambientStrength = 0.4;
const float specularStrength = 0.1;
const vec3[5][2] colorValues = {
{ vec3(227, 210, 182) / 256, vec3(188, 152, 130) / 256 },
{ vec3(141, 143, 130) / 256, vec3(221, 214, 186) / 256 },
{ vec3(165, 173, 174) / 256, vec3(227, 239, 237) / 256 },
{ vec3(227, 210, 182) / 256, vec3(188, 152, 130) / 256 },
{ vec3(141, 143, 130) / 256, vec3(221, 214, 186) / 256 },
};
const uint randomMask = 0xFFFFFFFF;

uint randomStep(inout uint seed) {
	return seed = (seed * 0x5DEECE66 + 0xB) & randomMask;
}

uint randomSeed(ivec3 from) {
	uint seed = from.x * 0xE66D + from.y * 0x5DEE + from.z * 0x19D3;
	for (int i = 0; i < 3; i++) {
		randomStep(seed);
	}
	return seed;
}

float randomFloat(inout uint seed) {
	return float(randomStep(seed)) / randomMask;
}

vec2 randomVec2(inout uint seed) {
	return vec2(randomFloat(seed), randomFloat(seed));
}

vec2 randomVec2(ivec3 from, uint offset) {
	uint seed = (randomSeed(from) + offset) & randomMask;
	return randomVec2(seed);
}

vec2 randomInterpoleratedVec2(vec3 from, uint offset) {
	ivec3 lower = ivec3(floor(from));
	vec3 diff = from - lower;
	//	vec3 diff = vec3(0);

	return mix(
	mix(mix(randomVec2(lower + ivec3(0, 0, 0), offset), randomVec2(lower + ivec3(1, 0, 0), offset), diff.x), mix(randomVec2(lower + ivec3(0, 1, 0), offset), randomVec2(lower + ivec3(1, 1, 0), offset), diff.x), diff.y),
	mix(mix(randomVec2(lower + ivec3(0, 0, 1), offset), randomVec2(lower + ivec3(1, 0, 1), offset), diff.x), mix(randomVec2(lower + ivec3(0, 1, 1), offset), randomVec2(lower + ivec3(1, 1, 1), offset), diff.x), diff.y),
	diff.z
	);
}

vec2 randomInterpoleratedSmoothedVec2(vec3 from, uint offset, vec2 strength) {
	return smoothstep(0, 1, clamp(randomInterpoleratedVec2(from, offset) * strength, 0, 1));
}

vec2 randomInterpoleratedVec2(vec3 from, uint offset, vec2 strength) {
	return randomInterpoleratedVec2(from, offset) * strength;
}

void main() {
	//color inputs
	vec3 posModelScaled = inPosModel / 6000.0 + 0.5;

	//random offset
	vec2 colorOffset = vec2(0.5);
	colorOffset += randomInterpoleratedSmoothedVec2(posModelScaled * vec3(0, 16, 0), 0x3F568A23, vec2(0.8));
	colorOffset += randomInterpoleratedVec2(posModelScaled * vec3(128, 128, 128), 0x63F68AC4, vec2(0.04, 0));
	//	colorOffset += randomInterpoleratedSmoothedVec2(posModelScaled * vec3(256, 256, 256), 0xA98C3AD2, vec2(0.04, 0));
	//	colorOffset += randomInterpoleratedSmoothedVec2(posModelScaled * vec3(512, 512, 512), 0xF126DE67, vec2(0.04, 0));
	//	colorOffset += randomInterpoleratedSmoothedVec2(posModelScaled * vec3(4096, 4096, 4096), 0xF126DE67, vec2(0.04, 0));

	float colorIndexFloat = clamp(colorOffset.x * colorValues.length(), 0, colorValues.length());
	int colorIndex = int(floor(colorIndexFloat));
	float colorIndexDiff = colorIndexFloat - colorIndex;
	vec3 color = mix(
	mix(colorValues[colorIndex][0], colorValues[colorIndex+1][0], colorIndexDiff),
	mix(colorValues[colorIndex][1], colorValues[colorIndex+1][1], colorIndexDiff),
	colorOffset.y
	);

	vec3 light = vec3(ambientStrength);

	//diffuse
	vec3 viewDir = normalize(uniformGlobal.cameraTranslation.offset - inPosWorldspace);
	float diff = max(dot(inNormal, uniformGlobal.lightDir), 0);
	light += diff;

	//specular
	vec3 reflectDir = reflect(-uniformGlobal.lightDir, inNormal);
	float spec = pow(max(dot(viewDir, reflectDir), 0.0), 16);
	light += specularStrength * spec;

	//outColor
	outColor = vec4(light * color, 1.0);
	//	outColor = vec4(color, 1.0);
	//	outColor = vec4(colorOffset, 0, 1);
}
