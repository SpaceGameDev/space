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


// A single iteration of Bob Jenkins' One-At-A-Time hashing algorithm.
uint hash(uint x) {
	x += (x << 10u);
	x ^= (x >>  6u);
	x += (x <<  3u);
	x ^= (x >> 11u);
	x += (x << 15u);
	return x;
}

// Compound versions of the hashing algorithm I whipped together.
uint hash(uvec2 v) { return hash(v.x ^ hash(v.y)); }
uint hash(uvec3 v) { return hash(v.x ^ hash(v.y) ^ hash(v.z)); }
uint hash(uvec4 v) { return hash(v.x ^ hash(v.y) ^ hash(v.z) ^ hash(v.w)); }

// Construct a float with half-open range [0:1] using low 23 bits.
// All zeroes yields 0.0, all ones yields the next smallest representable value below 1.0.
float floatConstruct(uint m) {
	const uint ieeeMantissa = 0x007FFFFFu;// binary32 mantissa bitmask
	const uint ieeeOne      = 0x3F800000u;// 1.0 in IEEE binary32

	m &= ieeeMantissa;// Keep only mantissa bits (fractional part)
	m |= ieeeOne;// Add fractional part to 1.0

	float  f = uintBitsToFloat(m);// Range [1:2]
	return f - 1.0;// Range [0:1]
}

// Pseudo-random value in half-open range [0:1].
float random(float x) { return floatConstruct(hash(floatBitsToUint(x))); }
float random(vec2  v) { return floatConstruct(hash(floatBitsToUint(v))); }
float random(vec3  v) { return floatConstruct(hash(floatBitsToUint(v))); }
float random(vec4  v) { return floatConstruct(hash(floatBitsToUint(v))); }









uint randomStep(inout uint seed) {
	return seed = hash(seed);
}

uint randomSeed(ivec3 from) {
	return hash(hash(uvec3(from)));
}

float randomFloat(inout uint seed) {
	return floatConstruct(randomStep(seed));
}

vec2 randomVec2(inout uint seed) {
	return vec2(randomFloat(seed), randomFloat(seed));
}

vec3 randomVec3(inout uint seed) {
	return vec3(randomFloat(seed), randomFloat(seed), randomFloat(seed));
}

vec2 randomVec2(ivec3 from, uint offset) {
	uint seed = (randomSeed(from) + offset);
	return randomVec2(seed);
}

vec3 randomVec3(ivec3 from, uint offset) {
	uint seed = (randomSeed(from) + offset);
	return randomVec3(seed);
}

vec2 randomInterpoleratedVec2(vec3 from, uint offset) {
	ivec3 lower = ivec3(floor(from));
	vec3 diff = from - lower;

	return mix(
	mix(mix(randomVec2(lower + ivec3(0, 0, 0), offset), randomVec2(lower + ivec3(1, 0, 0), offset), diff.x), mix(randomVec2(lower + ivec3(0, 1, 0), offset), randomVec2(lower + ivec3(1, 1, 0), offset), diff.x), diff.y),
	mix(mix(randomVec2(lower + ivec3(0, 0, 1), offset), randomVec2(lower + ivec3(1, 0, 1), offset), diff.x), mix(randomVec2(lower + ivec3(0, 1, 1), offset), randomVec2(lower + ivec3(1, 1, 1), offset), diff.x), diff.y),
	diff.z
	);
}

vec3 randomInterpoleratedVec3(vec3 from, uint offset) {
	ivec3 lower = ivec3(floor(from));
	vec3 diff = from - lower;

	return mix(
	mix(mix(randomVec3(lower + ivec3(0, 0, 0), offset), randomVec3(lower + ivec3(1, 0, 0), offset), diff.x), mix(randomVec3(lower + ivec3(0, 1, 0), offset), randomVec3(lower + ivec3(1, 1, 0), offset), diff.x), diff.y),
	mix(mix(randomVec3(lower + ivec3(0, 0, 1), offset), randomVec3(lower + ivec3(1, 0, 1), offset), diff.x), mix(randomVec3(lower + ivec3(0, 1, 1), offset), randomVec3(lower + ivec3(1, 1, 1), offset), diff.x), diff.y),
	diff.z
	);
}

vec2 randomInterpoleratedSmoothedVec2(vec3 from, uint offset, vec2 strength) {
	return smoothstep(0, 1, clamp(randomInterpoleratedVec2(from, offset) * strength, 0, 1));
}

vec3 randomInterpoleratedSmoothedVec3(vec3 from, uint offset, vec3 strength) {
	return smoothstep(0, 1, clamp(randomInterpoleratedVec3(from, offset) * strength, 0, 1));
}

void main() {
	//color inputs
	vec3 posModelScaled = inPosModel / 6000.0 + 0.5;

	//random offset
	vec3 colorOffset = posModelScaled;
	colorOffset += randomInterpoleratedSmoothedVec3(posModelScaled * vec3(4), 0xF126DE67, vec3(0.15));
	colorOffset += randomInterpoleratedSmoothedVec3(posModelScaled * vec3(16), 0xA98C3AD2, vec3(0.1));

	vec2 colorInput = vec2(0.5);
	colorInput += randomInterpoleratedSmoothedVec2(colorOffset * vec3(0, 16, 0), 0x3F568A23, vec2(0.8));
	colorInput += randomInterpoleratedSmoothedVec2(colorOffset * vec3(32, 32, 32), 0x63F68AC4, vec2(0, 0.2));

	float colorIndexFloat = clamp(colorInput.x * colorValues.length(), 0, colorValues.length());
	int colorIndex = int(floor(colorIndexFloat));
	float colorIndexDiff = colorIndexFloat - colorIndex;
	vec3 color = mix(
	mix(colorValues[colorIndex][0], colorValues[colorIndex+1][0], colorIndexDiff),
	mix(colorValues[colorIndex][1], colorValues[colorIndex+1][1], colorIndexDiff),
	colorInput.y
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
