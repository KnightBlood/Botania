#version 330
// Botania - MC 26.1 port of the 1.20.1 pylon glow fragment shader.
// Uses RenderPipelines.ENTITY_SNIPPET's vertex shader (core/entity.vsh); 26.1 replaced
// the single vertexDistance varying with spherical/cylindrical distances and the
// linear_fog() helper with apply_fog() reading the Fog UBO.
#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec4 vertexColor;
in vec4 lightMapColor;
in vec4 overlayColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    vec4 color = vec4(0);
    for (int i = -4; i < 4; i++) {
        for (int j = -3; j < 3; j++) {
            color += texture(Sampler0, texCoord0 + vec2(float(j), float(i)));
        }
    }
    if (color.a < 0.1) {
        discard;
    }
    // GameTime is in [0, 1) over course of the day, turn it back into ticks
    float time = GameTime * 24000;
    float brightness = sin(time / 20.0) * 0.5 + 0.15;
    float alpha = min(1, sin(time / 30.0) * 0.5 + 0.75);

    color = vec4((color * color * 0.0005 * brightness + texture(Sampler0, texCoord0)).rgb, alpha * vertexColor.a);
    color *= ColorModulator;

    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    color *= lightMapColor;
    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance,
            FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}
