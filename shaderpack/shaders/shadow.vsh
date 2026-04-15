#version 120

varying vec2 texcoord;

void main() {
    vec4 position = ftransform();

    // Shadow distortion — better quality near camera
    float len = length(position.xy);
    float distort = 0.15 + len * 0.85;
    position.xy /= distort;

    gl_Position = position;
    texcoord = (gl_TextureMatrix[0] * gl_MultiTexCoord0).xy;
}
