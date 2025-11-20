package relake.menu.ui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;
import relake.Client;
import relake.Constants;
import relake.animation.apelcin4ik.Animation;
import relake.animation.apelcin4ik.AnimationDirection;
import relake.animation.apelcin4ik.impl.EaseAnimation;
import relake.animation.excellent.util.Easings;
import relake.common.InstanceAccess;
import relake.common.util.*;
import relake.menu.ui.components.SearchComponent;
import relake.menu.ui.components.category.CategoryComponent;
import relake.menu.ui.components.module.ModuleComponent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.module.implement.render.ClientSoundsModule;
import relake.render.display.Render2D;
import relake.render.display.font.FontRegister;
import relake.render.display.font.TextRenderer;
import relake.render.display.shape.ShapeRenderer;
import relake.render.display.shape.Side;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

public class MenuScreen extends Screen implements InstanceAccess {

    ClientSoundsModule clientSoundsModule = Client.instance.moduleManager.clientSoundsModule;

    private final int speed = 225;

    private final Animation animation = new EaseAnimation(speed, 0);
    private final Animation categoryAnimation = new EaseAnimation(250, 0);
    private final Animation alphaAnimation = new EaseAnimation(speed - (speed / 5), 0);
    private final relake.animation.excellent.Animation scrollBarAnimation = new relake.animation.excellent.Animation(),
            scrollBarYAnimation = new relake.animation.excellent.Animation();
    private final relake.animation.excellent.Animation descAnimation = new relake.animation.excellent.Animation();
    private final List<CategoryComponent> categoryComponents = new ArrayList<>();
    private final List<ModuleComponent> moduleComponents = new ArrayList<>();
    public final SearchComponent searchComponent;

    private final int width = 710;
    private final int height = 450;

    private int x;
    private int y;

    private boolean scrooling = false;
    private float mouseYScroll;

    private final int sectionWidth = 200;
    public ModuleCategory category = ModuleCategory.Combat, mewCategory = category;

    public MenuScreen() {
        super(ITextComponent.getTextComponentOrEmpty(Constants.NAME));

        for (ModuleCategory value : ModuleCategory.values()) {
            CategoryComponent categoryComponent = new CategoryComponent(value);
            categoryComponent.scroll = 0F;
            categoryComponent.smoothedScroll = 0F;
            categoryComponents.add(categoryComponent);
        }

        for (Module module : Client.instance.moduleManager.modules) {
            moduleComponents.add(new ModuleComponent(module));
        }

        searchComponent = new SearchComponent();
    }

    @Override
    protected void init() {
        if (clientSoundsModule.openGUI.getValue()) SoundUtil.playSound("openscreen.wav", 0.07f);
        animation.setDirection(AnimationDirection.FORWARD);
        alphaAnimation.setDirection(AnimationDirection.FORWARD);
        descAnimation.setValue(0.F);
        descAnimation.run(0, .3F);
        for (ModuleComponent component : moduleComponents) {
            component.init();
        }
        super.init();
    }

    private Module lastDescModule = null;

    public void callShowDesc(Module module) {
        if (module == null) return;
        lastDescModule = module;
        descAnimation.setValue(0.F);
        descAnimation.run(1, 5.25F);
        descTimeoutTimer.reset();
    }

    private final StopWatch descTimeoutTimer = new StopWatch();

    private long timeStatusShowDesc() {
        return 6500L;
    }

