#version 120

// ──────────────────────────────────────────────
//  ColonyCraft Shaders — Composite Pass
//  Soft shadow mapping with warm/cool day-night
// ──────────────────────────────────────────────

const float SHADOW_DISTORT = 0.85;
const float SHADOW_BIAS    = 0.0008;
const float AMBIENT        = 0.45;

uniform sampler2D colortex0;
uniform sampler2D colortex1;
uniform sampler2D depthtex0;
uniform sampler2D shadowtex0;

uniform mat4  gbufferProjectionInverse;
uniform mat4  gbufferModelViewInverse;
uniform mat4  shadowModelView;
uniform mat4  shadowProjection;
uniform int   worldTime;
uniform float rainStrength;

varying vec2 texcoord;

vec3 distortShadowClip(vec3 clip) {
    float len = length(clip.xy);
    float distort = (1.0 - SHADOW_DISTORT) + len * SHADOW_DISTORT;
    clip.xy /= distort;
    return clip;
}

float sampleShadow(vec2 coord, float refDepth) {
    return step(refDepth - SHADOW_BIAS, texture2D(shadowtex0, coord).r);
}

void main() {
    vec3  color = texture2D(colortex0, texcoord).rgb;
    float depth = texture2D(depthtex0, texcoord).r;
    float skyLight = texture2D(colortex1, texcoord).g;  // lightmap V = sky

    if (depth < 1.0) {
        // ── Reconstruct world position ──
        vec4 clip = vec4(texcoord * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
        vec4 view = gbufferProjectionInverse * clip;
        view /= view.w;
        vec4 world = gbufferModelViewInverse * view;

        // ── Shadow-space transform (with matching distortion) ──
        vec4 sc = shadowProjection * (shadowModelView * world);
        sc.xyz /= sc.w;
        vec3 sCoord = distortShadowClip(sc.xyz) * 0.5 + 0.5;

        // ── 5-tap PCF soft shadow ──
        float texel  = 1.0 / 1024.0;
        float shadow = sampleShadow(sCoord.xy,                        sCoord.z);
        shadow += sampleShadow(sCoord.xy + vec2( texel, 0.0),  sCoord.z);
        shadow += sampleShadow(sCoord.xy + vec2(-texel, 0.0),  sCoord.z);
        shadow += sampleShadow(sCoord.xy + vec2(0.0,  texel),  sCoord.z);
        shadow += sampleShadow(sCoord.xy + vec2(0.0, -texel),  sCoord.z);
        shadow /= 5.0;

        // Outside shadow map ⇒ fully lit
        if (sCoord.x < 0.0 || sCoord.x > 1.0 ||
            sCoord.y < 0.0 || sCoord.y > 1.0) {
            shadow = 1.0;
        }

        // ── Day / night interpolation ──
        float dayFactor = 0.0;
        if (worldTime < 12000) {
            dayFactor = 1.0;
            if (worldTime < 1000)       dayFactor = float(worldTime) / 1000.0;
            else if (worldTime > 11000) dayFactor = float(12000 - worldTime) / 1000.0;
        } else if (worldTime < 13000) {
            dayFactor = float(13000 - worldTime) / 1000.0;
        }

        // Warm sun / cool moon (Colony Survival palette)
        vec3 sunTint  = vec3(1.03, 1.00, 0.95);
        vec3 moonTint = vec3(0.80, 0.85, 0.95);
        vec3 lightTint = mix(moonTint, sunTint, dayFactor);

        // Apply shadow only where sky light reaches
        float shadowResult = mix(AMBIENT, 1.0, shadow);
        float shadowFactor = mix(1.0, shadowResult, skyLight);
        color *= shadowFactor * lightTint;

        // Rain darkening
        color = mix(color, color * 0.75, rainStrength * 0.3);
    }

    /* DRAWBUFFERS:0 */
    gl_FragData[0] = vec4(color, 1.0);
}
