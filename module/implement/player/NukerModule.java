package relake.module.implement.player;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import relake.Client;
import relake.common.util.ChatUtil;
import relake.event.EventHandler;
import relake.event.impl.misc.BlockDamageEvent;
import relake.event.impl.misc.TickEvent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.FloatSetting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class NukerModule extends Module {
    public final List<Block> targetBlocks = new ArrayList<>();
    public final List<BlockPos> blockPositions = new ArrayList<>();

    public final Setting<Float> range = new FloatSetting("Радиус ломания")
            .range(1, 3)
            .setValue(3F);

    public NukerModule() {
        super("Nuker", "Ломает блоки в зоне досигаемости", "Breaks blocks in the reach area", ModuleCategory.Player);
        registerComponent(range);
    }
    public float getDistanceToBlockPos(Entity entIn, BlockPos posIn)
    {
        float f = (float)(posIn.getX() + .5D - entIn.getPosX());
        float f1 = (float)(posIn.getY() + .5D - entIn.getPosY());
        float f2 = (float)(posIn.getZ() + .5D - entIn.getPosZ());
        return MathHelper.sqrt(f * f + f1 * f1 + f2 * f2);
    }
    @EventHandler
    public void tick(TickEvent tickEvent) {
        int maxTargetBlocks = 5;

        if (targetBlocks.isEmpty()) {
            ChatUtil.send("Список блоков пуст. Используйте .nuker");
            switchState();
            return;
        }

        collectBlocks();

        if (blockPositions.isEmpty()) {
            return;
        }

        blockPositions.sort(Comparator.comparingDouble(blockPos -> getDistanceToBlockPos(mc.player, blockPos)));

        for (BlockPos blockPos : blockPositions) {
            if (maxTargetBlocks > 0) {
                if (mc.playerController.onPlayerDamageBlock(blockPos, Direction.UP)) mc.player.swingArm(Hand.MAIN_HAND);
               // if (mc.playerController.getIsHittingBlock()) maxTargetBlocks = 1;
                else if (mc.world.isAirBlock(blockPos)) {
                    blockPositions.remove(blockPos);
                    maxTargetBlocks--;
                }
            }

            //if (!mc.playerController.getIsHittingBlock()) {
            //    blockPositions.remove(0);
            //}
        }
    }


    private void collectBlocks() {
        blockPositions.clear();
        BlockPos playerPos = mc.player.getPosition();

        float range = this.range.getValue();
        for (float x = -range; x <= range; x++) {
            for (int y = 0; y <= range; y++) {
                for (float z = -range; z <= range; z++) {
                    BlockPos blockPos = playerPos.add(x, y, z);
                    Block block = mc.world.getBlockState(blockPos).getBlock();

                    if (!targetBlocks.contains(block) || block == Blocks.AIR) {
                        continue;
                    }

                    blockPositions.add(blockPos);
                }
            }
        }
    }
}
