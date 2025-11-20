package relake.module.implement.movement;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Pose;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import relake.common.util.MoveUtil;
import relake.event.EventHandler;
import relake.event.impl.misc.KeyboardEvent;
import relake.event.impl.misc.TickEvent;
import relake.event.impl.player.MovementInputEvent;
import relake.event.impl.player.PlayerEvent;
import relake.event.impl.player.PoseEvent;
import relake.event.impl.player.SpeedFactorEvent;
import relake.menu.ui.components.module.setting.SelectComponent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;
import relake.settings.implement.SelectSetting;

public class JesusModule extends Module {
    public final SelectSetting mode = new SelectSetting("Обход")
            .setValue("Солид",
                    "FunTime");
    
    public JesusModule() {
        super("Jesus", "Горизонтальный полёт с ускорением на поверхности воды", "Horizontal flight with acceleration on the surface of water", ModuleCategory.Movement);
        registerComponent(mode);
        mode.setSelected("FunTime");
    }

    private int ticks;
    private int ticksStop = 0;
    private float speed = 0.05F;

    @EventHandler
    public void tick(TickEvent event) {
        if (!mode.isSelected("FunTime")) return;

        if (mc.player.isInWater()) {
            if (!(!mc.world.getBlockState(new BlockPos(mc.player.getPosX(), mc.player.getPosY() + 0.2F, mc.player.getPosZ())).getFluidState().isEmpty()
                    || (mc.world.getBlockState(mc.player.getPosition().up()).getBlock() == Blocks.WATER))) {
                speed = 0.05F;

                mc.player.motion.y = 0;

                final KeyBinding[] pressedKeys = {mc.gameSettings.keyBindSneak, mc.gameSettings.keyBindJump};

                for (KeyBinding keyBinding : pressedKeys) {
                    keyBinding.setPressed(false);
                }
            }
        }
    }

    @EventHandler
    public void keyboard(KeyboardEvent event) {
        if (!mode.isSelected("FunTime")) return;

        if (mc.player.isInWater()) {
            if (event.getKey() == mc.gameSettings.keyBindJump.keyCode.getKeyCode()
                    || event.getKey() == mc.gameSettings.keyBindSneak.keyCode.getKeyCode()) {
                mc.player.setSneaking(false);
                mc.player.setSprinting(true);
                mc.player.setSwimming(true);

                final KeyBinding[] pressedKeys = {mc.gameSettings.keyBindSneak, mc.gameSettings.keyBindJump};

                for (KeyBinding keyBinding : pressedKeys) {
                    keyBinding.setPressed(false);
                }
            }
        }
    }

    @EventHandler
    public void move(MovementInputEvent event) {
        if (!mode.isSelected("FunTime")) return;

        if (mc.player.isInWater()) {
            mc.player.setSprinting(true);
            mc.player.setSwimming(true);

            event.setJump(false);
            event.setSneak(false);
        }
    }

    @EventHandler
    public void pose(PoseEvent event) {
        if (!mode.isSelected("FunTime")) return;

        if (mc.player.isInWater()) {
            event.setPose(Pose.STANDING);
        }
    }

