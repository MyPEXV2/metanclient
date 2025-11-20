package relake.module.implement.render;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;
import relake.Client;
import relake.animation.excellent.util.Easing;
import relake.animation.excellent.util.Easings;
import relake.common.util.ColorUtil;
import relake.common.util.MathUtil;
import relake.event.EventHandler;
import relake.event.impl.misc.TickEvent;
import relake.event.impl.render.WorldRenderEvent;
import relake.menu.ui.components.module.setting.SelectComponent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.render.world.Render3D;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;
import relake.settings.implement.ColorSetting;
import relake.settings.implement.SelectSetting;

import java.awt.*;

public class BlockOverlayModule extends Module {



    private final SelectSetting colorMode = new SelectSetting("Настройка цвета")
            .setValue("По прогрессу",
                    "Пикер",
                    "Клиентский");

    private final Setting<Color> pickColor = new ColorSetting("Пикер цвета").setValue(Color.CYAN).setVisible(() -> colorMode.isSelected("Пикер"));

    public BlockOverlayModule() {
        super("Block Overlay", "Рисует кастомную анимацию ломания блока", "Draws a custom animation of the block breaking", ModuleCategory.Render);
        registerComponent(colorMode, pickColor);
    }

    private boolean isHitting;
    private double prevPosX, prevPosY, prevPosZ;
    private BlockPos targetBlockPos;
    private float curDamageBlock, prevDamageBlock;

    @EventHandler
    public void onTickEvent(TickEvent event) {
        if (mc.player == null || mc.playerController == null || mc.world == null) return;
        this.isHitting = false;
        this.prevDamageBlock = this.curDamageBlock;
        if (mc.playerController.getIsHittingBlock() && mc.playerController.getCurrentBlock() != null) {
            if (this.targetBlockPos != null) {
                this.prevPosX = this.targetBlockPos.getX();
                this.prevPosY = this.targetBlockPos.getY();
                this.prevPosZ = this.targetBlockPos.getZ();
            }
            this.targetBlockPos = mc.playerController.getCurrentBlock();
            if (this.prevPosX == 0.D || this.prevPosY == 0.D || this.prevPosZ == 0.D) {
                this.prevPosX = this.targetBlockPos.getX();
                this.prevPosY = this.targetBlockPos.getY();
                this.prevPosZ = this.targetBlockPos.getZ();
            }
            this.curDamageBlock = mc.playerController.curBlockDamageMP;
            this.isHitting = true;
        } else {
            this.curDamageBlock = 0.F;
            this.prevPosX = 0.D;
            this.prevPosY = 0.D;
            this.prevPosZ = 0.D;
        }
    }
    private AxisAlignedBB getBlockAABB(BlockPos pos, float scaleFactor, float minScale, Easing ease, float partialTicks) {
        final double renderX = MathUtil.lerp(this.prevPosX, pos.getX(), partialTicks), renderY = MathUtil.lerp(this.prevPosY, pos.getY(), partialTicks), renderZ = MathUtil.lerp(this.prevPosZ, pos.getZ(), partialTicks);
        final AxisAlignedBB axisIn = mc.world.getBlockState(pos).getRenderShapeTrue(mc.world, pos).getBoundingBox().offset(renderX, renderY, renderZ);
        final double sX = axisIn.maxX - axisIn.minX, sY = axisIn.maxY - axisIn.minY, sZ = axisIn.maxZ - axisIn.minZ;
        scaleFactor = MathUtil.lerp(minScale, Math.min(scaleFactor + minScale, 1.F), scaleFactor);
        if (ease != null) scaleFactor = (float) ease.ease(scaleFactor);
        final float scaleNegative = 1.F - scaleFactor;
        return axisIn.grow(-sX * scaleNegative / 2.D, -sY * scaleNegative / 2.D, -sZ * scaleNegative / 2.D);
    }

    @EventHandler
    public void onWorldRenderEvent(WorldRenderEvent event) {
        if (!this.isHitting || this.targetBlockPos == null) return;
        final float partialTicks = event.getTicks();
        final float progress = MathUtil.lerp(this.prevDamageBlock, this.curDamageBlock, partialTicks);
        final AxisAlignedBB blockAABB = this.getBlockAABB(this.targetBlockPos, progress, .175F, Easings.QUAD_IN_OUT, partialTicks);
        if (blockAABB == null) return;
        float alphaPC = (progress > .5F ? 1.F - progress : progress) * 2.F;
        alphaPC = Math.min(alphaPC * 3.F, 1.F);
        int baseColor;
        switch (colorMode.getSelected()) {
            case "По прогрессу" -> baseColor = ColorUtil.getOverallColorFrom(ColorUtil.getColor(255, 0, 0), ColorUtil.getColor(0, 255, 0), Math.min((progress - .25F) / .75F, 1.F));
            case "Пикер" -> baseColor = pickColor.getValue().getRGB();
            case "Клиентский" -> baseColor = Client.instance.moduleManager.hudModule.color.getValue().getRGB();
            default -> baseColor = -1;
        }
        final int outlineColor = ColorUtil.multAlpha(baseColor, alphaPC), decussationColor = ColorUtil.multDark(outlineColor, .2F), fullColor = ColorUtil.multDark(outlineColor, .03F);
        Render3D.setup3dForBlockPos(event, () -> {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            Render3D.drawCanisterBox(event.getStack(), BUFFER, TESSELLATOR, blockAABB, outlineColor != 0, decussationColor != 0, fullColor != 0, outlineColor, decussationColor, fullColor);
            GL11.glLineStipple(1, Short.reverseBytes((short) -32));
            GL11.glEnable(GL11.GL_LINE_STIPPLE);
            GL11.glLineWidth(7.F);
            Render3D.drawCanisterBox(event.getStack(), BUFFER, TESSELLATOR, blockAABB, decussationColor != 0, false, false, decussationColor, 0, 0);
            GL11.glDisable(GL11.GL_LINE_STIPPLE);
            GL11.glLineWidth(1.F);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        }, true, true);
    }
}