    private float drawDesc(MatrixStack matrixIn, float x, float cy, int maxDescWidth, int baseColor) {
        descAnimation.update();
        descAnimation.setEasing(Easings.LINEAR);
        if (descAnimation.getToValue() == 1 && descAnimation.isFinished() && descTimeoutTimer.finished(timeStatusShowDesc()))
            descAnimation.run(0, 1.F);

        final float descAnimPC = Math.min(descAnimation.get() * 1.333333F, 1.F);
        if (descAnimPC == 0 || lastDescModule == null) return 0;
        String descString = lastDescModule.getDesc(!mc.gameSettings.language.toLowerCase().contains("en"), true) + "\u002E";
        if (descString.length() == 0) return descAnimPC;
        descString = MathUtil.getStringPercent(descString, descAnimPC);
        if (descString.length() == 0) return descAnimPC;

        float fontHeight = 12.F;
        TextRenderer font = Render2D.size(FontRegister.Type.BOLD, (int) (fontHeight - 1));
        float maxWordWidth = 0;
        for (final String stringIn : descString.split("\u0020")) {
            float width = font.getWidth(stringIn);
            if (maxWordWidth < width) maxWordWidth = width;
        }
        final List<String> lines = new CopyOnWriteArrayList<>();
        String tempString = "";
        for (final char chars : descString.toCharArray()) {
            tempString += String.valueOf(chars);
            if (font.getWidth(tempString) >= maxDescWidth - maxWordWidth && String.valueOf(chars).equals(" ")) {
                lines.add(tempString);
                tempString = "";
            }
        }
        if (!tempString.isEmpty()) lines.add(tempString);
        float textYAppend = fontHeight / 2.F * lines.size(), stepY = 0;
        for (final String stringInLine : lines) {
            font.string(matrixIn, stringInLine, x, cy - textYAppend + stepY, ColorUtil.applyOpacity(baseColor, descAnimPC * 255.F));
            stepY += fontHeight;
        }
        return descAnimPC;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    float mY = 0;

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        int windowWidth = mw.getWidth();
        int windowHeight = mw.getHeight();
        float anim = animation.get();
        float Aanim = alphaAnimation.get();

        x = windowWidth / 2 - width / 2;
        y = windowHeight / 2 - height / 2 + (int) ((anim - 1) * (animation.isBackward() ? -30.F : 15.F));

        int rgb = Client.instance.moduleManager.hudModule.color.getValue().getRGB();

        matrixStack.push();

        ShapeRenderer background = Render2D.box(matrixStack, 0, 0, windowWidth, windowHeight);
        background.quad(0, ColorUtil.applyOpacity(0xFF000000, Math.min(Aanim * 77, 77))); // 155F

        Render2D.pushAlpha(Math.min(Aanim * 255F, 255F));

        ShapeRenderer menu = Render2D.box(matrixStack, x, y, width, height);

        menu.expand(Side.ALL, 20);
        menu.expand(Side.RIGHT, 5);

        menu.quad(25, 0xB70E0E0F);
        menu.quad(25, ColorUtil.multAlpha(rgb, 0.15f / alphaAnimation.get()), 0xEE0E0E0F, 0xEE0E0E0F, ColorUtil.multAlpha(rgb, 0.15f / alphaAnimation.get()));
        menu.blur(25);

        int[] columnHeights = new int[2];
        int column = 0;
        int maxScroll = 0;

        for (ModuleComponent component : moduleComponents) {
            if (isSelectedCategory(component)) {
                if (isSearchedModule(component)) {
                    continue;
                }

                columnHeights[column] += (int) component.height + 18;
                maxScroll = Math.max(maxScroll, columnHeights[column]);

                column = columnHeights[0] <= columnHeights[1] ? 0 : 1;
            }
        }

        float scrollBarHeight = (float) ((menu.height) * (menu.height) / (double) maxScroll);
        scrollBarHeight = MathHelper.clamp(scrollBarHeight, 10, menu.height - 170);
        double clamped = MathHelper.clamp(maxScroll - (menu.height - 170), 0, maxScroll);

        this.scrollBarAnimation.update();
        this.scrollBarAnimation.run(scrollBarHeight, 0.15F, Easings.LINEAR, true);

        float scrollBarY = (float) (-selectedCategory().smoothedScroll / clamped) * (height - scrollBarHeight);

        this.scrollBarYAnimation.update();
        this.scrollBarYAnimation.run(scrollBarY, 0.15F, Easings.LINEAR, true);

        if (this.scrooling
                && MathUtil.isHovered(mouseX, mouseY, 0, y, mc.getMainWindow().getWidth(), height)) {

            int correctedMouseY = (int) (mouseY * mw.getGuiScaleFactor() - mouseYScroll);

            float scrollPosition = (correctedMouseY - y) / (height - scrollBarHeight);

            scrollPosition = Math.max(0, Math.min(scrollPosition, 1));

            selectedCategory().scroll = (float) (-clamped * scrollPosition);
        }

        ShapeRenderer scrollBG = Render2D.box(matrixStack, x + width + 9, y - 1, 7, 452);
        scrollBG.quad(2, 0xFF161617);

        ShapeRenderer scroll = Render2D.box(matrixStack, x + width + 10, y + this.scrollBarYAnimation.get(), 5, this.scrollBarAnimation.get());
        scroll.quad(2, 0xFFAAAAAA);

        ShapeRenderer section = Render2D.box(matrixStack, x, y, sectionWidth, height);
        section.quad(15, 0xB3111112);

        Render2D.size(FontRegister.Type.LOGO, 75).string(matrixStack, "R", section.x + 5, section.y - 5, rgb);
        Render2D.size(FontRegister.Type.BOLD, 20).string(matrixStack, Constants.NAME, section.x + 55, section.y + 20, -1);

        searchComponent.x = section.x + 15;
        searchComponent.y = section.y + 60;
        searchComponent.width = sectionWidth - 30;
        searchComponent.height = 25;
        searchComponent.render(matrixStack, mouseX, mouseY, partialTicks);

        drawCategory(matrixStack, mouseX, mouseY, partialTicks, section);

        drawUser(matrixStack, section);

        ShapeRenderer functionSection = Render2D.box(matrixStack, x + sectionWidth + 15, y, width - sectionWidth - 15, height);
        functionSection.quad(15, 0xB3111112);

        ShapeRenderer split = Render2D.box(matrixStack, functionSection.x + 15, functionSection.y + 55, functionSection.width - 30, 1);
        split.quad(0, 0x205F5F5F);

        float cA = (1F - categoryAnimation.get());

        Render2D.size(FontRegister.Type.BOLD, 20).string(matrixStack, category.name(), functionSection.x + 20F + cA * -10F, functionSection.y + 17, ColorUtil.applyOpacity(-1, this.categoryAnimation.get() * 255F));

        float descXOff = 8.F, descExpandOfRight = 20.F;
        float descX = functionSection.x + 25F + cA * -10F + Render2D.size(FontRegister.Type.BOLD, 20).getWidth(category.name());
        float descY = functionSection.y + 28.F;
        float descAlphaPC = drawDesc(matrixStack, descX + descXOff, descY, (int) (x + width - descX + descXOff - descExpandOfRight), ColorUtil.getOverallColorFrom(rgb, -1));
        if ((descAlphaPC = (float) Easings.QUAD_IN_OUT.ease(descAlphaPC)) > 0) {
            float heightVLine = 36.F * descAlphaPC;
            float widthVLine = 1.F;
            int lineColor = ColorUtil.applyOpacity(rgb, descAlphaPC * 255.F);
            ShapeRenderer vLine = Render2D.box(matrixStack, descX + descXOff / 2.F - widthVLine / 2.F, descY - heightVLine / 2.F, widthVLine, heightVLine);
            vLine.quad(0, lineColor);
            vLine.drawShadow(matrixStack, heightVLine / 2.25F, ColorUtil.applyOpacity(lineColor, 175.F));
        }

        Render2D.pushAlpha(this.categoryAnimation.get() * 255F);
        ShapeRenderer functionSection2 = Render2D.box(matrixStack, x + sectionWidth + 15, y + cA * 15F, width - sectionWidth - 15, height);
        drawModules(matrixStack, mouseX, mouseY, partialTicks, functionSection2, menu);

        Client.instance.windowManager.render(matrixStack, mouseX, mouseY, partialTicks);

        matrixStack.pop();

        Render2D.popAlpha();

        if (this.categoryAnimation.isFinished(AnimationDirection.BACKWARD)) {
            this.category = this.mewCategory;

            this.categoryAnimation.setDirection(true);
        } else {
            this.categoryAnimation.setDirection(this.category == this.mewCategory);
        }

        if (animation.isFinished(AnimationDirection.BACKWARD)) {
            mouseReleased(mouseX, mouseY, 0);

            super.closeScreen();
        }
    }

