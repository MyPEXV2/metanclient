package relake.render.display.bqrender.impl;

import org.byteq.scannable.IShader;

public class KawaseBlurUp implements IShader {
    @Override
    public String shader() {
        return """
                #version 120
                
                uniform sampler2D image;
                uniform float offset;
                uniform vec2 resolution;
                uniform float saturation;
                uniform float tintIntensity;
                uniform vec3 tintColor;
                
                vec3 adjustSaturation(vec3 color, float saturation) {
                    float gray = dot(color, vec3(0.299, 0.587, 0.114));
                    return mix(vec3(gray), color, saturation);
                }
                
                void main() {
                    vec2 uv = gl_TexCoord[0].xy / 2.0;
                    vec2 halfpixel = resolution / 2.0;
                
                    vec3 sum = texture2D(image, uv + vec2(-halfpixel.x * 2.0, 0.0) * offset).rgb;
                
                    sum += texture2D(image, uv + vec2(-halfpixel.x, halfpixel.y) * offset).rgb * 2.0;
                    sum += texture2D(image, uv + vec2(0.0, halfpixel.y * 2.0) * offset).rgb;
                    sum += texture2D(image, uv + vec2(halfpixel.x, halfpixel.y) * offset).rgb * 2.0;
                    sum += texture2D(image, uv + vec2(halfpixel.x * 2.0, 0.0) * offset).rgb;
                    sum += texture2D(image, uv + vec2(halfpixel.x, -halfpixel.y) * offset).rgb * 2.0;
                    sum += texture2D(image, uv + vec2(0.0, -halfpixel.y * 2.0) * offset).rgb;
                    sum += texture2D(image, uv + vec2(-halfpixel.x, -halfpixel.y) * offset).rgb * 2.0;
                
                    vec3 color = sum / 12.0;
                    color = adjustSaturation(color, saturation);
                    color = mix(color, tintColor, tintIntensity);
                
                    gl_FragColor = vec4(color, 1.0);
                }""";
    }

}
