struct Translation {
	mat3 rotation;
	vec3 offset;
};

vec3 translation_translate(Translation translation, vec3 vec) {
	return (vec + translation.offset) * translation.rotation;
}

vec3 translation_translateInverse(Translation translation, vec3 vec) {
	return (vec * transpose(translation.rotation)) - translation.offset;
}

vec3 translation_translateRelative(Translation translation, vec3 vec) {
	return (vec * transpose(translation.rotation)) + translation.offset;
}

vec3 translation_translateRelativeInverse(Translation translation, vec3 vec) {
	return (vec - translation.offset) * translation.rotation;
}
