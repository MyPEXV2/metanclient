package relake.menu.mainmenu;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import relake.Client;
import relake.account.Account;
import relake.animation.tenacity.Animation;
import relake.animation.tenacity.Direction;
import relake.common.util.ColorUtil;
import relake.common.util.MathUtil;
import relake.common.util.SoundUtil;
import relake.render.display.Render2D;
import relake.render.display.shape.ShapeRenderer;

import java.time.Duration;
import java.util.UUID;

@RequiredArgsConstructor
public class AccountComponent {
    private final Animation animation = new Animation(100, Duration.ofMillis(150))
            .setDirection(Direction.BACKWARD);

    public final Account account;

    public float x;
    public float y;
    public float width;
    public float height;

    public AccountComponent box(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        return this;
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        animation.switchDirection(MathUtil.isHovered(mouseX, mouseY, x, y, width, height));
        animation.setStart(255);

        int rgb = Client.instance.moduleManager.hudModule.color.getValue().getRGB();

        ShapeRenderer box = Render2D.box(matrixStack, x, y, width, height);
        box.quad(10, 0xFF151516);
//        box.outlineHud(10, 1, ColorUtil.applyOpacity(rgb, 75), 0xEE0E0E0F, 0xEE0E0E0F, ColorUtil.applyOpacity(rgb, 75));

        if (Minecraft.getInstance().session.getUsername().equalsIgnoreCase(account.getName())) {
            box.quad(10, ColorUtil.applyOpacity(rgb, 40), rgb, 0xFF0E0E0F, 0xFF0E0E0F);
        }

        ShapeRenderer close = Render2D.box(matrixStack, x + width - 30, y + 5, 25, 25);
        close.quad(8, ColorUtil.applyOpacity(rgb, animation.get()));
        Render2D.size(15).string(matrixStack, "X ", x + width - 24, y + 8, ColorUtil.applyOpacity(-1, animation.get()));

        Render2D.size(15).string(matrixStack, account.getName(), x + 6, y + 8, -1);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int key) {
        if (MathUtil.isHovered(mouseX, mouseY, x, y, width, height) && key == 0) {
            SoundUtil.playSound("accountswitch.wav", 0.1f);
            Minecraft.getInstance().session = new Session(account.getName(), String.valueOf(UUID.randomUUID()), "", "");
            Client.instance.accountManager.setLastLogin(account.getName());
            return true;
        }

        return false;
    }
}
