package relake.shader;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.math.vector.Matrix4f;
import org.lwjgl.opengl.GL20;
import relake.common.InstanceAccess;
import relake.common.util.FileUtil;
import java.io.ByteArrayInputStream;

import static net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_TEX;
import static org.lwjgl.opengl.ARBShaderObjects.*;
import static org.lwjgl.opengl.GL20.*;

public class ShaderRegister implements InstanceAccess {
    public static ShaderRegister
            ROUND_SHADER,
            GLOW_SHADER,
            FONT_RENDER,
            CORNER_ROUND_SHADER,
            TEXTURE_ROUND_SHADER,
            OUTLINE_ROUND_SHADER,
            ROUND_HUD_SHADER,
            OUTLINE_ROUND_HUD_SHADER,
            HANDS_REVERSE_SHADER,
            KAWASE_DOWN_SHADER,
            KAWASE_UP_SHADER,
            TORUS_SHADER,
            HEAD_SHADER,
            OUTLINE_MASK_SHADER;

    private final int shaderID = glCreateProgram();

    public ShaderRegister(String shader) {
        glAttachShader(shaderID, createShader(shader));
        glLinkProgram(shaderID);
    }
    public static void init() {
        //mr.hell moment xD
        OUTLINE_MASK_SHADER = new ShaderRegister("""
                #version 120
                
                uniform vec4 color;
                uniform sampler2D textureIn, textureToCheck;
                uniform vec2 texelSize, direction;
                uniform float size;
                
                #define offset direction * texelSize
                
                float smoothstep(float edge0, float edge1, float x) {
                    float t = clamp((x - edge0) / (edge1 - edge0), 0.0, 1.0);
                    return t * t * (3.0 - 2.0 * t);
                }
                
                void main() {
                    if (direction.y == 1) {
                        if (texture2D(textureToCheck, gl_TexCoord[0].st).a != 0.0) discard;
                    }
                
                    vec4 innerAlpha = texture2D(textureIn, gl_TexCoord[0].st);
                    innerAlpha *= innerAlpha.a;
                    for (float r = 1.0; r <= size; r ++) {
                        vec4 colorCurrent1 = texture2D(textureIn, gl_TexCoord[0].st + offset * r);
                        vec4 colorCurrent2 = texture2D(textureIn, gl_TexCoord[0].st - offset * r);
                        colorCurrent1.rgb *= colorCurrent1.a;
                        colorCurrent2.rgb *= colorCurrent2.a;
                        innerAlpha += (colorCurrent1 + colorCurrent2) * r;
                    }
                    float smoothAlpha = smoothstep(0.0, 1.0, innerAlpha.a); // Using smoothstep for alpha interpolation
                    gl_FragColor = vec4(innerAlpha.rgb / innerAlpha.a, mix(innerAlpha.a, 1.0 - exp(-innerAlpha.a), step(0.0, direction.y)) * smoothAlpha);
                }
                """);
        CORNER_ROUND_SHADER = new ShaderRegister("""
                #version 120
                uniform vec2 size;
                uniform vec4 round;
                uniform vec2 smoothness;
                uniform float value;
                uniform vec4 color;
                
                float test(vec2 vec_1, vec2 vec_2, vec4 vec_4) {
                    vec_4.xy = (vec_1.x > 0.0) ? vec_4.xy : vec_4.zw;
                    vec_4.x = (vec_1.y > 0.0) ? vec_4.x : vec_4.y;
                    vec2 coords = abs(vec_1) - vec_2 + vec_4.x;
                    return min(max(coords.x, coords.y), 0.0) + length(max(coords, vec2(0.0f))) - vec_4.x;
                }
                
                void main() {
                    vec2 st = gl_TexCoord[0].st * size;
                    vec2 halfSize = 0.5 * size;
                    float sa = 1.0 - smoothstep(smoothness.x, smoothness.y, test(halfSize - st, halfSize - value, round));
                    gl_FragColor = mix(vec4(color.rgb, 0.0), vec4(color.rgb, color.a), sa);
                }""");
        KAWASE_UP_SHADER = new ShaderRegister("""
                #version 120
                
                uniform sampler2D inTexture;
                uniform sampler2D textureToCheck;
                uniform vec2 halfpixel;
                uniform vec2 offset;
                uniform vec2 iResolution;
                uniform int check;
                
                void main() {
                    vec2 uv = gl_FragCoord.xy / iResolution;
                   \s
                    vec2 halfOffsetX = vec2(halfpixel.x, 0.0) * offset;
                    vec2 halfOffsetY = vec2(0.0, halfpixel.y) * offset;
                   \s
                    vec4 sum = texture2D(inTexture, uv - halfOffsetX * 2.0);
                    sum += texture2D(inTexture, uv - halfOffsetX + halfOffsetY) * 2.0;\s
                    sum += texture2D(inTexture, uv + halfOffsetY * 2.0);\s
                    sum += texture2D(inTexture, uv + halfOffsetX + halfOffsetY) * 2.0;\s
                    sum += texture2D(inTexture, uv + halfOffsetX * 2.0);\s
                    sum += texture2D(inTexture, uv + halfOffsetX - halfOffsetY) * 2.0;
                    sum += texture2D(inTexture, uv - halfOffsetY * 2.0);
                    sum += texture2D(inTexture, uv - halfOffsetX - halfOffsetY) * 2.0;
                   \s
                    vec4 color = sum / 12.0;
                   \s
                    float alpha = mix(1.0, texture2D(textureToCheck, gl_TexCoord[0].st).a, float(check));
                   \s
                    gl_FragColor = vec4(color.rgb, alpha);
                }
                
                """);

        TORUS_SHADER = new ShaderRegister("""
                
                				#version 150
                
                				uniform sampler2D inputSampler;
                				uniform vec2 inputResolution;
                				uniform float blurAmount;
                				uniform float reflect;
                				uniform float noiseValue;
                
                				in vec2 vertexPos;
                				uniform vec4 vertexColor;
                
                				out vec4 fragColor;
                
                				#define TAU 6.28318530718
                
                				//	Simplex 3D Noise
                				//	by Ian McEwan, Stefan Gustavson (https://github.com/stegu/webgl-noise)
                				//
                				vec4 permute(vec4 x){ return mod(((x*34.0)+1.0)*x, 289.0); }
                				vec4 taylorInvSqrt(vec4 r){ return 1.79284291400159 - 0.85373472095314 * r; }
                
                				float snoise(vec3 v){
                				    const vec2  C = vec2(1.0/6.0, 1.0/3.0);
                				    const vec4  D = vec4(0.0, 0.5, 1.0, 2.0);
                
                				    // First corner
                				    vec3 i  = floor(v + dot(v, C.yyy));
                				    vec3 x0 =   v - i + dot(i, C.xxx);
                
                				    // Other corners
                				    vec3 g = step(x0.yzx, x0.xyz);
                				    vec3 l = 1.0 - g;
                				    vec3 i1 = min(g.xyz, l.zxy);
                				    vec3 i2 = max(g.xyz, l.zxy);
                
                				    //  x0 = x0 - 0. + 0.0 * C
                				    vec3 x1 = x0 - i1 + 1.0 * C.xxx;
                				    vec3 x2 = x0 - i2 + 2.0 * C.xxx;
                				    vec3 x3 = x0 - 1. + 3.0 * C.xxx;
                
                				    // Permutations
                				    i = mod(i, 289.0);
                				    vec4 p = permute(permute(permute(
                				    i.z + vec4(0.0, i1.z, i2.z, 1.0))
                				    + i.y + vec4(0.0, i1.y, i2.y, 1.0))
                				    + i.x + vec4(0.0, i1.x, i2.x, 1.0));
                
                				    // Gradients
                				    // ( N*N points uniformly over a square, mapped onto an octahedron.)
                				    float n_ = 1.0/7.0;// N=7
                				    vec3  ns = n_ * D.wyz - D.xzx;
                
                				    vec4 j = p - 49.0 * floor(p * ns.z *ns.z);//  mod(p,N*N)
                
                				    vec4 x_ = floor(j * ns.z);
                				    vec4 y_ = floor(j - 7.0 * x_);// mod(j,N)
                
                				    vec4 x = x_ *ns.x + ns.yyyy;
                				    vec4 y = y_ *ns.x + ns.yyyy;
                				    vec4 h = 1.0 - abs(x) - abs(y);
                
                				    vec4 b0 = vec4(x.xy, y.xy);
                				    vec4 b1 = vec4(x.zw, y.zw);
                
                				    vec4 s0 = floor(b0)*2.0 + 1.0;
                				    vec4 s1 = floor(b1)*2.0 + 1.0;
                				    vec4 sh = -step(h, vec4(0.0));
                
                				    vec4 a0 = b0.xzyw + s0.xzyw*sh.xxyy;
                				    vec4 a1 = b1.xzyw + s1.xzyw*sh.zzww;
                
                				    vec3 p0 = vec3(a0.xy, h.x);
                				    vec3 p1 = vec3(a0.zw, h.y);
                				    vec3 p2 = vec3(a1.xy, h.z);
                				    vec3 p3 = vec3(a1.zw, h.w);
                
                				    //Normalise gradients
                				    vec4 norm = taylorInvSqrt(vec4(dot(p0, p0), dot(p1, p1), dot(p2, p2), dot(p3, p3)));
                				    p0 *= norm.x;
                				    p1 *= norm.y;
                				    p2 *= norm.z;
                				    p3 *= norm.w;
                
                				    // Mix final noise value
                				    vec4 m = max(0.6 - vec4(dot(x0, x0), dot(x1, x1), dot(x2, x2), dot(x3, x3)), 0.0);
                				    m = m * m;
                				    return 42.0 * dot(m*m, vec4(dot(p0, x0), dot(p1, x1),
                				    dot(p2, x2), dot(p3, x3)));
                				}
                
                				// Blur Function
                				vec4 blur(vec2 uv) {
                				    vec4 pixelColor = texture(inputSampler, uv);
                
                				    vec2 radius = vec2(blurAmount) / inputResolution;
                
                				    float blurQuality = 4.0;
                				    float blurDirections = 16.0;
                
                				    for (float d = 0.0; d < TAU; d += TAU / blurDirections) {
                				        for (float i = 1.0 / 4.0; i <= 1.0; i += 1.0 / blurQuality) {
                				            pixelColor += texture(inputSampler, uv + vec2(cos(d), sin(d)) * radius * i);
                				        }
                				    }
                
                				    // Normalize
                				    pixelColor /= blurQuality * blurDirections;
                				    return pixelColor;
                				}
                
                				void main() {
                				    vec2 uv = gl_FragCoord.xy / inputResolution.xy;
                				    vec2 reflectedUV = vec2(uv.x, 1.0 - uv.y);
                
                				    float time = mod(gl_FragCoord.x + gl_FragCoord.y, 1000.0) * 0.001;
                				    float noise = snoise(vec3(reflectedUV * reflect, 1));
                
                				    // sosal
                				    vec2 noisyUV = reflectedUV + vec2(noise * noiseValue, noise * noiseValue);
                
                				    vec4 reflectedColor = texture(inputSampler, noisyUV);
                
                				   vec4 blurredColor = blur(noisyUV);
                
                				    fragColor = vec4(blurredColor.rgb, vertexColor.a);
                				}
                
                
                """);

        KAWASE_DOWN_SHADER = new ShaderRegister("""
                #version 120
                
                 uniform sampler2D inTexture;
                 uniform vec2 offset;
                 uniform vec2 halfpixel;
                 uniform vec2 iResolution;
                
                 void main() {
                     vec2 uv = gl_FragCoord.xy / iResolution;
                     uv.y = 1.0 - uv.y;
                
                     vec2 offsetX = vec2(halfpixel.x, 0.0) * offset;
                     vec2 offsetY = vec2(0.0, halfpixel.y) * offset;
                    \s
                     vec4 sum = texture2D(inTexture, gl_TexCoord[0].st) * 4.0
                              + texture2D(inTexture, uv - offsetX - offsetY)
                              + texture2D(inTexture, uv + offsetX + offsetY)
                              + texture2D(inTexture, uv + offsetX - offsetY)
                              + texture2D(inTexture, uv - offsetX + offsetY);
                
                     gl_FragColor = vec4(sum.rgb * 0.125, 1.0);
                 }
                
                """);

        HANDS_REVERSE_SHADER = new ShaderRegister("""
                #version 120
                
                uniform sampler2D textureIn;
                uniform vec4 color;
                uniform float saturation;
                
                void main() {
                    vec2 texCoord = vec2(1.0 - gl_TexCoord[0].s, 1.0 - gl_TexCoord[0].t);
                    vec4 texColor = texture2D(textureIn, texCoord);
                    float brightness = (texColor.r + texColor.g + texColor.b) / saturation;
                    vec4 finalColor = mix(texColor, color, brightness);
                    gl_FragColor = finalColor;
                }
                
                """);

        ROUND_HUD_SHADER = new ShaderRegister("""
                #version 120
                
                uniform vec4 color1;
                uniform vec4 color2;
                uniform vec4 color3;
                uniform vec4 color4;
                
                uniform vec2 size;
                uniform vec4 round;
                uniform float value;
                uniform vec2 smoothness;
                uniform bool shadow;
                uniform float shadowAlpha;
                
                float roundedBox(vec2 center, vec2 size, vec4 radius) {
                    radius.xy = (center.x > 0.0) ? radius.xy : radius.zw;
                    radius.x  = (center.y > 0.0) ? radius.x : radius.y;
                
                    vec2 q = abs(center) - size + radius.x;
                    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - radius.x;
                }
                
                vec4 createGradient(vec2 pos) {
                    return mix(mix(color1, color2, pos.y), mix(color3, color4, pos.y), pos.x);
                }
                
                void main() {
                    vec2 tex = gl_TexCoord[0].st * size;
                
                    float distance = roundedBox(tex - (size / 2.0), (size / 2.0) - value, round);
                
                    float smoothedAlpha = (1.0 - smoothstep(smoothness.x, smoothness.y, distance));
                
                    vec4 gradient = createGradient(gl_TexCoord[0].st);
                
                    if (shadow) {
                        vec4 finalColor = mix(vec4(gradient.rgb, 0.0), vec4(gradient.rgb, gradient.a * smoothedAlpha), smoothedAlpha);
                        gl_FragColor = vec4(finalColor.rgb, finalColor.a * shadowAlpha);
                    } else {
                        gl_FragColor = vec4(gradient.rgb, gradient.a * smoothedAlpha);                        
                    }
                }""");

        OUTLINE_ROUND_HUD_SHADER = new ShaderRegister(
                """
                        #version 120
                                                       \s
                                        uniform vec4 color1;
                                        uniform vec4 color2;
                                        uniform vec4 color3;
                                        uniform vec4 color4;
                                                       \s
                                        uniform vec2 size;
                                        uniform vec4 round;
                                        uniform float value;
                                        uniform vec2 smoothness;
                                        uniform vec2 softness;
                                        uniform vec2 thickness;
                                                       \s
                                        float roundedBox(vec2 center, vec2 size, vec4 radius) {
                                            radius.xy = (center.x > 0.0) ? radius.xy : radius.zw;
                                            radius.x  = (center.y > 0.0) ? radius.x : radius.y;
                                                       \s
                                            vec2 q = abs(center) - size + radius.x;
                                            return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - radius.x;
                                        }
                                                       \s
                                        vec4 createGradient(vec2 pos) {
                                            return mix(mix(color1, color2, pos.y), mix(color3, color4, pos.y), pos.x);
                                        }
                                                       \s
                                        void main() {
                                            vec2 tex = (abs(gl_TexCoord[0].st - 0.5) + 0.5) * size;
                                                       \s
                                            float distance = roundedBox(tex - (size / 2.0), (size / 2.0) - value, round);
                                                       \s
                                            float smoothedAlpha = (1.0 - smoothstep(smoothness.x, smoothness.y, distance));
                                            float smoothedborderAlpha = (1.0 - smoothstep(softness.x, softness.y, distance));
                                            float borderAlpha = (1.0 - smoothstep(thickness.x, thickness.y, abs(distance)));
                                           \s
                                            vec4 gradient = createGradient(gl_TexCoord[0].st);
                        
                                            if (smoothedAlpha > 0.5) {
                                                gl_FragColor = vec4(gradient.rgb, gradient.a * smoothedAlpha * borderAlpha);
                                            } else {
                                                gl_FragColor = vec4(gradient.rgb, gradient.a * smoothedborderAlpha * borderAlpha);
                                            }
                                        }"""
        );

        OUTLINE_ROUND_SHADER = new ShaderRegister("""
                #version 120
                
                uniform vec2 size;
                uniform float radius;
                uniform float borderSize;
                uniform vec4 color;
                
                void main(void)
                {
                    vec2 position = (abs(gl_TexCoord[0].st - 0.5) + 0.5) * size;
                    float distance = length(max(position - size + radius + borderSize, 0.0)) - radius + 0.5;
                
                    float alpha = (radius == 0.0) ? 1.0 : color.a * (smoothstep(0.0, 1.0, distance) - smoothstep(0.0, 1.0, distance - borderSize));
                
                    gl_FragColor = vec4(color.rgb, alpha);
                }""");

        ROUND_SHADER = new ShaderRegister("""
                #version 120
                
                uniform vec2 size;
                uniform vec4 color1, color2, color3, color4;
                uniform float radius;
                uniform float noise;
                
                float calcLength(vec2 p, vec2 b, float r) {
                    return length(max(abs(p) - b , 0)) - r;
                }
                
                vec3 createGradient(vec2 coords, vec3 color1, vec3 color2, vec3 color3, vec3 color4){
                    vec3 color = mix(mix(color1.rgb, color2.rgb, coords.y), mix(color3.rgb, color4.rgb, coords.y), coords.x);
                    color += mix(noise, -noise, fract(sin(dot(coords.xy, vec2(12.9898, 78.233))) * 43758.5453));
                    return color;
                }
                
                void main() {
                    vec2 halfSize = size * .5;
                
                    float smoothedAlpha =  (1 - smoothstep(0, 2, calcLength(halfSize - (gl_TexCoord[0].xy * size), halfSize - radius - 1, radius))) * color1.a;
                    gl_FragColor = vec4(createGradient(gl_TexCoord[0].xy, color1.rgb, color2.rgb, color3.rgb, color4.rgb), smoothedAlpha);
                }        \s
                """);

        /*
                #version 120

                uniform vec2 size;
                uniform vec4 color1, color2, color3, color4;
                uniform float radius;
                uniform float noise;

                float calcLength(vec2 p, vec2 b, float r) {
                    return length(max(abs(p) - b , 0)) - r;
                }

                vec4 createGradient(vec2 coords, vec4 color1, vec4 color2, vec4 color3, vec4 color4) {
                    return mix(mix(color1, color2, coords.y), mix(color3, color4, coords.y), coords.x);
                }

                void main() {
                    vec2 halfSize = size * .5;

                    float smoothedAlpha = (1 - smoothstep(0, 2, calcLength(halfSize - (gl_TexCoord[0].xy * size), halfSize - radius - 1, radius)));
                    vec4 clr = createGradient(gl_TexCoord[0].xy, color1, color2, color3, color4);

                    smoothedAlpha = min(max(smoothedAlpha, 0.0), 1.0);
                    clr.a = min(max(clr.a, 0.0), 1.0);
                    clr.a *= smoothedAlpha;

                    gl_FragColor = clr;
                }
        */


        GLOW_SHADER = new ShaderRegister("""
                    #version 120
                
                    uniform vec2 size;
                    uniform vec4 color1, color2, color3, color4;
                    uniform float radius;
                    uniform float shadow;
                    uniform float noise;
                
                    float calcLength(vec2 p, vec2 b, float r) {
                        return length(max(abs(p) - b, 0.0)) - r;
                    }
                
                    vec3 createGradient(vec2 coords, vec3 color1, vec3 color2, vec3 color3, vec3 color4) {
                        vec3 color = mix(mix(color1.rgb, color2.rgb, coords.y), mix(color3.rgb, color4.rgb, coords.y), coords.x);
                        color += mix(noise, -noise, fract(sin(dot(coords.xy, vec2(12.9898, 78.233))) * 43758.5453));
                        return color;
                    }
                
                    void main() {
                        vec2 halfSize = size * 0.5;
                        float distance = calcLength(halfSize - (gl_TexCoord[0].st * size), halfSize - (shadow + (shadow * 0.75)), radius);
                
                        float smoothedAlpha = (1. - smoothstep(-shadow, shadow, distance)) * color1.a;
                
                        vec3 finalColor = createGradient(gl_TexCoord[0].xy, color1.rgb, color2.rgb, color3.rgb, color4.rgb);
                
                        gl_FragColor = mix(vec4(finalColor, 0.0), vec4(finalColor, smoothedAlpha), smoothedAlpha);
                    }
                """);

        FONT_RENDER = new ShaderRegister(""" 
                #version 120
                
                uniform sampler2D font;
                uniform vec4 inColor;
                uniform float width;    
                uniform float maxWidth;
                
                void main() {
                    float f = clamp(smoothstep(0.5, 1, 1 - (gl_FragCoord.x - maxWidth) / width), 0, 1);
                    vec2 pos = gl_TexCoord[0].xy;
                    vec4 color = texture2D(font, pos);
                    if(color.a > 0) {
                        color.a = color.a * f;
                    }
                    gl_FragColor = color * inColor;
                }
                """);

        TEXTURE_ROUND_SHADER = new ShaderRegister("""
                uniform vec2 rectSize;
                uniform sampler2D textureIn;
                uniform float radius, alpha;
                
                float roundedSDF(vec2 centerPos, vec2 size, float radius) {
                    return length(max(abs(centerPos) - size, 0.)) - radius;
                }
                
                void main() {
                    float distance = roundedSDF((rectSize * .5) - (gl_TexCoord[0].st * rectSize), (rectSize * .5) - radius - 1., radius);
                    float smoothedAlpha = (1.0 - smoothstep(0.0, 2.0, distance)) * alpha;
                
                    gl_FragColor = vec4(texture2D(textureIn, gl_TexCoord[0].st).rgb, smoothedAlpha);
                }
                """);

        HEAD_SHADER = new ShaderRegister(""" 
                #version 120
               
                 uniform sampler2D texture;
                 uniform float width;
                 uniform float height;
                 uniform float radius;
                 uniform float hurtStrength;
                 uniform float alpha;
                
                 float dstfn(vec2 p, vec2 b, float r) {
                     return length(max(abs(p) - b, 0.)) - r;
                 }
                
                 void main() {
                     vec2 tex = gl_TexCoord[0].st;
                     vec2 clippedTexCoord = vec2(
                            mix(8.0 / 64.0, 16.0 / 64.0, tex.x),
                            mix(8.0 / 64.0, 16.0 / 64.0, tex.y)
                     );
                     vec4 smpl = texture2D(texture, clippedTexCoord);
                     vec2 size = vec2(width, height);
                     vec2 pixel = tex * size;
                     vec2 centre = .5 * size;
                     float sa = smoothstep(0., 1, dstfn(centre - pixel, centre - radius - 1, radius));
                     vec4 c = mix(vec4(smpl.rgb, 1), vec4(smpl.rgb, 0), sa);
                     gl_FragColor = vec4(mix(smpl.rgb, vec3(1.0, 0.0, 0.0), hurtStrength), c.a * alpha);
                 }
                """);
    }

