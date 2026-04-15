#version 120

uniform sampler2D texture;

varying vec2 texcoord;

void main() {
    vec4 color = texture2D(texture, texcoord);
    if (color.a < 0.1) discard;
    gl_FragData[0] = color;
}
