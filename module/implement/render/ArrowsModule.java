package relake.module.implement.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import relake.Client;
import relake.animation.excellent.Animation;
import relake.animation.excellent.util.Easings;
import relake.common.util.ColorUtil;
import relake.common.util.MoveUtil;
import relake.event.EventHandler;
import relake.event.impl.render.ScreenRenderEvent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.FloatSetting;

import java.awt.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ArrowsModule extends Module {

    public final Setting<Float> arrowsDistance = new FloatSetting("Дистанция от прицела")
            .range(1.0f, 50.0F)
            .setValue(25F);


    public final Setting<Float> arrowScale = new FloatSetting("Размер стрелки")
            .range(1.0f, 1.5f)
            .setValue(1.0f);

    public ArrowsModule() {
        super("Arrows", "Показывает стрелки вокруг прицела в сторону окружающих игроков", "Shows the arrows around the scope towards the surrounding players", ModuleCategory.Render);
        registerComponent(arrowsDistance);
        registerComponent(arrowScale);
    }

    private final Animation animationStep = new Animation();
    private final Animation animatedYaw = new Animation();
    private final Animation animatedPitch = new Animation();
    private final Animation animation = new Animation();

    private final List<Arrow> playerList = new ArrayList<>();

    @EventHandler
    public void screenRender(ScreenRenderEvent event) {
        animationStep.update();
        animatedYaw.update();
        animatedPitch.update();
        animation.update();

        float size = 45 + arrowsDistance.getValue();

        if (mc.currentScreen instanceof InventoryScreen) size += 80;

        if (mc.player.isSneaking()) size -= 20;

        if (MoveUtil.isMoving()) size += 10;

        animatedYaw.run(mc.player.moveStrafing * 5, 0.75, Easings.EXPO_OUT);
        animatedPitch.run(mc.player.moveForward * 5, 0.75, Easings.EXPO_OUT);
        animation.run(mc.gameRenderer.getActiveRenderInfo().getYaw(), 0.75, Easings.EXPO_OUT, true);
        animationStep.run(size, 1, Easings.EXPO_OUT, false);

        List<Arrow> players = new ArrayList<>();

        for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
            Optional<Arrow> arrowConsumer = playerList.stream().filter(a -> a.getPlayer() == player).findFirst();

            if (!arrowConsumer.isEmpty()
                    && !isValidPlayer(player))
                continue;

            Arrow arrow = new Arrow(player, arrowConsumer.map(Arrow::getAnimation).orElse(new relake.animation.tenacity.Animation(1, Duration.ofMillis(200))));
            players.add(arrow);
        }

        List<Arrow> arrows = new ArrayList<>(playerList.stream().toList());
        arrows.removeIf(p -> players.stream().anyMatch(p2 -> p.getPlayer() == p2.getPlayer()));

        for (Arrow arrow : arrows) {
            arrow.getAnimation().switchDirection(false);

            if (!arrow.getAnimation().isDone())
                players.add(arrow);
        }

        playerList.clear();
        playerList.addAll(players);

        if (mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON) {
            for (int i = 0; i < playerList.size(); i++) {
                Arrow arrow = playerList.get(i);

                PlayerEntity player = arrow.player;

                if (!isValidPlayer(player))
                    continue;

                double x = player.lastTickPosX + (player.getPosX() - player.lastTickPosX) * mc.getRenderPartialTicks()
                        - mc.getRenderManager().info.getProjectedView().getX();
                double z = player.lastTickPosZ + (player.getPosZ() - player.lastTickPosZ) * mc.getRenderPartialTicks()
                        - mc.getRenderManager().info.getProjectedView().getZ();

                double cos = MathHelper.cos((float) (animation.getValue() * (Math.PI * 2 / 360)));
                double sin = MathHelper.sin((float) (animation.getValue() * (Math.PI * 2 / 360)));
                double rotY = -(z * cos - x * sin);
                double rotX = -(x * cos + z * sin);

                float angle = (float) (Math.atan2(rotY, rotX) * 180 / Math.PI);

                double x2 = animationStep.getValue() * arrow.animation.get() * MathHelper.cos((float) Math.toRadians(angle)) + mc.getMainWindow().getScaledWidth() / 2f;
                double y2 = animationStep.getValue() * arrow.animation.get() * MathHelper.sin((float) Math.toRadians(angle)) + mc.getMainWindow().getScaledHeight() / 2f;

                x2 += animatedYaw.getValue();
                y2 += animatedPitch.getValue();

                GlStateManager.pushMatrix();
                GlStateManager.disableBlend();
                GlStateManager.translated(x2, y2, 0);
                GlStateManager.rotatef(angle, 0f, 0f, 1f);
                GlStateManager.rotatef(90F, 0F, 0F, 1F);

                int rgb = Client.instance.moduleManager.hudModule.color.getValue().getRGB();

                int color = Client.instance.friendManager.isFriend(player.getNotHidedName().getString()) ? Color.GREEN.getRGB() :
                        ColorUtil.applyOpacity(rgb, arrow.animation.get() * 255);

                drawArrow(event.getMatrixStack(), 1F, -5.5F, 17, color, arrowScale.getValue());


                GlStateManager.enableBlend();
                GlStateManager.popMatrix();
            }
        }
    }

    private void drawArrow(MatrixStack matrixStack, float f, float f2, float f3, int n, float scale) {
        RenderSystem.pushMatrix();
        RenderSystem.disableLighting();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.shadeModel(7425);
        RenderSystem.disableCull();
        RenderSystem.disableAlphaTest();
        RenderSystem.blendFuncSeparate(770, 1, 0, 1);
        Matrix4f matrix4f = matrixStack.getLast().getMatrix();
        mc.getTextureManager().bindTexture(new ResourceLocation("relake/arrow.png"));

        float scaledSize = f3 * scale; // Применение масштаба

        BUFFER.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP);
        BUFFER.pos(matrix4f, f - scaledSize / 2.0f, f2 + scaledSize, 0.0f).color(n).tex(0.0f, 0.99f).lightmap(0, 240).endVertex();
        BUFFER.pos(matrix4f, f + scaledSize / 2.0f, f2 + scaledSize, 0.0f).color(n).tex(1.0f, 0.99f).lightmap(0, 240).endVertex();
        BUFFER.pos(matrix4f, f + scaledSize / 2.0f, f2, 0.0f).color(n).tex(1.0f, 0.0f).lightmap(0, 240).endVertex();
        BUFFER.pos(matrix4f, f - scaledSize / 2.0f, f2, 0.0f).color(n).tex(0.0f, 0.0f).lightmap(0, 240).endVertex();
        TESSELLATOR.draw();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.enableAlphaTest();
        RenderSystem.depthMask(true);
        RenderSystem.popMatrix();
    }

    private boolean isValidPlayer(PlayerEntity player) {
        return player != mc.player;
    }

    private class Arrow {
        private final PlayerEntity player;
        private final relake.animation.tenacity.Animation animation;

        public Arrow(PlayerEntity player, relake.animation.tenacity.Animation animation) {
            this.player = player;
            this.animation = animation;
        }

        public PlayerEntity getPlayer() {
            return player;
        }

        public relake.animation.tenacity.Animation getAnimation() {
            return animation;
        }
    }
}