    public final relake.animation.excellent.Animation categoryAnimY = new relake.animation.excellent.Animation().setValue(-1);
    public final relake.animation.excellent.Animation pickMe = new relake.animation.excellent.Animation();
    public final relake.animation.excellent.Animation alphaCategory = new relake.animation.excellent.Animation().setValue(-1);

    private void drawCategory(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks, ShapeRenderer section) {
        float yOffset = section.y + 110;
        float categoryX = 0F;
        float categoryY = 0F;
        float categoryW = 0F;
        float categoryH = 0F;

        for (CategoryComponent component : categoryComponents) {
            component.x = section.x + 15;
            component.y = yOffset;
            component.width = sectionWidth - 30;
            component.height = 35;

            if (component.getCategory() == category) {
                categoryX = section.x + 15;
                categoryY = yOffset;
                categoryW = sectionWidth - 30;
                categoryH = 35;
            }

            component.render(matrixStack, mouseX, mouseY, partialTicks);

            yOffset += 42;
        }

        if (categoryAnimY.get() == -1)
            categoryAnimY.set(categoryY - yOffset);

        categoryAnimY.update();
        alphaCategory.update();
        pickMe.update();

        categoryAnimY.run(categoryY - yOffset, 0.15F, Easings.LINEAR, true);
        alphaCategory.run(categoryAnimY.isFinish() ? 215F : 0F, 0.1F, Easings.LINEAR, false);

        if (pickMe.isFinished() && categoryAnimY.isFinish())
            pickMe.run(pickMe.get() == 0F ? 1F : 0F, 0.35F, Easings.SINE_IN_OUT, true);
        else if (!categoryAnimY.isFinish())
            pickMe.run(0F, 0.1F, Easings.SINE_IN_OUT, true);

        int rgb = Client.instance.moduleManager.hudModule.color.getValue().getRGB();

        float alph = Math.min(100F + alphaCategory.get(), 255F);

        float a = 2F;

        ShapeRenderer category = Render2D.box(matrixStack, categoryX - (pickMe.get() * a), categoryAnimY.get() - (pickMe.get() * a) + yOffset, categoryW + (pickMe.get() * a * 2F), categoryH + (pickMe.get() * a * 2F));

        category.drawShadow(matrixStack, 115, ColorUtil.applyOpacity(ColorUtil.applyOpacity(rgb, 35), alph));
        category.quad(8, ColorUtil.applyOpacity(ColorUtil.applyOpacity(rgb, 15), alph / 2), ColorUtil.applyOpacity(ColorUtil.applyOpacity(rgb, 15), alph), ColorUtil.applyOpacity(0xFF161617, alph), ColorUtil.applyOpacity(0xFF161617, alph));
        category.outline(8, 2, ColorUtil.applyOpacity(ColorUtil.applyOpacity(rgb, 15), alph));
    }

