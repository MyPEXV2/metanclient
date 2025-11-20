package relake.common.component.distant_charm;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import relake.common.util.ChatUtil;
import relake.common.util.MathUtil;
import relake.event.impl.render.WorldRenderEvent;
import relake.module.implement.render.ParticlesModule;
import relake.render.world.Render3D;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.lwjgl.opengl.GL11.GL_SMOOTH;

public class UParticleEffectUpdateAndRenderer {
    private final List<UParticle> particles;
    private final Random RAND;
    private UParticleEffectUpdateAndRenderer() {
        this.particles = new ArrayList<>();
        this.RAND = new Random();
    }
    public static UParticleEffectUpdateAndRenderer create() {
        return new UParticleEffectUpdateAndRenderer();
    }

    public void spawnParticlesVoid(Vector3d mainPos, int lifeTimeMin, int lifeTimeMax, float maxFlight, int count, float gravity) {
        float maxXZMotion = maxFlight / (lifeTimeMin / 50.F) * 1.12F, maxYMotion = maxFlight / (lifeTimeMin / 50.F) * .31F;
        for (int i = 0; i < count; i++) particles.add(new UParticle(mainPos, MathUtil.lerp(lifeTimeMin, lifeTimeMax, this.RAND.nextFloat()), maxXZMotion, maxYMotion, gravity));
    }

    public void updateParticlesList(World worldIn) {
        if (this.particles.isEmpty()) return;
        this.particles.removeIf(UParticle::removeIf);
        this.particles.forEach(UParticle::updateMotion);
    }

    public void renderParticles(WorldRenderEvent event, BufferBuilder buffer, Tessellator tessellator) {
        if (this.particles.isEmpty()) return;
        final int beginsCount = (int) (40 / .25F);
        final List<UVec3dColored>[] vecsPointBegins = new ArrayList[beginsCount];
        for (int i = 0; i < vecsPointBegins.length; i++) vecsPointBegins[i] = new ArrayList<>();
        Vector3d tempRenderPos;
        for (final UParticle particle : this.particles) {
            tempRenderPos = particle.getRenderPos(event.getTicks());
            vecsPointBegins[particle.getScaledBeginInt(beginsCount)].add(new UVec3dColored(tempRenderPos.getX(), tempRenderPos.getY(), tempRenderPos.getZ(), particle.getColor()));
        }
        final Runnable renderAll = () -> {
            //
            float pointScale = .25F;
            for (final List<UVec3dColored> points : vecsPointBegins) {
                if (!points.isEmpty()) {
                    GL11.glPointSize(pointScale);
                    buffer.begin(GL11.GL_POINTS, DefaultVertexFormats.POSITION_COLOR);
                    points.forEach(vec -> buffer.pos((float) vec.getX(), (float) vec.getY(), (float) vec.getZ()).color(vec.getColor()).endVertex());
                    tessellator.draw();
                }
                pointScale += .25F;
            }
            GL11.glPointSize(1.F);
            //
        };
        //finish draw
        Render3D.setup3dForBlockPos(event, renderAll, false, true);
    }
}
