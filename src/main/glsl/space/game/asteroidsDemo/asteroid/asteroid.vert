#version 450
#extension GL_ARB_separate_shader_objects : enable

#include "translation.glsl"

//main
layout(binding = 0) uniform UniformGlobal {
	mat4 projection;
	Translation cameraTranslation;
} uniformGlobal;

//in per vertex
layout(location = 0) in vec3 inPos;
layout(location = 1) in vec3 inNormal;

//in per instance
layout(location = 8 /*9, 10*/) in mat3 modelRotation;
layout(location = 11 /*12, 13*/) in mat3 modelRotationInverse;
layout(location = 14) in vec3 modelOffset;

layout(location = 0) out vec3 fragPosScreenspace;
layout(location = 1) out vec3 fragPosWorldspace;
layout(location = 2) out vec3 fragNormal;
layout(location = 3) out vec3 fragVertexDistance;

const vec3[3] vertexDistanceConst = vec3[3] (
vec3(1, 0, 0),
vec3(0, 1, 0),
vec3(0, 0, 1)
);

void main() {
	//position
	Translation modelTranslation = { modelRotation, modelOffset };
	vec3 posWorldSpace = translation_translateRelative(modelTranslation, inPos);
	fragPosWorldspace = posWorldSpace;
	vec3 posScreenspace = translation_translateRelativeInverse(uniformGlobal.cameraTranslation, posWorldSpace);
	fragPosScreenspace = posScreenspace;
	gl_Position = vec4(posScreenspace, 1.0) * uniformGlobal.projection;

	//other
	fragNormal = modelRotationInverse * inNormal;
	fragVertexDistance = vertexDistanceConst[gl_VertexIndex % 3];
}