    private void drawModules(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks, ShapeRenderer functionSection, ShapeRenderer menu) {
        int[] columnHeights = new int[2];
        int columnWidth = 225;

        StencilUtil.begin();

        ShapeRenderer stencil = Render2D.box(matrixStack, x + sectionWidth + 15, y + 56, width - sectionWidth - 15, height - 56);
        stencil.quad(0, 0xFFFFFFFF);

        StencilUtil.read(1);

        for (ModuleComponent component : moduleComponents) {
            if (isSelectedCategory(component)) {
                if (isSearchedModule(component)) {
                    continue;
                }

                int columnIndex = columnHeights[0] <= columnHeights[1] ? 0 : 1;

                component.x = functionSection.x + 15 + columnIndex * (columnWidth + 18);
                component.y = (float) (functionSection.y + 72 + columnHeights[columnIndex] + selectedCategory().smoothedScroll);
                component.width = columnWidth;
                component.height = 40;

                component.render(matrixStack, mouseX, mouseY, partialTicks);
                columnHeights[columnIndex] += (int) component.height + 18;
            }
        }

        StencilUtil.end();

        int maxColumnHeight = Math.max(columnHeights[0], columnHeights[1]);

        double clamped = MathHelper.clamp(maxColumnHeight - (menu.height - 170), 0, maxColumnHeight);
        selectedCategory().scroll = (float) MathHelper.clamp(selectedCategory().scroll, -clamped, 0);
        selectedCategory().smoothedScroll = MathUtil.fast((float) selectedCategory().smoothedScroll, selectedCategory().scroll, 10f);
    }


