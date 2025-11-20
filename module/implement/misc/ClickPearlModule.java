package relake.module.implement.misc;

import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.server.SHeldItemChangePacket;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;
import relake.Client;
import relake.common.component.ClickPearlComponent;
import relake.common.component.rotation.Rotation;
import relake.common.component.rotation.RotationComponent;
import relake.common.util.InventoryUtil;
import relake.event.EventHandler;
import relake.event.impl.misc.KeyboardEvent;
import relake.event.impl.misc.PacketEvent;
import relake.event.impl.misc.TickEvent;
import relake.menu.ui.components.module.setting.SelectComponent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;
import relake.settings.implement.KeySetting;
import relake.settings.implement.SelectSetting;

import java.util.Optional;

public class ClickPearlModule extends Module {

    public final SelectSetting mode = new SelectSetting("Режим")
            .setValue("Обычный",
                    "Легитный");

    private final Setting<Integer> key = new KeySetting("Клавиша для кидания")
            .setValue(GLFW.GLFW_MOUSE_BUTTON_MIDDLE);

    private int realRotationTicks;
    public int slot = -1;

    public ClickPearlModule() {
        super("Click Pearl", "Находит эндер жемчуг в хотбаре и использует его", "Ender finds a pearl in a hotbar and uses it", ModuleCategory.Misc);
        registerComponent(key, mode);
        mode.setSelected("Обычный");
    }

    @EventHandler
    public void keyboard(KeyboardEvent event) {
        if (event.getAction() != GLFW.GLFW_PRESS || event.getKey() != key.getValue() || mc.currentScreen != null) {
            return;
        }
        if (mc.player == null || mc.player.getCooldownTracker().hasCooldown(Items.ENDER_PEARL)) {
            return;
        }

        Optional<Integer> optionalSlot = InventoryUtil.findItem(itemStack -> itemStack.getItem() == Items.ENDER_PEARL, true, false);

        if (optionalSlot.isEmpty()) {
            return;
        }

        this.slot = optionalSlot.get();
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (this.slot == -1) {
            return;
        }

        ActiveRenderInfo activeRenderInfo = mc.gameRenderer.getActiveRenderInfo();
        float yaw = activeRenderInfo.getYaw();
        float pitch = activeRenderInfo.getPitch();

        RotationComponent.update(new Rotation(yaw, pitch), 360, 360, 1, 200);

        if (++this.realRotationTicks > 1) {
            if (this.mode.isSelected("Легитный")) {
                ClickPearlComponent.startLegitPearlThrow(this.slot);
            } else if (this.mode.isSelected("Обычный")) {
                if (slot < 9) {
                    mc.player.connection.sendPacket(new CHeldItemChangePacket(slot));
                    mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                    mc.player.swingArm(Hand.MAIN_HAND);
                    mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                } else {
                    mc.playerController.pickItem(slot);
                    mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                    mc.player.swingArm(Hand.MAIN_HAND);
                    mc.playerController.pickItem(slot);
                }
                this.reset();
            }
        }
    }

    @EventHandler
    public void onPacket(PacketEvent.Receive e) {
        if (mc.player == null) return;

        if (e.getPacket() instanceof SHeldItemChangePacket p) {
            int prevSlot = mc.player.inventory.currentItem;

            if (p.getHeldItemHotbarIndex() != prevSlot) {
                mc.player.connection.sendPacket(new CHeldItemChangePacket(prevSlot));
                e.cancel();
            }
        }
    }

    public void reset() {
        this.slot = -1;
        this.realRotationTicks = 0;
    }

    @Override
    public void disable() {
        super.disable();
        this.reset();
    }
}
