package relake.render.display.bqrender;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Cleanup;
import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.util.math.vector.Matrix4f;
import org.byteq.scannable.IShader;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;
import relake.common.InstanceAccess;
import relake.render.display.bqrender.impl.*;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL20.*;

@SuppressWarnings({"UnusedReturnValue", "unused"})
@Getter
public class Shaders implements InstanceAccess {
    private final int programID;
    private static final IShader vertex = new VertexGlsl();
    public static Shaders stencilShader;
    public static Shaders kawaseUp;
    public static Shaders kawaseDown;
    public static Shaders kawaseUpBloom;
    public static Shaders kawaseDownBloom;

    public static void loadShaders() {
        stencilShader = create(new StencilGlsl());
        kawaseUp = create(new KawaseBlurUp());
        kawaseDown = create(new KawaseBlurDown());
        kawaseUpBloom = create(new KawaseBloomUp());
        kawaseDownBloom = create(new KawaseBloomDown());
    }

    private Shaders(IShader fragmentShaderLoc, IShader vertexShaderLoc) {
        int program = glCreateProgram();
        int fragmentShaderID = createShader(new ByteArrayInputStream(fragmentShaderLoc.shader().getBytes()), GL_FRAGMENT_SHADER);
        GL20.glAttachShader(program, fragmentShaderID);
        int vertexShaderID = createShader(new ByteArrayInputStream(vertexShaderLoc.shader().getBytes()), GL_VERTEX_SHADER);
        GL20.glAttachShader(program, vertexShaderID);
        GL20.glLinkProgram(program);
        int status = glGetProgrami(program, GL_LINK_STATUS);
        if (status == 0) throw new IllegalStateException("Shader creation failed");
        this.programID = program;
    }

    public static Shaders create(IShader shader) {
        return new Shaders(shader, vertex);
    }

    public static Shaders create(IShader fragShader, IShader vertexShader) {
        return new Shaders(fragShader, vertexShader);
    }


    private int createShader(InputStream inputStream, int shaderType) {
        int shader = glCreateShader(shaderType);
        glShaderSource(shader, readInputStream(inputStream));
        glCompileShader(shader);
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == 0) {
            System.out.println(glGetShaderInfoLog(shader, 4096));
            throw new IllegalStateException(String.format("Shader (%s) failed to compile", shaderType));
        }
        return shader;
    }

    public void load() {
        glUseProgram(programID);
    }

    public void unload() {
        glUseProgram(0);
    }

    public int getUniform(String name) {
        return glGetUniformLocation(programID, name);
    }

    public Shaders setUniformf(String name, float... args) {
        int loc = glGetUniformLocation(programID, name);
        switch (args.length) {
            case 1 -> glUniform1f(loc, args[0]);
            case 2 -> glUniform2f(loc, args[0], args[1]);
            case 3 -> glUniform3f(loc, args[0], args[1], args[2]);
            case 4 -> glUniform4f(loc, args[0], args[1], args[2], args[3]);
        }
        return this;
    }

    public Shaders setUniformi(String name, int... args) {
        int loc = glGetUniformLocation(programID, name);
        switch (args.length) {
            case 1 -> glUniform1i(loc, args[0]);
            case 2 -> glUniform2i(loc, args[0], args[1]);
            case 3 -> glUniform3i(loc, args[0], args[1], args[2]);
            case 4 -> glUniform4i(loc, args[0], args[1], args[2], args[3]);
        }
        return this;
    }

    public Shaders setMat4fv(String name, FloatBuffer matrix) {
        int loc = glGetUniformLocation(programID, name);
        glUniformMatrix4fv(loc, false, matrix);
        return this;
    }

    public Shaders setMat4fv(String name, float[] matrix) {
        int loc = glGetUniformLocation(programID, name);
        glUniformMatrix4fv(loc, false, matrix);
        return this;
    }

    public Shaders setMat4fv(String name, MatrixStack matrix) {
        setMat4fv(name, matrix.getLast().getMatrix());
        return this;
    }

    public Shaders setMat4fv(String name, Matrix4f matrix) {
        FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(16);
        matrix.write(floatBuffer);
        setMat4fv(name, floatBuffer);
        return this;
    }

    @SneakyThrows
    private String readInputStream(InputStream inputStream) {
        @Cleanup BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        return reader.lines().collect(Collectors.joining("\n"));
    }
}