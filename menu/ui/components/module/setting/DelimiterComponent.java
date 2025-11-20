package relake.menu.ui.components.module.setting;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import relake.Client;
import relake.common.util.ColorUtil;
import relake.menu.ui.components.Component;
import relake.render.display.Render2D;
import relake.render.display.font.FontRegister;
import relake.render.display.font.TextRenderer;
import relake.render.display.shape.ShapeRenderer;
import relake.settings.implement.DelimiterSetting;

@Getter
@RequiredArgsConstructor
public class DelimiterComponent extends Component {

    private final DelimiterSetting setting;

    @Override
    public void updateComponent() {
        visible = setting.getVisible().get();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        TextRenderer font = Render2D.size(FontRegister.Type.BOLD, 10);
        float strW = Math.min(font.getWidth(setting.getName()), (int) width - 20), offsetsX = 2.F;
        int col = ColorUtil.getOverallColorFrom(-1, Client.instance.moduleManager.hudModule.color.getValue().getRGB(), 0.5f),
        colShadow = ColorUtil.multAlpha(col, .2F);
        font.string(matrixStack, setting.getName(), x + width / 2.F - strW / 2.F, y + 2F, col, (int) (width / 1.5F - 20));
        float lineH = 1.F, lineY = y + 9.F;
        //L
        ShapeRenderer L_BOX = Render2D.box(matrixStack, x + offsetsX, lineY - lineH / 2.F, width / 2.F - strW / 2.F - offsetsX, lineH);
        L_BOX.quad(0, col);
        L_BOX.drawShadow(matrixStack, height / 2, colShadow);
        //R
        float rX = x + offsetsX + width / 2.F + strW / 2.F + offsetsX;
        ShapeRenderer R_BOX = Render2D.box(matrixStack, rX, lineY - lineH / 2.F, x + width - offsetsX - rX, lineH);
        R_BOX.quad(0, col);
        R_BOX.drawShadow(matrixStack, height / 2, colShadow);
        height = 20;
    }
}
