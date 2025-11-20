package relake.module.implement.render;

import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import org.byteq.scannable.renderer.ScannerRenderer;
import org.lwjgl.opengl.GL11;
import relake.Client;
import relake.animation.excellent.util.Easings;
import relake.common.component.distant_charm.UCharmSFX;
import relake.common.component.distant_charm.UDeathMemoryUpdater;
import relake.common.component.distant_charm.UParticleEffectUpdateAndRenderer;
import relake.common.util.ChatUtil;
import relake.common.util.ColorUtil;
import relake.common.util.MathUtil;
import relake.common.util.StopWatch;
import relake.event.EventHandler;
import relake.event.impl.misc.TickEvent;
import relake.event.impl.player.AttackEvent;
import relake.event.impl.player.DeathEvent;
import relake.event.impl.player.PlayerEvent;
import relake.event.impl.render.Render3DEvent;
import relake.event.impl.render.ScreenRenderEvent;
import relake.event.impl.render.WorldRenderEvent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.render.display.Render2D;
import relake.render.display.shape.ShapeRenderer;
import java.awt.*;
import java.util.Objects;

public class FragEffectsModule extends Module {
    public int c1() {return Color.decode("#AA75FF").getRGB();}
    public int c2() {return Color.decode("#FF00D4").getRGB();}
    public int c3() {return Color.decode("#4B00FF").getRGB();}
    public int c4() {return Color.decode("#8500FF").getRGB();}
    public int c5() {return Color.decode("#FF0000").getRGB();}
    public int c6() {return Color.decode("#FFFFFF").getRGB();}
    public int c7() {return Color.decode("#A900FF").getRGB();}
    public int c8() {return Color.decode("#2700FF").getRGB();}
    private UCharmSFX charmSFX;
    private UDeathMemoryUpdater deathMemoryUpdater;
    private UParticleEffectUpdateAndRenderer particles;
    private ResourceLocation vignetteTexture1280x720px;
    public FragEffectsModule() {
        super("Frag Effects", "Отображает невероятно эпичные эффекты при убийствах", "Displays incredibly epic effects when killing", ModuleCategory.Render);
        this.charmSFX = UCharmSFX.create();
        this.deathMemoryUpdater = UDeathMemoryUpdater.create(950L, () -> new Runnable() {
            @Override
            public void run() {doCodeOnTargetedDeath();}
        }, () -> rotateToKilled());
        this.particles = UParticleEffectUpdateAndRenderer.create();
        this.vignetteTexture1280x720px = new ResourceLocation("relake/lightarroundscreen.png");
    }

    private void drawVignetteTextureIfEffectMoment(ScreenRenderEvent event) {
        final float effectPC = this.getEffectPC();
        if (effectPC == 0.F) return;
        float alphaPC = (float) Easings.QUINT_OUT.ease(effectPC);
        float scalePC = Math.min((alphaPC > .5F ? 1.F - alphaPC : alphaPC) * 4.25F, 1.F);

        final float w = mc.getMainWindow().getScaledWidth(), h = mc.getMainWindow().getScaledHeight();
        final float deltaEXT = 2.F;
        final float extPCX = w / deltaEXT * (1.F - scalePC), extPCY = h / deltaEXT * (1.F - scalePC);
        final float x = -extPCX, y = -extPCY, x2 = w + extPCX, y2 = h + extPCY;
        final int colorTex = ColorUtil.multAlpha(ColorUtil.getOverallColorFrom(ColorUtil.getColor(255, 60, 60), ColorUtil.getColor(255, 255, 255), effectPC), alphaPC);

        //draws
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        final ShapeRenderer box = Render2D.box(event.getMatrixStack(), x, y, x2 - x, y2 - y);
        for (int i = 0; i < 4; i++) box.texture(this.vignetteTexture1280x720px, new int[] {colorTex, colorTex, colorTex, colorTex}, true);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
    }

    private boolean mobDetect() {return true;}
    private boolean rotateToKilled() {return true;}

    @EventHandler
    public void onRendering(Render3DEvent.PostWorld event) {
        ScannerRenderer.render(event.getMatrix(), event.getProjectionMatrix());
    }

    @EventHandler
    public void onRendering(WorldRenderEvent event) {
        this.particles.renderParticles(event, BUFFER, TESSELLATOR);
    }

    @EventHandler
    public void renderThread(ScreenRenderEvent event) {
        this.drawVignetteTextureIfEffectMoment(event);
        charmSFX.inRenderThreadUpdate();
    }

