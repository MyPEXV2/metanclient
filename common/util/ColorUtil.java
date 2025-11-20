package relake.common.util;

import lombok.experimental.UtilityClass;
import net.minecraft.util.ColorHelper;
import net.minecraft.util.math.MathHelper;

import java.awt.*;

import static net.minecraft.util.ColorHelper.PackedColor.*;

@UtilityClass
public class ColorUtil {
    public int applyOpacity(int color, float opacity) {
        return ColorHelper.PackedColor.packColor((int) (getAlpha(color) * opacity / 255), getRed(color), getGreen(color), getBlue(color));
    }

    public int replAlpha(int color, int alpha) {
        return getColor(getRed(color), getGreen(color), getBlue(color), alpha);
    }

    public int replAlpha(int color, float alpha) {
        return getColor(getRed(color), getGreen(color), getBlue(color), alpha);
    }

    public int multAlpha(int color, float percent01) {
        return getColor(getRed(color), getGreen(color), getBlue(color), Math.round(getAlpha(color) * percent01));
    }

    public int multDark(int color, float brPC) {
        return getColor(Math.round(getRed(color) * brPC), Math.round(getGreen(color) * brPC), Math.round(getBlue(color) * brPC), getAlpha(color));
    }

    public int getColor(int red, int green, int blue, float alpha) {
        return getColor(red, green, blue, Math.round(alpha * 255));
    }

    public int getColor(int br, float alpha) {
        return getColor(br, br, br, alpha);
    }

    public int getColor(int red, int green, int blue, int alpha) {
        return computeColor(red, green, blue, alpha);
    }

    public int getColor(float red, float green, float blue, float alpha) {
        return computeColor(Math.round(red * 255), Math.round(green * 255), Math.round(blue * 255), Math.round(alpha * 255));
    }

    public int getColor(int red, int green, int blue) {
        return getColor(red, green, blue, 255);
    }

    private int computeColor(int red, int green, int blue, int alpha) {
        return ((MathHelper.clamp(alpha, 0, 255) << 24) |
                (MathHelper.clamp(red, 0, 255) << 16) |
                (MathHelper.clamp(green, 0, 255) << 8) |
                MathHelper.clamp(blue, 0, 255));
    }

    public int darker(int color, int factor) {
        int red = getRed(color);
        int green = getGreen(color);
        int blue = getBlue(color);

        red = Math.max(0, red - factor);
        green = Math.max(0, green - factor);
        blue = Math.max(0, blue - factor);

        return (0xFF << 24) | (red << 16) | (green << 8) | blue;
    }

    public int interpolateColor(int color, int color2, double value) {
        double percent = Math.min(1, Math.max(0, value / 100.0));

        int red = (int) (getRed(color) + percent * (getRed(color2) - getRed(color)));
        int green = (int) (getGreen(color) + percent * (getGreen(color2) - getGreen(color)));
        int blue = (int) (getBlue(color) + percent * (getBlue(color2) - getBlue(color)));
        int alpha = (int) (getAlpha(color) + percent * (getAlpha(color2) - getAlpha(color)));

        return (0xFF << 24) | (red << 16) | (green << 8) | blue;
    }

    public float[] getRGBAf(int hex) {
        float[] rgba = new float[4];
        rgba[0] = getRed(hex) / 255f;
        rgba[1] = getGreen(hex) / 255f;
        rgba[2] = getBlue(hex) / 255f;
        rgba[3] = getAlpha(hex) / 255f;
        return rgba;
    }

    public float[] getRGBf(int hex) {
        float[] rgba = new float[3];
        rgba[0] = getRed(hex) / 255f;
        rgba[1] = getGreen(hex) / 255f;
        rgba[2] = getBlue(hex) / 255f;
        return rgba;
    }

    public static int getRedFromColor(int color) {
        return (color >> 16 & 0xFF);
    }

    public static int getGreenFromColor(int color) {
        return (color >> 8 & 0xFF);
    }

    public static int getBlueFromColor(int color) {
        return (color & 0xFF);
    }

    public static int getAlphaFromColor(int color) {
        return (color >> 24 & 0xFF);
    }

    public static float getGLRedFromColor(int color) {
        return (color >> 16 & 0xFF) / 255f;
    }

    public static float getGLGreenFromColor(int color) {
        return (color >> 8 & 0xFF) / 255f;
    }

    public static float getGLBlueFromColor(int color) {
        return (color & 0xFF) / 255f;
    }

    public static float getGLAlphaFromColor(int color) {
        return (color >> 24 & 0xFF) / 255f;
    }


    public static int getOverallColorFrom(int color1, int color2) {

        int red1 = getRedFromColor(color1);
        int green1 = getGreenFromColor(color1);
        int blue1 = getBlueFromColor(color1);
        int alpha1 = getAlphaFromColor(color1);
        int red2 = getRedFromColor(color2);
        int green2 = getGreenFromColor(color2);
        int blue2 = getBlueFromColor(color2);
        int alpha2 = getAlphaFromColor(color2);

        int finalRed = (red1 + red2) / 2;
        int finalGreen = (green1 + green2) / 2;
        int finalBlue = (blue1 + blue2) / 2;
        int finalAlpha = (alpha1 + alpha2) / 2;

        return getColor(finalRed, finalGreen, finalBlue, finalAlpha);
    }

    public static int getOverallColorFrom(int color1, int color2, float percentTo2) {
        final int finalRed = (int) MathUtil.lerp(color1 >> 16 & 0xFF, color2 >> 16 & 0xFF, percentTo2),
                finalGreen = (int) MathUtil.lerp(color1 >> 8 & 0xFF, color2 >> 8 & 0xFF, percentTo2),
                finalBlue = (int) MathUtil.lerp(color1 & 0xFF, color2 & 0xFF, percentTo2),
                finalAlpha = (int) MathUtil.lerp(color1 >> 24 & 0xFF, color2 >> 24 & 0xFF, percentTo2);
        return getColor(finalRed, finalGreen, finalBlue, finalAlpha);
    }

    public static int getHue(int red, int green, int blue) {

        float min = Math.min(Math.min(red, green), blue);
        float max = Math.max(Math.max(red, green), blue);

        if (min == max) {
            return 0;
        }

        float hue = 0f;
        if (max == red) {
            hue = (green - blue) / (max - min);

        } else if (max == green) {
            hue = 2f + (blue - red) / (max - min);

        } else {
            hue = 4f + (red - green) / (max - min);
        }

        hue = hue * 60;
        if (hue < 0) hue = hue + 360;

        return Math.round(hue);
    }

    public static int getHueFromColor(int color) {
        return getHue(getRedFromColor(color), getGreenFromColor(color), getBlueFromColor(color));
    }

    public static float getBrightnessFromColor(int color) {
        final float[] athsb = Color.RGBtoHSB(getRedFromColor(color), getGreenFromColor(color), getBlueFromColor(color), null);
        return athsb[2];
    }

    public static float getSaturateFromColor(int color) {
        final float[] athsb = Color.RGBtoHSB(getRedFromColor(color), getGreenFromColor(color), getBlueFromColor(color), null);
        return athsb[1];
    }
    public int fade(int speed, int index, int first, int second) {
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        angle = angle >= 180 ? 360 - angle : angle;
        return interpolateColor(first, second, angle / 180f);
    }

}

