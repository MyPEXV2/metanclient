package relake.module.implement.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import org.lwjgl.opengl.GL11;
import relake.Client;
import relake.Constants;
import relake.animation.excellent.util.Easings;
import relake.common.util.*;
import relake.event.EventHandler;
import relake.event.impl.render.ScreenRenderEvent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.render.display.Render2D;
import relake.render.display.shape.ShapeRenderer;
import relake.render.display.shape.Side;
import relake.settings.Setting;
import relake.settings.implement.ColorSetting;
import relake.settings.implement.MultiSelectSetting;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

import static relake.render.display.font.FontRegister.Type.BOLD;
import static relake.render.display.font.FontRegister.Type.LOGO;

public class HUDModule extends Module {
    public final Setting<Color> color = new ColorSetting("Настройки цвета").setValue(0xFFC484FF);

    public final MultiSelectSetting selectComponent = new MultiSelectSetting("Отображать")
            .setValue("Watermark",
                    "PotionList",
                    "KeyBinds",
                    "TimerHud",
                    "InventoryHud",
                    "Cooldowns",
                    "HotBar",

                    "Armor",
                    "EventTimer");

    public HUDModule() {
        super("HUD", "Добавляет много разных информативных панелей и других элементов игры", "Adds many different informative panels and other game elements", ModuleCategory.Render);
        registerComponent(color, selectComponent);

        selectComponent.getSelected().add("Watermark");
        selectComponent.getSelected().add("PotionList");
        selectComponent.getSelected().add("KeyBinds");
        selectComponent.getSelected().add("TimerHud");
        selectComponent.getSelected().add("InventoryHud");
        selectComponent.getSelected().add("Cooldowns");
        selectComponent.getSelected().add("HotBar");

        selectComponent.getSelected().add("Armor");
        selectComponent.getSelected().add("EventTimer");
    }

    private final FrameCounter fpsCounter = FrameCounter.build();
    private final StopWatch waterMarkTimer = new StopWatch();
    private int waterMarkAnimDelay = 100;

