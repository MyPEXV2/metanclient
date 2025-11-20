package relake.common.util;

import lombok.experimental.UtilityClass;
import net.minecraft.util.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import relake.Client;
import relake.Constants;
import relake.common.InstanceAccess;

import java.awt.*;
import java.util.Objects;

@UtilityClass
public class ChatUtil implements InstanceAccess {
    public void send(Object text) {
        send(text.toString());
    }

    public void send(String text) {
        mc.ingameGUI.getChatGUI().printChatMessage(prefix().appendString(text));
    }

    public void send(ITextComponent text) {
        mc.ingameGUI.getChatGUI().printChatMessage(prefix().append(text));
    }

    public void sendHoverText(String text, String triggerText, String hoverText) {
        IFormattableTextComponent itextcomponent = prefix().appendString(text);

        itextcomponent.append((new StringTextComponent(" " + triggerText)).setStyle(Style.EMPTY.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent(hoverText)))));

        mc.ingameGUI.getChatGUI().printChatMessage(itextcomponent);
    }

    public void sendClickText(String text, String triggerText, ClickEvent clickEvent) {
        IFormattableTextComponent itextcomponent = prefix().appendString(text);

        itextcomponent.append((new StringTextComponent(" " + triggerText)).setStyle(Style.EMPTY.setClickEvent(clickEvent)));

        mc.ingameGUI.getChatGUI().printChatMessage(itextcomponent);
    }

    public void sendHoverText(String text, String hoverText) {
        ITextComponent itextcomponent = prefix().append(new StringTextComponent(text).setStyle(Style.EMPTY.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent(hoverText)))));

        mc.ingameGUI.getChatGUI().printChatMessage(itextcomponent);
    }

    private IFormattableTextComponent prefix() {
        int rgb = Client.instance.moduleManager.hudModule.color.getValue().getRGB();

        int[] color = {
                ColorUtil.darker(rgb, 35),
                ColorUtil.darker(rgb, 75)
        };

        Color color1 = new Color(ColorHelper.PackedColor.getRed(color[0]), ColorHelper.PackedColor.getGreen(color[0]), ColorHelper.PackedColor.getBlue(color[0]));
        Color color2 = new Color(ColorHelper.PackedColor.getRed(color[1]), ColorHelper.PackedColor.getGreen(color[1]), ColorHelper.PackedColor.getBlue(color[1]));

        return genGradientText("[MetaN Client] ", color1.getRGB(), color2.getRGB());
    }

    public static IFormattableTextComponent genGradientText(String text, int color1, int color2) {
        IFormattableTextComponent gradientComponent = new StringTextComponent("");
        Color[] color = genGradientForText(new Color(color1), new Color(color2), text.length());

        int i = 0;

        for (char ch : text.toCharArray()) {
            IFormattableTextComponent component = new StringTextComponent(String.valueOf(ch));
            Style style = new Style(net.minecraft.util.text.Color.fromInt(color[i].getRGB()), false, false, false, false, false, null, null, null, null);
            component.setStyle(style);
            gradientComponent.append(component);
            i++;
        }

        return gradientComponent;
    }

    public static Color[] genGradientForText(Color color1, Color color2, int length) {
        Color[] gradient = new Color[length];

        for (int i = 0; i < length; i++) {
            double pc = (double) i / (length - 1);
            gradient[i] = interpolate(color1, color2, pc);
        }

        return gradient;
    }

    public static Color interpolate(Color color1, Color color2, double amount) {
        float amount1 = (float) (1F - amount);
        amount1 = (float) MathUtil.clamp((float) amount, 0F, (float) 1F);

        return new Color(
                (int) MathHelper.interpolate(color1.getRed(), color2.getRed(), amount1),
                (int) MathHelper.interpolate(color1.getGreen(), color2.getGreen(), amount1),
                (int) MathHelper.interpolate(color1.getBlue(), color2.getBlue(), amount1),
                (int) MathHelper.interpolate(color1.getAlpha(), color2.getAlpha(), amount1)
        );
    }
}
