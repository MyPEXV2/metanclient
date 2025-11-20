package relake.module.implement.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AirItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import relake.Client;
import relake.common.util.ColorUtil;
import relake.common.util.ProjectionUtil;
import relake.event.EventHandler;
import relake.event.impl.render.ScreenRenderEvent;
import relake.event.impl.render.WorldRenderEvent;
import relake.menu.ui.components.module.setting.MultiSelectComponent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.render.display.Render2D;
import relake.render.display.font.FontRegister;
import relake.render.display.shape.ShapeRenderer;
import relake.render.display.shape.Side;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;
import relake.settings.implement.MultiSelectSetting;
import relake.settings.implement.SelectSetting;

import java.util.ArrayList;
import java.util.List;

public class PlayerESPModule extends Module {
    private Matrix4f matrix = new Matrix4f();

    public final MultiSelectSetting selectComponent = new MultiSelectSetting("Отображение")
            .setValue("Боксы",
                    "Здоровье",
                    "Имя",
                    "Сферы и её уровень",
                    "Броня",
                    "Отображение");

    public PlayerESPModule() {
        super("Player ESP", "Информативно подсвечивает окружающих вас игроков", "It highlights the players around you informatively", ModuleCategory.Render);
        registerComponent(selectComponent);
        selectComponent.getSelected().add("Боксы");
        selectComponent.getSelected().add("Имя");
        selectComponent.getSelected().add("Сферы и её уровень");
    }

    @EventHandler
    public void worldRender(WorldRenderEvent worldRenderEvent) {
        matrix = worldRenderEvent.getMatrix().copy();
        matrix.mul(worldRenderEvent.getStack().getLast()
                .getMatrix()
        );
    }

    @EventHandler
    public void screenRender(ScreenRenderEvent screenRenderEvent) {
        for (Entity entity : mc.world.getAllEntities()) {
            if (entity instanceof PlayerEntity player && player != mc.player) {
                MatrixStack matrixStack = screenRenderEvent.getMatrixStack();
                int rgb = Client.instance.moduleManager.hudModule.color.getValue().getRGB();

                Vector4f position = ProjectionUtil.getEntity2DPosition(matrix, entity);

                int padding = (int) ((position.getZ() - position.getX()) / 50);

                float x = position.getX() + padding,
                        y = position.getY(),
                        w = position.getZ() - x - padding,
                        h = position.getW() - y;

                if (selectComponent.isSelected("Боксы")) {
                    ShapeRenderer box = Render2D.box(matrixStack, x, y, w, h);
                    box.outline(1, 4, 0xFF000000);
                    box.expand(Side.ALL, -1);
                    box.outline(1, 2, rgb);
                }

                if (selectComponent.isSelected("Здоровье")) {
                    int height = (int) ((player.getHealthFixed() / player.getMaxHealth()) * h);

                    ShapeRenderer healthBar = Render2D.box(matrixStack, x + w + 1, y + 1.5f, 4, h - 3);
                    healthBar.quad(0, 0xFF000000);

                    float clamped = MathHelper.clamp(height, 0, h);
                    ShapeRenderer hss = Render2D.box(matrixStack, x + w + 2, y + h - clamped + 2, 2, clamped - 4);
                    hss.quad(0, rgb);
                }

                String health = TextFormatting.GRAY + " [" + TextFormatting.RED + Math.round(player.getHealthFixed()) + "hp" + TextFormatting.GRAY + "]";
                IFormattableTextComponent displayName = player.getDisplayName().deepCopy();

                if (Client.instance.friendManager.isFriend(player.getNotHidedName().getString())) displayName = new StringTextComponent(TextFormatting.GREEN + "[Друг] " + TextFormatting.RESET).append(displayName);

                displayName.appendString(health);

                List<ItemStack> stacks = new ArrayList<>();

                stacks.add(player.getHeldItemMainhand());
                player.getArmorInventoryList().forEach(stacks::add);
                stacks.add(player.getHeldItemOffhand());

                float factor = (float) mc.getMainWindow().getGuiScaleFactor();

                float scale = 1F / factor;

                stacks.removeIf(itemStack -> itemStack.getItem() instanceof AirItem);
                float x1 = x + w / 2 - (stacks.size() * (30 * scale)) / 2;

                if (selectComponent.isSelected("Здоровье") || selectComponent.isSelected("Сферы и её уровень")) {
                    float xOffset = 0;

                    for (ItemStack stack : stacks) {
                        GlStateManager.pushMatrix();
                        GlStateManager.scalef(scale, scale, scale);

                        if (selectComponent.isSelected("Броня")) {
                            mc.getItemRenderer().renderItemAndEffectIntoGUI(stack, ((x1 + xOffset) / scale) / factor, ((y - 40) / scale) / factor);
                            mc.getItemRenderer().renderItemOverlays(mc.fontRenderer, stack, ((x1 + xOffset) / scale) / factor, ((y - 38) / scale) / factor);
                            if (stack.getCount() > stack.getMaxStackSize()) stack.setCount(stack.getMaxStackSize());
                        }

                        if (selectComponent.isSelected("Сферы и её уровень")) {
                            String sphereLevel = getSphereLevel(stack);
                            if (!sphereLevel.equalsIgnoreCase("")) {
                                displayName.appendString(" " + TextFormatting.GRAY + " [" + TextFormatting.WHITE + sphereLevel + TextFormatting.GRAY + "]");
                            }
                        }

                        GlStateManager.popMatrix();
                        xOffset += 30 * scale;
                    }
                }

                if (selectComponent.isSelected("Имя")) {
                    float width = Render2D.size(FontRegister.Type.BOLD, 12).getWidth(StringUtils.stripControlCodes(displayName.getString())) + 5;

                    ShapeRenderer nametags = Render2D.box(matrixStack, x + w / 2 - width / 2, y - 21, width, 17);
                    nametags.expand(Side.ALL, 1);
                    nametags.quad(0, 0x50000000);
                    Render2D.size(FontRegister.Type.BOLD, 12).centeredString(matrixStack, displayName, x + w / 2, y - 20.5f, -1);
                }
            }
        }
    }

    private String getSphereLevel(ItemStack itemStack) {
        CompoundNBT tag = itemStack.getTag();

        if (tag == null) {
            return "";
        }

        String sphere = tag.getString("don-item");

        if (sphere.startsWith("sphere-")) {
            String suffix = sphere.substring(sphere.indexOf('-') + 1);
            if (tag.contains("tslevel")) {
                int tslevel = tag.getInt("tslevel");
                suffix += (tslevel == 3) ? TextFormatting.RED + " MAX" : TextFormatting.YELLOW + " " + tslevel + "/3";
            }
            return suffix;
        }

        return "";
    }
}
