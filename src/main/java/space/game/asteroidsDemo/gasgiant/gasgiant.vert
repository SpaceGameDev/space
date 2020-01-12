#version 450
#extension GL_ARB_separate_shader_objects : enable

#include "../Translation.glsl"

//uniform
#include "../renderPass/UniformGlobal.glsl"

//in per vertex
layout(location = 0) in vec3 inPos;
layout(location = 1) in vec3 inNormal;

//out
layout(location = 0) out vec3 fragPosScreenspace;
layout(location = 1) out vec3 fragPosWorldspace;
layout(location = 2) out vec3 fragNormal;
layout(location = 3) out vec3 fragPosModel;
layout(location = 4) out vec3 fragNormalModel;

void main() {
	//position
	vec3 posWorldSpace = translation_translateRelative(uniformGlobal.gasgiantTranslation, inPos);
	fragPosWorldspace = posWorldSpace;
	vec3 posScreenspace = translation_translateRelativeInverse(uniformGlobal.cameraTranslation, posWorldSpace);
	fragPosScreenspace = posScreenspace;
	gl_Position = vec4(posScreenspace, 1.0) * uniformGlobal.projection;

	//other
	fragNormal = inNormal * transpose(uniformGlobal.gasgiantTranslation.rotation);
	fragPosModel = inPos;
	fragNormalModel = inNormal;
}
