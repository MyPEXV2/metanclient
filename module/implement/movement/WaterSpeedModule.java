package relake.module.implement.movement;

import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import relake.common.util.MoveUtil;
import relake.event.EventHandler;
import relake.event.impl.player.SpeedFactorEvent;
import relake.module.Module;
import relake.module.ModuleCategory;

public class WaterSpeedModule extends Module {

    public WaterSpeedModule() {
        super("Water Speed", "Ускоряет передвижение находясь в воде", "Accelerates movement while in the water", ModuleCategory.Movement);
    }

    @EventHandler
    public void speed(SpeedFactorEvent event) {
        if (!mc.player.isInWater()
                || mc.player.hurtTime > 0
                || !MoveUtil.isMoving())
            return;

        boolean t = mc.player.ticksExisted % 2 == 0;
        int one = t ? 3 : 1;
        int two = t ? 1 : 3;

        for (int i = 0; i < one; i++) {
            mc.getConnection().sendPacket(
                    new CEntityActionPacket(mc.player, CEntityActionPacket.Action.PRESS_SHIFT_KEY)
            );
        }

        for (int i = 0; i < two; i++) {
            mc.getConnection().sendPacket(
                    new CEntityActionPacket(mc.player, CEntityActionPacket.Action.RELEASE_SHIFT_KEY)
            );
        }

        float bps = (float) (Math.hypot(mc.player.getPosX() - mc.player.prevPosX, mc.player.getPosZ() - mc.player.prevPosZ) * mc.timer.speed * 20.0D);

        ItemStack stack = mc.player.getItemStackFromSlot(EquipmentSlotType.FEET);
        int lvl = EnchantmentHelper.getEnchantmentLevel(Enchantments.DEPTH_STRIDER, stack);

        float speed = 1F;
        float speedA = 1F;

        float startSpeed = 0.0507F * speed;

        if (lvl > 0)
            startSpeed += 0.01F * speed;

        if (bps < 6.5F)
            startSpeed += 0.005F * speedA;
        else if (bps < 7F)
            startSpeed += 0.0025F * speedA;

        event.setSpeed(event.getSpeed() + startSpeed);
    }
}
