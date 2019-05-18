#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(location = 0) in vec3 inPos;
layout(location = 1) in vec3 inNormal;
layout(location = 2) in vec3 inColor;
layout(location = 3) out vec3 inCameraPos;

layout(location = 0) out vec4 outColor;

//const vec3[] lightPos = {vec3(0, 0, -2), vec3(-2, 0, -2), vec3(0, 2, -2), vec3(2, 0, -2)};
//const vec3[] lightColor = {vec3(1, 1, 1), vec3(1, 0, 0), vec3(0, 1, 0), vec3(0, 0, 1)};
const vec3[] lightPos = { vec3(-0.5, 0, -1), vec3(0, 0.5, -1), vec3(0.5, 0, -1) };
const vec3[] lightColor = { vec3(1, 0.25, 0.25), vec3(0.25, 1, 0.25), vec3(0.25, 0.25, 1) };
const float ambientStrength = 0.1;
const float specularStrength = 0.5;

void main() {
	vec3 light = vec3(ambientStrength);

	vec3 viewDir = normalize(inCameraPos - inPos);
	for (int i = 0; i < lightPos.length(); i++) {
		//diffuse
		vec3 lightDir = normalize(lightPos[i] - inPos);
		float diff = max(dot(inNormal, lightDir), 0);
		light += diff * lightColor[i];

		//specular
		vec3 reflectDir = reflect(-lightDir, inNormal);
		float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32);
		light += specularStrength * spec * lightColor[i];
	}

	outColor= vec4(light * inColor, 1.0);
	//outColor = vec4(inNormal / 2 + 0.5, 1.0);
}