    private void drawUser(MatrixStack matrixStack, ShapeRenderer section) {
        ShapeRenderer user = Render2D.box(matrixStack, section.x + 15, section.y + section.height - 65, sectionWidth - 30, 50);
        user.quad(15, 0x7E111112);
        user.outline(15, 1, 0xFF161617);

        ShapeRenderer avatar = Render2D.box(matrixStack, user.x + 10, user.y + 10, 30, 30);
        Render2D.size(FontRegister.Type.BOLD, 13).string(matrixStack, "1", avatar.x + 35, avatar.y + 2, -1, 65);
        Render2D.size(FontRegister.Type.BOLD, 11).string(matrixStack, "UID: " + 1, avatar.x + 35, avatar.y + 16, 0xFFAAAAAA, 65);

        ShapeRenderer status = Render2D.box(matrixStack, avatar.x + avatar.width - 10, avatar.y + avatar.height - 10, 10, 10);
        status.circle(0xFF10d973);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        searchComponent.mouseClicked(mouseX, mouseY, button);

        if (!Client.instance.windowManager.mouseClicked(mouseX, mouseY, button)) {
            if (handleCategoryComponents((component) -> component.mouseClicked(mouseX, mouseY, button))) {
                return true;
            }

            if (MathUtil.isHovered(mouseX, mouseY, x + sectionWidth + 15, y + 56, width - sectionWidth - 15, height - 56)) {
                if (handleModuleComponents((component) -> component.mouseClicked(mouseX, mouseY, button))) {
                    return true;
                }
            }

            int[] offsets = new int[2];
            int column = 0;
            int maxScroll = 0;

            for (ModuleComponent component : moduleComponents) {
                if (isSelectedCategory(component)) {
                    if (isSearchedModule(component)) {
                        continue;
                    }

                    offsets[column] += (int) component.height + 18;
                    maxScroll = Math.max(maxScroll, offsets[column]);
                    column = (column + 1) % 2;
                }
            }

            float scrollBarHeight = (float) (height * height / (double) maxScroll);
            scrollBarHeight = MathHelper.clamp(scrollBarHeight, 10, height - 170);
            double clamped = MathHelper.clamp(maxScroll - (height - 170) - (offsets.length * 18) - 18, 0, maxScroll);
            float scrollBarY = (float) (-selectedCategory().smoothedScroll / clamped) * (height - scrollBarHeight);

            if (MathUtil.isHovered(mouseX, mouseY, x + width + 10, y + scrollBarY, 5, scrollBarHeight)) {
                this.mouseYScroll = scrollBarHeight / 2F;

                this.scrooling = true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        searchComponent.mouseReleased(mouseX, mouseY, button);
        Client.instance.windowManager.mouseReleased(mouseX, mouseY, button);
        handleModuleComponents((component) -> component.mouseReleased(mouseX, mouseY, button));
        this.scrooling = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!Client.instance.windowManager.mouseScrolled(mouseX, mouseY, delta)) {
            if (MathUtil.isHovered(mouseX, mouseY, 0, y + 56, mc.getMainWindow().getWidth(), height - 56)) {
                if (handleModuleComponents((component) -> component.mouseScrolled(mouseX, mouseY, delta))) {
                    return true;
                }

                selectedCategory().scroll += (float) (delta * 40);
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchComponent.keyPressed(keyCode, scanCode, modifiers)) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) searchComponent.clearText();
            else return true;
        }

        if (!Client.instance.windowManager.keyPressed(keyCode, scanCode, modifiers)) {
            if (handleModuleComponents((component) -> component.keyPressed(keyCode, scanCode, modifiers))) {
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        searchComponent.charTyped(codePoint, modifiers);

        if (!Client.instance.windowManager.charTyped(codePoint, modifiers)) {
            if (handleModuleComponents((component) -> component.charTyped(codePoint, modifiers))) {
                return true;
            }
        }

        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void closeScreen() {
        minecraft.mouseHelper.grabMouse(false);
        Client.instance.configManager.moduleConfig.setName("default").save();
        animation.setDirection(AnimationDirection.BACKWARD);
        alphaAnimation.setDirection(AnimationDirection.BACKWARD);
    }

    private boolean handleCategoryComponents(Function<CategoryComponent, Boolean> action) {
        for (CategoryComponent component : categoryComponents) {
            if (action.apply(component)) {
                return true;
            }
        }
        return false;
    }

    private boolean handleModuleComponents(Function<ModuleComponent, Boolean> action) {
        for (ModuleComponent moduleComponent : moduleComponents) {
            if (isSelectedCategory(moduleComponent) && !isSearchedModule(moduleComponent)) {
                if (action.apply(moduleComponent)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isSearchedModule(ModuleComponent moduleComponent) {
        return !moduleComponent.getModule().getName().toLowerCase().replaceAll(" ", "").contains(searchComponent.getInputBuffer().toString().toLowerCase().replaceAll(" ", ""));
    }

    private boolean isSelectedCategory(ModuleComponent moduleComponent) {
        return moduleComponent.getModule().getModuleCategory() == category || !searchComponent.getInputBuffer().isEmpty();
    }

    @Override
    public void onClose() {
        super.onClose();

        if (clientSoundsModule.openGUI.getValue()) SoundUtil.playSound("closescreen.wav", 0.05f);

        for (ModuleComponent moduleComponent : moduleComponents) {
            moduleComponent.onClose();
        }
    }

    private CategoryComponent selectedCategory() {
        return categoryComponents.stream().filter(component -> component.getCategory().equals(category)).findFirst().orElse(null);
    }
}