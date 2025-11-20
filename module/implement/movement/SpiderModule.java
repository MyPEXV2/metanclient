package relake.module.implement.movement;

import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector2f;
import relake.common.util.ChatUtil;
import relake.common.util.InventoryUtil;
import relake.common.util.RayTraceUtil;
import relake.common.util.StopWatch;
import relake.event.EventHandler;
import relake.event.impl.player.PlayerEvent;
import relake.menu.ui.components.module.setting.SelectComponent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;
import relake.settings.implement.SelectSetting;

public class SpiderModule extends Module {

    public final SelectSetting mode = new SelectSetting("Обход")
            .setValue("Grim",
                    "Matrix");

    public SpiderModule() {
        super("Spider", "Позволяет взбираться по стенам как паук", "Allows you to climb walls like a spider", ModuleCategory.Movement);
        registerComponent(mode);
        mode.setSelected("Grim");
    }

    private final StopWatch stopWatch = new StopWatch();

    @EventHandler
    public void player(PlayerEvent event) {
        if (!mc.player.collidedHorizontally) {
            return;
        }

        if (mode.isSelected("Matrix")) handleMatrix(event);
        if (mode.isSelected("Grim")) handleGrim(event);
    }

    private void handleMatrix(PlayerEvent event) {
        if (stopWatch.finished(250)) {
            event.setOnGround(true);
            mc.player.setOnGround(true);
            mc.player.collidedVertically = true;
            mc.player.collidedHorizontally = true;
            mc.player.isAirBorne = true;
            mc.player.jump();
            stopWatch.reset();
        }
    }

    private void handleGrim(PlayerEvent event) {
        int slotInHotBar = InventoryUtil.findAnyBlockInHotBar();

        if (slotInHotBar == -1) {
            ChatUtil.send("Не нашел блоки в хотбаре!");
            switchState();
            return;
        }

        if (mc.player.fallDistance > 0 && mc.player.fallDistance < 1) {
            int old = mc.player.inventory.currentItem;

            event.setRotate(new Vector2f(mc.player.getHorizontalFacing().getHorizontalAngle(), 80));

            BlockRayTraceResult result = (BlockRayTraceResult) RayTraceUtil.rayTrace(4, event.getRotate().x, event.getRotate().y, mc.player);

            mc.player.swingArm(Hand.MAIN_HAND);
            mc.playerController.processRightClickBlock(mc.player, mc.world, Hand.MAIN_HAND, result);

            mc.player.inventory.currentItem = old;
            mc.player.fallDistance = 0;
        }
    }
}
