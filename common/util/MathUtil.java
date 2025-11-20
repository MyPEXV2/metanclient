package relake.common.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import relake.common.InstanceAccess;

import java.math.BigDecimal;
import java.math.RoundingMode;

@UtilityClass
public class MathUtil implements InstanceAccess {
    public float step(double value, double steps) {
        float roundedValue = (float) (Math.round(value / steps) * steps);
        return (float) (Math.round(roundedValue * 100.0) / 100.0);
    }

    public void scale(MatrixStack matrixStack, float x, float y, float scale) {
        matrixStack.translate(x, y, 0);
        matrixStack.scale(scale, scale, scale);
        matrixStack.translate(-x, -y, 0);
    }

    public static float easeInOutCubic(float x) {
        return x < 0.5 ? 4 * x * x * x : (float) (1 - Math.pow(-2 * x + 2, 3) / 2);
    }

    public void scaleFix(MatrixStack matrixStack, float x, float y, float scale) {
        Minecraft mc = Minecraft.getInstance();
        double factor = mc.getMainWindow().getGuiScaleFactor();

        float s = (float) (scale * factor);

        matrixStack.translate(x / factor, y / factor, 0);
        matrixStack.scaleFix(s, s, s);
        matrixStack.translate(-x / factor, -y / factor, 0);
    }

    public void scaleFix(MatrixStack matrixStack, float x, float y, float xScale, float yScale) {
        Minecraft mc = Minecraft.getInstance();
        double factor = mc.getMainWindow().getGuiScaleFactor();

        xScale *= factor;
        yScale *= factor;

        matrixStack.translate(x / factor, y / factor, 0);
        matrixStack.scaleFix(xScale, yScale, 1F);
        matrixStack.translate(-x / factor, -y / factor, 0);
    }

    public void rotate(MatrixStack matrixStack, float x, float y, float angel) {
        matrixStack.translate(x, y, 0);
        matrixStack.rotate(new Quaternion(Vector3f.XP.rotationDegrees(angel)));
        matrixStack.translate(-x, -y, 0);
    }

    public static double random(double min, double max) {
        return interpolate(max, min, (float) Math.random());
    }

    public static float random2(float min, float max) {
        return (float) (Math.random() * (max - min) + min);
    }

    public boolean isHovered(double mouseX, double mouseY, double x, double y, double width, double height) {
        Minecraft mc = Minecraft.getInstance();
        double factor = mc.getMainWindow().getGuiScaleFactor();

        mouseX = mouseX * factor;
        mouseY = mouseY * factor;

        return mouseX >= x
                && mouseX <= x + width
                && mouseY >= y
                && mouseY <= y + height;
    }

    public double interpolate(double previous, double current, float partialTicks) {
        return current + (previous - current) * (double) partialTicks;
    }

    public float fast(float end, float start, float multiple) {
        return (1 - clamp((float) (deltaTime() * multiple), 0, 1)) * end
                + clamp((float) (deltaTime() * multiple), 0, 1) * start;
    }

    public double deltaTime() {
        return Minecraft.debugFPS > 0 ? (1.0000 / Minecraft.debugFPS) : 1;
    }

    public static int clamp(int value, int min, int max) {
        return Math.min(max, Math.max(value, min));
    }

    public static float clamp(float value, float min, float max) {
        return Math.min(max, Math.max(value, min));
    }

    public static double clamp(double value, double min, double max) {
        return Math.min(max, Math.max(value, min));
    }

    public org.joml.Vector3d interpolate(Entity entity, float partialTicks) {
        double posX = interpolate(entity.lastTickPosX, entity.getPosX(), partialTicks);
        double posY = interpolate(entity.lastTickPosY, entity.getPosY(), partialTicks);
        double posZ = interpolate(entity.lastTickPosZ, entity.getPosZ(), partialTicks);
        return new org.joml.Vector3d(posX, posY, posZ);
    }

    public static Vector3d getEntityPos(Entity entity, float partialTicks) {
        double x = interpolate(entity.getPosX(), entity.lastTickPosX, partialTicks) - mc.getRenderManager().renderPosX(),
                y = interpolate(entity.getPosY(), entity.lastTickPosY, partialTicks) - mc.getRenderManager().renderPosY(),
                z = interpolate(entity.getPosZ(), entity.lastTickPosZ, partialTicks) - mc.getRenderManager().renderPosZ();

        return new Vector3d(x, y, z);
    }

    public static Vector3d getEntityPos(Entity entity) {
        float partialTicks = mc.getRenderPartialTicks();

        double x = entity.lastTickPosX + (entity.getPosX() - entity.lastTickPosX) * partialTicks,
                y = entity.lastTickPosY + (entity.getPosY() - entity.lastTickPosY) * partialTicks,
                z = entity.lastTickPosZ + (entity.getPosZ() - entity.lastTickPosZ) * partialTicks;

        return new Vector3d(x, y, z);
    }
    public static String getStringPercent(String text, final float percent10) {
        if (text.isEmpty()) return text;
        text = text.substring(0, (int) (MathUtil.clamp(percent10, 0, 1) * text.length()));
        return text;
    }

    public static int lerp(int a, int b, float f) {
        return a + (int) (f * (b - a));
    }

    public static float lerp(float a, float b, float f) {
        return a + f * (b - a);
    }

    public static double lerp(double a, double b, double f) {
        return a + f * (b - a);
    }

    public static double roundPROBLYA(float num, double increment) {
        double v = (double) Math.round(num / increment) * increment;
        BigDecimal bd = new BigDecimal(v);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
