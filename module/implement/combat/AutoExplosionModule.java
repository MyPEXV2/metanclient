package relake.module.implement.combat;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import relake.common.util.InventoryUtil;
import relake.event.EventHandler;
import relake.event.impl.misc.BlockPlaceEvent;
import relake.event.impl.misc.TickEvent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.FloatSetting;

import static net.minecraft.util.Hand.MAIN_HAND;


public class AutoExplosionModule extends Module {
    private boolean canPlace = false;

    private final Setting<Float> range = new FloatSetting("Радиус сробатывания")
            .range(2, 5)
            .setValue(3F);

    public AutoExplosionModule() {
        super("Auto Explosion", "Автоматически ставит и взрывает кристал при поставке обсидиана", "Automatically place and explodes the crystal when obsidian was placed", ModuleCategory.Combat);
    }

    @EventHandler
    public void blockPlace(BlockPlaceEvent blockPlaceEvent) {
        if (blockPlaceEvent.getBlock() == Blocks.OBSIDIAN) {
            int crystal = InventoryUtil.getHotBarItemSlot(Items.END_CRYSTAL);

            BlockPos pos = blockPlaceEvent.getPos();
            int oldSlot = mc.player.inventory.currentItem;

            if (crystal == -1 || !mc.world.isAirBlock(pos.up(1))) {
                return;
            }

            canPlace = true;

            mc.player.inventory.currentItem = crystal;
            Vector3d hitVec = new Vector3d(pos.getX(), pos.getY(), pos.getZ());
            mc.playerController.processRightClickBlock(mc.player, mc.world, MAIN_HAND, new BlockRayTraceResult(hitVec, Direction.UP, pos, false));
            mc.player.swingArm(MAIN_HAND);
            mc.player.inventory.currentItem = oldSlot;
        }
    }

    @EventHandler
    public void tick(TickEvent tickEvent) {
        for (Entity entity : mc.world.getAllEntities()) {
            if (entity instanceof EnderCrystalEntity crystal && mc.player.getDistance(crystal) <= range.getValue() && crystal.isAlive() && canPlace) {

                mc.playerController.attackEntity(mc.player, entity);
                mc.player.swingArm(MAIN_HAND);

                canPlace = false;
            }
        }
    }
}
