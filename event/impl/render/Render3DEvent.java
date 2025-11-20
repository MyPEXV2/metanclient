package relake.event.impl.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.math.vector.Matrix4f;
import relake.event.Event;

@Setter
@Getter
@AllArgsConstructor
public class Render3DEvent extends Event {
    private WorldRenderer context;
    private MatrixStack matrix;
    private Matrix4f projectionMatrix;
    private ActiveRenderInfo activeRenderInfo;
    private float partialTicks;
    private long finishTimeNano;
    private LightTexture lightTexture;

    public static class PreHand extends Render3DEvent {
        public PreHand(WorldRenderer context, MatrixStack matrix, Matrix4f projectionMatrix, ActiveRenderInfo activeRenderInfo, float partialTicks, long finishTimeNano, LightTexture lightTexture) {
            super(context, matrix, projectionMatrix, activeRenderInfo, partialTicks, finishTimeNano, lightTexture);
        }
    }

    public static class PostHand extends Render3DEvent {
        public PostHand(WorldRenderer context, MatrixStack matrix, Matrix4f projectionMatrix, ActiveRenderInfo activeRenderInfo, float partialTicks, long finishTimeNano, LightTexture lightTexture) {
            super(context, matrix, projectionMatrix, activeRenderInfo, partialTicks, finishTimeNano, lightTexture);
        }
    }

    public static class PreWorld extends Render3DEvent {
        public PreWorld(WorldRenderer context, MatrixStack matrix, Matrix4f projectionMatrix, ActiveRenderInfo activeRenderInfo, float partialTicks, long finishTimeNano, LightTexture lightTexture) {
            super(context, matrix, projectionMatrix, activeRenderInfo, partialTicks, finishTimeNano, lightTexture);
        }
    }

    public static class PostWorld extends Render3DEvent {
        public PostWorld(WorldRenderer context, MatrixStack matrix, Matrix4f projectionMatrix, ActiveRenderInfo activeRenderInfo, float partialTicks, long finishTimeNano, LightTexture lightTexture) {
            super(context, matrix, projectionMatrix, activeRenderInfo, partialTicks, finishTimeNano, lightTexture);
        }
    }
}