        @EventHandler
        public void test(ScreenRenderEvent event) {
            MatrixStack matrixStack = event.getMatrixStack();
            int rgb = color.getValue().getRGB();

            if (selectComponent.isSelected("Watermark")) {
                String mainWaterMarkText = Constants.NAME;
                double x = mc.player.getPosX() - mc.player.prevPosX;
                double z = mc.player.getPosZ() - mc.player.prevPosZ;
                String coordsText = Client.instance.moduleManager.streamerModeModule.disguiseCoordsString(((int) mc.player.getPosX() + " " + (int) mc.player.getPosY() + " " + (int) mc.player.getPosZ()));

                int startX = 17;
                int padding = 7;
                int textPadding = 5;
                int textOffset = 33;
                int outlineOffset = 8;
                float fontSize = 2f;
                int boxWidth = 55;
                int borderRadius = 10;
                int tabOffset = 20;

                // основной водяной знак
                ShapeRenderer box = Render2D.box(matrixStack, startX, startX, Render2D.size(BOLD, 13).getWidth(mainWaterMarkText) + 26 + boxWidth, 20);
                box.expand(Side.ALL, padding);
                box.quad(borderRadius, 0xB70E0E0F);
                box.quad(borderRadius, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 25), -1, 0.15f), 0xEE0E0E0F, 0xEE0E0E0F, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 25), -1, 0.15f));

                ShapeRenderer boxOutLine = Render2D.box(matrixStack, startX - outlineOffset, startX - outlineOffset, Render2D.size(BOLD, 13).getWidth(mainWaterMarkText) + 42 + boxWidth, 37);
                boxOutLine.outlineHud(borderRadius, 2, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 75), -1, 0.35f), 0xEE0E0E0F, 0xEE0E0E0F, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 75), -1, 0.35f));

                Render2D.size(LOGO, 50).string(matrixStack, "Z", box.x + 3, box.y - 6.f, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 175), -1, 0.35f));
                Render2D.size(BOLD, 13).string(matrixStack, " Prime", box.x + textOffset + 48 + textPadding, box.y + padding + fontSize, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 175), -1, 0.35f));
                Render2D.size(BOLD, 13).string(matrixStack, mainWaterMarkText, box.x + textOffset + textPadding, box.y + padding + fontSize, ColorUtil.applyOpacity(ColorUtil.getColor(215, 215, 215), 255));

                int currentX = startX + boxWidth + 93;

                // второй бокс для UID
                String uidLabel = "UID: ";
                String uidValue = "" + 1;
                ShapeRenderer uidBox = Render2D.box(matrixStack, currentX, startX, Render2D.size(BOLD, 13).getWidth(uidLabel + uidValue), 20);
                uidBox.expand(Side.ALL, padding);
                uidBox.quad(borderRadius, 0xB70E0E0F);
                uidBox.quad(borderRadius, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 25), -1, 0.15f), 0xEE0E0E0F, 0xEE0E0E0F, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 25), -1, 0.15f));

                ShapeRenderer uidBoxOutline = Render2D.box(matrixStack, currentX - outlineOffset, startX - outlineOffset, Render2D.size(BOLD, 13).getWidth(uidLabel + uidValue) + 18, 36);
                uidBoxOutline.outlineHud(borderRadius, 2, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 75), -1, 0.35f), 0xEE0E0E0F, 0xEE0E0E0F, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 75), -1, 0.35f));

                Render2D.size(BOLD, 13).string(matrixStack, uidLabel, uidBox.x + padding, uidBox.y + padding + fontSize, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 175), -1, 0.35f));
                Render2D.size(BOLD, 13).string(matrixStack, uidValue, uidBox.x + padding + Render2D.size(BOLD, 13).getWidth(uidLabel), uidBox.y + padding + fontSize, ColorUtil.applyOpacity(ColorUtil.getColor(215, 215, 215), 255));


                // третий бокс для Username
                currentX += (int) (Render2D.size(BOLD, 13).getWidth(uidLabel + uidValue) + tabOffset);
                String usernameLabel = "User: ";
                String usernameValue = "Zhukov";
                ShapeRenderer usernameBox = Render2D.box(matrixStack, currentX, startX, Render2D.size(BOLD, 13).getWidth(usernameLabel + usernameValue), 20);
                usernameBox.expand(Side.ALL, padding);
                usernameBox.quad(borderRadius, 0xB70E0E0F);
                usernameBox.quad(borderRadius, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 25), -1, 0.15f), 0xEE0E0E0F, 0xEE0E0E0F, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 25), -1, 0.15f));

                ShapeRenderer usernameBoxOutline = Render2D.box(matrixStack, currentX - outlineOffset, startX - outlineOffset, Render2D.size(BOLD, 13).getWidth(usernameLabel + usernameValue) + 17, 36);
                usernameBoxOutline.outlineHud(borderRadius, 2, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 75), -1, 0.35f), 0xEE0E0E0F, 0xEE0E0E0F, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 75), -1, 0.35f));

                Render2D.size(BOLD, 13).string(matrixStack, usernameLabel, usernameBox.x + padding, usernameBox.y + padding + fontSize, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 175), -1, 0.35f));
                Render2D.size(BOLD, 13).string(matrixStack, usernameValue, usernameBox.x + padding + Render2D.size(BOLD, 13).getWidth(usernameLabel), usernameBox.y + padding + fontSize, ColorUtil.applyOpacity(ColorUtil.getColor(215, 215, 215), 255));

                // четвертый бокс для времени
                currentX += (int) (Render2D.size(BOLD, 13).getWidth(usernameLabel + usernameValue) + tabOffset);
                String timeLabel = "Time: ";
                String timeValue = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
                ShapeRenderer timeBox = Render2D.box(matrixStack, currentX, startX, Render2D.size(BOLD, 13).getWidth(timeLabel + timeValue), 20);
                timeBox.expand(Side.ALL, padding);
                timeBox.quad(borderRadius, 0xB70E0E0F);
                timeBox.quad(borderRadius, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 25), -1, 0.15f), 0xEE0E0E0F, 0xEE0E0E0F, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 25), -1, 0.15f));

                ShapeRenderer timeBoxOutline = Render2D.box(matrixStack, currentX - outlineOffset, startX - outlineOffset, Render2D.size(BOLD, 13).getWidth(timeLabel + timeValue) + 18, 36);
                timeBoxOutline.outlineHud(borderRadius, 2, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 75), -1, 0.35f), 0xEE0E0E0F, 0xEE0E0E0F, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 75), -1, 0.35f));

                Render2D.size(BOLD, 13).string(matrixStack, timeLabel, timeBox.x + padding, timeBox.y + padding + fontSize, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 175), -1, 0.35f));
                Render2D.size(BOLD, 13).string(matrixStack, timeValue, timeBox.x + padding + Render2D.size(BOLD, 13).getWidth(timeLabel), timeBox.y + padding + fontSize, ColorUtil.applyOpacity(ColorUtil.getColor(215, 215, 215), 255));

                // некст элементы
                int newStartY = 56;

                // пятый бокс для координат
                int coordsBoxX = startX;
                String coordsLabel = "Coords: ";
                String coordsValue = coordsText;
                ShapeRenderer coordsBox = Render2D.box(matrixStack, coordsBoxX, newStartY, Render2D.size(BOLD, 13).getWidth(coordsLabel + coordsValue), 20);
                coordsBox.expand(Side.ALL, padding);
                coordsBox.quad(borderRadius, 0xB70E0E0F);
                coordsBox.quad(borderRadius, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 25), -1, 0.15f), 0xEE0E0E0F, 0xEE0E0E0F, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 25), -1, 0.15f));

                ShapeRenderer coordsBoxOutline = Render2D.box(matrixStack, coordsBoxX - outlineOffset, newStartY - outlineOffset, Render2D.size(BOLD, 13).getWidth(coordsLabel + coordsValue) + 17, 36);
                coordsBoxOutline.outlineHud(borderRadius, 2, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 75), -1, 0.35f), 0xEE0E0E0F, 0xEE0E0E0F, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 75), -1, 0.35f));

                Render2D.size(BOLD, 13).string(matrixStack, coordsLabel, coordsBox.x + padding, coordsBox.y + padding + fontSize, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 175), -1, 0.35f));
                Render2D.size(BOLD, 13).string(matrixStack, coordsValue, coordsBox.x + padding + Render2D.size(BOLD, 13).getWidth(coordsLabel), coordsBox.y + padding + fontSize, ColorUtil.applyOpacity(ColorUtil.getColor(215, 215, 215), 255));

                // шестой бокс для BPS
                int bpsBoxX = (int) (coordsBoxX + Render2D.size(BOLD, 13).getWidth(coordsLabel + coordsValue) + tabOffset);
                String bpsLabel = "BPS: ";
                String bpsValue = BigDecimal.valueOf(Math.sqrt(x * x + z * z) / (mc.timer.tickLength / 1000)).setScale(1, RoundingMode.HALF_UP).toString();
                ;
                ShapeRenderer bpsBox = Render2D.box(matrixStack, bpsBoxX, newStartY, Render2D.size(BOLD, 13).getWidth(bpsLabel + bpsValue), 20);
                bpsBox.expand(Side.ALL, padding);
                bpsBox.quad(borderRadius, 0xB70E0E0F);
                bpsBox.quad(borderRadius, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 25), -1, 0.15f), 0xEE0E0E0F, 0xEE0E0E0F, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 25), -1, 0.15f));

                ShapeRenderer bpsBoxOutline = Render2D.box(matrixStack, bpsBoxX - outlineOffset, newStartY - outlineOffset, Render2D.size(BOLD, 13).getWidth(bpsLabel + bpsValue) + 17, 36);
                bpsBoxOutline.outlineHud(borderRadius, 2, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 75), -1, 0.35f), 0xEE0E0E0F, 0xEE0E0E0F, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 75), -1, 0.35f));

                Render2D.size(BOLD, 13).string(matrixStack, bpsLabel, bpsBox.x + padding, bpsBox.y + padding + fontSize, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 175), -1, 0.35f));
                Render2D.size(BOLD, 13).string(matrixStack, bpsValue, bpsBox.x + padding + Render2D.size(BOLD, 13).getWidth(bpsLabel), bpsBox.y + padding + fontSize, ColorUtil.applyOpacity(ColorUtil.getColor(215, 215, 215), 255));

                // седьмой бокс для FPS
                int fpsBoxX = (int) (bpsBoxX + Render2D.size(BOLD, 13).getWidth(bpsLabel + bpsValue) + tabOffset);
                fpsCounter.renderThreadRead((int) MathUtil.clamp((float) fpsCounter.getFps() / 5.F, 10, 60));
                String fpsLabel = "FPS: ";
                String fpsValue =  fpsCounter.getFpsString(false);

                ShapeRenderer fpsBox = Render2D.box(matrixStack, fpsBoxX, newStartY, Render2D.size(BOLD, 13).getWidth(fpsLabel + fpsValue), 20);
                fpsBox.expand(Side.ALL, padding);
                fpsBox.quad(borderRadius, 0xB70E0E0F);
                fpsBox.quad(borderRadius, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 25), -1, 0.15f), 0xEE0E0E0F, 0xEE0E0E0F, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 25), -1, 0.15f));

                ShapeRenderer fpsBoxOutline = Render2D.box(matrixStack, fpsBoxX - outlineOffset, newStartY - outlineOffset, Render2D.size(BOLD, 13).getWidth(fpsLabel + fpsValue) + 18, 36);
                fpsBoxOutline.outlineHud(borderRadius, 2, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 75), -1, 0.35f), 0xEE0E0E0F, 0xEE0E0E0F, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 75), -1, 0.35f));

                Render2D.size(BOLD, 13).string(matrixStack, fpsLabel, fpsBox.x + padding, fpsBox.y + padding + fontSize, ColorUtil.getOverallColorFrom(ColorUtil.applyOpacity(rgb, 175), -1, 0.35f));
                Render2D.size(BOLD, 13).string(matrixStack, fpsValue, fpsBox.x + padding + Render2D.size(BOLD, 13).getWidth(fpsLabel), fpsBox.y + padding + fontSize, ColorUtil.applyOpacity(ColorUtil.getColor(215, 215, 215), 255));

                if (waterMarkTimer.finished(waterMarkAnimDelay)) {
                waterMarkAnimDelay = new Random().nextInt(3555, 12222);
                waterMarkTimer.reset();
            }
            int maxAnimTime = 1666;
            float animPC = waterMarkTimer.finished(maxAnimTime) ? 0.F : waterMarkTimer.elapsedTime() / (float) maxAnimTime;

            // анимка йоу
            if (animPC > 0) {
                float alphaPCAnim = (animPC > .5F ? 1.F - animPC : animPC) * 2.F;
                float ext = 6.F + 6.F * alphaPCAnim;
                float x1 = box.x + textOffset + 57 + textPadding - ext, x2 = x1 + Render2D.size(BOLD, 13).getWidth(" Client") + ext;
                ShapeRenderer boxGradient1 = Render2D.box(matrixStack, MathUtil.lerp(x1, x2, animPC), box.y + textPadding + fontSize + 7.5F, 0, 0);
                StencilUtil.begin();
                GL11.glEnable(GL11.GL_ALPHA_TEST);
                Render2D.size(BOLD, 13).string(matrixStack, " Client", box.x + textOffset + 48 + textPadding, box.y + 7 + fontSize, ColorUtil.applyOpacity(rgb, 235));
                GL11.glDisable(GL11.GL_ALPHA_TEST);
                StencilUtil.read(1);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                boxGradient1.drawShadow(matrixStack, ext * 2.F, 255.F, ColorUtil.getOverallColorFrom(rgb, -1, .4F));
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                StencilUtil.end();

                ext = 1.F + alphaPCAnim;
                boxGradient1.y = box.y + textPadding + fontSize + 4.5F - 6.F * (float) Easings.BOUNCE_IN.ease(alphaPCAnim);
                boxGradient1.drawShadow(matrixStack, ext * 2.F, alphaPCAnim * alphaPCAnim * 255.F, -1);
            }
        }
    }
}