    public void drawQuads(MatrixStack matrixStack) {
        drawQuads(matrixStack, 0, 0, mw.getScaledWidth(), mw.getScaledHeight());
    }

    public void drawQuads(MatrixStack matrixStack, double x, double y, double width, double height) {
        BUFFER.begin(GL_POLYGON, POSITION_TEX);
        {
            Matrix4f matrix = matrixStack.getLast().getMatrix();
            BUFFER.pos(matrix, (float) x, (float) y).tex(0, 0).endVertex();
            BUFFER.pos(matrix, (float) x, (float) (y + height)).tex(0, 1).endVertex();
            BUFFER.pos(matrix, (float) (x + width), (float) (y + height)).tex(1, 1).endVertex();
            BUFFER.pos(matrix, (float) (x + width), (float) y).tex(1, 0).endVertex();
        }
        TESSELLATOR.draw();
    }

    public void begin() {
        glUseProgram(shaderID);
    }

    public void end() {
        glUseProgram(0);
    }

    public void setUniformi(String name, int... args) {
        int loc = glGetUniformLocation(shaderID, name);
        switch (args.length) {
            case 1 -> glUniform1iARB(loc, args[0]);
            case 2 -> glUniform2iARB(loc, args[0], args[1]);
            case 3 -> glUniform3iARB(loc, args[0], args[1], args[2]);
            case 4 -> glUniform4iARB(loc, args[0], args[1], args[2], args[3]);
        }
    }

