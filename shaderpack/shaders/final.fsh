#version 120

// ──────────────────────────────────────────────
//  ColonyCraft Shaders — Final Pass
//  Colony Survival-inspired warm color grading
// ──────────────────────────────────────────────

uniform sampler2D colortex0;

varying vec2 texcoord;

void main() {
    vec3 color = texture2D(colortex0, texcoord).rgb;

    // Warm color temperature (golden Colony Survival feel)
    color *= vec3(1.04, 1.00, 0.94);

    // Saturation boost (+15%)
    float lum = dot(color, vec3(0.299, 0.587, 0.114));
    color = mix(vec3(lum), color, 1.15);

    // Slight contrast lift
    color = (color - 0.5) * 1.05 + 0.5;

    // Subtle vignette
    vec2 uv = texcoord - 0.5;
    float vignette = 1.0 - dot(uv, uv) * 0.5;
    color *= clamp(vignette, 0.0, 1.0);

    color = clamp(color, 0.0, 1.0);
    gl_FragData[0] = vec4(color, 1.0);
}
