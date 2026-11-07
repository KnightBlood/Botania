#version 330
// Botania - MC 26.1 port of the 1.20.1 crafting halo fragment shader.
// Bound to a pipeline built from RenderPipelines.ITEM_SNIPPET (core/item.vsh), which
// supplies vertexColor + texCoord0 as varyings.
#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    float timeTicks = GameTime * 24000;
    float brightness = sin(timeTicks / 20.0) * 0.5 + 0.5;

    vec4 sum = vec4(0);
    for (int i = -4; i < 4; i++) {
        for (int j = -3; j < 3; j++) {
            sum += texture(Sampler0, texCoord0 + vec2(float(j), float(i))) * 0.25;
        }
    }
    vec4 sampled = texture(Sampler0, texCoord0);
    vec4 color = vec4(0);
    if (sampled.r < 0.3) {
        color = sum * sum * 0.012 + sampled;
    } else {
        if (sampled.r < 0.5) {
            color = sum * sum * 0.009 * brightness + sampled;
        } else {
            color = sum * sum * 0.0075 * brightness + sampled;
        }
    }

    color *= vertexColor;
    if (color.a < 0.1) {
        discard;
    }
    fragColor = color * ColorModulator;
}