    private boolean isWasTargeted(LivingEntity living) {
        return living == Client.instance.moduleManager.attackAuraModule.getTarget();
    }

    @EventHandler
    public void onPlayerTickPre(PlayerEvent event) {
        if (mc.player == null) return;
        this.updateTickTimerValues();
    }

    @EventHandler
    public void onTickWorld(TickEvent event) {
        if (mc.world == null) return;
        this.particles.updateParticlesList(mc.world);
        if (mc.world.loadedLivingEntityList().isEmpty()) return;
        mc.world.loadedLivingEntityList().stream().filter(Objects::nonNull).filter(living -> isWasTargeted(living)).forEach(living -> this.deathMemoryUpdater.controllingAddingMemoryToEntity(living, mobDetect()));
        this.deathMemoryUpdater.updateAutoMemories();
        this.deathMemoryUpdater.removeAutoMemories();
    }

    @EventHandler
    public void onAttackEntityPre(AttackEvent event) {
        if (event.isPre() && event.getEntity() != null && event.getEntity() instanceof LivingEntity living && living.getHealth() > 0 && living.isAlive()) {
            this.deathMemoryUpdater.controllingAddingMemoryToEntity(living, mobDetect());
        }
    }

    @EventHandler
    public void onDeathEntity(DeathEvent event) {
        if (event.getPlayer() != null) {
            this.deathMemoryUpdater.onContains(event.getPlayer());
        }
    }


    private void initWorldEffectOnUpdate() {

        ScannerRenderer.INSTANCE.ping(this.deathMemoryUpdater.getLastKilledEntity() == null ? mc.gameRenderer.getActiveRenderInfo().getProjectedView() : this.deathMemoryUpdater.getLastKilledEntity().getEyePosition(mc.getRenderPartialTicks()));
        final long offsetLong = 75L;
        this.charmSFX.sfxPulseDistantRAND(0L);
        this.charmSFX.sfxKnockMain(offsetLong);
        this.charmSFX.sfxSparksBlockHit(offsetLong * 2L);
        this.charmSFX.sfxEchoMain(offsetLong * 3L);
        mc.timer.tempSpeed = .07F;
    }

    private boolean animate;
    private StopWatch linearTimer = new StopWatch();
    private long effectMSMax() {return 2800L;}
    private void restartEffect() {
        this.animate = true;
        this.linearTimer.reset();
    }
    private float getEffectPC() {
        final var val = this.animate ? Math.min(this.linearTimer.elapsedTime() / (float)effectMSMax(), 1.F) : 0.F;
        if (val == 1) this.animate = false;
        return val;
    }
    private int ticksForEditTimer;
    private float[] timerValues = new float[0];
    private void refillTimerValues(float... timerValue) {
        this.timerValues = timerValue;
        this.ticksForEditTimer = 0;
    }
    private void updateTickTimerValues() {
        ++this.ticksForEditTimer;
        if (this.ticksForEditTimer < this.timerValues.length) {
            mc.timer.tempSpeed = this.timerValues[this.ticksForEditTimer];
        }
    }

    //ON KILL TARGET, ALL TASKS TICK//
    private void doCodeOnTargetedDeath() {
        this.restartEffect();
        this.initWorldEffectOnUpdate();
        this.refillTimerValues(.125F, .175F, .3F, .65F, .8F);
        //if (this.deathMemoryUpdater.getLastKilledEntity() != null)
        //this.particles.spawnParticlesVoid(this.deathMemoryUpdater.getLastKilledEntity().getEyePosition(mc.getRenderPartialTicks()), 200, 2200, 4, 3500, .005F);
    }

    //MINECRAFT HOOK
    public float getMulCameraZoomValue() {
        if (this.isEnabled() && mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON) {
            float effectPC = Math.min(this.getEffectPC() * 3.F, 1.F);
            boolean stepOf05IsTrue = effectPC > .5F;
            if (effectPC > 0.F) {
                effectPC = (effectPC > .5F ? 1.F - effectPC : effectPC) * 2.F;
                effectPC = (float) (stepOf05IsTrue ? Easings.EXPO_IN : Easings.BACK_IN_OUT).ease(effectPC);
                effectPC = MathUtil.lerp(effectPC, effectPC * effectPC, .3F);
                return 1.F + effectPC / 1.25F;
            }
        }
        return 1.F;
    }
}
