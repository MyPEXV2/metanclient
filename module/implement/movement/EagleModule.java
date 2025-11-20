package relake.module.implement.movement;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import relake.event.EventHandler;
import relake.event.impl.misc.TickEvent;
import relake.module.Module;
import relake.module.ModuleCategory;

public class EagleModule extends Module {
    public EagleModule() {
        super("Eagle", "Препядствует падению с края блока", "Prevents falling off the edge of the block", ModuleCategory.Movement);
    }

    @EventHandler
    public void tick(TickEvent tickEvent) {
        BlockPos blockPos = mc.player.getPosition()
                .add(0, -1, 0);
        mc.gameSettings.keyBindSneak.setPressed(mc.world.getBlockState(blockPos).getBlock() == Blocks.AIR);
    }
}
