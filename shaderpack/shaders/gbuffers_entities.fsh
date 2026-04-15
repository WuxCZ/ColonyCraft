#version 120

uniform sampler2D texture;
uniform sampler2D lightmap;

varying vec2 texcoord;
varying vec2 lmcoord;
varying vec4 glcolor;

void main() {
    vec4 color = texture2D(texture, texcoord) * glcolor;
    color *= texture2D(lightmap, lmcoord);

    /* DRAWBUFFERS:01 */
    gl_FragData[0] = color;
    gl_FragData[1] = vec4(lmcoord, 0.0, 1.0);
}
