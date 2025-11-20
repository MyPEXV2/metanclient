package relake.menu.mainmenu;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Session;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;
import relake.Client;
import relake.account.Account;
import relake.common.util.ColorUtil;
import relake.common.util.MathUtil;
import relake.common.util.SoundUtil;
import relake.common.util.StencilUtil;
import relake.menu.Generator;
import relake.render.display.Render2D;
import relake.render.display.font.FontRegister;
import relake.render.display.shape.ShapeRenderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


@Getter
public class AccountScreen extends Screen {
    public static final List<AccountComponent> accountComponents = new ArrayList<>();
    private final List<Button> buttons = new ArrayList<>();

    public double scroll = 0;
    public double smoothedScroll = 0;

    private final TextField textField;
    private final Button clear;
    private final Button login;
    private final Button random;

    public AccountScreen() {
        super(StringTextComponent.EMPTY);

        textField = new TextField();

        accountComponents.clear();

        clear = new Button("Clear ", () -> {
            accountComponents.clear();

            Client.instance.accountManager.clear();
        });

        random = new Button("Generate ", () -> {
            String name = Generator.generateName();

            SoundUtil.playSound("accountswitch.wav", 0.1f);
            accountComponents.add(new AccountComponent(new Account(name)));

            Client.instance.accountManager.add(name);

            Minecraft.getInstance().session = new Session(name, String.valueOf(UUID.randomUUID()), "", "");

            Client.instance.accountManager.setLastLogin(name);
        });

        login = new Button("Add ", () -> {
            if (textField.getValue() == null || textField.getValue().isEmpty() || Client.instance.accountManager.contains(textField.getValue())) return;

            accountComponents.add(new AccountComponent(new Account(textField.getValue())));
            SoundUtil.playSound("accountswitch.wav", 0.1f);
            Client.instance.accountManager.add(textField.getValue());

            Minecraft.getInstance().session = new Session(textField.getValue(), String.valueOf(UUID.randomUUID()), "", "");

            Client.instance.accountManager.setLastLogin(textField.getValue());
        });

        buttons.addAll(Arrays.asList(clear, random, login));

        for (Account account : Client.instance.accountManager.accounts) {
            accountComponents.add(new AccountComponent(account));
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        MainWindow window = Minecraft.getInstance().getMainWindow();

        int rgb = Client.instance.moduleManager.hudModule.color.getValue().getRGB();

        ShapeRenderer box = Render2D.box(matrixStack, 0, 0, window.getWidth(), window.getHeight());
        box.quad(0, 0xFF0E0E0F);
        box.quad(0, ColorUtil.applyOpacity(rgb, 40), 0xFF0E0E0F, 0xFF0E0E0F, rgb);

        clear.box(window.getWidth() / 2 - 105, window.getHeight() / 2 + 105, 50, 40);
        random.box(window.getWidth() / 2 - 50, window.getHeight() / 2 + 105, 65, 40);
        login.box(window.getWidth() / 2 + 60, window.getHeight() / 2 + 105, 40, 40);

        textField.box(window.getWidth() / 2 - 105, window.getHeight() / 2 + 60, 210, 40);
        textField.render(matrixStack, mouseX, mouseY, partialTicks);

        Render2D.size(FontRegister.Type.BOLD, 20).string(matrixStack, "Account", window.getWidth() / 2 - 100, window.getHeight() / 2 - 187, -1);
        ShapeRenderer box2 = Render2D.box(matrixStack, window.getWidth() / 2 - 105, window.getHeight() / 2 - 155, 210, 210);
        box2.quad(10, 0x700E0E0F);

        if (Client.instance.accountManager.accounts.isEmpty()) {
            Render2D.size(FontRegister.Type.BOLD, 20).centeredString(matrixStack, "No Account", window.getWidth() / 2, window.getHeight() / 2 - 70, 0xAAFFFFFF);
        }
        StencilUtil.begin();
        ShapeRenderer box1 = Render2D.box(matrixStack, window.getWidth() / 2 - 100, window.getHeight() / 2 - 150, 200, 200);

        box1.quad(10, 0x50FFFFFF);

        StencilUtil.read(1);

        float yOffset = window.getHeight() / 2 - 150;
        int maxScroll = 0;

        for (AccountComponent accountComponent : accountComponents) {
            accountComponent.x = window.getWidth() / 2 - 100;
            accountComponent.y = (float) (yOffset + smoothedScroll);
            accountComponent.width = 200;
            accountComponent.height = 35;

            accountComponent.render(matrixStack, mouseX, mouseY, partialTicks);

            yOffset += 40;
            maxScroll = (int) Math.max(maxScroll, yOffset);
        }

        StencilUtil.end();
        double clamped = MathHelper.clamp(maxScroll - ((window.getHeight() / 2 - 150) + 170), 0, maxScroll);
        scroll = MathHelper.clamp(scroll, -clamped, 0);
        smoothedScroll = MathUtil.fast((float) smoothedScroll, (float) scroll, 15F);

        for (Button button : buttons) {
            button.render(matrixStack, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int key) {
        MainWindow window = Minecraft.getInstance().getMainWindow();
        List<AccountComponent> toRemove = new ArrayList<>();

        if (MathUtil.isHovered(mouseX, mouseY, window.getWidth() / 2 - 100, window.getHeight() / 2 - 150, 200, 200)) {
            for (AccountComponent accountComponent : accountComponents) {
                if (MathUtil.isHovered(mouseX, mouseY, accountComponent.x + accountComponent.width - 30, accountComponent.y + 5, 25, 25)) {
                    Client.instance.accountManager.remove(accountComponent.account.getName());
                    toRemove.add(accountComponent);
                } else {
                    accountComponent.mouseClicked(mouseX, mouseY, key);
                }
            }
        }

        accountComponents.removeAll(toRemove);

        textField.mouseClicked(mouseX, mouseY, key);

        for (Button button : buttons) {
            button.mouseClicked(mouseX, mouseY, key);
        }
        return super.mouseClicked(mouseX, mouseY, key);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        textField.charTyped(codePoint, modifiers);
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        textField.keyPressed(keyCode, scanCode, modifiers);
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            if ((textField.getValue() != null && !textField.getValue().isEmpty())) {
                if (!Client.instance.accountManager.contains(textField.getValue())) {
                    accountComponents.add(new AccountComponent(new Account(textField.getValue())));

                    Client.instance.accountManager.add(textField.getValue());

                    SoundUtil.playSound("accountswitch.wav", 0.1f);

                    Minecraft.getInstance().session = new Session(textField.getValue(), String.valueOf(UUID.randomUUID()), "", "");
                    Client.instance.accountManager.setLastLogin(textField.getValue());
                }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void closeScreen() {
        accountComponents.clear();
        textField.setFocused(false);
        Client.instance.configManager.accountConfig.save();
        super.closeScreen();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        scroll += delta * 20;
        return super.mouseScrolled(mouseX, mouseY, delta);
    }
}
