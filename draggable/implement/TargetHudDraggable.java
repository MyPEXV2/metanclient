package relake.draggable.implement;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import relake.Client;
import relake.animation.excellent.Animation;
import relake.animation.excellent.util.Easings;
import relake.common.util.*;
import relake.draggable.Draggable;
import relake.render.display.Render2D;
import relake.render.display.font.FontRegister;
import relake.render.display.shape.ShapeRenderer;
import relake.util.ScaleUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TargetHudDraggable extends Draggable {
    private LivingEntity currentTarget, soundTarget;

    private final Animation subAnimation = new Animation();
    private final Animation healthAnim = new Animation();

    public TargetHudDraggable() {
        super("TargetHud", 100, 100, 180, 55);
    }

    @Override
    public boolean visible() {
        boolean visible = Client.instance.moduleManager.targetHUDModule.isEnabled() && getTarget() != null;
        if (!visible) soundTarget = null;
        return visible;
    }

    @Override
    public void update() {
        final LivingEntity currentTempTarget = getTarget();
        if (currentTempTarget != null) {
            if (soundTarget != currentTempTarget) {
                if (Client.instance.moduleManager.clientSoundsModule.isEnabled() && Client.instance.moduleManager.clientSoundsModule.targets.getValue() && currentTempTarget != mc.player)
                    SoundUtil.playSound("targetselect.wav", .725F);
                soundTarget = currentTempTarget;
            }
        }
        currentTarget = currentTempTarget;
        if (currentTarget != null && currentTarget.hurtTime == 8 && Client.instance.moduleManager.targetHUDModule.particlesBool.getValue())
            addParticles(12, 450L);
    }

    private LivingEntity getTarget() {
        LivingEntity auraTarget = Client.instance.moduleManager.attackAuraModule.getTarget();
        if (auraTarget != null) return auraTarget;
        if (mc.currentScreen instanceof ChatScreen) return mc.player;
        if (mc.pointedEntity instanceof LivingEntity living) return living;
        return null;
    }

    @Override
    public void tick() {
    }

    @Override
    public void render(MatrixStack matrixStack, float partialTicks) {

        int round = 10;

        if (this.updatedAlpha == 0 && !particles.isEmpty()) particles.clear();

        if (Client.instance.moduleManager.targetHUDModule.mode.isSelected("Новый")) {
            if (currentTarget != null && currentTarget.hurtTime < 5)
                subAnimation.update();
            healthAnim.update();
            int rgb = Client.instance.moduleManager.hudModule.color.getValue().getRGB();

            if (currentTarget != null) {
                float barWidth = width - 58;
                float offsetX = 50;
                float offsetY = 30;
                float offsetH = 5;
                float healthBarWidth = (currentTarget.getHealthFixed() / currentTarget.getMaxHealth()) * barWidth;

                subAnimation.run(MathUtil.clamp(healthBarWidth, 0, barWidth), 0.035, Easings.QUAD_IN_OUT, false);
                if (healthAnim.get() > subAnimation.get()) subAnimation.setValue(healthAnim.get());
                healthAnim.run(MathUtil.clamp(healthBarWidth, 0, barWidth), 0.4, Easings.BACK_OUT, false);

                ShapeRenderer box = Render2D.box(matrixStack, x, y, width, height);

                box.quad(round, 0xB70E0E0F);
                box.quad(round, ColorUtil.applyOpacity(rgb, 35), 0xEE0E0E0F, 0xEE0E0E0F, ColorUtil.applyOpacity(rgb, 35));

                ShapeRenderer health = Render2D.box(matrixStack, x + offsetX, y + height - offsetY, barWidth, offsetH);
                health.quad(2, 0x580E0E0F);

                health.box(matrixStack, x + offsetX + 1, y + height - offsetY, (float) subAnimation.getValue(), offsetH);
                health.quad(2, ColorUtil.applyOpacity(rgb, 50));

                health.box(matrixStack, x + offsetX + 1.5f, y + height - offsetY, (float) healthAnim.getValue(), offsetH);
                health.quad(2, rgb, rgb, ColorUtil.darker(rgb, 65), ColorUtil.darker(rgb, 65));
                ShapeRenderer boxOutLine = Render2D.box(matrixStack, x - 2, y - 2, width + 4, height + 4);
                boxOutLine.outlineHud(round, 2, ColorUtil.applyOpacity(rgb, 75), 0xEE0E0E0F, 0xEE0E0E0F, ColorUtil.applyOpacity(rgb, 75));

                float headNewSize = 42.5f;
                float headPadd = 6.f;

                ShapeRenderer head = Render2D.box(matrixStack, x + headPadd, y + headPadd, headNewSize, headNewSize);
                //PARTICLES
                callSpawnParticlePos(head.x + head.width / 2.F, head.y + head.height / 2.F, (int) (headNewSize * 2.25F));
                matrixStack = drawAndUpdateParticles(matrixStack, headNewSize / 2.F, rgb, 1.F);
                ShapeRenderer.drawFace(matrixStack, this.currentTarget, head.x, head.y, (int) head.width, (int) head.height, 5, this.currentTarget.hurtTime / 20F);
                String name = currentTarget.getName().getString();
                Render2D.size(FontRegister.Type.BOLD, 12).string(matrixStack, "HP - " + (int) currentTarget.getHealthFixed(), x + offsetX, y + 32.5f, ColorUtil.applyOpacity(ColorUtil.getColor(200, 200, 200), 255), (int) (65 * getAnimation().get()));
                Render2D.size(FontRegister.Type.BOLD, 13).string(matrixStack, name, x + offsetX, y + 5.5f, ColorUtil.applyOpacity(ColorUtil.getColor(200, 200, 200), 255), (int) (82 * getAnimation().get()));
            }
        }

        float xPadding = 50f;
        float yPadding = 22f;
        float heightOffset = 10;

        if (Client.instance.moduleManager.targetHUDModule.mode.isSelected("Обычный")) {
            if (currentTarget != null && currentTarget.hurtTime < 5) subAnimation.update();
            healthAnim.update();

            int rgb = Client.instance.moduleManager.hudModule.color.getValue().getRGB();

            if (currentTarget != null) {

                float barWidth = width - 60;

                float healthBarWidth = (currentTarget.getHealthFixed() / currentTarget.getMaxHealth()) * barWidth;

                subAnimation.run(MathUtil.clamp(healthBarWidth, 0, barWidth), 0.035, Easings.QUAD_IN_OUT, false);
                if (healthAnim.get() > subAnimation.get()) subAnimation.setValue(healthAnim.get());
                healthAnim.run(MathUtil.clamp(healthBarWidth, 0, barWidth), 0.4, Easings.BACK_OUT, false);
                ShapeRenderer box = Render2D.box(matrixStack, x, y, width, height);


                box.quad(round, 0xB70E0E0F);
                box.quad(round, ColorUtil.applyOpacity(rgb, 35), 0xB70E0E0F, 0xB70E0E0F, ColorUtil.applyOpacity(rgb, 35));

                ShapeRenderer health = Render2D.box(matrixStack, x + xPadding, y + height - yPadding, barWidth, heightOffset);
                health.quad(4, 0x580E0E0F);

                health.box(matrixStack, x + xPadding, y + height - yPadding, (float) subAnimation.getValue(), heightOffset);
                health.quad(4, ColorUtil.applyOpacity(rgb, xPadding));

                health.box(matrixStack, x + xPadding, y + height - yPadding, (float) healthAnim.getValue(), heightOffset);
                health.quad(4, rgb, rgb, ColorUtil.darker(rgb, 25), ColorUtil.darker(rgb, 100));

                ShapeRenderer boxOutLine = Render2D.box(matrixStack, x - 2, y - 2, width + 4, height + 4);
                boxOutLine.outlineHud(round, 2, ColorUtil.applyOpacity(rgb, 75), 0xEE0E0E0F, 0xEE0E0E0F, ColorUtil.applyOpacity(rgb, 75));

                float headDefaultSize = 35f;
                ShapeRenderer head = Render2D.box(matrixStack, x + 10, y + 10, headDefaultSize, headDefaultSize);
                //PARTICLES
                callSpawnParticlePos(head.x + head.width / 2.F, head.y + head.height / 2.F, (int) (headDefaultSize * 2.25F));
                matrixStack = drawAndUpdateParticles(matrixStack, headDefaultSize / 2.F, rgb, 1.F);
                ShapeRenderer.drawFace(matrixStack, this.currentTarget, head.x, head.y, (int) head.width, (int) head.height, 5, this.currentTarget.hurtTime / 20F);
                String name = currentTarget.getName().getString();
                Render2D.size(FontRegister.Type.BOLD, 15).string(matrixStack, name, x + xPadding - 1.5f, y + 10, ColorUtil.applyOpacity(ColorUtil.getColor(200, 200, 200), 255), (int) (80));
            }
        }
//        if (Client.instance.moduleManager.targetHUDModule.mode.isSelected("Кружок")) {
//            if (currentTarget != null && currentTarget.hurtTime < 5) subAnimation.update();
//            healthAnim.update();
//
//            int rgb = Client.instance.moduleManager.hudModule.color.getValue().getRGB();
//
//            if (currentTarget != null) {
//
//                float barWidth = width - 60;
//
//                float healthBarWidth = (currentTarget.getHealthFixed() / currentTarget.getMaxHealth()) * barWidth;
//
//                subAnimation.run(MathUtil.clamp(healthBarWidth, 0, barWidth), 0.035, Easings.QUAD_IN_OUT, false);
//                if (healthAnim.get() > subAnimation.get()) subAnimation.setValue(healthAnim.get());
//                healthAnim.run(MathUtil.clamp(healthBarWidth, 0, barWidth), 0.4, Easings.BACK_OUT, false);
//                ShapeRenderer box = Render2D.box(matrixStack, x, y, width, height);
//
//
//                box.quad(round, 0xB70E0E0F);
//                box.quad(round, ColorUtil.applyOpacity(rgb, 35), 0xB70E0E0F, 0xB70E0E0F, ColorUtil.applyOpacity(rgb, 35));
//
//                ShapeRenderer boxOutLine = Render2D.box(matrixStack, x - 2, y - 2, width + 4, height + 4);
//                boxOutLine.outlineHud(round, 2, ColorUtil.applyOpacity(rgb, 75), 0xEE0E0E0F, 0xEE0E0E0F, ColorUtil.applyOpacity(rgb, 75));
//
//                float headDefaultSize = 48;
//                ShapeRenderer head = Render2D.box(matrixStack, x + 5, y + 3.5f, headDefaultSize, headDefaultSize);
//                //PARTICLES
//                callSpawnParticlePos(head.x + head.width / 2.F, head.y + head.height / 2.F, (int) (headDefaultSize * 2.25F));
//                matrixStack = drawAndUpdateParticles(matrixStack, headDefaultSize / 2.F, rgb, 1.F);
//
//                ShapeRenderer.drawFace(matrixStack, this.currentTarget, head.x, head.y, (int) head.width, (int) head.height, 23, this.currentTarget.hurtTime / 20F);
//
//                String name = currentTarget.getName().getString();
//                Render2D.size(FontRegister.Type.BOLD, 12).string(matrixStack, name, x + xPadding + 6f, y + 17.5f, ColorUtil.applyOpacity(ColorUtil.getColor(200, 200, 200), 255), (int) (85));
//
//                float circleSize = 22;
//                Render2D.duadsCircle(matrixStack,x + width- 9.5f - circleSize,y + height / 2F, circleSize, 359, 5, false, 0xB70E0E0F,(255 * getAnimation().get()));
//                Render2D.duadsCircle(matrixStack,x + width- 9.5f - circleSize,y + height / 2F, circleSize, ((float)subAnimation.getValue() * 359.f) / 175, 3, false, (115 * getAnimation().get()));
//                Render2D.duadsCircle(matrixStack,x + width- 9.5f - circleSize,y + height / 2F, circleSize, ((float)subAnimation.getValue() * 359.f) / 175, 4, false, (25 * getAnimation().get()));
//                Render2D.duadsCircle(matrixStack,x + width- 9.5f - circleSize,y + height / 2F, circleSize, ((float)healthAnim.getValue() * 359.) / 175, 5, false, (25 * getAnimation().get()));
//                Render2D.duadsCircle(matrixStack,x + width- 9.5f - circleSize,y + height / 2F, circleSize, ((float)healthAnim.getValue() * 359.) / 175, 4, false, (200 * getAnimation().get()));
//                Render2D.box(matrixStack, x + width - circleSize - 20,y + height - circleSize - 16.5f, circleSize, circleSize).drawShadow(matrixStack, 15, .2f, rgb);
//                Render2D.size(FontRegister.Type.BOLD, 9).centeredString(matrixStack, (int)currentTarget.getHealthFixed() + "hp", x + width- 12 - circleSize, y + 21, ColorUtil.applyOpacity(ColorUtil.getColor(200, 200, 200), 255), (int) (25 * getAnimation().get()));
//                this.width = 235;
//                this.height = 55;
//            }
//        }

        if (Client.instance.moduleManager.targetHUDModule.mode.isSelected("Современный")) {
            if (currentTarget != null && currentTarget.hurtTime < 5)
                subAnimation.update();
            healthAnim.update();
            int rgb = Client.instance.moduleManager.hudModule.color.getValue().getRGB();

            if (currentTarget != null) {
                int roundSmart = 8;
                int healthRound = 3;
                float barWidth = width - 10;
                float offsetX = 40;
                float offsetY = 54;
                float offsetH = 6;
                float xHealthPadding = -37f;
                float yHealthPadding = 41f;
                float xTextPadding = -1f;


                float healthBarWidth = (currentTarget.getHealthFixed() / currentTarget.getMaxHealth()) * barWidth;

                subAnimation.run(MathUtil.clamp(healthBarWidth, 0, barWidth), 0.035, Easings.QUAD_IN_OUT, false);
                if (healthAnim.get() > subAnimation.get()) subAnimation.setValue(healthAnim.get());
                healthAnim.run(MathUtil.clamp(healthBarWidth, 0, barWidth), 0.4, Easings.BACK_OUT, false);

                ShapeRenderer box = Render2D.box(matrixStack, x, y, width, height);

                //bg
                box.quad(roundSmart, 0xB70E0E0F);
                box.quad(roundSmart, ColorUtil.applyOpacity(rgb, 35), 0xEE0E0E0F, 0xEE0E0E0F, ColorUtil.applyOpacity(rgb, 35));


                //health coords
                ShapeRenderer health = Render2D.box(matrixStack, x + offsetX + xHealthPadding, y + height - offsetY + yHealthPadding, barWidth, offsetH);
                ShapeRenderer healthShadow = Render2D.box(matrixStack, x + offsetX - 5 + xHealthPadding, y + height - offsetY + yHealthPadding, barWidth + 10, offsetH);


                //health bg
                healthShadow.drawShadow(matrixStack, 15, 1, ColorUtil.applyOpacity(rgb, 45));
                health.quad(healthRound, 0x580E0E0F);


                //old health
                health.box(matrixStack, x + offsetX + 1 + xHealthPadding, y + height - offsetY + yHealthPadding, (float) subAnimation.getValue(), offsetH);
                health.quad(healthRound, ColorUtil.applyOpacity(rgb, 50));

                //health
                health.box(matrixStack, x + offsetX + 1.5f + xHealthPadding, y + height - offsetY + yHealthPadding, (float) healthAnim.getValue(), offsetH);
                health.quad(healthRound, rgb, rgb, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 65), -1, .4f), ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 65), -1, .4f));

                ShapeRenderer boxOutLine = Render2D.box(matrixStack, x - 2, y - 2, width + 4, height + 4);
                boxOutLine.outlineHud(round, 2, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 75), -1, 0.35f), 0xEE0E0E0F, 0xEE0E0E0F, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 75), -1, 0.35f));

                ShapeRenderer head = Render2D.box(matrixStack, x + 5, y + 5, 32, 32);
                //PARTICLES
                callSpawnParticlePos(head.x + head.width / 2.F, head.y + head.height / 2.F, (int) (32.F * 2.25F));
                matrixStack = drawAndUpdateParticles(matrixStack, 32.F / 2.F, rgb, 1.F);
                ShapeRenderer.drawFace(matrixStack, this.currentTarget, head.x, head.y, (int) head.width, (int) head.height, 5, this.currentTarget.hurtTime / 20F);
                String name = currentTarget.getName().getString();
                Render2D.size(FontRegister.Type.BOLD, 11).string(matrixStack, "HP - " + (int) currentTarget.getHealthFixed(), x + offsetX + xTextPadding, y + 21, ColorUtil.applyOpacity(ColorUtil.getColor(200, 200, 200), 255), (int) (65 * getAnimation().get()));
                Render2D.size(FontRegister.Type.BOLD, 15).string(matrixStack, name, x + offsetX + xTextPadding, y + 4, ColorUtil.applyOpacity(ColorUtil.getColor(200, 200, 200), 255), (int) (90 * getAnimation().get()));
            }
        }
    }

    public List<Particle> particles = new ArrayList<>();
    private final Random random = new Random();
    private final ResourceLocation particleTexture = new ResourceLocation("relake/point.png");

    private class Particle {
        public final float x, y, xOff, yOff;
        private final float maxTime;
        private final int rotateTo = (random.nextInt(2) == 1 ? 1 : -1) * random.nextInt(1440);
        private final StopWatch outTime = new StopWatch();

        private Particle(float x, float y, float randXYMax, float maxTime) {
            outTime.reset();
            this.x = x;
            this.y = y;
            float dst = (.3333F + (float) Math.random() * .6666F) * randXYMax;
            float rad = (float) Math.toRadians(Math.random() * 360.F);
            xOff = (float) Math.sin(rad) * dst;
            yOff = (float) -Math.cos(rad) * dst;
            this.maxTime = maxTime;
        }

        public float getTimePC() {
            return (float) Easings.QUAD_OUT.ease(Math.min(outTime.elapsedTime() / maxTime, 1.F));
        }

        public float getAlphaPC(float timePC) {
            return (timePC > .5F ? 1.F - timePC : timePC) * 2.F;
        }

        public float[] getPos(float timePC) {
            return new float[]{this.x + xOff * timePC, this.y + yOff * timePC};
        }

        public float getRotateAngle(float timePC) {
            return rotateTo * (float) Easings.CUBIC_IN_OUT.ease(timePC);
        }

        public int getColorIndex() {
            return rotateTo * 3;
        }

        public boolean isToRemove(float timePC) {
            return timePC == 1.F;
        }

        public void drawParticle(MatrixStack stack, int color, float timePC, float size, float alphaPC) {
            final float[] xy = getPos(timePC);
            final float rotate = getRotateAngle(timePC);
            size *= alphaPC;
            size /= 2.F;
            stack.push();
            stack.translate(xy[0], xy[1], 0);
            stack.rotate(Vector3f.ZP.rotationDegrees(rotate));
            stack.translate(-xy[0], -xy[1], 0);
            ShapeRenderer box = Render2D.box(stack, xy[0] - size, xy[1] - size, size * 2.F, size * 2.F);
            box.texture(particleTexture, color);
            stack.pop();
        }
    }

    private MatrixStack drawAndUpdateParticles(MatrixStack stack, float drawScalePix, /**/ int baseColor /**/, float alphaPC) {
        particles.removeIf(particle -> particle.isToRemove(particle.getTimePC()));
        if (particles.isEmpty()) return stack;
        stack.push();
        stack.scaleFix(1, 1, 1);
        for (Particle particle : particles) {
            float timePC = particle.getTimePC(), aPC = particle.getAlphaPC(timePC) * alphaPC;
            int color = ColorUtil.multAlpha(baseColor, aPC);//particle.getColorIndex()
            particle.drawParticle(stack, color, timePC, drawScalePix, alphaPC);
        }
        stack.pop();
        return stack;
    }

    private float tempSpawnXParts = Float.MIN_VALUE, tempSpawnYParts = Float.MIN_VALUE, tempSpawnRangeParts = Float.MIN_VALUE;

    private void callSpawnParticlePos(float x, float y, float range) {
        tempSpawnXParts = x;
        tempSpawnYParts = y;
        tempSpawnRangeParts = range;
    }

    private void addParticles(int addCount, long lifeTime) {
        for (int i = 0; i < addCount; ++i)
            particles.add(new Particle(tempSpawnXParts, tempSpawnYParts, tempSpawnRangeParts, lifeTime));
    }
}
