package relake.render.display.bqrender.impl;

import org.byteq.scannable.IShader;

public class KawaseBlurDown implements IShader {
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
                    vec2 uv = gl_TexCoord[0].xy * 2.0;
                    vec2 halfpixel = resolution * 2.0;
                
                    vec3 sum = texture2D(image, uv).rgb * 4.0;
                
                    sum += texture2D(image, uv - halfpixel.xy * offset).rgb;
                    sum += texture2D(image, uv + halfpixel.xy * offset).rgb;
                    sum += texture2D(image, uv + vec2(halfpixel.x, -halfpixel.y) * offset).rgb;
                    sum += texture2D(image, uv - vec2(halfpixel.x, -halfpixel.y) * offset).rgb;
                
                    vec3 color = sum / 8.0;
                    color = adjustSaturation(color, saturation);
                    color = mix(color, tintColor, tintIntensity);
                
                    gl_FragColor = vec4(color, 1.0);
                }""";
    }
}