    public void setUniform(String name, float... args) {
        int loc = glGetUniformLocation(shaderID, name);
        switch (args.length) {
            case 1 -> glUniform1fARB(loc, args[0]);
            case 2 -> glUniform2fARB(loc, args[0], args[1]);
            case 3 -> glUniform3fARB(loc, args[0], args[1], args[2]);
            case 4 -> glUniform4fARB(loc, args[0], args[1], args[2], args[3]);
        }
    }

    private int createShader(String fragmentShader) {
        int shader = glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(shader, FileUtil.readInputStream(new ByteArrayInputStream(fragmentShader.getBytes())));
        glCompileShader(shader);
        return shader;
    }

    public static Framebuffer createFrameBuffer(Framebuffer framebuffer) {
        return createFrameBuffer(framebuffer, false);
    }

    public static Framebuffer createFrameBuffer(Framebuffer framebuffer, boolean depth) {
        if (needsNewFramebuffer(framebuffer)) {
            if (framebuffer != null) {
                framebuffer.deleteFramebuffer();
            }
            return new Framebuffer(mw.getFramebufferWidth(), mw.getFramebufferHeight(), depth);
        }
        return framebuffer;
    }

    public static boolean needsNewFramebuffer(Framebuffer framebuffer) {
        return framebuffer == null || framebuffer.framebufferWidth != mw.getFramebufferWidth() || framebuffer.framebufferHeight != mw.getFramebufferHeight();
    }
}