    @EventHandler
    public void speed(SpeedFactorEvent event) {
        if (!mode.isSelected("FunTime")) return;

        if (!mc.world.getBlockState(new BlockPos(mc.player.getPosX(), mc.player.getPosY() + 0.3F, mc.player.getPosZ())).getFluidState().isEmpty()
                || mc.world.getBlockState(mc.player.getPosition().up()).getBlock() == Blocks.WATER)
            return;

        if (mc.player.isInWater()
                && MoveUtil.isMoving()
                && !mc.gameSettings.keyBindSneak.isKeyDown()
                && !mc.gameSettings.keyBindJump.isKeyDown()) {
            mc.player.setSwimming(true);
            mc.player.setSneaking(false);
            mc.player.setSprinting(true);

            if (mc.player.hurtTime > 0)
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

    @EventHandler
    public void player(PlayerEvent event) {
        if (mode.isSelected("Солид")) handleSolid(event);
        if (mode.isSelected("FunTime")) handleFunTime(event);
    }

    private void handleFunTime(PlayerEvent event) {
        if (mc.player.isInWater()) {
            if (!mc.world.getBlockState(new BlockPos(mc.player.getPosX(), mc.player.getPosY() + 0.2F, mc.player.getPosZ())).getFluidState().isEmpty()
                    || (mc.world.getBlockState(mc.player.getPosition().up()).getBlock() == Blocks.WATER)) {
                mc.player.setSneaking(false);
                mc.player.setSwimming(true);

                if (mc.player.motion.y >= 0) {
                    if (ticksStop >= 2) {
                        event.setRotate(new Vector2f(mc.player.rotationYaw, -90));

                        float top = 0;

                        while (!mc.world.getBlockState(new BlockPos(mc.player.getPosX(), mc.player.getPosY() + top, mc.player.getPosZ())).getFluidState().isEmpty()) {
                            top += 0.001f;
                        }

                        mc.player.motion.y = MathHelper.clamp(speed, 0, MathHelper.clamp(top - 0.05F, 0.05F, speed));

                        speed = MathHelper.clamp(speed + 0.015F, 0, mc.world.getBlockState(mc.player.getPosition().up()).getBlock() == Blocks.WATER ? 0.175F : 0.085F);
                    }

                    ticksStop++;
                }
            } else {
                speed = 0.05F;

                mc.player.motion.y = 0;
            }

            final KeyBinding[] pressedKeys = {mc.gameSettings.keyBindSneak, mc.gameSettings.keyBindJump};

            for (KeyBinding keyBinding : pressedKeys) {
                keyBinding.setPressed(false);
            }
        }
    }

    private void handleSolid(PlayerEvent event) {
        BlockPos playerPos = new BlockPos(mc.player.getPosX(), mc.player.getPosY() + 0.008D, mc.player.getPosZ());
        Block playerBlock = mc.world.getBlockState(playerPos).getBlock();

        if (playerBlock == Blocks.WATER && !mc.player.isOnGround()) {
            boolean isUp = mc.world.getBlockState(new BlockPos(mc.player.getPosX(), mc.player.getPosY() + 0.03D, mc.player.getPosZ())).getBlock() == Blocks.WATER;
            mc.player.jumpMovementFactor = 0.0F;
            float yPort = MoveUtil.getSpeed() > 0.1D ? 0.02F : 0.032F;
            mc.player.setVelocity(mc.player.motion.x, (double) mc.player.fallDistance < 3.5D ? (double) (isUp ? yPort : -yPort) : -0.1D, mc.player.motion.z);
        }

        if (mc.player.isInLiquid()) {
            mc.player.setVelocity(mc.player.motion.x, .16D, mc.player.motion.z);
        }

        double posY = mc.player.getPosY();
        if (posY > (double) ((int) posY) + 0.89D && posY <= (double) ((int) posY + 1) || (double) mc.player.fallDistance > 3.5D) {
            mc.player.getPositionVec().y = ((double) ((int) posY + 1) + 1.0E-45D);
            if (!mc.player.isInWater()) {
                BlockPos waterBlockPos = new BlockPos(mc.player.getPosX(), mc.player.getPosY() - 0.1D, mc.player.getPosZ());
                Block waterBlock = mc.world.getBlockState(waterBlockPos).getBlock();
                if (waterBlock == Blocks.WATER) {
                    event.setOnGround(false);
                    if (ticks == 1) {
                        MoveUtil.setSpeed(1.1f);
                        ticks = 0;
                    } else {
                        ticks = 1;
                    }
                }
            }
        }
    }

    @Override
    public void enable() {
        super.enable();
        if (mode.isSelected("FunTime")) {
            mc.player.setSwimming(false);
            mc.player.setSneaking(false);
            mc.player.setSprinting(false);

            speed = 0.05F;
            ticksStop = 0;
        }
    }

    @Override
    public void disable() {
        super.disable();
        if (mc.player == null) return;
        if (mode.isSelected("FunTime")) {
            mc.player.setSwimming(false);
            mc.player.setSneaking(false);
            mc.player.setSprinting(false);

            speed = 0.05F;
            ticksStop = 0;
        }
    }
}
