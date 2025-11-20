package relake.menu.mainmenu;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.WorldSelectionScreen;
import net.minecraft.util.text.StringTextComponent;
import relake.Client;
import relake.Constants;
import relake.common.util.ColorUtil;
import relake.common.util.SkinUtil;
import relake.render.display.Render2D;
import relake.render.display.font.FontRegister;
import relake.render.display.shape.ShapeRenderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainScreen extends Screen{
    private final List<Button> buttons = new ArrayList<>();
    private final List<Snow> snow = new ArrayList<>();

    private final Button single;
    private final Button multiplayer;
    private final Button options;
    private final Button account;
    private final Button exit;

    public MainScreen() {
        super(StringTextComponent.EMPTY);

        single = new Button("Одиночная игра", () -> this.minecraft.displayGuiScreen(new WorldSelectionScreen(this)));
        multiplayer = new Button("Сетевая игра", () -> this.minecraft.displayGuiScreen(new MultiplayerScreen(this)));
        options = new Button("Настройки", () -> this.minecraft.displayGuiScreen(new OptionsScreen(this, this.minecraft.gameSettings)));
        account = new Button("Аккаунты", () -> this.minecraft.displayGuiScreen(new AccountScreen()));

        exit = new Button(FontRegister.Type.ICONS, "h ", () -> this.minecraft.shutdown());

        buttons.addAll(Arrays.asList(single, multiplayer, options, account, exit));
    }

    @Override
    protected void init() {
        MainWindow window = Minecraft.getInstance().getMainWindow();

        snow.clear();
        for (int i = 0; i < 75; i++) {
            snow.add(new Snow((float) (Math.random() * window.getWidth()), (float) (Math.random() * window.getHeight())));
        }

        super.init();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        MainWindow window = Minecraft.getInstance().getMainWindow();

        int rgb = Client.instance.moduleManager.hudModule.color.getValue().getRGB();

        ShapeRenderer box = Render2D.box(matrixStack, 0, 0, window.getWidth(), window.getHeight());
        box.quad(0, 0xFF0E0E0F);
        box.quad(0, ColorUtil.applyOpacity(rgb, 40), 0xFF0E0E0F, 0xFF0E0E0F, rgb);

        for(int i = 0; i < snow.size(); ++i){
            final Snow particle = snow.get(i);
            particle.setPosY(particle.getPosY() + (float)(0.06*100) / Minecraft.debugFPS);

//            ShapeRenderer particleRender = Render2D.box(matrixStack, particle.getPosX(), particle.getPosY(), 6, 6);
//            particleRender.quad(6, -1);

            if(particle.getPosY() >= window.getHeight() + 1) {
                snow.remove(particle);
                snow.add(new Snow((float) (Math.random() * window.getWidth()), (float) (Math.random() * window.getHeight())));
            }
        }

        // Аву с сайта
        ShapeRenderer avatar = Render2D.box(matrixStack, window.getWidth() / 2 + 70, window.getHeight() / 2 - 120, 30, 30);


        Render2D.size(FontRegister.Type.BOLD, 20).string(matrixStack, Constants.NAME, window.getWidth() / 2 - 68, window.getHeight() / 2 - 120, -1);
        Render2D.size(FontRegister.Type.LOGO, 75).string(matrixStack, "R", window.getWidth() / 2 - 110, window.getHeight() / 2 - 142, rgb);

        single.box(window.getWidth() / 2 - 100, window.getHeight() / 2 - 80, 200, 40);
        multiplayer.box(window.getWidth() / 2 - 100, window.getHeight() / 2 - 35, 200, 40);
        options.box(window.getWidth() / 2 - 100, window.getHeight() / 2 + 10, 200, 40);
        account.box(window.getWidth() / 2 - 100, window.getHeight() / 2 + 55, 155, 40);
        exit.box(window.getWidth() / 2 + 60, window.getHeight() / 2 + 55, 40, 40);

        for (Button button : buttons) {
            button.render(matrixStack, mouseX, mouseY, partialTicks);
        }

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int key) {
        for (Button button : buttons) {
            button.mouseClicked(mouseX, mouseY, key);
        }
        return super.mouseClicked(mouseX, mouseY, key);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Getter
    @Setter
    private class Snow {
        private float posX;
        private float posY;

        public Snow(float posX, float posY) {
            this.posX = posX;
            this.posY = posY;
        }
    }
}
